package com.khorshed.mybank.activities.admin;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.khorshed.mybank.R;
import com.khorshed.mybank.activities.LoginActivity;
import com.khorshed.mybank.activities.staff.ChequeManagementActivity;

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
        SharedPreferences prefs = getSharedPreferences("MyBankPrefs", MODE_PRIVATE);
        String adminName = prefs.getString("adminName", "Admin");
        String profileImageUrl = prefs.getString("profileImageUrl", "");
        
        adminNameText.setText("Welcome, " + adminName);
        
        if (!profileImageUrl.isEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .into(adminProfileImage);
        }
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
            Toast.makeText(this, "Staff Management - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        customerOversightButton.setOnClickListener(v -> {
            Toast.makeText(this, "Customer Oversight - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        transactionMonitoringButton.setOnClickListener(v -> {
            Toast.makeText(this, "Transaction Monitoring - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        bankConfigurationButton.setOnClickListener(v -> {
            Toast.makeText(this, "Bank Configuration - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        reportsAnalyticsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Reports & Analytics - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        auditLogsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Audit Logs - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        systemSettingsButton.setOnClickListener(v -> {
            Toast.makeText(this, "System Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        adminManagementButton.setOnClickListener(v -> {
            Toast.makeText(this, "Admin Management - Coming Soon", Toast.LENGTH_SHORT).show();
        });
        
        chequeOversightButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChequeManagementActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }
}
