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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.adapters.AuditLogAdapter;
import com.khorshed.mybank.models.AuditLog;

import java.util.ArrayList;
import java.util.List;

public class AuditLogsActivity extends AppCompatActivity {

    private MaterialButton backButton, refreshButton;
    private EditText searchEditText;
    private Spinner filterSpinner;
    private RecyclerView auditLogsRecyclerView;
    private TextView emptyStateText;

    private FirebaseFirestore db;
    private AuditLogAdapter adapter;
    private List<AuditLog> auditLogsList;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_logs);

        db = FirebaseFirestore.getInstance();
        auditLogsList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupFilterSpinner();
        setupSearchListener();
        setupClickListeners();
        loadAuditLogs();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        searchEditText = findViewById(R.id.searchEditText);
        filterSpinner = findViewById(R.id.filterSpinner);
        auditLogsRecyclerView = findViewById(R.id.auditLogsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        adapter = new AuditLogAdapter(auditLogsList);
        auditLogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        auditLogsRecyclerView.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        String[] filters = {"All", "ADMIN", "STAFF", "CUSTOMER", "LOGIN", "LOGOUT", 
                           "CREATE", "UPDATE", "DELETE", "SUCCESS", "FAILED"};
        
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        refreshButton.setOnClickListener(v -> {
            loadAuditLogs();
            Toast.makeText(this, "Refreshing audit logs...", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyFilters() {
        String searchQuery = searchEditText.getText().toString();
        adapter.filter(searchQuery, currentFilter);
        updateEmptyState();
    }

    private void loadAuditLogs() {
        db.collection("auditLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    auditLogsList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AuditLog log = document.toObject(AuditLog.class);
                        log.setId(document.getId());
                        auditLogsList.add(log);
                    }

                    adapter.updateData(auditLogsList);
                    applyFilters();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading audit logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            auditLogsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            auditLogsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    // Utility method to create audit log entries (can be called from other activities)
    public static void createAuditLog(String userType, String username, String action, 
                                     String module, String details, String status, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        AuditLog log = new AuditLog();
        log.setTimestamp(com.google.firebase.Timestamp.now());
        log.setUserType(userType);
        log.setUsername(username);
        log.setAction(action);
        log.setModule(module);
        log.setDetails(details);
        log.setStatus(status);
        log.setUserId(userId);

        db.collection("auditLogs")
                .add(log)
                .addOnSuccessListener(documentReference -> {
                    // Audit log created successfully
                })
                .addOnFailureListener(e -> {
                    // Failed to create audit log
                });
    }
}
