package com.khorshed.mybank.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;
import com.khorshed.mybank.activities.admin.AdminDashboardActivity;
import com.khorshed.mybank.activities.customer.CustomerDashboardActivity;
import com.khorshed.mybank.activities.staff.StaffDashboardActivity;
import com.khorshed.mybank.utils.BiometricHelper;
import com.khorshed.mybank.utils.PreferenceManager;
import com.khorshed.mybank.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, biometricButton;
    private TextView registerLink, forgotPasswordLink;
    private CheckBox rememberMeCheckbox;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        preferenceManager = new PreferenceManager(this);
        
        initViews();
        setupObservers();
        setupClickListeners();
        loadSavedEmail();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        biometricButton = findViewById(R.id.biometricButton);
        registerLink = findViewById(R.id.registerLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        progressBar = findViewById(R.id.progressBar);

        // Initialize clear, staff portal, and admin portal buttons
        Button clearButton = findViewById(R.id.clearButton);
        Button staffPortalButton = findViewById(R.id.staffPortalButton);
        Button adminPortalButton = findViewById(R.id.adminPortalButton);

        // Clear button functionality
        clearButton.setOnClickListener(v -> {
            emailInput.setText("");
            passwordInput.setText("");
            rememberMeCheckbox.setChecked(false);
        });

        // Staff portal button - navigate to dedicated staff login page
        staffPortalButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffLoginActivity.class));
        });

        // Admin portal button - navigate to dedicated admin login page
        adminPortalButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class));
        });

        if (BiometricHelper.isBiometricAvailable(this) && preferenceManager.isBiometricEnabled()) {
            biometricButton.setVisibility(View.VISIBLE);
        } else {
            biometricButton.setVisibility(View.GONE);
        }
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getIsSuccess().observe(this, success -> {
            if (success != null && success) {
                navigateBasedOnRole();
            }
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());

        biometricButton.setOnClickListener(v -> {
            BiometricHelper.showBiometricPrompt(
                this,
                "Login to MY BANK",
                "Use your fingerprint or face",
                "Authenticate to access your account",
                new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onSuccess() {
                        navigateBasedOnRole();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing
                    }
                }
            );
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void performLogin() {
        String phoneNumber = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            emailInput.setError("Phone number is required");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (rememberMeCheckbox.isChecked()) {
            preferenceManager.setRememberMe(true);
            preferenceManager.setLastEmail(phoneNumber);
        } else {
            preferenceManager.setRememberMe(false);
            preferenceManager.setLastEmail("");
        }

        // Login with phone number and password
        loginWithPhoneAndPassword(phoneNumber, password);
    }
    
    private void loginWithPhoneAndPassword(String phoneNumber, String password) {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        
        // Find user by phone number to get their email
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("phone", phoneNumber)
                .whereEqualTo("role", "CUSTOMER")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getString("userId");
                        
                        // Check if user has an active account
                        checkAccountStatusAndLogin(userId, email, password, phoneNumber);
                    } else {
                        // No user found - check if application exists but not approved yet
                        checkApplicationStatus(phoneNumber);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void checkApplicationStatus(String phoneNumber) {
        // Check if there's a pending application
        FirebaseFirestore.getInstance()
                .collection("account_applications")
                .whereEqualTo("phone", phoneNumber)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String status = queryDocumentSnapshots.getDocuments().get(0).getString("status");
                        
                        if ("PENDING".equals(status)) {
                            new AlertDialog.Builder(this)
                                .setTitle("Application Pending")
                                .setMessage("Your account application is still pending approval.\\n\\n" +
                                        "✅ Your application has been submitted\\n" +
                                        "⏳ Waiting for staff approval\\n" +
                                        "📧 You will receive an email once approved\\n\\n" +
                                        "Please wait for the approval notification before attempting to login.")
                                .setPositiveButton("OK", null)
                                .show();
                        } else if ("REJECTED".equals(status)) {
                            String reason = queryDocumentSnapshots.getDocuments().get(0).getString("rejectionReason");
                            new AlertDialog.Builder(this)
                                .setTitle("Application Rejected")
                                .setMessage("Unfortunately, your account application has been rejected.\\n\\n" +
                                        "Reason: " + (reason != null ? reason : "Not specified") + "\\n\\n" +
                                        "Please contact our customer support for more information.")
                                .setPositiveButton("OK", null)
                                .show();
                        }
                    } else {
                        Toast.makeText(this, "No account or application found with this phone number.\\n" +
                                "Please register first.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(this, "No customer account found with this phone number", 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void checkAccountStatusAndLogin(String userId, String email, String password, String phoneNumber) {
        // Check if user has an active account
        FirebaseFirestore.getInstance()
                .collection("accounts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "ACTIVE")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User has an active account, proceed with login
                        authViewModel.login(email, password);
                    } else {
                        // No active account found
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        
                        new AlertDialog.Builder(this)
                            .setTitle("Account Not Active")
                            .setMessage("Your account application may still be pending approval.\\n\\n" +
                                    "Please wait for staff approval before logging in.\\n" +
                                    "You will receive an email notification once your account is approved.")
                            .setPositiveButton("OK", null)
                            .show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(this, "Error checking account status: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateBasedOnRole() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user == null) return;

            Intent intent;
            switch (user.getRole()) {
                case "CUSTOMER":
                    intent = new Intent(this, CustomerDashboardActivity.class);
                    break;
                case "STAFF":
                    intent = new Intent(this, StaffDashboardActivity.class);
                    break;
                case "ADMIN":
                    intent = new Intent(this, AdminDashboardActivity.class);
                    break;
                default:
                    Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show();
                    return;
            }
            startActivity(intent);
            finish();
        });
    }

    private void loadSavedEmail() {
        if (preferenceManager.isRememberMe()) {
            emailInput.setText(preferenceManager.getLastEmail());
            rememberMeCheckbox.setChecked(true);
        }
    }
}
