package com.khorshed.mybank.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Cheque;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChequeAdapter extends RecyclerView.Adapter<ChequeAdapter.ChequeViewHolder> {

    private List<Cheque> chequeList;

    public ChequeAdapter(List<Cheque> chequeList) {
        this.chequeList = chequeList;
    }

    @NonNull
    @Override
    public ChequeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cheque, parent, false);
        return new ChequeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChequeViewHolder holder, int position) {
        Cheque cheque = chequeList.get(position);
        
        holder.chequeNumberText.setText(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "N/A");
        holder.accountNumberText.setText(cheque.getAccountNumber() != null ? cheque.getAccountNumber() : "N/A");
        holder.payeeNameText.setText(cheque.getPayeeName() != null ? cheque.getPayeeName() : "N/A");
        holder.amountText.setText(String.format(Locale.getDefault(), "৳ %.2f", cheque.getAmount()));
        
        String status = cheque.getStatus() != null ? cheque.getStatus() : "UNKNOWN";
        holder.statusText.setText(status);
        
        if ("CLEARED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#4CAF50"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
        } else if ("PENDING_CLEARANCE".equals(status) || "DEPOSITED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#FF9800"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
        } else if ("BOUNCED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#F44336"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
        } else if ("CANCELLED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#9E9E9E"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        } else if ("ISSUED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#2196F3"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        }
        
        if (cheque.getIssuedDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.issuedDateText.setText("Issued: " + sdf.format(cheque.getIssuedDate()));
        } else {
            holder.issuedDateText.setText("Issued: N/A");
        }
    }

    @Override
    public int getItemCount() {
        return chequeList.size();
    }

    static class ChequeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView chequeNumberText;
        TextView accountNumberText;
        TextView payeeNameText;
        TextView amountText;
        TextView statusText;
        TextView issuedDateText;

        public ChequeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            chequeNumberText = itemView.findViewById(R.id.chequeNumberText);
            accountNumberText = itemView.findViewById(R.id.accountNumberText);
            payeeNameText = itemView.findViewById(R.id.payeeNameText);
            amountText = itemView.findViewById(R.id.amountText);
            statusText = itemView.findViewById(R.id.statusText);
            issuedDateText = itemView.findViewById(R.id.issuedDateText);
        }
    }
}
