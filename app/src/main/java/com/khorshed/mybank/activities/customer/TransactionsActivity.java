package com.khorshed.mybank.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Transaction;
import com.khorshed.mybank.utils.FormatUtils;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionsActivity extends AppCompatActivity {

    private CustomerViewModel customerViewModel;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        accountId = getIntent().getStringExtra("ACCOUNT_ID");
        
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        loadTransactions();
    }

    private void loadTransactions() {
        if (accountId != null) {
            customerViewModel.getTransactions(accountId).observe(this, transactions -> {
                if (transactions != null && !transactions.isEmpty()) {
                    showTransactionsDialog(transactions);
                } else {
                    Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            // Load current account's transactions
            customerViewModel.getCurrentAccount().observe(this, account -> {
                if (account != null) {
                    customerViewModel.getTransactions(account.getAccountId()).observe(this, transactions -> {
                        if (transactions != null && !transactions.isEmpty()) {
                            showTransactionsDialog(transactions);
                        } else {
                            Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        }
    }

    private void showTransactionsDialog(List<Transaction> transactions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📋 Transaction History");
        
        StringBuilder message = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        
        int count = 0;
        for (Transaction transaction : transactions) {
            if (count >= 20) break; // Show last 20 transactions
            
            String typeIcon = getTypeIcon(transaction.getType());
            message.append(typeIcon).append(" ").append(transaction.getType()).append("\n");
            message.append("Amount: ").append(FormatUtils.formatCurrency(transaction.getAmount())).append("\n");
            message.append("Balance: ").append(FormatUtils.formatCurrency(transaction.getBalanceAfter())).append("\n");
            
            if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                message.append("Note: ").append(transaction.getDescription()).append("\n");
            }
            
            if (transaction.getCreatedAt() != null) {
                message.append("Date: ").append(dateFormat.format(transaction.getCreatedAt())).append("\n");
            }
            
            message.append("\n");
            count++;
        }
        
        message.append("\nShowing last ").append(count).append(" transactions");
        
        TextView messageView = new TextView(this);
        messageView.setText(message.toString());
        messageView.setPadding(50, 40, 50, 40);
        messageView.setTextSize(14);
        
        builder.setView(messageView);
        builder.setPositiveButton("Close", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        
        builder.setOnCancelListener(dialog -> finish());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getTypeIcon(String type) {
        switch (type.toUpperCase()) {
            case "DEPOSIT":
                return "💰";
            case "WITHDRAWAL":
                return "💸";
            case "TRANSFER":
                return "💳";
            default:
                return "📄";
        }
    }
}
