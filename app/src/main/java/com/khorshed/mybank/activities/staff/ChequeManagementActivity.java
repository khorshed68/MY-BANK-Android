package com.khorshed.mybank.activities.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.R;
import com.khorshed.mybank.adapter.ChequeRequestAdapter;
import com.khorshed.mybank.models.ChequeBookRequest;
import com.khorshed.mybank.models.ChequeBook;
import com.khorshed.mybank.models.Cheque;
import com.khorshed.mybank.models.DepositedCheque;
import com.khorshed.mybank.services.EmailService;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.graphics.Color;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChequeManagementActivity extends AppCompatActivity 
    implements ChequeRequestAdapter.OnRequestSelectedListener {

    private TabLayout chequeTabLayout;
    private Button refreshAllButton, backToDashboardButton;
    private Button approveButton, rejectButton;
    private Button clearChequeButton, bounceChequeButton;
    private EditText approvalRemarksInput, rejectionReasonInput;
    private EditText clearanceRemarksInput, bounceReasonInput;
    private CheckBox signatureVerifiedCheckbox;
    private RecyclerView chequeRequestsRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout actionSection, emptyStateLayout;
    private LinearLayout pendingRequestsLayout, clearanceLayout, allBooksLayout;
    private LinearLayout allChequesLayout, statisticsLayout;
    private TableLayout depositedChequesTable;
    private TableRow emptyChequeClearanceRow;
    
    // All Books components
    private EditText searchBooksInput;
    private Button searchBooksButton;
    private Spinner statusFilterSpinner;
    private TableLayout allBooksTable;
    private TableRow emptyAllBooksRow;
    private List<ChequeBook> allChequeBooks = new ArrayList<>();
    private List<ChequeBook> filteredChequeBooks = new ArrayList<>();
    
    // All Cheques components
    private EditText searchChequesInput;
    private Button searchChequesButton;
    private Spinner chequeStatusFilterSpinner;
    private TableLayout allChequesTable;
    private TableRow emptyAllChequesRow;
    private List<Cheque> allCheques = new ArrayList<>();
    private List<Cheque> filteredCheques = new ArrayList<>();
    
    // Statistics components
    private TextView totalRequestsText, pendingApprovalText, approvedBooksText, activeBooksText;
    private TextView totalChequesText, pendingClearanceText, clearedChequesText, bouncedChequesText;
    
    private ChequeRequestAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ChequeBookRequest selectedRequest;
    private DepositedCheque selectedCheque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheque_management);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        initializeViews();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        
        loadPendingRequests();
    }
    
    private void initializeViews() {
        chequeTabLayout = findViewById(R.id.chequeTabLayout);
        refreshAllButton = findViewById(R.id.refreshAllButton);
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        approveButton = findViewById(R.id.approveButton);
        rejectButton = findViewById(R.id.rejectButton);
        approvalRemarksInput = findViewById(R.id.approvalRemarksInput);
        rejectionReasonInput = findViewById(R.id.rejectionReasonInput);
        chequeRequestsRecyclerView = findViewById(R.id.chequeRequestsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        actionSection = findViewById(R.id.actionSection);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        pendingRequestsLayout = findViewById(R.id.pendingRequestsLayout);
        clearanceLayout = findViewById(R.id.clearanceLayout);
        allBooksLayout = findViewById(R.id.allBooksLayout);
        allChequesLayout = findViewById(R.id.allChequesLayout);
        statisticsLayout = findViewById(R.id.statisticsLayout);
        
        // Cheque clearance components
        depositedChequesTable = findViewById(R.id.depositedChequesTable);
        emptyChequeClearanceRow = findViewById(R.id.emptyChequeClearanceRow);
        clearChequeButton = findViewById(R.id.clearChequeButton);
        bounceChequeButton = findViewById(R.id.bounceChequeButton);
        clearanceRemarksInput = findViewById(R.id.clearanceRemarksInput);
        bounceReasonInput = findViewById(R.id.bounceReasonInput);
        signatureVerifiedCheckbox = findViewById(R.id.signatureVerifiedCheckbox);
        
        // All Books components
        searchBooksInput = findViewById(R.id.searchBooksInput);
        searchBooksButton = findViewById(R.id.searchBooksButton);
        statusFilterSpinner = findViewById(R.id.statusFilterSpinner);
        allBooksTable = findViewById(R.id.allBooksTable);
        emptyAllBooksRow = findViewById(R.id.emptyAllBooksRow);
        
        // All Cheques components
        searchChequesInput = findViewById(R.id.searchChequesInput);
        searchChequesButton = findViewById(R.id.searchChequesButton);
        chequeStatusFilterSpinner = findViewById(R.id.chequeStatusFilterSpinner);
        allChequesTable = findViewById(R.id.allChequesTable);
        emptyAllChequesRow = findViewById(R.id.emptyAllChequesRow);
        
        // Statistics components
        totalRequestsText = findViewById(R.id.totalRequestsText);
        pendingApprovalText = findViewById(R.id.pendingApprovalText);
        approvedBooksText = findViewById(R.id.approvedBooksText);
        activeBooksText = findViewById(R.id.activeBooksText);
        totalChequesText = findViewById(R.id.totalChequesText);
        pendingClearanceText = findViewById(R.id.pendingClearanceText);
        clearedChequesText = findViewById(R.id.clearedChequesText);
        bouncedChequesText = findViewById(R.id.bouncedChequesText);
        
        setupStatusSpinner();
        setupChequeStatusSpinner();
    }
    
    private void setupTabs() {
        chequeTabLayout.addTab(chequeTabLayout.newTab().setText("Pending Requests"));
        chequeTabLayout.addTab(chequeTabLayout.newTab().setText("Cheque Clearance"));
        chequeTabLayout.addTab(chequeTabLayout.newTab().setText("All Cheque Books"));
        chequeTabLayout.addTab(chequeTabLayout.newTab().setText("All Cheques"));
        chequeTabLayout.addTab(chequeTabLayout.newTab().setText("Statistics"));
        
        chequeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void switchTab(int position) {
        // Hide all layouts
        pendingRequestsLayout.setVisibility(View.GONE);
        clearanceLayout.setVisibility(View.GONE);
        allBooksLayout.setVisibility(View.GONE);
        allChequesLayout.setVisibility(View.GONE);
        statisticsLayout.setVisibility(View.GONE);
        
        // Show selected layout
        switch (position) {
            case 0:
                pendingRequestsLayout.setVisibility(View.VISIBLE);
                loadPendingRequests();
                break;
            case 1:
                clearanceLayout.setVisibility(View.VISIBLE);
                loadDepositedCheques();
                break;
            case 2:
                allBooksLayout.setVisibility(View.VISIBLE);
                loadAllChequeBooks();
                break;
            case 3:
                allChequesLayout.setVisibility(View.VISIBLE);
                loadAllCheques();
                break;
            case 4:
                statisticsLayout.setVisibility(View.VISIBLE);
                loadStatistics();
                break;
        }
    }
    
    private void setupRecyclerView() {
        adapter = new ChequeRequestAdapter(this);
        chequeRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chequeRequestsRecyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backToDashboardButton.setOnClickListener(v -> finish());
        
        refreshAllButton.setOnClickListener(v -> {
            loadPendingRequests();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
        });
        
        approveButton.setOnClickListener(v -> {
            if (selectedRequest != null) {
                String remarks = approvalRemarksInput.getText().toString().trim();
                approveRequest(selectedRequest, remarks);
            }
        });
        
        rejectButton.setOnClickListener(v -> {
            if (selectedRequest != null) {
                String reason = rejectionReasonInput.getText().toString().trim();
                if (reason.isEmpty()) {
                    rejectionReasonInput.setError("Rejection reason is required");
                    rejectionReasonInput.requestFocus();
                    return;
                }
                rejectRequest(selectedRequest, reason);
            }
        });
        
        // Cheque clearance listeners
        clearChequeButton.setOnClickListener(v -> {
            if (selectedCheque != null) {
                clearCheque();
            } else {
                Toast.makeText(this, "Please select a cheque from the table", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        bounceChequeButton.setOnClickListener(v -> {
            if (selectedCheque != null) {
                String reason = bounceReasonInput.getText().toString().trim();
                if (reason.isEmpty()) {
                    bounceReasonInput.setError("Bounce reason is required");
                    bounceReasonInput.requestFocus();
                    return;
                }
                bounceCheque(reason);
            } else {
                Toast.makeText(this, "Please select a cheque from the table", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        // All Books listeners
        searchBooksButton.setOnClickListener(v -> filterChequeBooks());
        
        searchBooksInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChequeBooks();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // All Cheques listeners
        searchChequesButton.setOnClickListener(v -> filterCheques());
        
        searchChequesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCheques();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadPendingRequests() {
        showLoading(true);
        
        db.collection("cheque_book_requests")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<ChequeBookRequest> requests = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    ChequeBookRequest request = doc.toObject(ChequeBookRequest.class);
                    request.setRequestId(doc.getId());
                    requests.add(request);
                }
                
                showLoading(false);
                
                if (requests.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    adapter.setRequests(requests);
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading requests: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    @Override
    public void onRequestSelected(ChequeBookRequest request) {
        selectedRequest = request;
        actionSection.setVisibility(View.VISIBLE);
        approveButton.setEnabled(true);
        rejectButton.setEnabled(true);
        approvalRemarksInput.setText("");
        rejectionReasonInput.setText("");
    }
    
    private void approveRequest(ChequeBookRequest request, String remarks) {
        showLoading(true);
        
        // Get staff name
        String staffName = getSharedPreferences("MyBankPrefs", MODE_PRIVATE)
            .getString("staffName", "Staff");
        
        // Update request status
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "APPROVED");
        updates.put("approvedBy", staffName);
        updates.put("approvalRemarks", remarks);
        updates.put("processedDate", new Date());
        
        db.collection("cheque_book_requests")
            .document(request.getRequestId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Create cheque book record
                createChequeBook(request, staffName);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error approving request: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createChequeBook(ChequeBookRequest request, String staffName) {
        // Generate cheque book number
        String chequeBookNumber = "BK" + System.currentTimeMillis();
        
        // Generate cheque number range
        long baseNumber = 37993740000000L;
        long startChequeNumber = baseNumber + (long)(Math.random() * 10000000);
        long endChequeNumber = startChequeNumber + (request.getNumberOfLeaves() - 1);
        
        Map<String, Object> chequeBook = new HashMap<>();
        chequeBook.put("chequeBookNumber", chequeBookNumber);
        chequeBook.put("accountId", request.getAccountId());
        chequeBook.put("accountNumber", request.getAccountNumber());
        chequeBook.put("userId", request.getUserId());
        chequeBook.put("numberOfLeaves", request.getNumberOfLeaves());
        chequeBook.put("startChequeNumber", startChequeNumber);
        chequeBook.put("endChequeNumber", endChequeNumber);
        chequeBook.put("issuedBy", staffName);
        chequeBook.put("issuedDate", new Date());
        chequeBook.put("status", "ACTIVE");
        chequeBook.put("usedLeaves", 0);
        
        db.collection("cheque_books")
            .document(chequeBookNumber)
            .set(chequeBook)
            .addOnSuccessListener(aVoid -> {
                // Log activity
                StaffActivityLogsActivity.logActivity("APPROVE_CHEQUE_REQUEST", 
                    request.getAccountNumber(), 
                    "Approved cheque book request for " + request.getCustomerName() + 
                    " with " + request.getNumberOfLeaves() + " leaves");
                
                // Send email notification for cheque book approval
                db.collection("users").document(request.getUserId())
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String userEmail = userDoc.getString("email");
                        if (userEmail != null && !userEmail.isEmpty()) {
                            EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                                    .customerName(request.getCustomerName())
                                    .accountNumber(request.getAccountNumber())
                                    .chequeBookNumber(chequeBookNumber)
                                    .numberOfLeaves(request.getNumberOfLeaves())
                                    .startingChequeNumber(String.valueOf(startChequeNumber))
                                    .endingChequeNumber(String.valueOf(endChequeNumber))
                                    .issuedBy(staffName)
                                    .timestamp(new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date()))
                                    .build();
                            
                            EmailService.sendEmail(userEmail, EmailService.NotificationType.CHEQUE_APPROVED, emailData);
                        }
                    });
                
                showLoading(false);
                
                new AlertDialog.Builder(this)
                    .setTitle("Request Approved")
                    .setMessage("Cheque book request has been approved successfully.\n\n" +
                        "Cheque Book Number: " + chequeBookNumber + "\n" +
                        "Cheque Range: " + startChequeNumber + " - " + endChequeNumber + "\n" +
                        "Leaves: " + request.getNumberOfLeaves())
                    .setPositiveButton("OK", (dialog, which) -> {
                        actionSection.setVisibility(View.GONE);
                        loadPendingRequests();
                    })
                    .show();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error creating cheque book: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void rejectRequest(ChequeBookRequest request, String reason) {
        showLoading(true);
        
        // Get staff name
        String staffName = getSharedPreferences("MyBankPrefs", MODE_PRIVATE)
            .getString("staffName", "Staff");
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "REJECTED");
        updates.put("rejectedBy", staffName);
        updates.put("rejectionReason", reason);
        updates.put("processedDate", new Date());
        
        db.collection("cheque_book_requests")
            .document(request.getRequestId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Log activity
                StaffActivityLogsActivity.logActivity("REJECT_CHEQUE_REQUEST", 
                    request.getAccountNumber(), 
                    "Rejected cheque book request for " + request.getCustomerName() + 
                    ". Reason: " + reason);
                
                showLoading(false);
                
                new AlertDialog.Builder(this)
                    .setTitle("Request Rejected")
                    .setMessage("Cheque book request has been rejected.\n\nReason: " + reason)
                    .setPositiveButton("OK", (dialog, which) -> {
                        actionSection.setVisibility(View.GONE);
                        loadPendingRequests();
                    })
                    .show();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error rejecting request: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        chequeRequestsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    // Cheque Clearance Methods
    
    private void loadDepositedCheques() {
        showLoading(true);
        
        db.collection("deposited_cheques")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Clear existing rows except header and empty row
                int childCount = depositedChequesTable.getChildCount();
                if (childCount > 2) {
                    depositedChequesTable.removeViews(2, childCount - 2);
                }
                
                if (querySnapshot.isEmpty()) {
                    emptyChequeClearanceRow.setVisibility(View.VISIBLE);
                } else {
                    emptyChequeClearanceRow.setVisibility(View.GONE);
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        DepositedCheque cheque = doc.toObject(DepositedCheque.class);
                        cheque.setDepositId(doc.getId());
                        addChequeRowToTable(cheque);
                    }
                }
                
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading deposited cheques: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void addChequeRowToTable(DepositedCheque cheque) {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackgroundResource(android.R.drawable.list_selector_background);
        
        // Cheque Number
        addCellToRow(row, cheque.getChequeNumber());
        
        // Account
        addCellToRow(row, cheque.getAccountNumber());
        
        // Payer Name
        addCellToRow(row, cheque.getPayerName());
        
        // Amount
        addCellToRow(row, String.format("%.2f", cheque.getAmount()));
        
        // Account Balance
        addCellToRow(row, String.format("%.2f", cheque.getAccountBalance()));
        
        // Issue Date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", 
            java.util.Locale.getDefault());
        addCellToRow(row, cheque.getIssueDate() != null ? 
            sdf.format(cheque.getIssueDate()) : "N/A");
        
        // Deposit Date
        addCellToRow(row, cheque.getDepositDate() != null ? 
            sdf.format(cheque.getDepositDate()) : "N/A");
        
        // Deposited By
        addCellToRow(row, cheque.getDepositedByStaff());
        
        // Set click listener for row selection
        row.setOnClickListener(v -> {
            selectedCheque = cheque;
            // Highlight selected row
            for (int i = 2; i < depositedChequesTable.getChildCount(); i++) {
                depositedChequesTable.getChildAt(i).setBackgroundColor(
                    getResources().getColor(android.R.color.transparent));
            }
            row.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            
            Toast.makeText(this, "Selected cheque: " + cheque.getChequeNumber(), 
                Toast.LENGTH_SHORT).show();
        });
        
        depositedChequesTable.addView(row);
    }
    
    private void addCellToRow(TableRow row, String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(12, 12, 12, 12);
        cell.setTextSize(14);
        row.addView(cell);
    }
    
    private void clearCheque() {
        if (selectedCheque == null) return;
        
        new AlertDialog.Builder(this)
            .setTitle("Clear Cheque")
            .setMessage("Are you sure you want to clear this cheque?\n\n" +
                "Cheque Number: " + selectedCheque.getChequeNumber() + "\n" +
                "Amount: TAKA " + String.format("%.2f", selectedCheque.getAmount()) + "\n" +
                "Account: " + selectedCheque.getAccountNumber())
            .setPositiveButton("Clear", (dialog, which) -> processClearCheque())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void processClearCheque() {
        showLoading(true);
        
        String staffName = getSharedPreferences("MyBankPrefs", MODE_PRIVATE)
            .getString("staffName", "Staff");
        String remarks = clearanceRemarksInput.getText().toString().trim();
        boolean signatureVerified = signatureVerifiedCheckbox.isChecked();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CLEARED");
        updates.put("clearedBy", staffName);
        updates.put("clearedDate", new Date());
        updates.put("signatureVerified", signatureVerified);
        if (!remarks.isEmpty()) {
            updates.put("clearanceRemarks", remarks);
        }
        
        db.collection("deposited_cheques")
            .document(selectedCheque.getDepositId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Credit the amount to account
                creditAmountToAccount(selectedCheque.getAccountNumber(), 
                    selectedCheque.getAmount(), selectedCheque.getChequeNumber());
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error clearing cheque: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void creditAmountToAccount(String accountNumber, double amount, String chequeNumber) {
        db.collection("accounts")
            .whereEqualTo("accountNumber", accountNumber)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    com.google.firebase.firestore.DocumentSnapshot doc = 
                        querySnapshot.getDocuments().get(0);
                    double currentBalance = doc.getDouble("balance");
                    double newBalance = currentBalance + amount;
                    
                    doc.getReference().update("balance", newBalance)
                        .addOnSuccessListener(aVoid -> {
                            // Log activity
                            StaffActivityLogsActivity.logActivity("CLEAR_CHEQUE", 
                                accountNumber, 
                                "Cleared cheque " + chequeNumber + " for TAKA " + 
                                String.format("%.2f", amount));
                            
                            showLoading(false);
                            
                            new AlertDialog.Builder(this)
                                .setTitle("Cheque Cleared")
                                .setMessage("Cheque has been cleared successfully!\n\n" +
                                    "Amount Credited: TAKA " + String.format("%.2f", amount) + "\n" +
                                    "New Balance: TAKA " + String.format("%.2f", newBalance))
                                .setPositiveButton("OK", (dialog, which) -> {
                                    clearanceRemarksInput.setText("");
                                    signatureVerifiedCheckbox.setChecked(false);
                                    selectedCheque = null;
                                    loadDepositedCheques();
                                })
                                .show();
                        });
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error crediting amount: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void bounceCheque(String reason) {
        if (selectedCheque == null) return;
        
        new AlertDialog.Builder(this)
            .setTitle("Bounce Cheque")
            .setMessage("Are you sure you want to bounce this cheque?\n\n" +
                "Cheque Number: " + selectedCheque.getChequeNumber() + "\n" +
                "Amount: TAKA " + String.format("%.2f", selectedCheque.getAmount()) + "\n" +
                "Reason: " + reason)
            .setPositiveButton("Bounce", (dialog, which) -> processBounceCheque(reason))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void processBounceCheque(String reason) {
        showLoading(true);
        
        String staffName = getSharedPreferences("MyBankPrefs", MODE_PRIVATE)
            .getString("staffName", "Staff");
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "BOUNCED");
        updates.put("bouncedBy", staffName);
        updates.put("bouncedDate", new Date());
        updates.put("bounceReason", reason);
        
        db.collection("deposited_cheques")
            .document(selectedCheque.getDepositId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Log activity
                StaffActivityLogsActivity.logActivity("BOUNCE_CHEQUE", 
                    selectedCheque.getAccountNumber(), 
                    "Bounced cheque " + selectedCheque.getChequeNumber() + 
                    " - Reason: " + reason);
                
                showLoading(false);
                
                new AlertDialog.Builder(this)
                    .setTitle("Cheque Bounced")
                    .setMessage("Cheque has been bounced.\n\n" +
                        "Reason: " + reason + "\n\n" +
                        "Customer will be notified.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        bounceReasonInput.setText("");
                        selectedCheque = null;
                        loadDepositedCheques();
                    })
                    .show();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error bouncing cheque: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    // All Cheque Books Methods
    
    private void setupStatusSpinner() {
        String[] statuses = {"ALL", "PENDING", "APPROVED", "ISSUED", "COMPLETED", "REJECTED", "CANCELLED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilterSpinner.setAdapter(adapter);
        
        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterChequeBooks();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadAllChequeBooks() {
        showLoading(true);
        
        db.collection("cheque_books")
            .orderBy("issuedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allChequeBooks.clear();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ChequeBook book = document.toObject(ChequeBook.class);
                    allChequeBooks.add(book);
                }
                
                filterChequeBooks();
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading cheque books: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void filterChequeBooks() {
        String searchQuery = searchBooksInput.getText().toString().toLowerCase().trim();
        String selectedStatus = statusFilterSpinner.getSelectedItem().toString();
        
        filteredChequeBooks.clear();
        
        for (ChequeBook book : allChequeBooks) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                book.getChequeBookNumber().toLowerCase().contains(searchQuery) ||
                book.getAccountNumber().toLowerCase().contains(searchQuery) ||
                (book.getUserId() != null && book.getUserId().toLowerCase().contains(searchQuery));
            
            boolean matchesStatus = selectedStatus.equals("ALL") || 
                book.getStatus().equalsIgnoreCase(selectedStatus);
            
            if (matchesSearch && matchesStatus) {
                filteredChequeBooks.add(book);
            }
        }
        
        displayChequeBooks();
    }
    
    private void displayChequeBooks() {
        // Clear existing rows except header
        int childCount = allBooksTable.getChildCount();
        if (childCount > 1) {
            allBooksTable.removeViews(1, childCount - 1);
        }
        
        if (filteredChequeBooks.isEmpty()) {
            // Show empty state
            emptyAllBooksRow.setVisibility(View.VISIBLE);
            allBooksTable.addView(emptyAllBooksRow);
        } else {
            emptyAllBooksRow.setVisibility(View.GONE);
            
            // Add rows for filtered cheque books
            for (ChequeBook book : filteredChequeBooks) {
                addBookRowToTable(book);
            }
        }
    }
    
    private void addBookRowToTable(ChequeBook book) {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);
        row.setBackgroundColor(Color.WHITE);
        
        // Get customer name from Firestore
        loadCustomerName(book.getUserId(), customerName -> {
            // Book Number
            addCellToBookRow(row, book.getChequeBookNumber());
            
            // Account Number
            addCellToBookRow(row, book.getAccountNumber());
            
            // Customer Name
            addCellToBookRow(row, customerName);
            
            // Status
            TextView statusCell = new TextView(this);
            statusCell.setText(book.getStatus());
            statusCell.setPadding(12, 12, 12, 12);
            statusCell.setTextSize(13);
            statusCell.setMinWidth(dpToPx(120));
            
            // Color code status
            switch (book.getStatus()) {
                case "ISSUED":
                    statusCell.setTextColor(Color.parseColor("#10B981")); // Green
                    break;
                case "PENDING":
                    statusCell.setTextColor(Color.parseColor("#F59E0B")); // Orange
                    break;
                case "REJECTED":
                case "CANCELLED":
                    statusCell.setTextColor(Color.parseColor("#EF4444")); // Red
                    break;
                default:
                    statusCell.setTextColor(Color.parseColor("#6B7280")); // Gray
                    break;
            }
            row.addView(statusCell);
            
            // Remaining Leaves
            int remainingLeaves = book.getNumberOfLeaves() - book.getUsedLeaves();
            addCellToBookRow(row, String.valueOf(remainingLeaves));
            
            // Approval Date
            String approvalDate = "";
            if (book.getIssuedDate() != null) {
                approvalDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", 
                    java.util.Locale.getDefault()).format(book.getIssuedDate());
            }
            addCellToBookRow(row, approvalDate);
            
            allBooksTable.addView(row);
        });
    }
    
    private void addCellToBookRow(TableRow row, String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(12, 12, 12, 12);
        cell.setTextSize(13);
        cell.setTextColor(Color.parseColor("#374151"));
        
        // Set minimum width based on content
        if (text.length() > 15) {
            cell.setMinWidth(dpToPx(180));
        } else {
            cell.setMinWidth(dpToPx(100));
        }
        
        row.addView(cell);
    }
    
    private void loadCustomerName(String userId, OnCustomerNameLoadedListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onCustomerNameLoaded("N/A");
            return;
        }
        
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullName");
                    listener.onCustomerNameLoaded(name != null ? name : "N/A");
                } else {
                    listener.onCustomerNameLoaded("N/A");
                }
            })
            .addOnFailureListener(e -> listener.onCustomerNameLoaded("N/A"));
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    interface OnCustomerNameLoadedListener {
        void onCustomerNameLoaded(String name);
    }
    
    // All Cheques Methods
    
    private void setupChequeStatusSpinner() {
        String[] statuses = {"ALL", "ISSUED", "DEPOSITED", "PENDING_CLEARANCE", "CLEARED", "BOUNCED", "CANCELLED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chequeStatusFilterSpinner.setAdapter(adapter);
        
        chequeStatusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCheques();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadAllCheques() {
        showLoading(true);
        
        // Load cheques from deposited_cheques collection (these are the actual cheque records)
        db.collection("deposited_cheques")
            .orderBy("depositDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allCheques.clear();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Convert DepositedCheque to Cheque for display
                    DepositedCheque depositedCheque = document.toObject(DepositedCheque.class);
                    
                    Cheque cheque = new Cheque();
                    cheque.setChequeId(depositedCheque.getDepositId());
                    cheque.setChequeNumber(depositedCheque.getChequeNumber());
                    cheque.setAccountNumber(depositedCheque.getAccountNumber());
                    cheque.setAmount(depositedCheque.getAmount());
                    cheque.setPayeeName(depositedCheque.getPayerName());
                    cheque.setStatus(depositedCheque.getStatus());
                    cheque.setClearanceDate(depositedCheque.getClearedDate() != null ? 
                        depositedCheque.getClearedDate() : depositedCheque.getBouncedDate());
                    cheque.setDepositedDate(depositedCheque.getDepositDate());
                    cheque.setIssuedDate(depositedCheque.getIssueDate());
                    
                    allCheques.add(cheque);
                }
                
                // Also load issued cheques that haven't been deposited yet from cheque_books
                loadIssuedCheques();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading cheques: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadIssuedCheques() {
        // Load cheque books to generate issued cheques
        db.collection("cheque_books")
            .whereEqualTo("status", "ISSUED")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ChequeBook book = document.toObject(ChequeBook.class);
                    
                    // Generate cheques for this book
                    long startNum = book.getStartChequeNumber();
                    long endNum = book.getEndChequeNumber();
                    
                    for (long i = startNum; i <= endNum; i++) {
                        String chequeNum = String.valueOf(i);
                        
                        // Check if this cheque is already in deposited cheques
                        boolean alreadyExists = false;
                        for (Cheque existingCheque : allCheques) {
                            if (existingCheque.getChequeNumber().equals(chequeNum)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        
                        if (!alreadyExists) {
                            Cheque cheque = new Cheque();
                            cheque.setChequeNumber(chequeNum);
                            cheque.setChequeBookNumber(book.getChequeBookNumber());
                            cheque.setAccountNumber(book.getAccountNumber());
                            cheque.setUserId(book.getUserId());
                            cheque.setAmount(0.0);
                            cheque.setPayeeName("");
                            cheque.setStatus("ISSUED");
                            cheque.setIssuedDate(book.getIssuedDate());
                            
                            allCheques.add(cheque);
                        }
                    }
                }
                
                filterCheques();
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                filterCheques();
                showLoading(false);
            });
    }
    
    private void filterCheques() {
        String searchQuery = searchChequesInput.getText().toString().toLowerCase().trim();
        String selectedStatus = chequeStatusFilterSpinner.getSelectedItem().toString();
        
        filteredCheques.clear();
        
        for (Cheque cheque : allCheques) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                cheque.getChequeNumber().toLowerCase().contains(searchQuery) ||
                (cheque.getAccountNumber() != null && cheque.getAccountNumber().toLowerCase().contains(searchQuery)) ||
                (cheque.getPayeeName() != null && cheque.getPayeeName().toLowerCase().contains(searchQuery));
            
            boolean matchesStatus = selectedStatus.equals("ALL") || 
                cheque.getStatus().equalsIgnoreCase(selectedStatus);
            
            if (matchesSearch && matchesStatus) {
                filteredCheques.add(cheque);
            }
        }
        
        displayCheques();
    }
    
    private void displayCheques() {
        // Clear existing rows except header
        int childCount = allChequesTable.getChildCount();
        if (childCount > 1) {
            allChequesTable.removeViews(1, childCount - 1);
        }
        
        if (filteredCheques.isEmpty()) {
            // Show empty state
            emptyAllChequesRow.setVisibility(View.VISIBLE);
            allChequesTable.addView(emptyAllChequesRow);
        } else {
            emptyAllChequesRow.setVisibility(View.GONE);
            
            // Add rows for filtered cheques
            for (Cheque cheque : filteredCheques) {
                addChequeRowToTable(cheque);
            }
        }
    }
    
    private void addChequeRowToTable(Cheque cheque) {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);
        row.setBackgroundColor(Color.WHITE);
        
        // Cheque Number
        addCellToChequeRow(row, cheque.getChequeNumber());
        
        // Account Number
        addCellToChequeRow(row, cheque.getAccountNumber() != null ? cheque.getAccountNumber() : "N/A");
        
        // Amount (TAKA)
        String amountStr = cheque.getAmount() > 0 ? String.format("%.2f", cheque.getAmount()) : "0.0";
        addCellToChequeRow(row, amountStr);
        
        // Payee
        addCellToChequeRow(row, cheque.getPayeeName() != null && !cheque.getPayeeName().isEmpty() ? 
            cheque.getPayeeName() : "");
        
        // Status
        TextView statusCell = new TextView(this);
        statusCell.setText(cheque.getStatus());
        statusCell.setPadding(12, 12, 12, 12);
        statusCell.setTextSize(13);
        statusCell.setMinWidth(dpToPx(150));
        
        // Color code status
        switch (cheque.getStatus()) {
            case "ISSUED":
                statusCell.setTextColor(Color.parseColor("#06B6D4")); // Cyan
                break;
            case "DEPOSITED":
                statusCell.setTextColor(Color.parseColor("#F59E0B")); // Orange
                break;
            case "PENDING_CLEARANCE":
                statusCell.setTextColor(Color.parseColor("#8B5CF6")); // Purple
                break;
            case "CLEARED":
                statusCell.setTextColor(Color.parseColor("#10B981")); // Green
                break;
            case "BOUNCED":
                statusCell.setTextColor(Color.parseColor("#EF4444")); // Red
                break;
            case "CANCELLED":
                statusCell.setTextColor(Color.parseColor("#6B7280")); // Gray
                break;
            default:
                statusCell.setTextColor(Color.parseColor("#374151")); // Dark Gray
                break;
        }
        row.addView(statusCell);
        
        // Clearance Date
        String clearanceDate = "";
        if (cheque.getClearanceDate() != null) {
            clearanceDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", 
                java.util.Locale.getDefault()).format(cheque.getClearanceDate());
        }
        addCellToChequeRow(row, clearanceDate);
        
        allChequesTable.addView(row);
    }
    
    private void addCellToChequeRow(TableRow row, String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(12, 12, 12, 12);
        cell.setTextSize(13);
        cell.setTextColor(Color.parseColor("#374151"));
        
        // Set minimum width based on content
        if (text != null && text.length() > 15) {
            cell.setMinWidth(dpToPx(180));
        } else {
            cell.setMinWidth(dpToPx(120));
        }
        
        row.addView(cell);
    }
    
    // Statistics Methods
    
    private void loadStatistics() {
        showLoading(true);
        loadChequeBookStatistics();
        loadChequeStatistics();
    }
    
    private void loadChequeBookStatistics() {
        // Count total requests
        db.collection("cheque_book_requests")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalRequests = queryDocumentSnapshots.size();
                totalRequestsText.setText(String.valueOf(totalRequests));
                
                // Count pending approval
                int pendingApproval = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String status = doc.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        pendingApproval++;
                    }
                }
                pendingApprovalText.setText(String.valueOf(pendingApproval));
            })
            .addOnFailureListener(e -> {
                totalRequestsText.setText("0");
                pendingApprovalText.setText("0");
            });
        
        // Count approved and active books
        db.collection("cheque_books")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int approvedBooks = queryDocumentSnapshots.size();
                approvedBooksText.setText(String.valueOf(approvedBooks));
                
                // Count active books (status = ISSUED or ACTIVE)
                int activeBooks = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String status = doc.getString("status");
                    if ("ISSUED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status)) {
                        activeBooks++;
                    }
                }
                activeBooksText.setText(String.valueOf(activeBooks));
                
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                approvedBooksText.setText("0");
                activeBooksText.setText("0");
                showLoading(false);
            });
    }
    
    private void loadChequeStatistics() {
        // Load from deposited_cheques collection
        db.collection("deposited_cheques")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int pendingClearance = 0;
                int clearedCheques = 0;
                int bouncedCheques = 0;
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String status = doc.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        pendingClearance++;
                    } else if ("CLEARED".equalsIgnoreCase(status)) {
                        clearedCheques++;
                    } else if ("BOUNCED".equalsIgnoreCase(status)) {
                        bouncedCheques++;
                    }
                }
                
                pendingClearanceText.setText(String.valueOf(pendingClearance));
                clearedChequesText.setText(String.valueOf(clearedCheques));
                bouncedChequesText.setText(String.valueOf(bouncedCheques));
                
                // Calculate total cheques from cheque books
                calculateTotalCheques();
            })
            .addOnFailureListener(e -> {
                pendingClearanceText.setText("0");
                clearedChequesText.setText("0");
                bouncedChequesText.setText("0");
            });
    }
    
    private void calculateTotalCheques() {
        db.collection("cheque_books")
            .whereEqualTo("status", "ISSUED")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalCheques = 0;
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Long numberOfLeaves = doc.getLong("numberOfLeaves");
                    if (numberOfLeaves != null) {
                        totalCheques += numberOfLeaves.intValue();
                    }
                }
                
                totalChequesText.setText(String.valueOf(totalCheques));
            })
            .addOnFailureListener(e -> {
                totalChequesText.setText("0");
            });
    }
}
