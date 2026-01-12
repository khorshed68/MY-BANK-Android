package com.khorshed.mybank.activities.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.R;
import com.khorshed.mybank.adapters.ChequeBookAdapter;
import com.khorshed.mybank.adapters.ChequeAdapter;
import com.khorshed.mybank.adapters.ChequeTableAdapter;
import com.khorshed.mybank.adapters.TransactionHistoryAdapter;
import com.khorshed.mybank.adapters.EligibilityCriteriaAdapter;
import com.khorshed.mybank.models.Cheque;
import com.khorshed.mybank.models.ChequeBook;
import com.khorshed.mybank.models.ChequeAuditLog;
import com.khorshed.mybank.models.ChequeEligibilityCriteria;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChequeOversightActivity extends AppCompatActivity {

    private ImageView backButton;
    private Button refreshButton, exportReportButton, backToDashboardButton;
    
    // Tab buttons
    private Button overviewTab, allChequeBooksTab, allChequesTab, transactionHistoryTab, eligibilitySettingsTab;
    
    // Overview Statistics
    private TextView totalChequeBooksText, pendingApprovalText, activeBooksText;
    private TextView totalChequesIssuedText, clearedChequesText, bouncedChequesText;
    private TextView totalAmountClearedText, totalAmountBouncedText;
    
    // Content containers
    private ScrollView overviewContainer;
    private ScrollView chequeBooksContainer;
    private ScrollView chequesContainer;
    private ScrollView transactionHistoryContainer;
    private ScrollView eligibilitySettingsContainer;
    
    // Cheque Books filters
    private EditText searchInput, fromDateInput, toDateInput;
    private Spinner statusSpinner;
    private Button searchButton, filterButton;
    
    // Cheques filters
    private EditText searchChequesInput, fromDateChequesInput, toDateChequesInput;
    private Spinner chequeStatusSpinner;
    private Button searchChequesButton, filterChequesButton;
    private TextView emptyChequesText;
    
    // Transaction History filters
    private EditText fromDateTransactionInput, toDateTransactionInput;
    private Spinner transactionTypeSpinner;
    private Button filterTransactionButton;
    private TextView emptyTransactionText;
    
    // Eligibility Settings
    private Spinner accountTypeEligibilitySpinner;
    private EditText minBalanceInput, minAccountAgeInput, maxBooksPerYearInput, leavesPerBookInput;
    private CheckBox activeEligibilityCheckbox;
    private Button saveCriteriaButton, clearFormButton, deleteSelectedButton;
    private TextView emptyEligibilityText;
    
    // RecyclerViews
    private RecyclerView chequeBooksRecyclerView, chequesRecyclerView, transactionHistoryRecyclerView, eligibilityCriteriaRecyclerView;
    private ChequeBookAdapter chequeBookAdapter;
    private ChequeAdapter chequeAdapter;
    private ChequeTableAdapter chequeTableAdapter;
    private TransactionHistoryAdapter transactionHistoryAdapter;
    private EligibilityCriteriaAdapter eligibilityCriteriaAdapter;
    
    private ProgressBar loadingProgressBar;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<ChequeBook> chequeBookList;
    private List<ChequeBook> filteredChequeBookList;
    private List<Cheque> chequeList;
    private List<Cheque> filteredChequeList;
    private List<ChequeAuditLog> transactionList;
    private List<ChequeAuditLog> filteredTransactionList;
    private List<ChequeEligibilityCriteria> criteriaList;
    private String currentTab = "overview";
    private ChequeEligibilityCriteria selectedCriteria = null;
    
    // Date range for filtering
    private Date fromDate = null;
    private Date toDate = null;
    private Date fromDateCheques = null;
    private Date toDateCheques = null;
    private Date fromDateTransaction = null;
    private Date toDateTransaction = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheque_oversight);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        chequeBookList = new ArrayList<>();
        filteredChequeBookList = new ArrayList<>();
        chequeList = new ArrayList<>();
        filteredChequeList = new ArrayList<>();
        transactionList = new ArrayList<>();
        filteredTransactionList = new ArrayList<>();
        criteriaList = new ArrayList<>();
        
        initializeViews();
        setupClickListeners();
        loadOverviewStatistics();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        exportReportButton = findViewById(R.id.exportReportButton);
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        
        // Tabs
        overviewTab = findViewById(R.id.overviewTab);
        allChequeBooksTab = findViewById(R.id.allChequeBooksTab);
        allChequesTab = findViewById(R.id.allChequesTab);
        transactionHistoryTab = findViewById(R.id.transactionHistoryTab);
        eligibilitySettingsTab = findViewById(R.id.eligibilitySettingsTab);
        
        // Overview statistics
        totalChequeBooksText = findViewById(R.id.totalChequeBooksText);
        pendingApprovalText = findViewById(R.id.pendingApprovalText);
        activeBooksText = findViewById(R.id.activeBooksText);
        totalChequesIssuedText = findViewById(R.id.totalChequesIssuedText);
        clearedChequesText = findViewById(R.id.clearedChequesText);
        bouncedChequesText = findViewById(R.id.bouncedChequesText);
        totalAmountClearedText = findViewById(R.id.totalAmountClearedText);
        totalAmountBouncedText = findViewById(R.id.totalAmountBouncedText);
        
        // Containers
        overviewContainer = findViewById(R.id.overviewContainer);
        chequeBooksContainer = findViewById(R.id.chequeBooksContainer);
        chequesContainer = findViewById(R.id.chequesContainer);
        transactionHistoryContainer = findViewById(R.id.transactionHistoryContainer);
        eligibilitySettingsContainer = findViewById(R.id.eligibilitySettingsContainer);
        
        // RecyclerViews
        chequeBooksRecyclerView = findViewById(R.id.chequeBooksRecyclerView);
        chequesRecyclerView = findViewById(R.id.chequesRecyclerView);
        transactionHistoryRecyclerView = findViewById(R.id.transactionHistoryRecyclerView);
        eligibilityCriteriaRecyclerView = findViewById(R.id.eligibilityCriteriaRecyclerView);
        
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Setup RecyclerViews
        chequeBookAdapter = new ChequeBookAdapter(this, chequeBookList);
        chequeBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chequeBooksRecyclerView.setAdapter(chequeBookAdapter);
        chequeBooksRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        
        chequeTableAdapter = new ChequeTableAdapter(chequeList);
        chequesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chequesRecyclerView.setAdapter(chequeTableAdapter);
        chequesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        
        transactionHistoryAdapter = new TransactionHistoryAdapter(transactionList);
        transactionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionHistoryRecyclerView.setAdapter(transactionHistoryAdapter);
        transactionHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        
        eligibilityCriteriaAdapter = new EligibilityCriteriaAdapter(criteriaList, this::onCriteriaSelected);
        eligibilityCriteriaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eligibilityCriteriaRecyclerView.setAdapter(eligibilityCriteriaAdapter);
        eligibilityCriteriaRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        
        // Initialize cheque books filters
        searchInput = findViewById(R.id.searchInput);
        statusSpinner = findViewById(R.id.statusSpinner);
        fromDateInput = findViewById(R.id.fromDateInput);
        toDateInput = findViewById(R.id.toDateInput);
        searchButton = findViewById(R.id.searchButton);
        filterButton = findViewById(R.id.filterButton);
        
        // Initialize cheques filters
        searchChequesInput = findViewById(R.id.searchChequesInput);
        chequeStatusSpinner = findViewById(R.id.chequeStatusSpinner);
        fromDateChequesInput = findViewById(R.id.fromDateChequesInput);
        toDateChequesInput = findViewById(R.id.toDateChequesInput);
        searchChequesButton = findViewById(R.id.searchChequesButton);
        filterChequesButton = findViewById(R.id.filterChequesButton);
        emptyChequesText = findViewById(R.id.emptyChequesText);
        
        // Initialize transaction history filters
        transactionTypeSpinner = findViewById(R.id.transactionTypeSpinner);
        fromDateTransactionInput = findViewById(R.id.fromDateTransactionInput);
        toDateTransactionInput = findViewById(R.id.toDateTransactionInput);
        filterTransactionButton = findViewById(R.id.filterTransactionButton);
        emptyTransactionText = findViewById(R.id.emptyTransactionText);
        
        // Initialize eligibility settings components
        accountTypeEligibilitySpinner = findViewById(R.id.accountTypeEligibilitySpinner);
        minBalanceInput = findViewById(R.id.minBalanceInput);
        minAccountAgeInput = findViewById(R.id.minAccountAgeInput);
        maxBooksPerYearInput = findViewById(R.id.maxBooksPerYearInput);
        leavesPerBookInput = findViewById(R.id.leavesPerBookInput);
        activeEligibilityCheckbox = findViewById(R.id.activeEligibilityCheckbox);
        saveCriteriaButton = findViewById(R.id.saveCriteriaButton);
        clearFormButton = findViewById(R.id.clearFormButton);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        emptyEligibilityText = findViewById(R.id.emptyEligibilityText);
        
        // Setup status spinners
        setupStatusSpinner();
        setupChequeStatusSpinner();
        setupTransactionTypeSpinner();
        setupAccountTypeSpinner();
    }
    
    private void setupStatusSpinner() {
        String[] statuses = {"ALL", "PENDING", "APPROVED", "ISSUED", "COMPLETED", "REJECTED", "CANCELLED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
    }
    
    private void setupChequeStatusSpinner() {
        String[] statuses = {"ALL", "ISSUED", "DEPOSITED", "PENDING_CLEARANCE", "CLEARED", "BOUNCED", "CANCELLED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chequeStatusSpinner.setAdapter(adapter);
    }
    
    private void setupTransactionTypeSpinner() {
        String[] types = {"ALL", "STATUS_CHANGE", "ISSUE", "DEPOSIT", "CLEAR", "BOUNCE", "CANCEL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transactionTypeSpinner.setAdapter(adapter);
    }
    
    private void setupAccountTypeSpinner() {
        String[] accountTypes = {"CURRENT", "SALARY", "SAVINGS", "FIXED_DEPOSIT", "RECURRING"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeEligibilitySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        backToDashboardButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> {
            switch (currentTab) {
                case "overview":
                    loadOverviewStatistics();
                    break;
                case "chequeBooks":
                    loadAllChequeBooks();
                    break;
                case "cheques":
                    loadAllCheques();
                    break;
            }
            Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show();
        });
        
        exportReportButton.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting report...", Toast.LENGTH_SHORT).show();
            exportReport();
        });
        
        overviewTab.setOnClickListener(v -> showTab("overview"));
        allChequeBooksTab.setOnClickListener(v -> showTab("chequeBooks"));
        allChequesTab.setOnClickListener(v -> showTab("cheques"));
        transactionHistoryTab.setOnClickListener(v -> showTab("transactions"));
        eligibilitySettingsTab.setOnClickListener(v -> showTab("eligibility"));
        
        // Search and Filter buttons
        searchButton.setOnClickListener(v -> performSearch());
        filterButton.setOnClickListener(v -> performFilter());
        
        // Cheques Search and Filter buttons
        searchChequesButton.setOnClickListener(v -> performChequeSearch());
        filterChequesButton.setOnClickListener(v -> performChequeFilter());
        
        // Transaction History Filter button
        filterTransactionButton.setOnClickListener(v -> performTransactionFilter());
        
        // Date pickers
        fromDateInput.setOnClickListener(v -> showDatePicker(true, false, false));
        toDateInput.setOnClickListener(v -> showDatePicker(false, false, false));
        
        // Cheques Date pickers
        fromDateChequesInput.setOnClickListener(v -> showDatePicker(true, true, false));
        toDateChequesInput.setOnClickListener(v -> showDatePicker(false, true, false));
        
        // Transaction History Date pickers
        fromDateTransactionInput.setOnClickListener(v -> showDatePicker(true, false, true));
        toDateTransactionInput.setOnClickListener(v -> showDatePicker(false, false, true));
        
        // Eligibility Settings buttons
        saveCriteriaButton.setOnClickListener(v -> saveCriteria());
        clearFormButton.setOnClickListener(v -> clearEligibilityForm());
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedCriteria());
    }

    private void showTab(String tab) {
        currentTab = tab;
        
        // Reset all tab backgrounds
        overviewTab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        allChequeBooksTab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        allChequesTab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        transactionHistoryTab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        eligibilitySettingsTab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        
        // Hide all containers
        overviewContainer.setVisibility(View.GONE);
        chequeBooksContainer.setVisibility(View.GONE);
        chequesContainer.setVisibility(View.GONE);
        transactionHistoryContainer.setVisibility(View.GONE);
        eligibilitySettingsContainer.setVisibility(View.GONE);
        
        // Show selected tab
        switch (tab) {
            case "overview":
                overviewTab.setBackgroundColor(getResources().getColor(android.R.color.white));
                overviewContainer.setVisibility(View.VISIBLE);
                loadOverviewStatistics();
                break;
            case "chequeBooks":
                allChequeBooksTab.setBackgroundColor(getResources().getColor(android.R.color.white));
                chequeBooksContainer.setVisibility(View.VISIBLE);
                loadAllChequeBooks();
                break;
            case "cheques":
                allChequesTab.setBackgroundColor(getResources().getColor(android.R.color.white));
                chequesContainer.setVisibility(View.VISIBLE);
                loadAllCheques();
                break;
            case "transactions":
                transactionHistoryTab.setBackgroundColor(getResources().getColor(android.R.color.white));
                transactionHistoryContainer.setVisibility(View.VISIBLE);
                loadTransactionHistory();
                break;
            case "eligibility":
                eligibilitySettingsTab.setBackgroundColor(getResources().getColor(android.R.color.white));
                eligibilitySettingsContainer.setVisibility(View.VISIBLE);
                loadEligibilitySettings();
                break;
        }
    }

    private void loadOverviewStatistics() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        // Load Cheque Book Statistics
        loadChequeBookStats();
        
        // Load Cheque Statistics
        loadChequeStats();
        
        // Load Financial Statistics
        loadFinancialStats();
    }

    private void loadChequeBookStats() {
        // Total Cheque Books
        db.collection("chequeBooks")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int total = queryDocumentSnapshots.size();
                totalChequeBooksText.setText(String.valueOf(total));
                
                // Count pending approvals
                int pending = 0;
                int active = 0;
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ChequeBook book = doc.toObject(ChequeBook.class);
                    if (book.getStatus() != null) {
                        if (book.getStatus().equals("PENDING")) {
                            pending++;
                        } else if (book.getStatus().equals("ACTIVE")) {
                            active++;
                        }
                    }
                }
                
                pendingApprovalText.setText(String.valueOf(pending));
                activeBooksText.setText(String.valueOf(active));
            })
            .addOnFailureListener(e -> {
                totalChequeBooksText.setText("0");
                pendingApprovalText.setText("0");
                activeBooksText.setText("0");
            });
    }

    private void loadChequeStats() {
        db.collection("cheques")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int total = queryDocumentSnapshots.size();
                totalChequesIssuedText.setText(String.valueOf(total));
                
                int cleared = 0;
                int bounced = 0;
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Cheque cheque = doc.toObject(Cheque.class);
                    if (cheque.getStatus() != null) {
                        if (cheque.getStatus().equals("CLEARED")) {
                            cleared++;
                        } else if (cheque.getStatus().equals("BOUNCED")) {
                            bounced++;
                        }
                    }
                }
                
                clearedChequesText.setText(String.valueOf(cleared));
                bouncedChequesText.setText(String.valueOf(bounced));
            })
            .addOnFailureListener(e -> {
                totalChequesIssuedText.setText("0");
                clearedChequesText.setText("0");
                bouncedChequesText.setText("0");
            });
    }

    private void loadFinancialStats() {
        db.collection("cheques")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                double totalCleared = 0.0;
                double totalBounced = 0.0;
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Cheque cheque = doc.toObject(Cheque.class);
                    if (cheque.getStatus() != null) {
                        if (cheque.getStatus().equals("CLEARED")) {
                            totalCleared += cheque.getAmount();
                        } else if (cheque.getStatus().equals("BOUNCED")) {
                            totalBounced += cheque.getAmount();
                        }
                    }
                }
                
                totalAmountClearedText.setText(String.format(Locale.getDefault(), "TAKA %.2f", totalCleared));
                totalAmountBouncedText.setText(String.format(Locale.getDefault(), "TAKA %.2f", totalBounced));
                
                loadingProgressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                totalAmountClearedText.setText("TAKA 0.00");
                totalAmountBouncedText.setText("TAKA 0.00");
                loadingProgressBar.setVisibility(View.GONE);
            });
    }

    private void loadAllChequeBooks() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        db.collection("chequeBooks")
            .orderBy("issuedDate")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                chequeBookList.clear();
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ChequeBook book = doc.toObject(ChequeBook.class);
                    chequeBookList.add(book);
                }
                
                chequeBookAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load cheque books: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadAllCheques() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        db.collection("cheques")
            .orderBy("issuedDate")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                chequeList.clear();
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Cheque cheque = doc.toObject(Cheque.class);
                    chequeList.add(cheque);
                }
                
                // Load customer names for all cheques
                loadCustomerNamesForCheques();
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load cheques: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadCustomerNamesForCheques() {
        if (chequeList.isEmpty()) {
            chequeTableAdapter.notifyDataSetChanged();
            loadingProgressBar.setVisibility(View.GONE);
            emptyChequesText.setVisibility(View.VISIBLE);
            return;
        }
        
        emptyChequesText.setVisibility(View.GONE);
        
        // Load customer names for each cheque
        int[] loadedCount = {0};
        for (Cheque cheque : chequeList) {
            if (cheque.getUserId() != null && !cheque.getUserId().isEmpty()) {
                db.collection("users")
                    .document(cheque.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String customerName = documentSnapshot.getString("name");
                            cheque.setCustomerName(customerName);
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] == chequeList.size()) {
                            chequeTableAdapter.notifyDataSetChanged();
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == chequeList.size()) {
                            chequeTableAdapter.notifyDataSetChanged();
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                    });
            } else {
                loadedCount[0]++;
                if (loadedCount[0] == chequeList.size()) {
                    chequeTableAdapter.notifyDataSetChanged();
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadTransactionHistory() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        db.collection("cheque_audit_logs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                transactionList.clear();
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ChequeAuditLog log = doc.toObject(ChequeAuditLog.class);
                    transactionList.add(log);
                }
                
                if (transactionList.isEmpty()) {
                    emptyTransactionText.setVisibility(View.VISIBLE);
                } else {
                    emptyTransactionText.setVisibility(View.GONE);
                }
                
                transactionHistoryAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                emptyTransactionText.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Failed to load transaction history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void performTransactionFilter() {
        String selectedType = transactionTypeSpinner.getSelectedItem().toString();
        
        filteredTransactionList = new ArrayList<>();
        
        for (ChequeAuditLog log : transactionList) {
            boolean typeMatch = selectedType.equals("ALL") || 
                (log.getType() != null && log.getType().equals(selectedType));
            boolean dateMatch = true;
            
            // Check date range if dates are selected
            if (fromDateTransaction != null || toDateTransaction != null) {
                Date logDate = log.getTimestamp();
                if (logDate != null) {
                    if (fromDateTransaction != null && logDate.before(fromDateTransaction)) {
                        dateMatch = false;
                    }
                    if (toDateTransaction != null && logDate.after(toDateTransaction)) {
                        dateMatch = false;
                    }
                } else {
                    dateMatch = false;
                }
            }
            
            if (typeMatch && dateMatch) {
                filteredTransactionList.add(log);
            }
        }
        
        String filterSummary = "Filters: Type=" + selectedType;
        if (fromDateTransaction != null) {
            filterSummary += ", From=" + dateFormat.format(fromDateTransaction);
        }
        if (toDateTransaction != null) {
            filterSummary += ", To=" + dateFormat.format(toDateTransaction);
        }
        
        Toast.makeText(this, filterSummary + "\nFound " + filteredTransactionList.size() + " result(s)", Toast.LENGTH_SHORT).show();
        
        if (filteredTransactionList.isEmpty()) {
            emptyTransactionText.setVisibility(View.VISIBLE);
        } else {
            emptyTransactionText.setVisibility(View.GONE);
        }
        
        transactionHistoryAdapter.updateList(filteredTransactionList);
    }

    private void loadEligibilitySettings() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        db.collection("cheque_eligibility_criteria")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                criteriaList.clear();
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ChequeEligibilityCriteria criteria = doc.toObject(ChequeEligibilityCriteria.class);
                    criteriaList.add(criteria);
                }
                
                if (criteriaList.isEmpty()) {
                    emptyEligibilityText.setVisibility(View.VISIBLE);
                } else {
                    emptyEligibilityText.setVisibility(View.GONE);
                }
                
                eligibilityCriteriaAdapter.updateList(criteriaList);
                loadingProgressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                emptyEligibilityText.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Failed to load eligibility criteria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void onCriteriaSelected(ChequeEligibilityCriteria criteria, int position) {
        selectedCriteria = criteria;
        deleteSelectedButton.setEnabled(true);
        
        // Load criteria into form for editing
        accountTypeEligibilitySpinner.setSelection(getAccountTypePosition(criteria.getAccountType()));
        minBalanceInput.setText(String.valueOf(criteria.getMinBalance()));
        minAccountAgeInput.setText(String.valueOf(criteria.getMinAccountAgeDays()));
        maxBooksPerYearInput.setText(String.valueOf(criteria.getMaxBooksPerYear()));
        leavesPerBookInput.setText(String.valueOf(criteria.getLeavesPerBook()));
        activeEligibilityCheckbox.setChecked(criteria.isActive());
        
        saveCriteriaButton.setText("Update Criteria");
    }
    
    private int getAccountTypePosition(String accountType) {
        String[] types = {"CURRENT", "SALARY", "SAVINGS", "FIXED_DEPOSIT", "RECURRING"};
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(accountType)) {
                return i;
            }
        }
        return 0;
    }
    
    private void saveCriteria() {
        // Validate inputs
        String accountType = accountTypeEligibilitySpinner.getSelectedItem().toString();
        String minBalanceStr = minBalanceInput.getText().toString().trim();
        String minAgeStr = minAccountAgeInput.getText().toString().trim();
        String maxBooksStr = maxBooksPerYearInput.getText().toString().trim();
        String leavesStr = leavesPerBookInput.getText().toString().trim();
        
        if (minBalanceStr.isEmpty() || minAgeStr.isEmpty() || maxBooksStr.isEmpty() || leavesStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double minBalance = Double.parseDouble(minBalanceStr);
        int minAge = Integer.parseInt(minAgeStr);
        int maxBooks = Integer.parseInt(maxBooksStr);
        int leaves = Integer.parseInt(leavesStr);
        boolean active = activeEligibilityCheckbox.isChecked();
        
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        if (selectedCriteria != null) {
            // Update existing criteria
            selectedCriteria.setAccountType(accountType);
            selectedCriteria.setMinBalance(minBalance);
            selectedCriteria.setMinAccountAgeDays(minAge);
            selectedCriteria.setMaxBooksPerYear(maxBooks);
            selectedCriteria.setLeavesPerBook(leaves);
            selectedCriteria.setActive(active);
            
            db.collection("cheque_eligibility_criteria")
                .document(selectedCriteria.getCriteriaId())
                .set(selectedCriteria)
                .addOnSuccessListener(aVoid -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Eligibility criteria updated successfully", Toast.LENGTH_SHORT).show();
                    clearEligibilityForm();
                    loadEligibilitySettings();
                })
                .addOnFailureListener(e -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update criteria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            // Check if criteria already exists for this account type
            db.collection("cheque_eligibility_criteria")
                .whereEqualTo("accountType", accountType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Criteria for " + accountType + " already exists. Please edit the existing one.", Toast.LENGTH_LONG).show();
                    } else {
                        // Create new criteria
                        ChequeEligibilityCriteria newCriteria = new ChequeEligibilityCriteria(
                            accountType, minBalance, minAge, maxBooks, leaves, active
                        );
                        
                        String criteriaId = db.collection("cheque_eligibility_criteria").document().getId();
                        newCriteria.setCriteriaId(criteriaId);
                        
                        db.collection("cheque_eligibility_criteria")
                            .document(criteriaId)
                            .set(newCriteria)
                            .addOnSuccessListener(aVoid -> {
                                loadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Eligibility criteria saved successfully", Toast.LENGTH_SHORT).show();
                                clearEligibilityForm();
                                loadEligibilitySettings();
                            })
                            .addOnFailureListener(e -> {
                                loadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed to save criteria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error checking existing criteria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }
    
    private void clearEligibilityForm() {
        accountTypeEligibilitySpinner.setSelection(0);
        minBalanceInput.setText("");
        minAccountAgeInput.setText("");
        maxBooksPerYearInput.setText("");
        leavesPerBookInput.setText("");
        activeEligibilityCheckbox.setChecked(true);
        selectedCriteria = null;
        saveCriteriaButton.setText("Save Criteria");
        deleteSelectedButton.setEnabled(false);
        eligibilityCriteriaAdapter.clearSelection();
    }
    
    private void deleteSelectedCriteria() {
        if (selectedCriteria == null) {
            Toast.makeText(this, "Please select a criteria to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete Criteria")
            .setMessage("Are you sure you want to delete eligibility criteria for " + selectedCriteria.getAccountType() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                loadingProgressBar.setVisibility(View.VISIBLE);
                
                db.collection("cheque_eligibility_criteria")
                    .document(selectedCriteria.getCriteriaId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Criteria deleted successfully", Toast.LENGTH_SHORT).show();
                        clearEligibilityForm();
                        loadEligibilitySettings();
                    })
                    .addOnFailureListener(e -> {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to delete criteria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void performSearch() {
        String searchQuery = searchInput.getText().toString().trim().toLowerCase();
        
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter search term", Toast.LENGTH_SHORT).show();
            return;
        }
        
        filteredChequeBookList = new ArrayList<>();
        for (ChequeBook book : chequeBookList) {
            if (book.getBookNumber().toLowerCase().contains(searchQuery) ||
                book.getAccountNumber().toLowerCase().contains(searchQuery) ||
                book.getUserId().toLowerCase().contains(searchQuery)) {
                filteredChequeBookList.add(book);
            }
        }
        
        if (filteredChequeBookList.isEmpty()) {
            Toast.makeText(this, "No results found for \"" + searchQuery + "\"", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Found " + filteredChequeBookList.size() + " result(s)", Toast.LENGTH_SHORT).show();
        }
        
        updateChequeBookList(filteredChequeBookList);
    }
    
    private void performFilter() {
        String selectedStatus = statusSpinner.getSelectedItem().toString();
        
        filteredChequeBookList = new ArrayList<>();
        
        for (ChequeBook book : chequeBookList) {
            boolean statusMatch = selectedStatus.equals("ALL") || book.getStatus().equals(selectedStatus);
            boolean dateMatch = true;
            
            // Check date range if dates are selected
            if (fromDate != null || toDate != null) {
                Date requestDate = book.getRequestDate();
                if (requestDate != null) {
                    if (fromDate != null && requestDate.before(fromDate)) {
                        dateMatch = false;
                    }
                    if (toDate != null && requestDate.after(toDate)) {
                        dateMatch = false;
                    }
                } else {
                    dateMatch = false;
                }
            }
            
            if (statusMatch && dateMatch) {
                filteredChequeBookList.add(book);
            }
        }
        
        String filterSummary = "Filters: Status=" + selectedStatus;
        if (fromDate != null) {
            filterSummary += ", From=" + dateFormat.format(fromDate);
        }
        if (toDate != null) {
            filterSummary += ", To=" + dateFormat.format(toDate);
        }
        
        Toast.makeText(this, filterSummary + "\nFound " + filteredChequeBookList.size() + " result(s)", Toast.LENGTH_SHORT).show();
        
        updateChequeBookList(filteredChequeBookList);
    }
    
    private void showDatePicker(boolean isFromDate, boolean isChequeFilter, boolean isTransactionFilter) {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                Date selectedDate = calendar.getTime();
                String formattedDate = dateFormat.format(selectedDate);
                
                if (isTransactionFilter) {
                    if (isFromDate) {
                        fromDateTransaction = selectedDate;
                        fromDateTransactionInput.setText(formattedDate);
                    } else {
                        toDateTransaction = selectedDate;
                        toDateTransactionInput.setText(formattedDate);
                    }
                } else if (isChequeFilter) {
                    if (isFromDate) {
                        fromDateCheques = selectedDate;
                        fromDateChequesInput.setText(formattedDate);
                    } else {
                        toDateCheques = selectedDate;
                        toDateChequesInput.setText(formattedDate);
                    }
                } else {
                    if (isFromDate) {
                        fromDate = selectedDate;
                        fromDateInput.setText(formattedDate);
                    } else {
                        toDate = selectedDate;
                        toDateInput.setText(formattedDate);
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void performChequeSearch() {
        String searchQuery = searchChequesInput.getText().toString().trim().toLowerCase();
        
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter search term", Toast.LENGTH_SHORT).show();
            return;
        }
        
        filteredChequeList = new ArrayList<>();
        for (Cheque cheque : chequeList) {
            String chequeNumber = cheque.getChequeNumber() != null ? cheque.getChequeNumber().toLowerCase() : "";
            String accountNumber = cheque.getAccountNumber() != null ? cheque.getAccountNumber().toLowerCase() : "";
            String customerName = cheque.getCustomerName() != null ? cheque.getCustomerName().toLowerCase() : "";
            String payeeName = cheque.getPayeeName() != null ? cheque.getPayeeName().toLowerCase() : "";
            
            if (chequeNumber.contains(searchQuery) ||
                accountNumber.contains(searchQuery) ||
                customerName.contains(searchQuery) ||
                payeeName.contains(searchQuery)) {
                filteredChequeList.add(cheque);
            }
        }
        
        if (filteredChequeList.isEmpty()) {
            Toast.makeText(this, "No results found for \"" + searchQuery + "\"", Toast.LENGTH_SHORT).show();
            emptyChequesText.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Found " + filteredChequeList.size() + " result(s)", Toast.LENGTH_SHORT).show();
            emptyChequesText.setVisibility(View.GONE);
        }
        
        updateChequeList(filteredChequeList);
    }
    
    private void performChequeFilter() {
        String selectedStatus = chequeStatusSpinner.getSelectedItem().toString();
        
        filteredChequeList = new ArrayList<>();
        
        for (Cheque cheque : chequeList) {
            boolean statusMatch = selectedStatus.equals("ALL") || 
                (cheque.getStatus() != null && cheque.getStatus().equals(selectedStatus));
            boolean dateMatch = true;
            
            // Check date range if dates are selected
            if (fromDateCheques != null || toDateCheques != null) {
                Date issueDate = cheque.getIssuedDate();
                if (issueDate != null) {
                    if (fromDateCheques != null && issueDate.before(fromDateCheques)) {
                        dateMatch = false;
                    }
                    if (toDateCheques != null && issueDate.after(toDateCheques)) {
                        dateMatch = false;
                    }
                } else {
                    dateMatch = false;
                }
            }
            
            if (statusMatch && dateMatch) {
                filteredChequeList.add(cheque);
            }
        }
        
        String filterSummary = "Filters: Status=" + selectedStatus;
        if (fromDateCheques != null) {
            filterSummary += ", From=" + dateFormat.format(fromDateCheques);
        }
        if (toDateCheques != null) {
            filterSummary += ", To=" + dateFormat.format(toDateCheques);
        }
        
        Toast.makeText(this, filterSummary + "\nFound " + filteredChequeList.size() + " result(s)", Toast.LENGTH_SHORT).show();
        
        if (filteredChequeList.isEmpty()) {
            emptyChequesText.setVisibility(View.VISIBLE);
        } else {
            emptyChequesText.setVisibility(View.GONE);
        }
        
        updateChequeList(filteredChequeList);
    }
    
    private void updateChequeList(List<Cheque> cheques) {
        chequeTableAdapter.updateList(cheques);
    }
    
    private void updateChequeBookList(List<ChequeBook> books) {
        chequeBookAdapter = new ChequeBookAdapter(this, books);
        chequeBooksRecyclerView.setAdapter(chequeBookAdapter);
    }

    private void exportReport() {
        // Placeholder for export functionality
        StringBuilder report = new StringBuilder();
        report.append("CHEQUE SYSTEM OVERSIGHT REPORT\n");
        report.append("================================\n\n");
        report.append("Cheque Book Statistics:\n");
        report.append("Total Cheque Books: ").append(totalChequeBooksText.getText()).append("\n");
        report.append("Pending Approval: ").append(pendingApprovalText.getText()).append("\n");
        report.append("Active Books: ").append(activeBooksText.getText()).append("\n\n");
        report.append("Cheque Statistics:\n");
        report.append("Total Cheques Issued: ").append(totalChequesIssuedText.getText()).append("\n");
        report.append("Cleared Cheques: ").append(clearedChequesText.getText()).append("\n");
        report.append("Bounced Cheques: ").append(bouncedChequesText.getText()).append("\n\n");
        report.append("Financial Statistics:\n");
        report.append("Total Amount Cleared: ").append(totalAmountClearedText.getText()).append("\n");
        report.append("Total Amount Bounced: ").append(totalAmountBouncedText.getText()).append("\n\n");
        report.append("Generated on: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
        
        // Create and show dialog with report
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Oversight Report")
            .setMessage(report.toString())
            .setPositiveButton("Share", (dialog, which) -> {
                android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, report.toString());
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Cheque System Oversight Report");
                startActivity(android.content.Intent.createChooser(shareIntent, "Share Report"));
            })
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTab.equals("overview")) {
            loadOverviewStatistics();
        }
    }
}
