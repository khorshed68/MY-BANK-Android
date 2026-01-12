package com.khorshed.mybank;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.khorshed.mybank.adapters.BankConfigurationAdapter;
import com.khorshed.mybank.models.BankConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankConfigurationActivity extends AppCompatActivity {

    private MaterialButton backButton, updateButton;
    private Spinner categorySpinner;
    private RecyclerView configRecyclerView;
    private TextView emptyStateText;

    private FirebaseFirestore db;
    private BankConfigurationAdapter configAdapter;
    private List<BankConfiguration> configList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_configuration);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Setup category spinner
        setupCategorySpinner();

        // Load configurations or create default
        loadConfigurations();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.updateButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        configRecyclerView = findViewById(R.id.configRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        configList = new ArrayList<>();
        configAdapter = new BankConfigurationAdapter(configList);
        configRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        configRecyclerView.setAdapter(configAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> saveConfigurations());
    }

    private void setupCategorySpinner() {
        String[] categories = {"All", "BALANCE", "FEE", "INTEREST_RATE", "PENALTY", "SECURITY", "TRANSACTION_LIMIT"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                configAdapter.filterByCategory(selectedCategory);
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadConfigurations() {
        db.collection("bankConfiguration")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Create default configurations
                        createDefaultConfigurations();
                    } else {
                        configList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String id = doc.getId();
                            String key = doc.getString("key");
                            String value = doc.getString("value");
                            String category = doc.getString("category");
                            String description = doc.getString("description");

                            BankConfiguration config = new BankConfiguration(id, key, value, category, description);
                            configList.add(config);
                        }

                        configAdapter.updateData(configList);
                        updateEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading configurations: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Create default configurations on error
                    createDefaultConfigurations();
                });
    }

    private void createDefaultConfigurations() {
        configList.clear();

        // Add default configurations
        configList.add(new BankConfiguration("", "MIN_BALANCE_CURRENT", "5000", "BALANCE", "Minimum balance for current account"));
        configList.add(new BankConfiguration("", "MIN_BALANCE_SAVINGS", "1000", "BALANCE", "Minimum balance for savings account"));
        configList.add(new BankConfiguration("", "TRANSFER_FEE", "5", "FEE", "Fee for inter-account transfer"));
        configList.add(new BankConfiguration("", "WITHDRAWAL_FEE", "10", "FEE", "Fee for ATM withdrawal"));
        configList.add(new BankConfiguration("", "CURRENT_INTEREST_RATE", "0.5", "INTEREST_RATE", "Annual interest rate for current accounts (%)"));
        configList.add(new BankConfiguration("", "LOAN_INTEREST_RATE", "8.5", "INTEREST_RATE", "Annual interest rate for loans (%)"));
        configList.add(new BankConfiguration("", "SAVINGS_INTEREST_RATE", "3.5", "INTEREST_RATE", "Annual interest rate for savings accounts (%)"));
        configList.add(new BankConfiguration("", "LOW_BALANCE_PENALTY", "100", "PENALTY", "Penalty for maintaining balance below minimum"));
        configList.add(new BankConfiguration("", "MAX_LOGIN_ATTEMPTS", "3", "SECURITY", "Maximum failed login attempts before lock"));
        configList.add(new BankConfiguration("", "SESSION_TIMEOUT_MINUTES", "15", "SECURITY", "Session timeout in minutes"));
        configList.add(new BankConfiguration("", "DAILY_TRANSFER_LIMIT", "100000", "TRANSACTION_LIMIT", "Daily transfer limit"));
        configList.add(new BankConfiguration("", "DAILY_WITHDRAWAL_LIMIT", "50000", "TRANSACTION_LIMIT", "Daily withdrawal limit"));
        configList.add(new BankConfiguration("", "MONTHLY_TRANSACTION_LIMIT", "500000", "TRANSACTION_LIMIT", "Monthly transaction limit"));

        configAdapter.updateData(configList);
        updateEmptyState();

        // Save defaults to Firestore
        saveConfigurations();
    }

    private void saveConfigurations() {
        List<BankConfiguration> allConfigs = configAdapter.getAllConfigurations();

        if (allConfigs.isEmpty()) {
            Toast.makeText(this, "No configurations to save", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Update Configuration")
                .setMessage("Are you sure you want to update the bank configuration?")
                .setPositiveButton("Update", (dialog, which) -> {
                    int successCount = 0;
                    int totalCount = allConfigs.size();

                    for (BankConfiguration config : allConfigs) {
                        Map<String, Object> configData = new HashMap<>();
                        configData.put("key", config.getKey());
                        configData.put("value", config.getValue());
                        configData.put("category", config.getCategory());
                        configData.put("description", config.getDescription());

                        if (config.getId() == null || config.getId().isEmpty()) {
                            // Create new document
                            db.collection("bankConfiguration")
                                    .add(configData)
                                    .addOnSuccessListener(documentReference -> {
                                        config.setId(documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error creating " + config.getKey(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Update existing document
                            db.collection("bankConfiguration").document(config.getId())
                                    .set(configData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Success
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error updating " + config.getKey(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    Toast.makeText(this, "Configuration updated successfully", Toast.LENGTH_SHORT).show();
                    loadConfigurations();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (configAdapter.getItemCount() == 0) {
            configRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            configRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }
}
