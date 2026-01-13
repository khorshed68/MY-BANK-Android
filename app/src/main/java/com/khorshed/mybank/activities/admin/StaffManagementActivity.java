package com.khorshed.mybank.activities.admin;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;
import com.khorshed.mybank.adapters.StaffAdapter;
import com.khorshed.mybank.models.Staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffManagementActivity extends AppCompatActivity implements StaffAdapter.OnStaffClickListener {

    private MaterialButton backButton, createStaffButton;
    private MaterialButton activateButton, deactivateButton, resetPasswordButton, viewActivityButton;
    private EditText searchEditText;
    private Spinner filterSpinner;
    private RecyclerView staffRecyclerView;
    private TextView emptyStateText;
    
    private StaffAdapter staffAdapter;
    private List<Staff> staffList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup Spinner
        setupFilterSpinner();
        
        // Setup listeners
        setupListeners();
        
        // Load staff data
        loadStaffData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        createStaffButton = findViewById(R.id.createStaffButton);
        activateButton = findViewById(R.id.activateButton);
        deactivateButton = findViewById(R.id.deactivateButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        viewActivityButton = findViewById(R.id.viewActivityButton);
        searchEditText = findViewById(R.id.searchEditText);
        filterSpinner = findViewById(R.id.filterSpinner);
        staffRecyclerView = findViewById(R.id.staffRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        staffList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        staffAdapter = new StaffAdapter(staffList, this);
        staffRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffRecyclerView.setAdapter(staffAdapter);
    }

    private void setupFilterSpinner() {
        List<String> filterOptions = new ArrayList<>();
        filterOptions.add("All");
        filterOptions.add("Active");
        filterOptions.add("Inactive");
        filterOptions.add("Suspended");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Create Staff button
        createStaffButton.setOnClickListener(v -> {
            Intent intent = new Intent(StaffManagementActivity.this, CreateStaffActivity.class);
            startActivity(intent);
        });

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                staffAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter spinner
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                staffAdapter.filterByStatus(selectedStatus);
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Activate button
        activateButton.setOnClickListener(v -> {
            List<Staff> selectedStaff = staffAdapter.getSelectedStaff();
            if (selectedStaff.isEmpty()) {
                Toast.makeText(this, "Please select staff members to activate", Toast.LENGTH_SHORT).show();
                return;
            }
            updateStaffStatus(selectedStaff, "Active");
        });

        // Deactivate button
        deactivateButton.setOnClickListener(v -> {
            List<Staff> selectedStaff = staffAdapter.getSelectedStaff();
            if (selectedStaff.isEmpty()) {
                Toast.makeText(this, "Please select staff members to deactivate", Toast.LENGTH_SHORT).show();
                return;
            }
            updateStaffStatus(selectedStaff, "Inactive");
        });

        // Reset Password button
        resetPasswordButton.setOnClickListener(v -> {
            List<Staff> selectedStaff = staffAdapter.getSelectedStaff();
            if (selectedStaff.isEmpty()) {
                Toast.makeText(this, "Please select staff members to reset password", Toast.LENGTH_SHORT).show();
                return;
            }
            showResetPasswordConfirmation(selectedStaff);
        });

        // View Activity button
        viewActivityButton.setOnClickListener(v -> {
            List<Staff> selectedStaff = staffAdapter.getSelectedStaff();
            if (selectedStaff.isEmpty()) {
                Toast.makeText(this, "Please select a staff member to view activity", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStaff.size() > 1) {
                Toast.makeText(this, "Please select only one staff member", Toast.LENGTH_SHORT).show();
                return;
            }
            viewStaffActivity(selectedStaff.get(0));
        });
    }

    private void loadStaffData() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    staffList.clear();
                    int index = 1;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Check if this is a staff member
                        String role = doc.getString("role");
                        if (role != null && role.equalsIgnoreCase("STAFF")) {
                            Staff staff = new Staff();
                            staff.setId(String.valueOf(index++));
                            staff.setUsername(doc.getString("username") != null ? doc.getString("username") : doc.getString("email") != null ? doc.getString("email").split("@")[0] : "N/A");
                            staff.setFullName(doc.getString("name") != null ? doc.getString("name") : doc.getString("fullName") != null ? doc.getString("fullName") : "N/A");
                            staff.setEmail(doc.getString("email") != null ? doc.getString("email") : "N/A");
                            staff.setRole(doc.getString("role") != null ? doc.getString("role") : "STAFF");
                            
                            // Get status - check multiple possible field names
                            String status = "Active";
                            if (doc.contains("status")) {
                                status = doc.getString("status");
                            } else if (doc.contains("isActive")) {
                                Boolean isActive = doc.getBoolean("isActive");
                                status = (isActive != null && isActive) ? "Active" : "Inactive";
                            }
                            staff.setStatus(status);
                            
                            staffList.add(staff);
                        }
                    }
                    staffAdapter.updateStaffList(staffList);
                    updateEmptyState();
                    Toast.makeText(this, "Loaded " + staffList.size() + " staff members", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading staff: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateEmptyState();
                });
    }

    private void updateStaffStatus(List<Staff> staffMembers, String newStatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Status Change");
        builder.setMessage("Are you sure you want to " + newStatus.toLowerCase() + " " + 
                         staffMembers.size() + " staff member(s)?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            for (Staff staff : staffMembers) {
                // Update in Firestore
                db.collection("users")
                        .whereEqualTo("username", staff.getUsername())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("status", newStatus);
                                // Also update isActive field if it exists
                                updates.put("isActive", newStatus.equalsIgnoreCase("Active"));
                                doc.getReference().update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            // Log the action
                                            logAdminAction("Staff Status Update", 
                                                "Changed status of " + staff.getUsername() + " to " + newStatus);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update " + staff.getUsername(), 
                                                Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error finding user: " + staff.getUsername(), 
                                Toast.LENGTH_SHORT).show();
                        });
            }
            Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show();
            // Reload data after a short delay
            new android.os.Handler().postDelayed(this::loadStaffData, 1000);
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void showResetPasswordConfirmation(List<Staff> staffMembers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Reset password for " + staffMembers.size() + " staff member(s)? " +
                         "A temporary password will be sent to their email.");
        builder.setPositiveButton("Reset", (dialog, which) -> {
            for (Staff staff : staffMembers) {
                resetStaffPassword(staff);
            }
            Toast.makeText(this, "Password reset emails sent", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void resetStaffPassword(Staff staff) {
        // Send password reset email via Firebase Auth
        mAuth.sendPasswordResetEmail(staff.getEmail())
                .addOnSuccessListener(aVoid -> {
                    // Log the action
                    logAdminAction("Password Reset", "Reset password for " + staff.getUsername());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send reset email to " + staff.getEmail(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void viewStaffActivity(Staff staff) {
        Intent intent = new Intent(this, StaffActivityLogActivity.class);
        intent.putExtra("staffUsername", staff.getUsername());
        intent.putExtra("staffName", staff.getFullName());
        startActivity(intent);
    }

    private void logAdminAction(String action, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("adminId", mAuth.getCurrentUser().getUid());
        log.put("action", action);
        log.put("details", details);
        log.put("timestamp", System.currentTimeMillis());

        db.collection("adminLogs").add(log);
    }

    private void updateEmptyState() {
        if (staffAdapter.getItemCount() == 0) {
            emptyStateText.setVisibility(View.VISIBLE);
            staffRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            staffRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStaffClick(Staff staff, int position) {
        // Handle staff item click - could show details dialog
        showStaffDetails(staff);
    }

    @Override
    public void onStaffSelectionChanged(List<Staff> selectedStaff) {
        // Update button states based on selection
        boolean hasSelection = !selectedStaff.isEmpty();
        activateButton.setEnabled(hasSelection);
        deactivateButton.setEnabled(hasSelection);
        resetPasswordButton.setEnabled(hasSelection);
        viewActivityButton.setEnabled(selectedStaff.size() == 1);
    }

    private void showStaffDetails(Staff staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Staff Details");
        
        String details = "ID: " + staff.getId() + "\n" +
                        "Username: " + staff.getUsername() + "\n" +
                        "Full Name: " + staff.getFullName() + "\n" +
                        "Email: " + staff.getEmail() + "\n" +
                        "Role: " + staff.getRole() + "\n" +
                        "Status: " + staff.getStatus();
        
        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadStaffData();
    }
}
