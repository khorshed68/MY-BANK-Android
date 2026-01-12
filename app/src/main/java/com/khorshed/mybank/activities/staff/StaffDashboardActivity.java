package com.khorshed.mybank.activities.staff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
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
                
                // Load profile picture from Firestore (Base64 encoded)
                loadProfilePicture(user.getUserId());
                
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

    /**
     * Load and decode Base64 profile picture from Firestore
     */
    private void loadProfilePicture(String userId) {
        if (userId == null) return;
        
        // Load profile picture from Firestore (Base64 encoded)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Load Base64 encoded image from profileImageUrl field
                    String base64Image = documentSnapshot.getString("profileImageUrl");
                    
                    if (base64Image != null && !base64Image.isEmpty() && 
                        !base64Image.equals("default") &&
                        (base64Image.startsWith("data:image") || base64Image.length() > 100)) {
                        
                        try {
                            // Remove data URI prefix if present
                            String cleanBase64 = base64Image;
                            if (base64Image.startsWith("data:image")) {
                                cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
                            }
                            
                            // Decode Base64 string to byte array
                            byte[] imageBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                            
                            // Convert byte array to Bitmap
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            
                            if (bitmap != null) {
                                // Display profile picture with circular crop using Glide
                                Glide.with(this)
                                    .load(bitmap)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_person)
                                    .into(staffProfileImage);
                                android.util.Log.d("StaffDashboard", "✅ Base64 image loaded successfully");
                                return;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("StaffDashboard", "❌ Error decoding Base64 image", e);
                        }
                    }
                    
                    // No valid profile picture - use default
                    Glide.with(this)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(staffProfileImage);
                } else {
                    // Document doesn't exist - use default
                    Glide.with(this)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(staffProfileImage);
                }
            })
            .addOnFailureListener(e -> {
                // Loading failed - use default icon
                Glide.with(this)
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(staffProfileImage);
                android.util.Log.e("StaffDashboard", "Failed to load profile data", e);
            });
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
