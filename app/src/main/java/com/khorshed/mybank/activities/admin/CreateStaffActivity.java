package com.khorshed.mybank.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateStaffActivity extends AppCompatActivity {

    private TextInputEditText fullNameInput, emailInput, usernameInput, passwordInput, phoneInput, departmentInput;
    private MaterialButton backButton, createButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_staff);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        createButton = findViewById(R.id.createButton);
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        departmentInput = findViewById(R.id.departmentInput);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        createButton.setOnClickListener(v -> {
            if (validateInputs()) {
                createStaffMember();
            }
        });
    }

    private boolean validateInputs() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (fullName.isEmpty()) {
            fullNameInput.setError("Full name is required");
            fullNameInput.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return false;
        }

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void createStaffMember() {
        showLoading(true);

        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String department = departmentInput.getText().toString().trim();

        // Create authentication user first
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();

                    // Create user profile in Firestore
                    Map<String, Object> staffData = new HashMap<>();
                    staffData.put("userId", userId);
                    staffData.put("name", fullName);
                    staffData.put("fullName", fullName);
                    staffData.put("email", email);
                    staffData.put("username", username);
                    staffData.put("phone", phone);
                    staffData.put("department", department);
                    staffData.put("role", "STAFF");
                    staffData.put("status", "Active");
                    staffData.put("isActive", true);
                    staffData.put("createdAt", new Date());
                    staffData.put("createdBy", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "ADMIN");

                    db.collection("users")
                            .document(userId)
                            .set(staffData)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                Toast.makeText(this, "✅ Staff member created successfully!", Toast.LENGTH_SHORT).show();
                                
                                // Log the action
                                logAdminAction("Staff Creation", "Created staff: " + username + " (" + email + ")");
                                
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Error saving staff data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error creating account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void logAdminAction(String action, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("adminId", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "UNKNOWN");
        log.put("action", action);
        log.put("details", details);
        log.put("timestamp", new Date());

        db.collection("adminLogs").add(log);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createButton.setEnabled(!show);
        backButton.setEnabled(!show);
    }
}
