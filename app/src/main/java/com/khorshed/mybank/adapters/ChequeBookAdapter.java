package com.khorshed.mybank.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.ChequeBook;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChequeBookAdapter extends RecyclerView.Adapter<ChequeBookAdapter.ChequeBookViewHolder> {

    private Context context;
    private List<ChequeBook> chequeBookList;

    public ChequeBookAdapter(Context context, List<ChequeBook> chequeBookList) {
        this.context = context;
        this.chequeBookList = chequeBookList;
    }

    @NonNull
    @Override
    public ChequeBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cheque_book_table, parent, false);
        return new ChequeBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChequeBookViewHolder holder, int position) {
        ChequeBook book = chequeBookList.get(position);
        
        holder.bookNumberText.setText(book.getChequeBookNumber() != null ? book.getChequeBookNumber() : "N/A");
        holder.accountText.setText(book.getAccountNumber() != null ? book.getAccountNumber() : "N/A");
        holder.customerText.setText(book.getUserId() != null ? book.getUserId() : "N/A");
        
        String status = book.getStatus() != null ? book.getStatus() : "UNKNOWN";
        holder.statusText.setText(status);
        
        // Set status color
        switch (status) {
            case "ISSUED":
                holder.statusText.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "PENDING":
                holder.statusText.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "APPROVED":
                holder.statusText.setTextColor(Color.parseColor("#2196F3"));
                break;
            case "COMPLETED":
                holder.statusText.setTextColor(Color.parseColor("#9E9E9E"));
                break;
            case "REJECTED":
            case "CANCELLED":
                holder.statusText.setTextColor(Color.parseColor("#F44336"));
                break;
            default:
                holder.statusText.setTextColor(Color.parseColor("#424242"));
                break;
        }
        
        holder.totalLeavesText.setText(String.valueOf(book.getNumberOfLeaves()));
        
        int remaining = book.getNumberOfLeaves() - book.getUsedLeaves();
        holder.remainingText.setText(String.valueOf(remaining));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        if (book.getRequestDate() != null) {
            holder.requestDateText.setText(sdf.format(book.getRequestDate()));
        } else if (book.getIssuedDate() != null) {
            holder.requestDateText.setText(sdf.format(book.getIssuedDate()));
        } else {
            holder.requestDateText.setText("N/A");
        }
        
        holder.approvedByText.setText(book.getApprovedBy() != null ? book.getApprovedBy() : 
                                       (book.getIssuedBy() != null ? book.getIssuedBy() : "N/A"));
        
        // Alternate row colors
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }
    }

    @Override
    public int getItemCount() {
        return chequeBookList.size();
    }

    static class ChequeBookViewHolder extends RecyclerView.ViewHolder {
        TextView bookNumberText;
        TextView accountText;
        TextView customerText;
        TextView statusText;
        TextView totalLeavesText;
        TextView remainingText;
        TextView requestDateText;
        TextView approvedByText;

        public ChequeBookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookNumberText = itemView.findViewById(R.id.bookNumberText);
            accountText = itemView.findViewById(R.id.accountText);
            customerText = itemView.findViewById(R.id.customerText);
            statusText = itemView.findViewById(R.id.statusText);
            totalLeavesText = itemView.findViewById(R.id.totalLeavesText);
            remainingText = itemView.findViewById(R.id.remainingText);
            requestDateText = itemView.findViewById(R.id.requestDateText);
            approvedByText = itemView.findViewById(R.id.approvedByText);
        }
    }
}
