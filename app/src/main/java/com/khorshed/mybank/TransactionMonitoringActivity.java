package com.khorshed.mybank;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.adapters.TransactionMonitoringAdapter;
import com.khorshed.mybank.models.TransactionMonitoring;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionMonitoringActivity extends AppCompatActivity {

    private MaterialButton backButton, searchButton, refreshButton;
    private MaterialButton flagTransactionButton, reverseTransactionButton;
    private EditText searchEditText, startDateEditText, endDateEditText;
    private Spinner filterSpinner;
    private RecyclerView transactionsRecyclerView;
    private TextView emptyStateText, summaryText;

    private FirebaseFirestore db;
    private TransactionMonitoringAdapter transactionAdapter;
    private List<TransactionMonitoring> transactionList;

    private String selectedStartDate = "";
    private String selectedEndDate = "";
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_monitoring);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Setup filter spinner
        setupFilterSpinner();

        // Load transactions data
        loadTransactionsData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);
        refreshButton = findViewById(R.id.refreshButton);
        flagTransactionButton = findViewById(R.id.flagTransactionButton);
        reverseTransactionButton = findViewById(R.id.reverseTransactionButton);
        searchEditText = findViewById(R.id.searchEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        filterSpinner = findViewById(R.id.filterSpinner);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        summaryText = findViewById(R.id.summaryText);
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionMonitoringAdapter(transactionList);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                transactionAdapter.filter(s.toString());
                updateSummary();
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Date pickers
        startDateEditText.setOnClickListener(v -> showDatePicker(true));
        endDateEditText.setOnClickListener(v -> showDatePicker(false));

        // Search button
        searchButton.setOnClickListener(v -> applyDateFilter());

        // Refresh button
        refreshButton.setOnClickListener(v -> {
            selectedStartDate = "";
            selectedEndDate = "";
            startDateEditText.setText("");
            endDateEditText.setText("");
            searchEditText.setText("");
            filterSpinner.setSelection(0);
            loadTransactionsData();
        });

        // Action buttons
        flagTransactionButton.setOnClickListener(v -> flagSelectedTransactions());
        reverseTransactionButton.setOnClickListener(v -> reverseSelectedTransactions());
    }

    private void setupFilterSpinner() {
        String[] filterOptions = {"All Transactions", "Deposit", "Withdraw", "Transfer", "Flagged", "Today"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = filterOptions[position];
                transactionAdapter.filterByType(selectedType);
                updateSummary();
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDatePicker(boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                            year, month + 1, dayOfMonth);
                    String displayDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", 
                            dayOfMonth, month + 1, year);
                    
                    if (isStartDate) {
                        selectedStartDate = selectedDate;
                        startDateEditText.setText(displayDate);
                    } else {
                        selectedEndDate = selectedDate;
                        endDateEditText.setText(displayDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void applyDateFilter() {
        if (!selectedStartDate.isEmpty() && !selectedEndDate.isEmpty()) {
            transactionAdapter.filterByDateRange(selectedStartDate, selectedEndDate);
            updateSummary();
            updateEmptyState();
        } else {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTransactionsData() {
        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionList.clear();
                    int idCounter = 1;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String transactionId = doc.getId();
                        String type = doc.getString("type");
                        
                        Double amountObj = doc.getDouble("amount");
                        double amount = (amountObj != null) ? amountObj : 0.0;
                        
                        String status = doc.getString("status");
                        if (status == null || status.isEmpty()) {
                            status = "Normal";
                        }
                        
                        Object timestampObj = doc.get("timestamp");
                        String dateTime = formatDateTime(timestampObj);
                        
                        String description = doc.getString("description");
                        if (description == null || description.isEmpty()) {
                            description = "N/A";
                        }

                        // Get account information
                        String accountId = doc.getString("accountId");
                        String fromAccountId = doc.getString("fromAccountId");
                        String toAccountId = doc.getString("toAccountId");
                        
                        // Determine primary account
                        String primaryAccountId = accountId != null ? accountId : 
                                                 (fromAccountId != null ? fromAccountId : toAccountId);

                        // Load account number
                        if (primaryAccountId != null) {
                            loadAccountAndAddTransaction(String.valueOf(idCounter), transactionId, 
                                    primaryAccountId, type, amount, dateTime, description, status);
                        } else {
                            // Add transaction without account number
                            TransactionMonitoring transaction = new TransactionMonitoring(
                                    String.valueOf(idCounter), transactionId, "N/A", "",
                                    type, amount, dateTime, description, status);
                            transactionList.add(transaction);
                        }
                        
                        idCounter++;
                    }

                    transactionAdapter.updateData(transactionList);
                    updateSummary();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading transactions: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void loadAccountAndAddTransaction(String id, String transactionId, String accountId,
                                             String type, double amount, String dateTime, 
                                             String description, String status) {
        db.collection("accounts").document(accountId)
                .get()
                .addOnSuccessListener(accountDoc -> {
                    String accountNumber = "N/A";
                    if (accountDoc.exists()) {
                        accountNumber = accountDoc.getString("accountNumber");
                        if (accountNumber == null || accountNumber.isEmpty()) {
                            accountNumber = "N/A";
                        }
                    }

                    TransactionMonitoring transaction = new TransactionMonitoring(
                            id, transactionId, accountNumber, accountId,
                            type, amount, dateTime, description, status);
                    
                    transactionList.add(transaction);
                    transactionAdapter.updateData(transactionList);
                    updateSummary();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    TransactionMonitoring transaction = new TransactionMonitoring(
                            id, transactionId, "N/A", accountId,
                            type, amount, dateTime, description, status);
                    
                    transactionList.add(transaction);
                    transactionAdapter.updateData(transactionList);
                    updateSummary();
                    updateEmptyState();
                });
    }

    private String formatDateTime(Object dateObj) {
        if (dateObj == null) {
            return "N/A";
        }
        
        try {
            if (dateObj instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) dateObj).toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return sdf.format(date);
            } else if (dateObj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return sdf.format((Date) dateObj);
            } else if (dateObj instanceof String) {
                return (String) dateObj;
            } else if (dateObj instanceof Long) {
                Date date = new Date((Long) dateObj);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "N/A";
    }

    private void updateSummary() {
        int count = transactionAdapter.getItemCount();
        double totalAmount = transactionAdapter.getTotalAmount();
        summaryText.setText(String.format(Locale.getDefault(), 
                "Showing %d transactions | Total Amount: ৳%.2f", count, totalAmount));
    }

    private void updateEmptyState() {
        if (transactionAdapter.getItemCount() == 0) {
            transactionsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            transactionsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private void flagSelectedTransactions() {
        List<TransactionMonitoring> selectedTransactions = transactionAdapter.getSelectedTransactions();
        
        if (selectedTransactions.isEmpty()) {
            Toast.makeText(this, "Please select at least one transaction to flag", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Flag Transactions")
                .setMessage("Are you sure you want to flag " + selectedTransactions.size() + " transaction(s)?")
                .setPositiveButton("Flag", (dialog, which) -> {
                    for (TransactionMonitoring transaction : selectedTransactions) {
                        updateTransactionStatus(transaction.getTransactionId(), "Flagged");
                    }
                    Toast.makeText(this, selectedTransactions.size() + " transaction(s) flagged", 
                            Toast.LENGTH_SHORT).show();
                    loadTransactionsData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reverseSelectedTransactions() {
        List<TransactionMonitoring> selectedTransactions = transactionAdapter.getSelectedTransactions();
        
        if (selectedTransactions.isEmpty()) {
            Toast.makeText(this, "Please select at least one transaction to reverse", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Reverse Transactions")
                .setMessage("Are you sure you want to reverse " + selectedTransactions.size() + 
                        " transaction(s)? This action cannot be undone!")
                .setPositiveButton("Reverse", (dialog, which) -> {
                    for (TransactionMonitoring transaction : selectedTransactions) {
                        reverseTransaction(transaction);
                    }
                    Toast.makeText(this, selectedTransactions.size() + " transaction(s) reversed", 
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reverseTransaction(TransactionMonitoring transaction) {
        // Update transaction status to Reversed
        updateTransactionStatus(transaction.getTransactionId(), "Reversed");
        
        // TODO: Implement actual reversal logic (reverse account balances)
        // This would require updating the account balances based on the transaction type
        // For now, we just mark it as reversed
    }

    private void updateTransactionStatus(String transactionId, String newStatus) {
        if (transactionId == null || transactionId.isEmpty()) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        db.collection("transactions").document(transactionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Success - data will be reloaded
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating transaction: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
}
