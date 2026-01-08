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

import com.khorshed.mybank.R;
import com.khorshed.mybank.activities.admin.AdminDashboardActivity;
import com.khorshed.mybank.activities.staff.StaffActivityLogsActivity;
import com.khorshed.mybank.activities.staff.StaffDashboardActivity;
import com.khorshed.mybank.viewmodel.AuthViewModel;

public class StaffLoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton, backButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        initViews();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.staffUsernameInput);
        passwordInput = findViewById(R.id.staffPasswordInput);
        loginButton = findViewById(R.id.staffLoginButton);
        backButton = findViewById(R.id.staffBackButton);
        registerLink = findViewById(R.id.staffRegisterLink);
        progressBar = findViewById(R.id.staffProgressBar);
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
                navigateToStaffDashboard();
            }
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performStaffLogin());

        backButton.setOnClickListener(v -> {
            finish();
        });

        registerLink.setOnClickListener(v -> {
            // Navigate to staff registration page
            startActivity(new Intent(this, StaffRegisterActivity.class));
        });
    }

    private void performStaffLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Perform login through ViewModel with username
        authViewModel.loginWithUsername(username, password);
    }

    private void navigateToStaffDashboard() {
        // After successful login, check user role and navigate accordingly
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Intent intent;
                String role = user.getRole();
                
                // Save staff name to SharedPreferences for later use
                getSharedPreferences("MyBankPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("staffName", user.getName())
                    .apply();
                
                // Allow all staff roles: TELLER, OFFICER, MANAGER, and ADMIN
                if ("TELLER".equals(role) || "OFFICER".equals(role) || "MANAGER".equals(role)) {
                    intent = new Intent(this, StaffDashboardActivity.class);
                    // Log staff login activity
                    StaffActivityLogsActivity.logActivity("LOGIN", "", 
                        "Staff " + user.getName() + " logged in successfully");
                } else if ("ADMIN".equals(role)) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                    // Log admin login activity
                    StaffActivityLogsActivity.logActivity("LOGIN", "", 
                        "Admin " + user.getName() + " logged in successfully");
                } else {
                    Toast.makeText(this, "Access denied. Staff credentials required.", Toast.LENGTH_LONG).show();
                    authViewModel.logout();
                    return;
                }
                
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
