package com.khorshed.mybank.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.khorshed.mybank.R;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

public class SettingsActivity extends AppCompatActivity {

    private CustomerViewModel customerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        showSettingsDialog();
    }

    private void showSettingsDialog() {
        String[] options = {
            "🔔 Enable Notifications",
            "🔕 Disable Notifications",
            "🔒 Enable Biometric Login",
            "🔓 Disable Biometric Login",
            "ℹ️ App Information"
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Settings");
        builder.setItems(options, (dialog, which) -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            switch (which) {
                case 0: // Enable Notifications
                    if (userId != null) {
                        customerViewModel.updateNotificationSettings(userId, true);
                        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                case 1: // Disable Notifications
                    if (userId != null) {
                        customerViewModel.updateNotificationSettings(userId, false);
                        Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                case 2: // Enable Biometric
                    if (userId != null) {
                        customerViewModel.updateBiometricSettings(userId, true);
                        Toast.makeText(this, "Biometric login enabled", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                case 3: // Disable Biometric
                    if (userId != null) {
                        customerViewModel.updateBiometricSettings(userId, false);
                        Toast.makeText(this, "Biometric login disabled", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                case 4: // App Info
                    showAppInfo();
                    break;
            }
        });
        
        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        
        builder.setOnCancelListener(dialog -> finish());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAppInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ℹ️ App Information");
        
        StringBuilder info = new StringBuilder();
        info.append("🏛️ MY BANK\n\n");
        info.append("Version: 1.0.0\n\n");
        info.append("A modern banking application\n");
        info.append("with secure and easy transactions.\n\n");
        info.append("© 2026 MY BANK. All rights reserved.");
        
        TextView messageView = new TextView(this);
        messageView.setText(info.toString());
        messageView.setPadding(50, 40, 50, 40);
        messageView.setTextSize(14);
        
        builder.setView(messageView);
        builder.setPositiveButton("Close", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
