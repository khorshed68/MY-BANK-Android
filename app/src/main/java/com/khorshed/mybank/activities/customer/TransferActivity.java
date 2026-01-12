package com.khorshed.mybank.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.utils.FormatUtils;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

public class TransferActivity extends AppCompatActivity {

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
                showTransferDialog();
            }
        });
    }

    private void setupObservers() {
        customerViewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Transfer successful!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        customerViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showTransferDialog() {
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(this);
        customBuilder.setTitle("💳 Fund Transfer");
        
        // Build message
        StringBuilder message = new StringBuilder();
        message.append("From Account: ").append(FormatUtils.formatAccountNumber(currentAccount.getAccountNumber())).append("\n");
        message.append("Available Balance: ").append(FormatUtils.formatCurrency(currentAccount.getBalance())).append("\n\n");
        
        final EditText accountInput = new EditText(this);
        accountInput.setHint("Recipient Account Number");
        accountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        
        final EditText amountInput = new EditText(this);
        amountInput.setHint("Amount");
        amountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        final EditText descInput = new EditText(this);
        descInput.setHint("Description (optional)");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        TextView messageView = new TextView(this);
        messageView.setText(message.toString());
        messageView.setPadding(0, 0, 0, 20);
        
        layout.addView(messageView);
        layout.addView(accountInput);
        layout.addView(amountInput);
        layout.addView(descInput);
        
        customBuilder.setView(layout);
        customBuilder.setPositiveButton("Transfer", (dialog, which) -> {
            String toAccount = accountInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            
            if (toAccount.isEmpty()) {
                Toast.makeText(this, "Please enter recipient account number", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            if (toAccount.equals(currentAccount.getAccountNumber())) {
                Toast.makeText(this, "Cannot transfer to the same account", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
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
                    description = "Transfer to " + toAccount;
                }
                
                customerViewModel.transfer(currentAccount, toAccount, amount, description);
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
