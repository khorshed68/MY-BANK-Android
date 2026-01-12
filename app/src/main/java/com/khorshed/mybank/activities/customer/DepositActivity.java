package com.khorshed.mybank.activities.customer;

import android.app.AlertDialog;
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
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.utils.FormatUtils;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

public class DepositActivity extends AppCompatActivity {

    private TextView accountNumberText, currentBalanceText;
    private EditText amountInput, descriptionInput;
    private Button depositButton, cancelButton;
    private ProgressBar progressBar;
    private CustomerViewModel customerViewModel;
    private Account currentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initViews();
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        setupObservers();
        setupClickListeners();
        loadAccountData();
    }

    private void initViews() {
        accountNumberText = new TextView(this);
        currentBalanceText = new TextView(this);
        amountInput = new EditText(this);
        descriptionInput = new EditText(this);
        depositButton = new Button(this);
        cancelButton = new Button(this);
        progressBar = new ProgressBar(this);
    }

    private void loadAccountData() {
        customerViewModel.getCurrentAccount().observe(this, account -> {
            if (account != null) {
                currentAccount = account;
                showDepositDialog();
            }
        });
    }

    private void setupObservers() {
        customerViewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Deposit successful!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        customerViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        // Implemented in dialog
    }

    private void showDepositDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        
        // Create custom dialog
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(this);
        customBuilder.setTitle("💰 Deposit Money");
        
        // Create dialog content
        View contentView = getLayoutInflater().inflate(android.R.layout.select_dialog_item, null);
        
        // Build message
        StringBuilder message = new StringBuilder();
        message.append("Account: ").append(FormatUtils.formatAccountNumber(currentAccount.getAccountNumber())).append("\n");
        message.append("Current Balance: ").append(FormatUtils.formatCurrency(currentAccount.getBalance())).append("\n\n");
        message.append("Enter deposit amount:");
        
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
        customBuilder.setPositiveButton("Deposit", (dialog, which) -> {
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
                
                if (description.isEmpty()) {
                    description = "Cash Deposit";
                }
                
                customerViewModel.deposit(currentAccount, amount, description);
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
