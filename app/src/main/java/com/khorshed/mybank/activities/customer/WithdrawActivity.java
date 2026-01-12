package com.khorshed.mybank.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.utils.FormatUtils;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

public class WithdrawActivity extends AppCompatActivity {

    private CustomerViewModel customerViewModel;
    private Account currentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        setupObservers();
        loadAccountData();
    }

    private void loadAccountData() {
        customerViewModel.getCurrentAccount().observe(this, account -> {
            if (account != null) {
                currentAccount = account;
                showWithdrawDialog();
            }
        });
    }

    private void setupObservers() {
        customerViewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Withdrawal successful!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        customerViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showWithdrawDialog() {
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(this);
        customBuilder.setTitle("💸 Withdraw Money");
        
        // Build message
        StringBuilder message = new StringBuilder();
        message.append("Account: ").append(FormatUtils.formatAccountNumber(currentAccount.getAccountNumber())).append("\n");
        message.append("Available Balance: ").append(FormatUtils.formatCurrency(currentAccount.getBalance())).append("\n\n");
        message.append("Enter withdrawal amount:");
        
        final EditText input = new EditText(this);
        input.setHint("Amount");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        final EditText descInput = new EditText(this);
        descInput.setHint("Description (optional)");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        TextView messageView = new TextView(this);
        messageView.setText(message.toString());
        messageView.setPadding(0, 0, 0, 20);
        
        layout.addView(messageView);
        layout.addView(input);
        layout.addView(descInput);
        
        customBuilder.setView(layout);
        customBuilder.setPositiveButton("Withdraw", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                if (amount > currentAccount.getBalance()) {
                    Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                if (description.isEmpty()) {
                    description = "Cash Withdrawal";
                }
                
                customerViewModel.withdraw(currentAccount, amount, description);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        customBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        
        customBuilder.setOnCancelListener(dialog -> finish());
        
        AlertDialog dialog = customBuilder.create();
        dialog.show();
    }
}
