package com.khorshed.mybank.activities.customer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;
import com.khorshed.mybank.activities.LoginActivity;
import com.khorshed.mybank.services.EmailService;
import com.khorshed.mybank.utils.FormatUtils;
import com.khorshed.mybank.utils.ImageUtils;
import com.khorshed.mybank.viewmodel.AuthViewModel;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

public class CustomerDashboardActivity extends AppCompatActivity {

    private TextView welcomeText, accountNumberText, balanceText;
    private ImageView profileImageView;
    private MaterialButton createAccountButton, checkBalanceButton, accountOverviewButton;
    private MaterialButton transactionHistoryButton, depositButton, withdrawButton;
    private MaterialButton fundTransferButton, chequeServicesButton, settingsButton;
    private MaterialButton historyButton, profileSettingsButton, logoutButton, exitButton;
    private CustomerViewModel customerViewModel;
    private AuthViewModel authViewModel;
    private String currentAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        initViews();
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        accountNumberText = findViewById(R.id.accountNumberText);
        balanceText = findViewById(R.id.balanceText);
        profileImageView = findViewById(R.id.profileImageView);
        
        // Account Operations
        createAccountButton = findViewById(R.id.createAccountButton);
        checkBalanceButton = findViewById(R.id.checkBalanceButton);
        accountOverviewButton = findViewById(R.id.accountOverviewButton);
        transactionHistoryButton = findViewById(R.id.transactionHistoryButton);
        
        // Hide create account button - not needed for customers
        if (createAccountButton != null) {
            createAccountButton.setVisibility(android.view.View.GONE);
        }
        
        // Transactions
        depositButton = findViewById(R.id.depositButton);
        withdrawButton = findViewById(R.id.withdrawButton);
        fundTransferButton = findViewById(R.id.fundTransferButton);
        
        // Cheque Management
        chequeServicesButton = findViewById(R.id.chequeServicesButton);
        
        // Notifications
        settingsButton = findViewById(R.id.settingsButton);
        historyButton = findViewById(R.id.historyButton);
        
        // System
        profileSettingsButton = findViewById(R.id.profileSettingsButton);
        logoutButton = findViewById(R.id.logoutButton);
        exitButton = findViewById(R.id.exitButton);
    }

    private void loadUserData() {
        customerViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                welcomeText.setText("Welcome, " + user.getName().toUpperCase() + " (Account: 1)");
                
                // Load profile picture from Firebase Storage
                loadProfilePicture(user.getUserId());
            }
        });

        customerViewModel.getCurrentAccount().observe(this, account -> {
            if (account != null) {
                currentAccountId = account.getAccountId();
                accountNumberText.setText(FormatUtils.formatAccountNumber(account.getAccountNumber()));
                balanceText.setText(FormatUtils.formatCurrency(account.getBalance()));
                
                if (account.isFrozen()) {
                    Toast.makeText(this, "Your account is frozen. Please contact support.", 
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupClickListeners() {
        // Account Operations
        createAccountButton.setOnClickListener(v -> {
            // Show account creation info dialog
            new android.app.AlertDialog.Builder(this)
                .setTitle("💳 Create New Account")
                .setMessage("To create a new account, please visit your nearest branch or contact customer support.\n\n📞 Support: 1-800-MY-BANK\n✉️ Email: support@mybank.com")
                .setPositiveButton("OK", null)
                .show();
        });
        
        if (checkBalanceButton != null) {
            checkBalanceButton.setOnClickListener(v -> {
                if (balanceText.getText() != null && currentAccountId != null) {
                customerViewModel.getCurrentAccount().observe(this, account -> {
                    if (account != null) {
                        new android.app.AlertDialog.Builder(this)
                            .setTitle("💰 Account Balance")
                            .setMessage("Account: " + FormatUtils.formatAccountNumber(account.getAccountNumber()) + "\n\n" +
                                       "Current Balance:\n" + FormatUtils.formatCurrency(account.getBalance()) + "\n\n" +
                                       "Account Type: " + account.getAccountType() + "\n" +
                                       "Status: " + (account.isFrozen() ? "❌ Frozen" : "✅ Active"))
                            .setPositiveButton("OK", null)
                            .show();
                    }
                });
                }
            });
        }
        
        if (accountOverviewButton != null) {
            accountOverviewButton.setOnClickListener(v -> {
                customerViewModel.getCurrentAccount().observe(this, account -> {
                    if (account != null) {
                    customerViewModel.getTransactions(account.getAccountId()).observe(this, transactions -> {
                        int transactionCount = transactions != null ? transactions.size() : 0;
                        
                        StringBuilder overview = new StringBuilder();
                        overview.append("📊 ACCOUNT OVERVIEW\n\n");
                        overview.append("Account Number:\n").append(FormatUtils.formatAccountNumber(account.getAccountNumber())).append("\n\n");
                        overview.append("Account Type:\n").append(account.getAccountType()).append("\n\n");
                        overview.append("Current Balance:\n").append(FormatUtils.formatCurrency(account.getBalance())).append("\n\n");
                        overview.append("Currency: TAKA\n\n");
                        overview.append("Total Transactions: ").append(transactionCount).append("\n\n");
                        overview.append("Account Status:\n").append(account.isFrozen() ? "❌ Frozen" : "✅ Active");
                        
                        android.widget.TextView messageView = new android.widget.TextView(this);
                        messageView.setText(overview.toString());
                        messageView.setPadding(50, 40, 50, 40);
                        messageView.setTextSize(14);
                        
                        new android.app.AlertDialog.Builder(this)
                            .setTitle("📋 Account Overview")
                            .setView(messageView)
                            .setPositiveButton("Close", null)
                            .show();
                    });
                }
                });
            });
        }
        
        if (transactionHistoryButton != null) {
            transactionHistoryButton.setOnClickListener(v -> {
                if (currentAccountId != null) {
                    Intent intent = new Intent(this, TransactionsActivity.class);
                    intent.putExtra("ACCOUNT_ID", currentAccountId);
                    startActivity(intent);
                } else {
                    // If currentAccountId is null, fetch it first
                    customerViewModel.getCurrentAccount().observe(this, account -> {
                    if (account != null) {
                        currentAccountId = account.getAccountId();
                        Intent intent = new Intent(this, TransactionsActivity.class);
                        intent.putExtra("ACCOUNT_ID", currentAccountId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Unable to load transactions. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                }
            });
        }
        
        // Transactions
        if (depositButton != null) {
            depositButton.setOnClickListener(v -> 
                startActivity(new Intent(this, DepositActivity.class)));
        }
        
        if (withdrawButton != null) {
            withdrawButton.setOnClickListener(v -> 
                startActivity(new Intent(this, WithdrawActivity.class)));
        }
        
        if (fundTransferButton != null) {
            fundTransferButton.setOnClickListener(v -> 
                startActivity(new Intent(this, TransferActivity.class)));
        }
        
        // Cheque Management
        if (chequeServicesButton != null) {
            chequeServicesButton.setOnClickListener(v -> 
                startActivity(new Intent(this, CustomerChequeManagementActivity.class)));
        }
        
        // Notifications
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> 
                startActivity(new Intent(this, SettingsActivity.class)));
        }
        
        if (historyButton != null) {
            historyButton.setOnClickListener(v -> {
                // Show notification history
                showNotificationHistory();
            });
        }
        
        // System
        if (profileSettingsButton != null) {
            profileSettingsButton.setOnClickListener(v -> 
                startActivity(new Intent(this, ProfileActivity.class)));
        }
        
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Send logout email notification before actually logging out
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                    // Fetch current user and account data for email
                    customerViewModel.getCurrentUser().observe(this, user -> {
                        if (user != null) {
                            customerViewModel.getCurrentAccount().observe(this, account -> {
                                if (account != null) {
                                    EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                                            .customerName(user.getName())
                                            .accountNumber(account.getAccountNumber())
                                            .timestamp(new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date()))
                                            .build();
                                    
                                    EmailService.sendEmail(firebaseUser.getEmail(), EmailService.NotificationType.LOGOUT, emailData);
                                }
                            });
                        }
                    });
                }
                
                authViewModel.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
        
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                finishAffinity(); // Close all activities and exit app
            });
        }
    }

    private void showNotificationHistory() {
        // Get transaction history to generate notifications
        if (currentAccountId != null) {
            customerViewModel.getTransactions(currentAccountId).observe(this, transactions -> {
                StringBuilder notifications = new StringBuilder();
                notifications.append("🔔 NOTIFICATION HISTORY\n\n");
                
                if (transactions != null && !transactions.isEmpty()) {
                    int count = 0;
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault());
                    
                    for (com.khorshed.mybank.models.Transaction transaction : transactions) {
                        if (count >= 15) break; // Show last 15 notifications
                        
                        String icon = "";
                        String message = "";
                        
                        switch (transaction.getType().toUpperCase()) {
                            case "DEPOSIT":
                                icon = "💰";
                                message = "Deposit of " + FormatUtils.formatCurrency(transaction.getAmount()) + " successful";
                                break;
                            case "WITHDRAWAL":
                                icon = "💸";
                                message = "Withdrawal of " + FormatUtils.formatCurrency(transaction.getAmount()) + " successful";
                                break;
                            case "TRANSFER":
                                icon = "💳";
                                message = "Transfer of " + FormatUtils.formatCurrency(transaction.getAmount()) + " completed";
                                break;
                            default:
                                icon = "📄";
                                message = transaction.getType() + " - " + FormatUtils.formatCurrency(transaction.getAmount());
                        }
                        
                        notifications.append(icon).append(" ").append(message).append("\n");
                        if (transaction.getCreatedAt() != null) {
                            notifications.append("   ").append(dateFormat.format(transaction.getCreatedAt())).append("\n");
                        }
                        notifications.append("\n");
                        count++;
                    }
                    
                    notifications.append("Showing last ").append(count).append(" notifications");
                } else {
                    notifications.append("No notifications yet.\n\n");
                    notifications.append("You will receive notifications for:\n");
                    notifications.append("• Account deposits\n");
                    notifications.append("• Withdrawals\n");
                    notifications.append("• Fund transfers\n");
                    notifications.append("• Account updates\n");
                }
                
                android.widget.TextView messageView = new android.widget.TextView(this);
                messageView.setText(notifications.toString());
                messageView.setPadding(50, 40, 50, 40);
                messageView.setTextSize(14);
                
                new android.app.AlertDialog.Builder(this)
                    .setTitle("🔔 Notifications")
                    .setView(messageView)
                    .setPositiveButton("Close", null)
                    .show();
            });
        } else {
            customerViewModel.getCurrentAccount().observe(this, account -> {
                if (account != null) {
                    currentAccountId = account.getAccountId();
                    showNotificationHistory();
                } else {
                    Toast.makeText(this, "Unable to load notifications", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
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
                            Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            
                            if (bitmap != null) {
                                // Display profile picture with circular crop using Glide
                                Glide.with(this)
                                    .load(bitmap)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_person)
                                    .into(profileImageView);
                                android.util.Log.d("CustomerDashboard", "✅ Base64 image loaded successfully");
                                return;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("CustomerDashboard", "❌ Error decoding Base64 image", e);
                        }
                    }
                    
                    // No valid profile picture - use default
                    Glide.with(this)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(profileImageView);
                } else {
                    // Document doesn't exist - use default
                    Glide.with(this)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(profileImageView);
                }
            })
            .addOnFailureListener(e -> {
                // Loading failed - use default icon
                Glide.with(this)
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(profileImageView);
                android.util.Log.e("CustomerDashboard", "Failed to load profile data", e);
            });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile picture when returning to dashboard (e.g., after updating in ProfileActivity)
        customerViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                loadProfilePicture(user.getUserId());
            }
        });
    }
}