package com.khorshed.mybank.activities.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.khorshed.mybank.AccountApprovalActivity;
import com.khorshed.mybank.R;
import com.khorshed.mybank.activities.LoginActivity;
import com.khorshed.mybank.viewmodel.AuthViewModel;
import com.khorshed.mybank.viewmodel.StaffViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StaffDashboardActivity extends AppCompatActivity {

    private ImageView staffProfileImage;
    private TextView staffNameText, staffEmailText, staffRoleText, lastLoginText, welcomeText;
    private MaterialButton pendingRequestsButton, createAccountButton, viewCustomersButton;
    private MaterialButton manageChequesButton, generateReportsButton, activityLogsButton;
    private MaterialButton refreshButton, logoutButton;
    
    private StaffViewModel staffViewModel;
    private AuthViewModel authViewModel;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        initViews();
        staffViewModel = new ViewModelProvider(this).get(StaffViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        staffProfileImage = findViewById(R.id.staffProfileImage);
        staffNameText = findViewById(R.id.staffNameText);
        staffEmailText = findViewById(R.id.staffEmailText);
        staffRoleText = findViewById(R.id.staffRoleText);
        lastLoginText = findViewById(R.id.lastLoginText);
        welcomeText = findViewById(R.id.welcomeText);
        
        // Account Management
        pendingRequestsButton = findViewById(R.id.pendingRequestsButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        viewCustomersButton = findViewById(R.id.viewCustomersButton);
        
        // Cheque Management
        manageChequesButton = findViewById(R.id.manageChequesButton);
        
        // Reports & Analytics
        generateReportsButton = findViewById(R.id.generateReportsButton);
        activityLogsButton = findViewById(R.id.activityLogsButton);
        
        // Bottom Actions
        refreshButton = findViewById(R.id.refreshButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadUserData() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUserRole = user.getRole();
                
                // Update UI with user information
                staffNameText.setText(user.getName());
                staffEmailText.setText(user.getEmail());
                staffRoleText.setText(getRoleDisplayName(currentUserRole));
                welcomeText.setText("Welcome, " + user.getName() + "!");
                
                // Set last login time
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault());
                lastLoginText.setText("Last Login: " + sdf.format(new Date()));
                
                // Load profile image if available
                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                    Glide.with(this)
                        .load(user.getProfileImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(staffProfileImage);
                }
                
                // Configure permissions based on role
                configurePermissions(currentUserRole);
            }
        });
    }

    private String getRoleDisplayName(String role) {
        if (role == null) return "Staff";
        
        switch (role.toUpperCase()) {
            case "MANAGER":
                return "Manager";
            case "TELLER":
                return "Teller";
            case "OFFICER":
                return "Officer";
            case "STAFF":
                return "Staff";
            default:
                return role;
        }
    }

    private void configurePermissions(String role) {
        if (role == null) return;
        
        switch (role.toUpperCase()) {
            case "MANAGER":
                // Managers have full access
                enableAllFeatures();
                break;
                
            case "OFFICER":
                // Officers can do most things except some admin functions
                enableAllFeatures();
                // Could restrict certain features if needed
                break;
                
            case "TELLER":
                // Tellers have limited access
                pendingRequestsButton.setEnabled(true);
                createAccountButton.setEnabled(false); // Tellers cannot create accounts
                viewCustomersButton.setEnabled(true);
                manageChequesButton.setEnabled(true);
                generateReportsButton.setEnabled(false); // Tellers cannot generate reports
                activityLogsButton.setEnabled(true);
                
                // Visual feedback for disabled buttons
                createAccountButton.setAlpha(0.5f);
                generateReportsButton.setAlpha(0.5f);
                break;
                
            default:
                // Default: limited access
                enableAllFeatures();
                break;
        }
    }

    private void enableAllFeatures() {
        pendingRequestsButton.setEnabled(true);
        createAccountButton.setEnabled(true);
        viewCustomersButton.setEnabled(true);
        manageChequesButton.setEnabled(true);
        generateReportsButton.setEnabled(true);
        activityLogsButton.setEnabled(true);
        
        // Reset alpha
        pendingRequestsButton.setAlpha(1.0f);
        createAccountButton.setAlpha(1.0f);
        viewCustomersButton.setAlpha(1.0f);
        manageChequesButton.setAlpha(1.0f);
        generateReportsButton.setAlpha(1.0f);
        activityLogsButton.setAlpha(1.0f);
    }

    private void setupClickListeners() {
        // Account Management
        pendingRequestsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountApprovalActivity.class));
        });
        
        createAccountButton.setOnClickListener(v -> {
            if (!createAccountButton.isEnabled()) {
                Toast.makeText(this, "You don't have permission to create accounts", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, CreateAccountActivity.class));
        });
        
        viewCustomersButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchCustomersActivity.class));
        });
        
        // Cheque Management
        manageChequesButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ChequeManagementActivity.class));
        });
        
        // Reports & Analytics
        generateReportsButton.setOnClickListener(v -> {
            if (!generateReportsButton.isEnabled()) {
                Toast.makeText(this, "You don't have permission to generate reports", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, ReportsActivity.class));
        });
        
        activityLogsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffActivityLogsActivity.class));
        });
        
        // Bottom Actions
        refreshButton.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            loadUserData();
        });
        
        logoutButton.setOnClickListener(v -> {
            authViewModel.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadUserData();
    }
}
