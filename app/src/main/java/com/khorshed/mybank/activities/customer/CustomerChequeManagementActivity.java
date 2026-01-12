package com.khorshed.mybank.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.Cheque;
import com.khorshed.mybank.models.ChequeBook;
import com.khorshed.mybank.models.ChequeBookRequest;
import com.khorshed.mybank.services.EmailService;
import com.khorshed.mybank.models.DepositedCheque;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CustomerChequeManagementActivity extends AppCompatActivity {

    private Spinner accountSpinner;
    private TextView balanceText, eligibilityText;
    private Button requestChequeButton, refreshButton, backButton;
    private TabLayout tabLayout;
    private ScrollView contentScrollView;
    private ProgressBar progressBar;
    
    // Tab content views
    private LinearLayout myRequestsLayout, myBooksLayout, myChequesLayout, 
                         depositChequeLayout, transactionHistoryLayout;
    
    // Tables
    private TableLayout requestsTable, booksTable, chequesTable, 
                        depositsTable, transactionsTable;
    
    // Deposit form fields
    private EditText chequeNumberInput, payerNameInput, chequeAmountInput;
    private Button depositChequeButton;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private List<Account> userAccounts = new ArrayList<>();
    private Account selectedAccount;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cheque_management);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        
        initializeViews();
        setupTabs();
        setupListeners();
        
        loadUserAccounts();
    }
    
    private void initializeViews() {
        accountSpinner = findViewById(R.id.accountSpinner);
        balanceText = findViewById(R.id.balanceText);
        eligibilityText = findViewById(R.id.eligibilityText);
        requestChequeButton = findViewById(R.id.requestChequeButton);
        refreshButton = findViewById(R.id.refreshButton);
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.tabLayout);
        contentScrollView = findViewById(R.id.contentScrollView);
        progressBar = findViewById(R.id.progressBar);
        
        myRequestsLayout = findViewById(R.id.myRequestsLayout);
        myBooksLayout = findViewById(R.id.myBooksLayout);
        myChequesLayout = findViewById(R.id.myChequesLayout);
        depositChequeLayout = findViewById(R.id.depositChequeLayout);
        transactionHistoryLayout = findViewById(R.id.transactionHistoryLayout);
        
        requestsTable = findViewById(R.id.requestsTable);
        booksTable = findViewById(R.id.booksTable);
        chequesTable = findViewById(R.id.chequesTable);
        depositsTable = findViewById(R.id.depositsTable);
        transactionsTable = findViewById(R.id.transactionsTable);
        
        // Deposit form fields
        chequeNumberInput = findViewById(R.id.chequeNumberInput);
        payerNameInput = findViewById(R.id.payerNameInput);
        chequeAmountInput = findViewById(R.id.chequeAmountInput);
        depositChequeButton = findViewById(R.id.depositChequeButton);
    }
    
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("My Cheque Books"));
        tabLayout.addTab(tabLayout.newTab().setText("My Cheques"));
        tabLayout.addTab(tabLayout.newTab().setText("Deposit Cheque"));
        tabLayout.addTab(tabLayout.newTab().setText("Transaction History"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        // Set initial tab to My Cheque Books (shows requests)
        tabLayout.selectTab(tabLayout.getTabAt(0));
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> {
            if (selectedAccount != null) {
                loadAllData();
            }
        });
        
        requestChequeButton.setOnClickListener(v -> showRequestChequeDialog());
        
        depositChequeButton.setOnClickListener(v -> submitDepositRequest());
        
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAccount = userAccounts.get(position);
                updateAccountInfo();
                loadAllData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadUserAccounts() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("accounts")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userAccounts.clear();
                    List<String> accountDisplayList = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Account account = document.toObject(Account.class);
                        userAccounts.add(account);
                        accountDisplayList.add(account.getAccountNumber() + " - " + account.getAccountType());
                    }
                    
                    if (!userAccounts.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, accountDisplayList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        accountSpinner.setAdapter(adapter);
                        
                        selectedAccount = userAccounts.get(0);
                        updateAccountInfo();
                        loadAllData();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "No active accounts found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading accounts: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateAccountInfo() {
        if (selectedAccount != null) {
            balanceText.setText(String.format("Balance: TAKA %.2f", selectedAccount.getBalance()));
            
            // Check eligibility (e.g., minimum balance required)
            if (selectedAccount.getBalance() >= 1000) {
                eligibilityText.setText("✓ Eligible for Cheque Book");
                eligibilityText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                requestChequeButton.setEnabled(true);
            } else {
                eligibilityText.setText("✗ Minimum balance TAKA 1000 required");
                eligibilityText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                requestChequeButton.setEnabled(false);
            }
        }
    }
    
    private void loadAllData() {
        loadChequeBookRequests();
        loadChequeBooks();
        // Additional data loading can be added for other tabs
    }
    
    private void loadChequeBookRequests() {
        if (selectedAccount == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("cheque_book_requests")
                .whereEqualTo("accountNumber", selectedAccount.getAccountNumber())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear existing rows except header
                    requestsTable.removeViews(1, Math.max(0, requestsTable.getChildCount() - 1));
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        addEmptyRow(requestsTable, "No cheque book requests found");
                    } else {
                        // Convert to list and sort by date in memory
                        List<ChequeBookRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ChequeBookRequest request = document.toObject(ChequeBookRequest.class);
                            requests.add(request);
                        }
                        
                        // Sort by request date descending (newest first)
                        requests.sort((r1, r2) -> {
                            if (r1.getRequestDate() == null && r2.getRequestDate() == null) return 0;
                            if (r1.getRequestDate() == null) return 1;
                            if (r2.getRequestDate() == null) return -1;
                            return r2.getRequestDate().compareTo(r1.getRequestDate());
                        });
                        
                        for (ChequeBookRequest request : requests) {
                            addRequestRow(request);
                        }
                    }
                    
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading requests: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadChequeBooks() {
        if (selectedAccount == null) return;
        
        db.collection("cheque_books")
                .whereEqualTo("accountNumber", selectedAccount.getAccountNumber())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear existing rows except header
                    booksTable.removeViews(1, Math.max(0, booksTable.getChildCount() - 1));
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        addEmptyRow(booksTable, "No cheque books found");
                    } else {
                        // Convert to list and sort by date in memory
                        List<Map<String, Object>> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            books.add(document.getData());
                        }
                        
                        // Sort by issued date descending (newest first)
                        books.sort((b1, b2) -> {
                            Object date1 = b1.get("issuedDate");
                            Object date2 = b2.get("issuedDate");
                            
                            if (date1 == null && date2 == null) return 0;
                            if (date1 == null) return 1;
                            if (date2 == null) return -1;
                            
                            if (date1 instanceof Date && date2 instanceof Date) {
                                return ((Date) date2).compareTo((Date) date1);
                            }
                            return 0;
                        });
                        
                        for (Map<String, Object> bookData : books) {
                            addChequeBookRow(bookData);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading cheque books: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void addRequestRow(ChequeBookRequest request) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT));
        row.setPadding(8, 8, 8, 8);
        
        // Generate cheque numbers for display (simulated)
        long startCheque = 37993740000000L + new Random().nextInt(10000000);
        long endCheque = startCheque + (request.getNumberOfLeaves() - 1);
        
        // Book Number (use requestId or generate one)
        addCellToRow(row, request.getRequestId() != null ? 
            request.getRequestId().substring(0, Math.min(15, request.getRequestId().length())) : "N/A");
        
        // Account
        addCellToRow(row, selectedAccount.getAccountNumber().substring(
            Math.max(0, selectedAccount.getAccountNumber().length() - 1)));
        
        // Start Cheque #
        addCellToRow(row, String.valueOf(startCheque));
        
        // End Cheque #
        addCellToRow(row, String.valueOf(endCheque));
        
        // Total Leaves
        addCellToRow(row, String.valueOf(request.getNumberOfLeaves()));
        
        // Remaining
        addCellToRow(row, String.valueOf(request.getNumberOfLeaves()));
        
        // Status with color
        TextView statusCell = createTextView(request.getStatus());
        switch (request.getStatus()) {
            case "PENDING":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "APPROVED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "ISSUED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "REJECTED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
        row.addView(statusCell);
        
        // Request Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());
        addCellToRow(row, request.getRequestDate() != null ? 
            sdf.format(request.getRequestDate()) : "N/A");
        
        requestsTable.addView(row);
    }
    
    private void addChequeBookRow(Map<String, Object> bookData) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT));
        row.setPadding(8, 8, 8, 8);
        
        String chequeBookNumber = (String) bookData.get("chequeBookNumber");
        Long numberOfLeaves = (Long) bookData.get("numberOfLeaves");
        Long usedLeaves = bookData.get("usedLeaves") != null ? (Long) bookData.get("usedLeaves") : 0L;
        String status = (String) bookData.get("status");
        
        // Get cheque numbers from database or generate if not exists
        Long startCheque = bookData.get("startChequeNumber") != null ? 
            (Long) bookData.get("startChequeNumber") : 
            37993740000000L + new Random().nextInt(10000000);
        Long endCheque = bookData.get("endChequeNumber") != null ? 
            (Long) bookData.get("endChequeNumber") : 
            startCheque + (numberOfLeaves != null ? numberOfLeaves.intValue() - 1 : 24);
        
        addCellToRow(row, chequeBookNumber);
        addCellToRow(row, String.valueOf(startCheque));
        addCellToRow(row, String.valueOf(endCheque));
        addCellToRow(row, String.valueOf(numberOfLeaves != null ? numberOfLeaves : 25));
        addCellToRow(row, String.valueOf(numberOfLeaves != null ? numberOfLeaves - usedLeaves : 25));
        
        TextView statusCell = createTextView(status != null ? status : "ACTIVE");
        statusCell.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        row.addView(statusCell);
        
        booksTable.addView(row);
    }
    
    private void addCellToRow(TableRow row, String text) {
        TextView cell = createTextView(text);
        row.addView(cell);
    }
    
    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(12, 12, 12, 12);
        tv.setTextSize(14);
        tv.setLayoutParams(new TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f));
        return tv;
    }
    
    private void addEmptyRow(TableLayout table, String message) {
        TableRow row = new TableRow(this);
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setPadding(16, 16, 16, 16);
        tv.setTextSize(14);
        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tv.setGravity(android.view.Gravity.CENTER);
        row.addView(tv);
        table.addView(row);
    }
    
    private void switchTab(int position) {
        // Hide all layouts
        myRequestsLayout.setVisibility(View.GONE);
        myBooksLayout.setVisibility(View.GONE);
        myChequesLayout.setVisibility(View.GONE);
        depositChequeLayout.setVisibility(View.GONE);
        transactionHistoryLayout.setVisibility(View.GONE);
        
        // Show selected layout
        switch (position) {
            case 0:
                myRequestsLayout.setVisibility(View.VISIBLE);
                loadChequeBookRequests();
                loadChequeBooks(); // Also load approved books
                break;
            case 1:
                myChequesLayout.setVisibility(View.VISIBLE);
                loadMyCheques();
                break;
            case 2:
                depositChequeLayout.setVisibility(View.VISIBLE);
                loadDepositedCheques();
                break;
            case 3:
                transactionHistoryLayout.setVisibility(View.VISIBLE);
                loadTransactionHistory();
                break;
        }
    }
    
    // MY CHEQUES TAB - Load all cheques from customer's cheque books
    private void loadMyCheques() {
        if (selectedAccount == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Clear existing rows except header
        chequesTable.removeViews(1, Math.max(0, chequesTable.getChildCount() - 1));
        
        // First, get all cheque books for this account
        db.collection("cheque_books")
                .whereEqualTo("accountNumber", selectedAccount.getAccountNumber())
                .whereEqualTo("status", "ACTIVE")
                .get()
                .addOnSuccessListener(chequeBookSnapshots -> {
                    if (chequeBookSnapshots.isEmpty()) {
                        addEmptyRow(chequesTable, "No active cheque books found");
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    
                    List<Cheque> allCheques = new ArrayList<>();
                    
                    // Generate cheques for each cheque book
                    for (QueryDocumentSnapshot bookDoc : chequeBookSnapshots) {
                        Map<String, Object> bookData = bookDoc.getData();
                        Long startNum = (Long) bookData.get("startChequeNumber");
                        Long endNum = (Long) bookData.get("endChequeNumber");
                        String bookNumber = (String) bookData.get("chequeBookNumber");
                        Object issuedDateObj = bookData.get("issuedDate");
                        Date issuedDate = issuedDateObj instanceof Date ? (Date) issuedDateObj : new Date();
                        
                        if (startNum != null && endNum != null) {
                            for (long i = startNum; i <= endNum; i++) {
                                Cheque cheque = new Cheque();
                                cheque.setChequeNumber(String.valueOf(i));
                                cheque.setChequeBookNumber(bookNumber);
                                cheque.setAccountNumber(selectedAccount.getAccountNumber());
                                cheque.setStatus("ISSUED");
                                cheque.setIssuedDate(issuedDate);
                                allCheques.add(cheque);
                            }
                        }
                    }
                    
                    // Now check deposited_cheques to update status of used cheques
                    db.collection("deposited_cheques")
                            .whereEqualTo("accountNumber", selectedAccount.getAccountNumber())
                            .get()
                            .addOnSuccessListener(depositSnapshots -> {
                                // Update cheque statuses based on deposited cheques
                                for (QueryDocumentSnapshot depositDoc : depositSnapshots) {
                                    String chequeNum = depositDoc.getString("chequeNumber");
                                    String status = depositDoc.getString("status");
                                    
                                    for (Cheque cheque : allCheques) {
                                        if (cheque.getChequeNumber().equals(chequeNum)) {
                                            cheque.setStatus(status != null ? status : "DEPOSITED");
                                            Object depositDateObj = depositDoc.get("depositDate");
                                            if (depositDateObj instanceof Date) {
                                                cheque.setDepositedDate((Date) depositDateObj);
                                            }
                                            break;
                                        }
                                    }
                                }
                                
                                // Display cheques
                                if (allCheques.isEmpty()) {
                                    addEmptyRow(chequesTable, "No cheques found");
                                } else {
                                    for (Cheque cheque : allCheques) {
                                        addChequeRow(cheque);
                                    }
                                }
                                
                                progressBar.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                // Even if deposited cheques fail, show the issued cheques
                                for (Cheque cheque : allCheques) {
                                    addChequeRow(cheque);
                                }
                                progressBar.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading cheques: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void addChequeRow(Cheque cheque) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT));
        row.setPadding(8, 8, 8, 8);
        
        // Cheque Number
        addCellToRow(row, cheque.getChequeNumber());
        
        // Status with color
        TextView statusCell = createTextView(cheque.getStatus() != null ? cheque.getStatus() : "ISSUED");
        switch (cheque.getStatus() != null ? cheque.getStatus() : "ISSUED") {
            case "ISSUED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                break;
            case "PENDING":
            case "DEPOSITED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "CLEARED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "BOUNCED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
        row.addView(statusCell);
        
        // Date Issued
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        addCellToRow(row, cheque.getIssuedDate() != null ? 
            sdf.format(cheque.getIssuedDate()) : "N/A");
        
        chequesTable.addView(row);
    }
    
    // DEPOSIT CHEQUE TAB - Submit deposit request
    private void submitDepositRequest() {
        if (selectedAccount == null) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String chequeNumber = chequeNumberInput.getText().toString().trim();
        String payerName = payerNameInput.getText().toString().trim();
        String amountStr = chequeAmountInput.getText().toString().trim();
        
        if (chequeNumber.isEmpty() || payerName.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        String depositId = UUID.randomUUID().toString();
        
        DepositedCheque depositedCheque = new DepositedCheque();
        depositedCheque.setDepositId(depositId);
        depositedCheque.setChequeNumber(chequeNumber);
        depositedCheque.setAccountNumber(selectedAccount.getAccountNumber());
        depositedCheque.setPayerName(payerName);
        depositedCheque.setAmount(amount);
        depositedCheque.setAccountBalance(selectedAccount.getBalance());
        depositedCheque.setDepositedBy(currentUserId);
        depositedCheque.setDepositedByStaff("Self-Service");
        depositedCheque.setStatus("PENDING");
        depositedCheque.setIssueDate(new Date());
        depositedCheque.setDepositDate(new Date());
        depositedCheque.setSignatureVerified(false);
        
        db.collection("deposited_cheques")
                .document(depositId)
                .set(depositedCheque)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    
                    // Clear inputs
                    chequeNumberInput.setText("");
                    payerNameInput.setText("");
                    chequeAmountInput.setText("");
                    
                    Toast.makeText(this, "Cheque deposit request submitted successfully!", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Reload the deposited cheques table
                    loadDepositedCheques();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error submitting deposit: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadDepositedCheques() {
        if (selectedAccount == null) return;
        
        // Clear existing rows except header
        depositsTable.removeViews(1, Math.max(0, depositsTable.getChildCount() - 1));
        
        db.collection("deposited_cheques")
                .whereEqualTo("accountNumber", selectedAccount.getAccountNumber())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        addEmptyRow(depositsTable, "No deposit requests found");
                    } else {
                        List<DepositedCheque> deposits = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            DepositedCheque deposit = document.toObject(DepositedCheque.class);
                            deposits.add(deposit);
                        }
                        
                        // Sort by deposit date descending
                        deposits.sort((d1, d2) -> {
                            if (d1.getDepositDate() == null && d2.getDepositDate() == null) return 0;
                            if (d1.getDepositDate() == null) return 1;
                            if (d2.getDepositDate() == null) return -1;
                            return d2.getDepositDate().compareTo(d1.getDepositDate());
                        });
                        
                        for (DepositedCheque deposit : deposits) {
                            addDepositRow(deposit);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading deposits: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void addDepositRow(DepositedCheque deposit) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT));
        row.setPadding(8, 8, 8, 8);
        
        // Cheque Number
        addCellToRow(row, deposit.getChequeNumber());
        
        // Payer Name
        addCellToRow(row, deposit.getPayerName());
        
        // Amount
        addCellToRow(row, String.format("TAKA %.2f", deposit.getAmount()));
        
        // Status with color
        TextView statusCell = createTextView(deposit.getStatus() != null ? deposit.getStatus() : "PENDING");
        switch (deposit.getStatus() != null ? deposit.getStatus() : "PENDING") {
            case "PENDING":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "CLEARED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "BOUNCED":
                statusCell.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
        row.addView(statusCell);
        
        // Deposit Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        addCellToRow(row, deposit.getDepositDate() != null ? 
            sdf.format(deposit.getDepositDate()) : "N/A");
        
        depositsTable.addView(row);
    }
    
    // TRANSACTION HISTORY TAB - Load cheque-related transactions
    private void loadTransactionHistory() {
        if (selectedAccount == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Clear existing rows except header
        transactionsTable.removeViews(1, Math.max(0, transactionsTable.getChildCount() - 1));
        
        // Load transactions from the transactions collection
        db.collection("transactions")
                .whereEqualTo("accountNumber", selectedAccount.getAccountNumber())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        addEmptyRow(transactionsTable, "No transactions found");
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    
                    List<Map<String, Object>> transactions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> transaction = document.getData();
                        transaction.put("id", document.getId());
                        transactions.add(transaction);
                    }
                    
                    // Sort by timestamp descending
                    transactions.sort((t1, t2) -> {
                        Object date1 = t1.get("timestamp");
                        Object date2 = t2.get("timestamp");
                        
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;
                        
                        if (date1 instanceof Date && date2 instanceof Date) {
                            return ((Date) date2).compareTo((Date) date1);
                        }
                        return 0;
                    });
                    
                    for (Map<String, Object> transaction : transactions) {
                        addTransactionRow(transaction);
                    }
                    
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading transactions: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void addTransactionRow(Map<String, Object> transaction) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT));
        row.setPadding(8, 8, 8, 8);
        
        // Transaction ID (shortened)
        String transId = (String) transaction.get("id");
        addCellToRow(row, transId != null ? transId.substring(0, Math.min(8, transId.length())) + "..." : "N/A");
        
        // Type
        String type = (String) transaction.get("transactionType");
        addCellToRow(row, type != null ? type : "N/A");
        
        // Amount with color
        Double amount = transaction.get("amount") instanceof Long ? 
            ((Long) transaction.get("amount")).doubleValue() : 
            (Double) transaction.get("amount");
        TextView amountCell = createTextView(String.format("TAKA %.2f", amount != null ? amount : 0.0));
        
        // Color code based on transaction type
        if (type != null) {
            if (type.equals("DEPOSIT") || type.equals("CREDIT") || type.contains("RECEIVE")) {
                amountCell.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (type.equals("WITHDRAWAL") || type.equals("DEBIT") || type.contains("SEND")) {
                amountCell.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
        row.addView(amountCell);
        
        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Object timestampObj = transaction.get("timestamp");
        String dateStr = "N/A";
        if (timestampObj instanceof Date) {
            dateStr = sdf.format((Date) timestampObj);
        } else if (timestampObj != null) {
            dateStr = timestampObj.toString();
        }
        addCellToRow(row, dateStr);
        
        transactionsTable.addView(row);
    }
    
    private void showComingSoon(String feature) {
        Toast.makeText(this, feature + " - Coming Soon", Toast.LENGTH_SHORT).show();
    }
    
    private void showRequestChequeDialog() {
        if (selectedAccount == null) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_request_cheque, null);
        final Spinner leavesSpinner = dialogView.findViewById(R.id.leavesSpinner);
        
        String[] leavesOptions = {"10 Leaves", "25 Leaves", "50 Leaves", "100 Leaves"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, leavesOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leavesSpinner.setAdapter(adapter);
        leavesSpinner.setSelection(1); // Default to 25 leaves
        
        new AlertDialog.Builder(this)
            .setTitle("Request Cheque Book")
            .setMessage("Account: " + selectedAccount.getAccountNumber() + "\n" +
                       "Type: " + selectedAccount.getAccountType() + "\n" +
                       "Balance: TAKA " + String.format("%.2f", selectedAccount.getBalance()))
            .setView(dialogView)
            .setPositiveButton("Submit Request", (dialog, which) -> {
                int selectedLeaves = Integer.parseInt(
                    leavesOptions[leavesSpinner.getSelectedItemPosition()].split(" ")[0]);
                submitChequeBookRequest(selectedLeaves);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void submitChequeBookRequest(int numberOfLeaves) {
        progressBar.setVisibility(View.VISIBLE);
        
        String requestId = UUID.randomUUID().toString();
        
        // Get user name from Firestore
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String userName = documentSnapshot.getString("name");
                
                ChequeBookRequest request = new ChequeBookRequest();
                request.setRequestId(requestId);
                request.setUserId(currentUserId);
                request.setAccountId(selectedAccount.getAccountNumber());
                request.setAccountNumber(selectedAccount.getAccountNumber());
                request.setCustomerName(userName != null ? userName : "Customer");
                request.setAccountType(selectedAccount.getAccountType());
                request.setBalance(selectedAccount.getBalance());
                request.setNumberOfLeaves(numberOfLeaves);
                request.setStatus("PENDING");
                request.setRequestDate(new Date());
                
                db.collection("cheque_book_requests")
                    .document(requestId)
                    .set(request)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        
                        // Send email notification for cheque book request
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null && firebaseUser.getEmail() != null) {
                            EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                                    .customerName(userName != null ? userName : "Customer")
                                    .accountNumber(selectedAccount.getAccountNumber())
                                    .numberOfLeaves(numberOfLeaves)
                                    .requestId(requestId)
                                    .timestamp(new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date()))
                                    .build();
                            
                            EmailService.sendEmail(firebaseUser.getEmail(), EmailService.NotificationType.CHEQUE_REQUEST, emailData);
                        }
                        
                        new AlertDialog.Builder(this)
                            .setTitle("Request Submitted")
                            .setMessage("Your cheque book request has been submitted successfully!\n\n" +
                                "Request ID: " + requestId.substring(0, 8) + "...\n" +
                                "Leaves: " + numberOfLeaves + "\n\n" +
                                "Your request will be processed by staff within 2-3 business days.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Switch to first tab to show the new request
                                tabLayout.selectTab(tabLayout.getTabAt(0));
                            })
                            .show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error submitting request: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error getting user info: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}
