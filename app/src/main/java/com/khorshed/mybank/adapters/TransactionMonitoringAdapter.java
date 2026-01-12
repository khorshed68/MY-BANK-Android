package com.khorshed.mybank.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.TransactionMonitoring;

import java.util.ArrayList;
import java.util.List;

public class TransactionMonitoringAdapter extends RecyclerView.Adapter<TransactionMonitoringAdapter.TransactionViewHolder> {

    private List<TransactionMonitoring> transactionList;
    private List<TransactionMonitoring> filteredTransactionList;

    public TransactionMonitoringAdapter(List<TransactionMonitoring> transactionList) {
        this.transactionList = transactionList;
        this.filteredTransactionList = new ArrayList<>(transactionList);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_monitoring, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionMonitoring transaction = filteredTransactionList.get(position);
        
        holder.idText.setText(transaction.getId());
        holder.accountText.setText(transaction.getAccountNumber());
        holder.typeText.setText(transaction.getType());
        
        // Format amount
        String amountFormatted = String.format("৳%.2f", transaction.getAmount());
        holder.amountText.setText(amountFormatted);
        
        holder.dateTimeText.setText(transaction.getDateTime());
        holder.descriptionText.setText(transaction.getDescription());
        holder.statusText.setText(transaction.getStatus());
        
        // Set status color
        int statusColor;
        switch (transaction.getStatus().toLowerCase()) {
            case "normal":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark);
                break;
            case "flagged":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark);
                break;
            case "reversed":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark);
                break;
            default:
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray);
                break;
        }
        holder.statusText.setTextColor(statusColor);
        
        // Handle checkbox
        holder.transactionCheckBox.setChecked(transaction.isSelected());
        holder.transactionCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            transaction.setSelected(isChecked);
        });
        
        // Handle row click
        holder.itemView.setOnClickListener(v -> {
            transaction.setSelected(!transaction.isSelected());
            holder.transactionCheckBox.setChecked(transaction.isSelected());
        });
    }

    @Override
    public int getItemCount() {
        return filteredTransactionList.size();
    }

    // Filter by search query
    public void filter(String query) {
        filteredTransactionList.clear();
        if (query.isEmpty()) {
            filteredTransactionList.addAll(transactionList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (TransactionMonitoring transaction : transactionList) {
                if (transaction.getAccountNumber().toLowerCase().contains(lowerCaseQuery) ||
                    transaction.getId().toLowerCase().contains(lowerCaseQuery)) {
                    filteredTransactionList.add(transaction);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Filter by type
    public void filterByType(String type) {
        filteredTransactionList.clear();
        if (type.equals("All Transactions") || type.isEmpty()) {
            filteredTransactionList.addAll(transactionList);
        } else if (type.equals("Today")) {
            // Filter today's transactions
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            for (TransactionMonitoring transaction : transactionList) {
                if (transaction.getDateTime().startsWith(today)) {
                    filteredTransactionList.add(transaction);
                }
            }
        } else {
            // Filter by transaction type or status
            for (TransactionMonitoring transaction : transactionList) {
                if (transaction.getType().equalsIgnoreCase(type) || 
                    transaction.getStatus().equalsIgnoreCase(type)) {
                    filteredTransactionList.add(transaction);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Filter by date range
    public void filterByDateRange(String startDate, String endDate) {
        filteredTransactionList.clear();
        for (TransactionMonitoring transaction : transactionList) {
            String transactionDate = transaction.getDateTime().substring(0, 10); // Get YYYY-MM-DD
            if (transactionDate.compareTo(startDate) >= 0 && transactionDate.compareTo(endDate) <= 0) {
                filteredTransactionList.add(transaction);
            }
        }
        notifyDataSetChanged();
    }

    // Get selected transactions
    public List<TransactionMonitoring> getSelectedTransactions() {
        List<TransactionMonitoring> selected = new ArrayList<>();
        for (TransactionMonitoring transaction : filteredTransactionList) {
            if (transaction.isSelected()) {
                selected.add(transaction);
            }
        }
        return selected;
    }

    // Update data
    public void updateData(List<TransactionMonitoring> newTransactionList) {
        this.transactionList = newTransactionList;
        this.filteredTransactionList = new ArrayList<>(newTransactionList);
        notifyDataSetChanged();
    }

    // Calculate total amount
    public double getTotalAmount() {
        double total = 0;
        for (TransactionMonitoring transaction : filteredTransactionList) {
            total += transaction.getAmount();
        }
        return total;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        CheckBox transactionCheckBox;
        TextView idText;
        TextView accountText;
        TextView typeText;
        TextView amountText;
        TextView dateTimeText;
        TextView descriptionText;
        TextView statusText;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionCheckBox = itemView.findViewById(R.id.transactionCheckBox);
            idText = itemView.findViewById(R.id.idText);
            accountText = itemView.findViewById(R.id.accountText);
            typeText = itemView.findViewById(R.id.typeText);
            amountText = itemView.findViewById(R.id.amountText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}
