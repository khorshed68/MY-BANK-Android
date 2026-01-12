package com.khorshed.mybank.activities.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
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
    private LinearLayout transactionsContainer;
    private TextView transactionCountText;
    private MaterialButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        
        accountId = getIntent().getStringExtra("ACCOUNT_ID");
        
        initViews();
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        loadTransactions();
    }

    private void initViews() {
        transactionsContainer = findViewById(R.id.transactionsContainer);
        transactionCountText = findViewById(R.id.transactionCountText);
        closeButton = findViewById(R.id.closeButton);
        
        closeButton.setOnClickListener(v -> finish());
    }

    private void loadTransactions() {
        if (accountId != null) {
            customerViewModel.getTransactions(accountId).observe(this, transactions -> {
                if (transactions != null && !transactions.isEmpty()) {
                    displayTransactions(transactions);
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
                            displayTransactions(transactions);
                        } else {
                            Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        }
    }

    private void displayTransactions(List<Transaction> transactions) {
        // Clear previous views
        transactionsContainer.removeAllViews();
        
        // Update transaction count
        transactionCountText.setText("Total Transactions: " + transactions.size());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        LayoutInflater inflater = LayoutInflater.from(this);
        
        // Display all transactions (no limit)
        for (Transaction transaction : transactions) {
            View itemView = inflater.inflate(R.layout.item_transaction, transactionsContainer, false);
            
            TextView iconView = itemView.findViewById(R.id.transactionIcon);
            TextView typeView = itemView.findViewById(R.id.transactionType);
            TextView dateView = itemView.findViewById(R.id.transactionDate);
            TextView amountView = itemView.findViewById(R.id.transactionAmount);
            TextView balanceView = itemView.findViewById(R.id.transactionBalance);
            TextView descriptionView = itemView.findViewById(R.id.transactionDescription);
            
            // Set transaction icon and type
            String typeIcon = getTypeIcon(transaction.getType());
            iconView.setText(typeIcon);
            typeView.setText(transaction.getType().toUpperCase());
            
            // Set date
            if (transaction.getCreatedAt() != null) {
                dateView.setText(dateFormat.format(transaction.getCreatedAt()));
            } else {
                dateView.setText("Date not available");
            }
            
            // Set amount with color based on transaction type
            String amountText = FormatUtils.formatCurrency(transaction.getAmount());
            amountView.setText(amountText);
            
            // Color coding: Green for deposits, Red for withdrawals/transfers
            if ("DEPOSIT".equalsIgnoreCase(transaction.getType())) {
                amountView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                amountView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            
            // Set balance after transaction
            balanceView.setText("Balance After: " + FormatUtils.formatCurrency(transaction.getBalanceAfter()));
            
            // Set description if available
            if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText("Note: " + transaction.getDescription());
            } else {
                descriptionView.setVisibility(View.GONE);
            }
            
            transactionsContainer.addView(itemView);
        }
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
