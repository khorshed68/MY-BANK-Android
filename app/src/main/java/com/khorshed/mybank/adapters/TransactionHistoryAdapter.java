package com.khorshed.mybank.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.ChequeAuditLog;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {

    private List<ChequeAuditLog> transactionList;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public TransactionHistoryAdapter(List<ChequeAuditLog> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history_row, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        ChequeAuditLog log = transactionList.get(position);
        
        // Cheque Number
        holder.chequeNumberText.setText(log.getChequeNumber() != null ? log.getChequeNumber() : "N/A");
        
        // Account Number
        holder.accountText.setText(log.getAccountNumber() != null ? log.getAccountNumber() : "N/A");
        
        // Type
        holder.typeText.setText(log.getType() != null ? log.getType() : "N/A");
        
        // Old Status
        holder.oldStatusText.setText(log.getOldStatus() != null && !log.getOldStatus().isEmpty() ? log.getOldStatus() : "-");
        
        // New Status
        String newStatus = log.getNewStatus() != null ? log.getNewStatus() : "-";
        holder.newStatusText.setText(newStatus);
        
        // Set color for new status
        if ("CLEARED".equals(newStatus)) {
            holder.newStatusText.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("BOUNCED".equals(newStatus)) {
            holder.newStatusText.setTextColor(Color.parseColor("#F44336"));
        } else if ("ISSUED".equals(newStatus)) {
            holder.newStatusText.setTextColor(Color.parseColor("#2196F3"));
        } else if ("DEPOSITED".equals(newStatus) || "PENDING_CLEARANCE".equals(newStatus)) {
            holder.newStatusText.setTextColor(Color.parseColor("#FF9800"));
        } else if ("CANCELLED".equals(newStatus)) {
            holder.newStatusText.setTextColor(Color.parseColor("#9E9E9E"));
        }
        
        // Amount
        holder.amountText.setText(String.format(Locale.getDefault(), "%.1f", log.getAmount()));
        
        // Date & Time
        if (log.getTimestamp() != null) {
            holder.dateTimeText.setText(dateTimeFormat.format(log.getTimestamp()));
        } else {
            holder.dateTimeText.setText("");
        }
        
        // Performed By
        holder.performedByText.setText(log.getPerformedByName() != null ? log.getPerformedByName() : "N/A");
        
        // User Type
        holder.userTypeText.setText(log.getUserType() != null ? log.getUserType() : "N/A");
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateList(List<ChequeAuditLog> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView chequeNumberText;
        TextView accountText;
        TextView typeText;
        TextView oldStatusText;
        TextView newStatusText;
        TextView amountText;
        TextView dateTimeText;
        TextView performedByText;
        TextView userTypeText;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            chequeNumberText = itemView.findViewById(R.id.chequeNumberText);
            accountText = itemView.findViewById(R.id.accountText);
            typeText = itemView.findViewById(R.id.typeText);
            oldStatusText = itemView.findViewById(R.id.oldStatusText);
            newStatusText = itemView.findViewById(R.id.newStatusText);
            amountText = itemView.findViewById(R.id.amountText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            performedByText = itemView.findViewById(R.id.performedByText);
            userTypeText = itemView.findViewById(R.id.userTypeText);
        }
    }
}
