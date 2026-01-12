package com.khorshed.mybank.activities.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.AuditLogsActivity;
import com.khorshed.mybank.BankConfigurationActivity;
import com.khorshed.mybank.CustomerOversightActivity;
import com.khorshed.mybank.R;
import com.khorshed.mybank.ReportsAnalyticsActivity;
import com.khorshed.mybank.SystemSettingsActivity;
import com.khorshed.mybank.TransactionMonitoringActivity;
import com.khorshed.mybank.activities.LoginActivity;
import com.khorshed.mybank.activities.admin.AdminManagementActivity;
import com.khorshed.mybank.activities.admin.ChequeOversightActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView adminNameText;
    private ImageView adminProfileImage;
    private Button refreshButton, logoutButton;
    
    // Statistics TextViews - First Row
    private TextView totalCustomersText, totalAccountsText, totalStaffText, totalAdminsText;
    
    // Statistics TextViews - Second Row
    private TextView todayTransactionsText, transactionAmountText, activeAccountsText, pendingApprovalsText;
    
    // Statistics TextViews - Third Row
    private TextView suspiciousActivitiesText;
    
    // Quick Action Buttons
    private Button staffManagementButton, customerOversightButton, transactionMonitoringButton;
    private Button bankConfigurationButton, reportsAnalyticsButton, auditLogsButton;
    private Button systemSettingsButton, adminManagementButton, chequeOversightButton;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        initializeViews();
        loadAdminInfo();
        loadStatistics();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header components
        adminNameText = findViewById(R.id.adminNameText);
        adminProfileImage = findViewById(R.id.adminProfileImage);
        refreshButton = findViewById(R.id.refreshButton);
        logoutButton = findViewById(R.id.logoutButton);
        
        // Statistics - First Row
        totalCustomersText = findViewById(R.id.totalCustomersText);
        totalAccountsText = findViewById(R.id.totalAccountsText);
        totalStaffText = findViewById(R.id.totalStaffText);
        totalAdminsText = findViewById(R.id.totalAdminsText);
        
        // Statistics - Second Row
        todayTransactionsText = findViewById(R.id.todayTransactionsText);
        transactionAmountText = findViewById(R.id.transactionAmountText);
        activeAccountsText = findViewById(R.id.activeAccountsText);
        pendingApprovalsText = findViewById(R.id.pendingApprovalsText);
        
        // Statistics - Third Row
        suspiciousActivitiesText = findViewById(R.id.suspiciousActivitiesText);
        
        // Quick Action Buttons
        staffManagementButton = findViewById(R.id.staffManagementButton);
        customerOversightButton = findViewById(R.id.customerOversightButton);
        transactionMonitoringButton = findViewById(R.id.transactionMonitoringButton);
        bankConfigurationButton = findViewById(R.id.bankConfigurationButton);
        reportsAnalyticsButton = findViewById(R.id.reportsAnalyticsButton);
        auditLogsButton = findViewById(R.id.auditLogsButton);
        systemSettingsButton = findViewById(R.id.systemSettingsButton);
        adminManagementButton = findViewById(R.id.adminManagementButton);
        chequeOversightButton = findViewById(R.id.chequeOversightButton);
    }

    private void loadAdminInfo() {
        // Get current admin user ID
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            
            // Load admin info from Firestore
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) {
                            adminNameText.setText(name);
                        }
                        
                        // Load profile picture (Base64 encoded)
                        loadProfilePicture(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminDashboard", "Failed to load admin info", e);
                    adminNameText.setText("Admin");
                });
        }
    }
    
    /**
     * Load and decode Base64 profile picture from Firestore
     */
    private void loadProfilePicture(String userId) {
        if (userId == null) return;
        
        // Load profile picture from Firestore (Base64 encoded)
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
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(adminProfileImage);
                                android.util.Log.d("AdminDashboard", "✅ Base64 image loaded successfully");
                                return;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AdminDashboard", "❌ Error decoding Base64 image", e);
                        }
                    }
                    
                    // No valid profile picture - use default
                    Glide.with(this)
                        .load(R.mipmap.ic_launcher)
                        .circleCrop()
                        .into(adminProfileImage);
                } else {
                    // Document doesn't exist - use default
                    Glide.with(this)
                        .load(R.mipmap.ic_launcher)
                        .circleCrop()
                        .into(adminProfileImage);
                }
            })
            .addOnFailureListener(e -> {
                // Loading failed - use default icon
                Glide.with(this)
                    .load(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(adminProfileImage);
                android.util.Log.e("AdminDashboard", "Failed to load profile data", e);
            });
    }

    private void loadStatistics() {
        loadCustomersCount();
        loadAccountsCount();
        loadStaffCount();
        loadAdminsCount();
        loadTodayTransactions();
        loadActiveAccounts();
        loadPendingApprovals();
        loadSuspiciousActivities();
    }

    private void loadCustomersCount() {
        db.collection("users")
            .whereEqualTo("role", "CUSTOMER")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                totalCustomersText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                totalCustomersText.setText("0");
            });
    }

    private void loadAccountsCount() {
        db.collection("accounts")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                totalAccountsText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                totalAccountsText.setText("0");
            });
    }

    private void loadStaffCount() {
        db.collection("users")
            .whereEqualTo("role", "STAFF")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                totalStaffText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                totalStaffText.setText("0");
            });
    }

    private void loadAdminsCount() {
        db.collection("users")
            .whereEqualTo("role", "ADMIN")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                totalAdminsText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                totalAdminsText.setText("0");
            });
    }

    private void loadTodayTransactions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();
        
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = queryDocumentSnapshots.size();
                double totalAmount = 0.0;
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Double amount = document.getDouble("amount");
                    if (amount != null) {
                        totalAmount += amount;
                    }
                }
                
                todayTransactionsText.setText(String.valueOf(count));
                transactionAmountText.setText("৳ " + String.format(Locale.getDefault(), "%.2f", totalAmount));
            })
            .addOnFailureListener(e -> {
                todayTransactionsText.setText("0");
                transactionAmountText.setText("৳ 0.00");
            });
    }

    private void loadActiveAccounts() {
        db.collection("accounts")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                activeAccountsText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                activeAccountsText.setText("0");
            });
    }

    private void loadPendingApprovals() {
        db.collection("account_applications")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                pendingApprovalsText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                pendingApprovalsText.setText("0");
            });
    }

    private void loadSuspiciousActivities() {
        db.collection("activity_logs")
            .whereEqualTo("suspicious", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                suspiciousActivitiesText.setText(String.valueOf(queryDocumentSnapshots.size()));
            })
            .addOnFailureListener(e -> {
                suspiciousActivitiesText.setText("0");
            });
    }

    private void setupClickListeners() {
        refreshButton.setOnClickListener(v -> {
            loadStatistics();
            Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
        });
        
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            SharedPreferences prefs = getSharedPreferences("MyBankPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        staffManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffManagementActivity.class);
            startActivity(intent);
        });
        
        customerOversightButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerOversightActivity.class);
            startActivity(intent);
        });
        
        transactionMonitoringButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionMonitoringActivity.class);
            startActivity(intent);
        });
        
        bankConfigurationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, BankConfigurationActivity.class);
            startActivity(intent);
        });
        
        reportsAnalyticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportsAnalyticsActivity.class);
            startActivity(intent);
        });
        
        auditLogsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AuditLogsActivity.class);
            startActivity(intent);
        });
        
        systemSettingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SystemSettingsActivity.class);
            startActivity(intent);
        });
        
        adminManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManagementActivity.class);
            startActivity(intent);
        });
        
        chequeOversightButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChequeOversightActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }
}
