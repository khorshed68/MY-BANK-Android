package com.khorshed.mybank.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.khorshed.mybank.R;
import com.khorshed.mybank.viewmodel.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetButton;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            resetButton.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getIsSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Password reset email sent! Check your inbox.", 
                    Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupClickListeners() {
        resetButton.setOnClickListener(v -> performPasswordReset());
        
        findViewById(R.id.backToLoginLink).setOnClickListener(v -> finish());
    }

    private void performPasswordReset() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        authViewModel.resetPassword(email);
    }
}
