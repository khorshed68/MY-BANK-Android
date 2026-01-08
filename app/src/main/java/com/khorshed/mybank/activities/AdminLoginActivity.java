package com.khorshed.mybank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;
import com.khorshed.mybank.activities.admin.AdminDashboardActivity;
import com.khorshed.mybank.viewmodel.AuthViewModel;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton, backButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        firestore = FirebaseFirestore.getInstance();
        
        initViews();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.adminUsernameInput);
        passwordInput = findViewById(R.id.adminPasswordInput);
        loginButton = findViewById(R.id.adminLoginButton);
        backButton = findViewById(R.id.adminBackButton);
        registerLink = findViewById(R.id.adminRegisterLink);
        progressBar = findViewById(R.id.adminProgressBar);
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
                navigateToAdminDashboard();
            }
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performAdminLogin());

        backButton.setOnClickListener(v -> {
            finish();
        });

        registerLink.setOnClickListener(v -> {
            // Navigate to admin registration page
            startActivity(new Intent(this, AdminRegistrationActivity.class));
        });
    }

    private void performAdminLogin() {
        String usernameOrEmail = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (usernameOrEmail.isEmpty()) {
            usernameInput.setError("Admin username/email is required");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Check if input is email or username
        if (usernameOrEmail.contains("@")) {
            // Input is an email, login directly
            authViewModel.login(usernameOrEmail, password);
        } else {
            // Input is a username, find the email first
            findEmailByUsername(usernameOrEmail, password);
        }
    }

    private void findEmailByUsername(String username, String password) {
        firestore.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("role", "ADMIN")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty() && querySnapshot.getDocuments().size() > 0) {
                        String email = querySnapshot.getDocuments().get(0).getString("email");
                        if (email != null && !email.isEmpty()) {
                            // Found email, now login
                            authViewModel.login(email, password);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            Toast.makeText(this, "Email not found for this username", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Toast.makeText(this, "Admin username not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToAdminDashboard() {
        // After successful login, check user role and navigate accordingly
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Only allow ADMIN role
                if ("ADMIN".equals(user.getRole())) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Access denied. Administrator credentials required.", Toast.LENGTH_LONG).show();
                    authViewModel.logout();
                }
            }
        });
    }
}
