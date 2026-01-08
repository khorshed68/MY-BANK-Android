package com.khorshed.mybank.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.AccountApplication;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountApplicationAdapter extends RecyclerView.Adapter<AccountApplicationAdapter.ViewHolder> {

    private List<AccountApplication> applications = new ArrayList<>();
    private OnApplicationActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "BD"));

    public interface OnApplicationActionListener {
        void onViewDetails(AccountApplication application);
        void onApprove(AccountApplication application);
        void onReject(AccountApplication application);
    }

    public AccountApplicationAdapter(OnApplicationActionListener listener) {
        this.listener = listener;
        currencyFormat.setMaximumFractionDigits(2);
    }

    public void setApplications(List<AccountApplication> applications) {
        this.applications = applications != null ? applications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<AccountApplication> getApplications() {
        return applications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_application_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountApplication application = applications.get(position);
        
        // Safely display application ID
        String appId = application.getApplicationId();
        if (appId != null && appId.length() > 8) {
            holder.tvApplicationId.setText("ID: #" + appId.substring(0, 8));
        } else if (appId != null) {
            holder.tvApplicationId.setText("ID: #" + appId);
        } else {
            holder.tvApplicationId.setText("ID: #N/A");
        }
        
        holder.tvName.setText(application.getName() != null ? application.getName() : "N/A");
        holder.tvPhone.setText(application.getPhone() != null ? application.getPhone() : "N/A");
        holder.tvAccountType.setText(application.getAccountType() != null ? application.getAccountType() : "N/A");
        
        // Format currency
        String deposit = currencyFormat.format(application.getInitialDeposit())
                .replace("BDT", "৳");
        holder.tvInitialDeposit.setText(deposit);
        
        // Format date
        if (application.getSubmittedAt() != null) {
            holder.tvRequestDate.setText(dateFormat.format(application.getSubmittedAt()));
        } else {
            holder.tvRequestDate.setText("N/A");
        }
        
        // Set status with appropriate background
        String status = application.getStatus();
        if (status == null) {
            status = "PENDING";
        }
        holder.tvStatus.setText(status);
        switch (status) {
            case "PENDING":
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
                break;
            case "APPROVED":
                holder.tvStatus.setBackgroundResource(R.drawable.status_approved_bg);
                break;
            case "REJECTED":
                holder.tvStatus.setBackgroundResource(R.drawable.status_rejected_bg);
                break;
        }
        
        // Show/hide action buttons based on status
        if ("PENDING".equals(status)) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
        
        // Set click listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(application);
            }
        });
        
        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApprove(application);
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(application);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvApplicationId, tvName, tvPhone, tvAccountType;
        TextView tvInitialDeposit, tvRequestDate, tvStatus;
        LinearLayout actionButtonsLayout;
        Button btnViewDetails, btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvApplicationId = itemView.findViewById(R.id.tvApplicationId);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAccountType = itemView.findViewById(R.id.tvAccountType);
            tvInitialDeposit = itemView.findViewById(R.id.tvInitialDeposit);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
