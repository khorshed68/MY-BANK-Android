package com.khorshed.mybank.activities.staff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.R;
import com.khorshed.mybank.adapter.CustomerAccountAdapter;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.CustomerAccount;
import com.khorshed.mybank.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchCustomersActivity extends AppCompatActivity 
    implements CustomerAccountAdapter.OnCustomerClickListener {

    private EditText searchEditText;
    private Spinner statusSpinner;
    private Button searchButton, refreshButton, showBlockedButton, backToDashboardButton;
    private RecyclerView customersRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    
    private CustomerAccountAdapter adapter;
    private FirebaseFirestore db;
    
    private String currentSearchQuery = "";
    private String currentStatusFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_customers);
        
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupStatusSpinner();
        setupRecyclerView();
        setupListeners();
        
        loadCustomers();
    }
    
    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        statusSpinner = findViewById(R.id.statusSpinner);
        searchButton = findViewById(R.id.searchButton);
        refreshButton = findViewById(R.id.refreshButton);
        showBlockedButton = findViewById(R.id.showBlockedButton);
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }
    
    private void setupStatusSpinner() {
        String[] statuses = {"All", "ACTIVE", "BLOCKED", "FROZEN", "PENDING"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
    }
    
    private void setupRecyclerView() {
        adapter = new CustomerAccountAdapter(this);
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customersRecyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backToDashboardButton.setOnClickListener(v -> finish());
        
        searchButton.setOnClickListener(v -> {
            currentSearchQuery = searchEditText.getText().toString();
            applyFilters();
        });
        
        refreshButton.setOnClickListener(v -> {
            searchEditText.setText("");
            currentSearchQuery = "";
            statusSpinner.setSelection(0);
            currentStatusFilter = "All";
            loadCustomers();
        });
        
        showBlockedButton.setOnClickListener(v -> {
            statusSpinner.setSelection(1); // Select "BLOCKED"
            currentStatusFilter = "BLOCKED";
            applyFilters();
        });
        
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        // Real-time search as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadCustomers() {
        showLoading(true);
        
        // First, get all customer users
        db.collection("users")
            .whereEqualTo("role", "CUSTOMER")
            .get()
            .addOnSuccessListener(userSnapshot -> {
                List<CustomerAccount> customerAccounts = new ArrayList<>();
                
                if (userSnapshot.isEmpty()) {
                    showLoading(false);
                    showEmptyState(true);
                    return;
                }
                
                // For each customer user, get their account
                int totalUsers = userSnapshot.size();
                final int[] processedCount = {0};
                
                for (QueryDocumentSnapshot userDoc : userSnapshot) {
                    User user = userDoc.toObject(User.class);
                    String userId = userDoc.getId();
                    
                    // Get account for this user
                    db.collection("accounts")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(accountSnapshot -> {
                            processedCount[0]++;
                            
                            if (!accountSnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot accountDoc : accountSnapshot) {
                                    Account account = accountDoc.toObject(Account.class);
                                    
                                    // Create CustomerAccount object
                                    CustomerAccount customerAccount = new CustomerAccount();
                                    customerAccount.setAccountNumber(account.getAccountNumber() != null ? 
                                        account.getAccountNumber() : "N/A");
                                    customerAccount.setCustomerName(user.getName());
                                    customerAccount.setEmail(user.getEmail());
                                    customerAccount.setPhone(user.getPhone());
                                    customerAccount.setAccountType(account.getAccountType());
                                    customerAccount.setBalance(account.getBalance());
                                    customerAccount.setStatus(account.isFrozen() ? "FROZEN" : 
                                        (user.isActive() ? "ACTIVE" : "BLOCKED"));
                                    
                                    // Format created date
                                    if (account.getCreatedAt() != null) {
                                        SimpleDateFormat sdf = new SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm", Locale.getDefault());
                                        customerAccount.setCreatedDate(
                                            sdf.format(account.getCreatedAt()));
                                    } else {
                                        customerAccount.setCreatedDate("N/A");
                                    }
                                    
                                    customerAccount.setUserId(userId);
                                    customerAccount.setAccountId(accountDoc.getId());
                                    
                                    customerAccounts.add(customerAccount);
                                }
                            }
                            
                            // If all users processed, update adapter
                            if (processedCount[0] == totalUsers) {
                                showLoading(false);
                                if (customerAccounts.isEmpty()) {
                                    showEmptyState(true);
                                } else {
                                    showEmptyState(false);
                                    adapter.setCustomerAccounts(customerAccounts);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            processedCount[0]++;
                            if (processedCount[0] == totalUsers) {
                                showLoading(false);
                                if (customerAccounts.isEmpty()) {
                                    showEmptyState(true);
                                } else {
                                    showEmptyState(false);
                                    adapter.setCustomerAccounts(customerAccounts);
                                }
                            }
                        });
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading customers: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void applyFilters() {
        adapter.filter(currentSearchQuery, currentStatusFilter);
        showEmptyState(adapter.getItemCount() == 0);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        customersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        customersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCustomerClick(CustomerAccount account) {
        // Show customer details dialog
        new AlertDialog.Builder(this)
            .setTitle("Customer Details")
            .setMessage(
                "Name: " + account.getCustomerName() + "\n" +
                "Account: " + account.getAccountNumber() + "\n" +
                "Email: " + account.getEmail() + "\n" +
                "Phone: " + account.getPhone() + "\n" +
                "Type: " + account.getAccountType() + "\n" +
                "Balance: ৳" + String.format(Locale.US, "%.2f", account.getBalance()) + "\n" +
                "Status: " + account.getStatus() + "\n" +
                "Created: " + account.getCreatedDate()
            )
            .setPositiveButton("OK", null)
            .show();
    }
}
