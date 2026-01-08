package com.khorshed.mybank.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.ChequeBookRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChequeRequestAdapter extends RecyclerView.Adapter<ChequeRequestAdapter.ViewHolder> {

    private List<ChequeBookRequest> requests;
    private ChequeBookRequest selectedRequest;
    private OnRequestSelectedListener listener;
    private SimpleDateFormat dateFormat;
    private int selectedPosition = -1;

    public interface OnRequestSelectedListener {
        void onRequestSelected(ChequeBookRequest request);
    }

    public ChequeRequestAdapter(OnRequestSelectedListener listener) {
        this.requests = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    public void setRequests(List<ChequeBookRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    public ChequeBookRequest getSelectedRequest() {
        return selectedRequest;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_cheque_request_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChequeBookRequest request = requests.get(position);
        
        // Book Number (Request ID)
        if (request.getRequestId() != null) {
            holder.bookNumberText.setText(request.getRequestId());
        } else {
            holder.bookNumberText.setText("N/A");
        }
        
        // Account Number
        if (request.getAccountNumber() != null) {
            holder.accountText.setText(request.getAccountNumber());
        } else {
            holder.accountText.setText("-");
        }
        
        // Customer Name
        if (request.getCustomerName() != null) {
            holder.customerNameText.setText(request.getCustomerName());
        } else {
            holder.customerNameText.setText("Unknown");
        }
        
        // Account Type
        if (request.getAccountType() != null) {
            holder.accountTypeText.setText(request.getAccountType());
        } else {
            holder.accountTypeText.setText("-");
        }
        
        // Balance
        holder.balanceText.setText(String.format(Locale.US, "%.1f", request.getBalance()));
        
        // Number of Leaves
        holder.leavesText.setText(String.valueOf(request.getNumberOfLeaves()));
        
        // Request Date
        if (request.getRequestDate() != null) {
            holder.requestDateText.setText(dateFormat.format(request.getRequestDate()));
        } else {
            holder.requestDateText.setText("N/A");
        }
        
        // Highlight selected row
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E0F2FE"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            selectedRequest = request;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onRequestSelected(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookNumberText;
        TextView accountText;
        TextView customerNameText;
        TextView accountTypeText;
        TextView balanceText;
        TextView leavesText;
        TextView requestDateText;

        ViewHolder(View itemView) {
            super(itemView);
            bookNumberText = itemView.findViewById(R.id.bookNumberText);
            accountText = itemView.findViewById(R.id.accountText);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            accountTypeText = itemView.findViewById(R.id.accountTypeText);
            balanceText = itemView.findViewById(R.id.balanceText);
            leavesText = itemView.findViewById(R.id.leavesText);
            requestDateText = itemView.findViewById(R.id.requestDateText);
        }
    }
}
