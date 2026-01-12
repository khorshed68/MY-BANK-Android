package com.khorshed.mybank.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Cheque;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChequeTableAdapter extends RecyclerView.Adapter<ChequeTableAdapter.ChequeTableViewHolder> {

    private List<Cheque> chequeList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ChequeTableAdapter(List<Cheque> chequeList) {
        this.chequeList = chequeList;
    }

    @NonNull
    @Override
    public ChequeTableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cheque_table_row, parent, false);
        return new ChequeTableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChequeTableViewHolder holder, int position) {
        Cheque cheque = chequeList.get(position);
        
        // Cheque Number
        holder.chequeNumberText.setText(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "N/A");
        
        // Account Number
        holder.accountText.setText(cheque.getAccountNumber() != null ? cheque.getAccountNumber() : "N/A");
        
        // Customer Name
        holder.customerText.setText(cheque.getCustomerName() != null ? cheque.getCustomerName() : "N/A");
        
        // Amount
        holder.amountText.setText(String.format(Locale.getDefault(), "%.1f", cheque.getAmount()));
        
        // Payee Name
        holder.payeeText.setText(cheque.getPayeeName() != null && !cheque.getPayeeName().isEmpty() ? cheque.getPayeeName() : "");
        
        // Status
        String status = cheque.getStatus() != null ? cheque.getStatus() : "UNKNOWN";
        holder.statusText.setText(status);
        
        // Set status color
        if ("CLEARED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("PENDING_CLEARANCE".equals(status) || "DEPOSITED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#FF9800"));
        } else if ("BOUNCED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#F44336"));
        } else if ("CANCELLED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#9E9E9E"));
        } else if ("ISSUED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#2196F3"));
        }
        
        // Issue Date
        if (cheque.getIssuedDate() != null) {
            holder.issueDateText.setText(dateFormat.format(cheque.getIssuedDate()));
        } else {
            holder.issueDateText.setText("");
        }
        
        // Clearance Date
        if (cheque.getClearanceDate() != null) {
            holder.clearanceDateText.setText(dateFormat.format(cheque.getClearanceDate()));
        } else {
            holder.clearanceDateText.setText("");
        }
        
        // Bounce Reason
        holder.bounceReasonText.setText(cheque.getBounceReason() != null ? cheque.getBounceReason() : "");
    }

    @Override
    public int getItemCount() {
        return chequeList.size();
    }

    public void updateList(List<Cheque> newList) {
        this.chequeList = newList;
        notifyDataSetChanged();
    }

    static class ChequeTableViewHolder extends RecyclerView.ViewHolder {
        TextView chequeNumberText;
        TextView accountText;
        TextView customerText;
        TextView amountText;
        TextView payeeText;
        TextView statusText;
        TextView issueDateText;
        TextView clearanceDateText;
        TextView bounceReasonText;

        public ChequeTableViewHolder(@NonNull View itemView) {
            super(itemView);
            chequeNumberText = itemView.findViewById(R.id.chequeNumberText);
            accountText = itemView.findViewById(R.id.accountText);
            customerText = itemView.findViewById(R.id.customerText);
            amountText = itemView.findViewById(R.id.amountText);
            payeeText = itemView.findViewById(R.id.payeeText);
            statusText = itemView.findViewById(R.id.statusText);
            issueDateText = itemView.findViewById(R.id.issueDateText);
            clearanceDateText = itemView.findViewById(R.id.clearanceDateText);
            bounceReasonText = itemView.findViewById(R.id.bounceReasonText);
        }
    }
}
