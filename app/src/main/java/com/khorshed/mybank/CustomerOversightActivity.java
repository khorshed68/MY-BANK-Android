package com.khorshed.mybank;

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
import com.khorshed.mybank.adapters.CustomerAccountAdapter;
import com.khorshed.mybank.models.CustomerAccount;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerOversightActivity extends AppCompatActivity {

    private MaterialButton backButton, blockButton, unblockButton, approveButton, viewDetailsButton;
    private EditText searchEditText;
    private Spinner filterSpinner;
    private RecyclerView accountsRecyclerView;
    private TextView emptyStateText;
    private TextView totalAccountsText, activeAccountsText, blockedAccountsText, totalBalanceText;

    private FirebaseFirestore db;
    private CustomerAccountAdapter accountAdapter;
    private List<CustomerAccount> accountList;

    private int totalAccounts = 0;
    private int activeAccounts = 0;
    private int blockedAccounts = 0;
    private double totalBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_oversight);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Setup filter spinner
        setupFilterSpinner();

        // Load accounts data
        loadAccountsData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        blockButton = findViewById(R.id.blockButton);
        unblockButton = findViewById(R.id.unblockButton);
        approveButton = findViewById(R.id.approveButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
        searchEditText = findViewById(R.id.searchEditText);
        filterSpinner = findViewById(R.id.filterSpinner);
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        totalAccountsText = findViewById(R.id.totalAccountsText);
        activeAccountsText = findViewById(R.id.activeAccountsText);
        blockedAccountsText = findViewById(R.id.blockedAccountsText);
        totalBalanceText = findViewById(R.id.totalBalanceText);
    }

    private void setupRecyclerView() {
        accountList = new ArrayList<>();
        accountAdapter = new CustomerAccountAdapter(accountList);
        accountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        accountsRecyclerView.setAdapter(accountAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                accountAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        blockButton.setOnClickListener(v -> blockSelectedAccounts());
        unblockButton.setOnClickListener(v -> unblockSelectedAccounts());
        approveButton.setOnClickListener(v -> approveSelectedAccounts());
        viewDetailsButton.setOnClickListener(v -> viewAccountDetails());
    }

    private void setupFilterSpinner() {
        String[] filterOptions = {"All", "Active", "Blocked", "Pending"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = filterOptions[position];
                accountAdapter.filterByStatus(selectedStatus);
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAccountsData() {
        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    accountList.clear();
                    totalAccounts = 0;
                    activeAccounts = 0;
                    blockedAccounts = 0;
                    totalBalance = 0.0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String accountId = doc.getId();
                        String accountNumber = doc.getString("accountNumber");
                        String userId = doc.getString("userId");
                        String accountType = doc.getString("accountType");
                        
                        Double balanceObj = doc.getDouble("balance");
                        double balance = (balanceObj != null) ? balanceObj : 0.0;
                        
                        String status = doc.getString("status");
                        if (status == null || status.isEmpty()) {
                            status = "Active";
                        }
                        
                        Object createdAtObj = doc.get("createdAt");
                        String createdDate = formatDate(createdAtObj);

                        // Load customer name from users collection
                        loadCustomerNameAndAddAccount(accountId, accountNumber, userId, 
                                accountType, balance, status, createdDate);

                        // Update statistics
                        totalAccounts++;
                        totalBalance += balance;
                        if ("Active".equalsIgnoreCase(status)) {
                            activeAccounts++;
                        } else if ("Blocked".equalsIgnoreCase(status)) {
                            blockedAccounts++;
                        }
                    }

                    updateStatistics();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading accounts: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void loadCustomerNameAndAddAccount(String accountId, String accountNumber, 
                                               String userId, String accountType, 
                                               double balance, String status, String createdDate) {
        if (userId != null && !userId.isEmpty()) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String customerName = "Unknown";
                        String email = "";
                        String phone = "";
                        
                        if (userDoc.exists()) {
                            customerName = userDoc.getString("fullName");
                            if (customerName == null || customerName.isEmpty()) {
                                customerName = userDoc.getString("name");
                            }
                            if (customerName == null || customerName.isEmpty()) {
                                customerName = userDoc.getString("username");
                            }
                            if (customerName == null) {
                                customerName = "Unknown";
                            }
                            
                            email = userDoc.getString("email");
                            phone = userDoc.getString("phone");
                        }

                        CustomerAccount account = new CustomerAccount(
                                accountNumber, customerName, email, phone,
                                accountType, balance, status, createdDate, 
                                userId, accountId);
                        
                        accountList.add(account);
                        accountAdapter.updateData(accountList);
                        updateEmptyState();
                    })
                    .addOnFailureListener(e -> {
                        CustomerAccount account = new CustomerAccount(
                                accountNumber, "Unknown", "", "",
                                accountType, balance, status, createdDate, 
                                userId, accountId);
                        
                        accountList.add(account);
                        accountAdapter.updateData(accountList);
                        updateEmptyState();
                    });
        } else {
            CustomerAccount account = new CustomerAccount(
                    accountNumber, "Unknown", "", "",
                    accountType, balance, status, createdDate, 
                    userId, accountId);
            
            accountList.add(account);
            accountAdapter.updateData(accountList);
            updateEmptyState();
        }
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return "N/A";
        }
        
        try {
            if (dateObj instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) dateObj).toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(date);
            } else if (dateObj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format((Date) dateObj);
            } else if (dateObj instanceof String) {
                return (String) dateObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "N/A";
    }

    private void updateStatistics() {
        totalAccountsText.setText(String.valueOf(totalAccounts));
        activeAccountsText.setText(String.valueOf(activeAccounts));
        blockedAccountsText.setText(String.valueOf(blockedAccounts));
        totalBalanceText.setText(String.format("৳%.2f", totalBalance));
    }

    private void updateEmptyState() {
        if (accountAdapter.getItemCount() == 0) {
            accountsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            accountsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private void blockSelectedAccounts() {
        List<CustomerAccount> selectedAccounts = accountAdapter.getSelectedAccounts();
        
        if (selectedAccounts.isEmpty()) {
            Toast.makeText(this, "Please select at least one account to block", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Block Accounts")
                .setMessage("Are you sure you want to block " + selectedAccounts.size() + " account(s)?")
                .setPositiveButton("Block", (dialog, which) -> {
                    for (CustomerAccount account : selectedAccounts) {
                        updateAccountStatus(account.getAccountId(), "Blocked");
                    }
                    Toast.makeText(this, selectedAccounts.size() + " account(s) blocked", 
                            Toast.LENGTH_SHORT).show();
                    loadAccountsData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void unblockSelectedAccounts() {
        List<CustomerAccount> selectedAccounts = accountAdapter.getSelectedAccounts();
        
        if (selectedAccounts.isEmpty()) {
            Toast.makeText(this, "Please select at least one account to unblock", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Unblock Accounts")
                .setMessage("Are you sure you want to unblock " + selectedAccounts.size() + " account(s)?")
                .setPositiveButton("Unblock", (dialog, which) -> {
                    for (CustomerAccount account : selectedAccounts) {
                        updateAccountStatus(account.getAccountId(), "Active");
                    }
                    Toast.makeText(this, selectedAccounts.size() + " account(s) unblocked", 
                            Toast.LENGTH_SHORT).show();
                    loadAccountsData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveSelectedAccounts() {
        List<CustomerAccount> selectedAccounts = accountAdapter.getSelectedAccounts();
        
        if (selectedAccounts.isEmpty()) {
            Toast.makeText(this, "Please select at least one account to approve", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Approve Accounts")
                .setMessage("Are you sure you want to approve " + selectedAccounts.size() + " account(s)?")
                .setPositiveButton("Approve", (dialog, which) -> {
                    for (CustomerAccount account : selectedAccounts) {
                        updateAccountStatus(account.getAccountId(), "Active");
                    }
                    Toast.makeText(this, selectedAccounts.size() + " account(s) approved", 
                            Toast.LENGTH_SHORT).show();
                    loadAccountsData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void viewAccountDetails() {
        List<CustomerAccount> selectedAccounts = accountAdapter.getSelectedAccounts();
        
        if (selectedAccounts.isEmpty()) {
            Toast.makeText(this, "Please select an account to view details", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAccounts.size() > 1) {
            Toast.makeText(this, "Please select only one account to view details", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        CustomerAccount account = selectedAccounts.get(0);
        
        String details = "Account Number: " + account.getAccountNumber() + "\n" +
                        "Customer Name: " + account.getCustomerName() + "\n" +
                        "Email: " + account.getEmail() + "\n" +
                        "Phone: " + account.getPhone() + "\n" +
                        "Account Type: " + account.getAccountType() + "\n" +
                        "Balance: ৳" + String.format("%.2f", account.getBalance()) + "\n" +
                        "Status: " + account.getStatus() + "\n" +
                        "Created Date: " + account.getCreatedDate();

        new AlertDialog.Builder(this)
                .setTitle("Account Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateAccountStatus(String accountId, String newStatus) {
        if (accountId == null || accountId.isEmpty()) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        db.collection("accounts").document(accountId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Success - data will be reloaded
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating account: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
}
