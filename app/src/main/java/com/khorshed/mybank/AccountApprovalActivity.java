package com.khorshed.mybank;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.khorshed.mybank.adapter.AccountApplicationAdapter;
import com.khorshed.mybank.models.AccountApplication;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AccountApprovalActivity extends AppCompatActivity 
        implements AccountApplicationAdapter.OnApplicationActionListener {

    private RecyclerView recyclerView;
    private AccountApplicationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvPendingCount;
    private LinearLayout emptyStateLayout;
    private Spinner spinnerFilter;
    private ImageView btnBack;
    private Button btnRefresh;
    
    private FirebaseFirestore db;
    private String currentFilter = "All";
    private List<AccountApplication> allApplications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_approval);
        
        initializeViews();
        setupFirestore();
        setupRecyclerView();
        setupFilterSpinner();
        setupClickListeners();
        
        loadApplications();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.applicationsRecyclerView);
        progressBar = findViewById(R.id.loadingProgress);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        spinnerFilter = findViewById(R.id.filterSpinner);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new AccountApplicationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        String[] filters = {"All", "PENDING", "APPROVED", "REJECTED"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, filters);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);
        
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                filterApplications();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadApplications());
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        
        db.collection("account_applications")
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allApplications.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AccountApplication application = document.toObject(AccountApplication.class);
                        if (application != null) {
                            allApplications.add(application);
                        }
                    }
                    
                    updatePendingCount();
                    filterApplications();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading applications: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePendingCount() {
        int pendingCount = 0;
        for (AccountApplication app : allApplications) {
            if ("PENDING".equals(app.getStatus())) {
                pendingCount++;
            }
        }
        tvPendingCount.setText(String.valueOf(pendingCount));
    }

    private void filterApplications() {
        List<AccountApplication> filtered = new ArrayList<>();
        
        if ("All".equals(currentFilter)) {
            filtered.addAll(allApplications);
        } else {
            for (AccountApplication app : allApplications) {
                if (currentFilter.equals(app.getStatus())) {
                    filtered.add(app);
                }
            }
        }
        
        adapter.setApplications(filtered);
        
        if (filtered.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewDetails(AccountApplication application) {
        // Create and show details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_application_details, null);
        
        TextView tvName = dialogView.findViewById(R.id.tvDetailName);
        TextView tvEmail = dialogView.findViewById(R.id.tvDetailEmail);
        TextView tvPhone = dialogView.findViewById(R.id.tvDetailPhone);
        TextView tvAddress = dialogView.findViewById(R.id.tvDetailAddress);
        TextView tvIdentityType = dialogView.findViewById(R.id.tvDetailIdentityType);
        TextView tvIdentityNumber = dialogView.findViewById(R.id.tvDetailIdentityNumber);
        TextView tvAccountType = dialogView.findViewById(R.id.tvDetailAccountType);
        TextView tvInitialDeposit = dialogView.findViewById(R.id.tvDetailInitialDeposit);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailStatus);
        TextView tvReviewedBy = dialogView.findViewById(R.id.tvDetailReviewedBy);
        TextView tvRejectionReason = dialogView.findViewById(R.id.tvDetailRejectionReason);
        
        tvName.setText(application.getName());
        tvEmail.setText(application.getEmail());
        tvPhone.setText(application.getPhone());
        tvAddress.setText(application.getAddress());
        tvIdentityType.setText(application.getIdentityType());
        tvIdentityNumber.setText(application.getIdentityNumber());
        tvAccountType.setText(application.getAccountType());
        tvInitialDeposit.setText("৳ " + String.format("%.2f", application.getInitialDeposit()));
        tvStatus.setText(application.getStatus());
        
        if (application.getReviewedBy() != null && !application.getReviewedBy().isEmpty()) {
            tvReviewedBy.setText("Reviewed by: " + application.getReviewedBy());
            tvReviewedBy.setVisibility(View.VISIBLE);
        } else {
            tvReviewedBy.setVisibility(View.GONE);
        }
        
        if (application.getRejectionReason() != null && !application.getRejectionReason().isEmpty()) {
            tvRejectionReason.setText("Reason: " + application.getRejectionReason());
            tvRejectionReason.setVisibility(View.VISIBLE);
        } else {
            tvRejectionReason.setVisibility(View.GONE);
        }
        
        builder.setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onApprove(AccountApplication application) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Application")
                .setMessage("Are you sure you want to approve this account application for " + 
                        application.getName() + "?")
                .setPositiveButton("Approve", (dialog, which) -> approveApplication(application))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onReject(AccountApplication application) {
        // Show rejection reason dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rejection_reason, null);
        EditText etReason = dialogView.findViewById(R.id.etRejectionReason);
        
        builder.setView(dialogView)
                .setTitle("Reject Application")
                .setMessage("Please provide a reason for rejection:")
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Please provide a rejection reason",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        rejectApplication(application, reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveApplication(AccountApplication application) {
        progressBar.setVisibility(View.VISIBLE);
        
        // First, create Firebase Auth user with the stored password
        String email = application.getEmail();
        String password = application.getPassword();
        
        if (password == null || password.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error: Password not found in application",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                
                // Send email verification
                authResult.getUser().sendEmailVerification();
                
                // Create user profile in Firestore
                createUserProfile(userId, application, () -> {
                    // After user created, update application status
                    updateApplicationStatus(application, userId);
                });
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error creating user account: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createUserProfile(String userId, AccountApplication application, Runnable onSuccess) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", application.getName());
        userData.put("email", application.getEmail());
        userData.put("phone", application.getPhone());
        userData.put("role", "CUSTOMER");
        userData.put("active", true);
        userData.put("createdAt", new Date());
        
        // Add profile image URL if uploaded
        if (application.getProfileImageUrl() != null && !application.getProfileImageUrl().isEmpty()) {
            userData.put("profileImageUrl", application.getProfileImageUrl());
        }
        
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                if (onSuccess != null) {
                    onSuccess.run();
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Profile creation failed: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateApplicationStatus(AccountApplication application, String userId) {
        // Update application status and userId
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "APPROVED");
        updates.put("userId", userId); // Update with actual Firebase Auth userId
        updates.put("reviewedBy", getCurrentStaffName());
        updates.put("reviewedAt", new Date());
        
        db.collection("account_applications")
                .document(application.getApplicationId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update the application object with new userId
                    application.setUserId(userId);
                    // Create the actual bank account
                    createBankAccount(application);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error approving application: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createBankAccount(AccountApplication application) {
        // Generate account number
        String accountNumber = generateAccountNumber();
        
        // Create account data
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("accountNumber", accountNumber);
        accountData.put("userId", application.getUserId());
        accountData.put("accountType", application.getAccountType());
        accountData.put("balance", application.getInitialDeposit());
        accountData.put("status", "ACTIVE");
        accountData.put("createdAt", new Date());
        accountData.put("createdBy", getCurrentStaffName());
        
        db.collection("accounts")
                .document(accountNumber)
                .set(accountData)
                .addOnSuccessListener(aVoid -> {
                    // Send approval email
                    sendApprovalEmail(application, accountNumber);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error creating account: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void sendApprovalEmail(AccountApplication application, String accountNumber) {
        // Prepare email content
        String subject = "Account Approved - " + application.getName();
        String body = buildApprovalEmailBody(application, accountNumber);
        
        // Print email to console for testing
        Log.d("EMAIL_NOTIFICATION", "========================================");
        Log.d("EMAIL_NOTIFICATION", "EMAIL NOTIFICATION - ACCOUNT APPROVED");
        Log.d("EMAIL_NOTIFICATION", "========================================");
        Log.d("EMAIL_NOTIFICATION", "TO: " + application.getEmail());
        Log.d("EMAIL_NOTIFICATION", "SUBJECT: " + subject);
        Log.d("EMAIL_NOTIFICATION", "----------------------------------------");
        Log.d("EMAIL_NOTIFICATION", "BODY:");
        Log.d("EMAIL_NOTIFICATION", body);
        Log.d("EMAIL_NOTIFICATION", "========================================");
        
        // Also store in Firestore for backend processing (optional)
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("to", application.getEmail());
        emailData.put("subject", subject);
        emailData.put("body", body);
        emailData.put("sentAt", new Date());
        
        db.collection("mail")
                .add(emailData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, 
                            "Application approved! Account created and email printed to console.",
                            Toast.LENGTH_LONG).show();
                    loadApplications();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, 
                            "Account created and email printed to console!",
                            Toast.LENGTH_LONG).show();
                    loadApplications();
                });
    }
    
    private String buildApprovalEmailBody(AccountApplication application, String accountNumber) {
        return "Dear " + application.getName() + ",\n\n" +
                "Congratulations! Your account application has been approved.\n\n" +
                "Account Details:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Account Number: " + accountNumber + "\n" +
                "Account Type: " + application.getAccountType() + "\n" +
                "Account Holder: " + application.getName() + "\n" +
                "Phone Number: " + application.getPhone() + "\n" +
                "Initial Deposit: ৳ " + String.format("%.2f", application.getInitialDeposit()) + "\n" +
                "Status: ACTIVE\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Login Information:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "You can now login to your account using:\n" +
                "• Phone Number: " + application.getPhone() + "\n" +
                "• Password: (The password you set during registration)\n\n" +
                "Please keep your password secure and do not share it with anyone.\n" +
                "You can change your password after logging in.\n\n" +
                "Thank you for choosing our bank!\n\n" +
                "Best Regards,\n" +
                "MY BANK Team\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "This is an automated email. Please do not reply to this email.\n" +
                "For any queries, please contact our customer support.";
    }
    
    private String generateUniquePassword() {
        // Generate password pattern: "Bank" + 6 random characters (letters and digits)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder("Bank");
        
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    private void rejectApplication(AccountApplication application, String reason) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "REJECTED");
        updates.put("reviewedBy", getCurrentStaffName());
        updates.put("reviewedAt", new Date());
        updates.put("rejectionReason", reason);
        
        db.collection("account_applications")
                .document(application.getApplicationId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Application rejected successfully",
                            Toast.LENGTH_SHORT).show();
                    loadApplications();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error rejecting application: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String generateAccountNumber() {
        // Generate a 10-digit account number
        long timestamp = System.currentTimeMillis();
        return String.format("%010d", timestamp % 10000000000L);
    }

    private String getCurrentStaffName() {
        // Get staff name from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyBankPrefs", MODE_PRIVATE);
        String staffName = prefs.getString("staffName", null);
        
        if (staffName != null && !staffName.isEmpty()) {
            return staffName;
        }
        
        // Fallback: Get from Firebase Auth
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            return email != null ? email : "Staff Member";
        }
        
        return "Staff Member";
    }
}
