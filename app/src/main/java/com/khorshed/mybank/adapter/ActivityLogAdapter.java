package com.khorshed.mybank.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.ActivityLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ViewHolder> {

    private List<ActivityLog> activityLogs;
    private List<ActivityLog> filteredLogs;
    private SimpleDateFormat dateFormat;

    public ActivityLogAdapter() {
        this.activityLogs = new ArrayList<>();
        this.filteredLogs = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US);
    }

    public void setActivityLogs(List<ActivityLog> logs) {
        this.activityLogs = logs;
        this.filteredLogs = new ArrayList<>(logs);
        notifyDataSetChanged();
    }

    public void filter(String staffFilter, String actionFilter) {
        filteredLogs.clear();
        
        for (ActivityLog log : activityLogs) {
            boolean matchesStaff = staffFilter == null || staffFilter.equals("All Staff") || 
                (log.getStaffMemberName() != null && log.getStaffMemberName().equals(staffFilter));
            
            boolean matchesAction = actionFilter == null || actionFilter.equals("All Actions") || 
                (log.getAction() != null && log.getAction().equals(actionFilter));
            
            if (matchesStaff && matchesAction) {
                filteredLogs.add(log);
            }
        }
        
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_activity_log_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityLog log = filteredLogs.get(position);
        
        // Timestamp
        if (log.getTimestamp() != null) {
            holder.timestampText.setText(dateFormat.format(log.getTimestamp()));
        } else {
            holder.timestampText.setText("N/A");
        }
        
        // Staff Member
        if (log.getStaffMemberName() != null) {
            holder.staffMemberText.setText(log.getStaffMemberName());
        } else {
            holder.staffMemberText.setText("Unknown");
        }
        
        // Action
        if (log.getAction() != null) {
            holder.actionText.setText(log.getAction());
        } else {
            holder.actionText.setText("N/A");
        }
        
        // Target Account
        if (log.getTargetAccount() != null && !log.getTargetAccount().isEmpty()) {
            holder.targetAccountText.setText(log.getTargetAccount());
        } else {
            holder.targetAccountText.setText("-");
        }
        
        // Details
        if (log.getDetails() != null) {
            holder.detailsText.setText(log.getDetails());
        } else {
            holder.detailsText.setText("No details available");
        }
    }

    @Override
    public int getItemCount() {
        return filteredLogs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestampText;
        TextView staffMemberText;
        TextView actionText;
        TextView targetAccountText;
        TextView detailsText;

        ViewHolder(View itemView) {
            super(itemView);
            timestampText = itemView.findViewById(R.id.timestampText);
            staffMemberText = itemView.findViewById(R.id.staffMemberText);
            actionText = itemView.findViewById(R.id.actionText);
            targetAccountText = itemView.findViewById(R.id.targetAccountText);
            detailsText = itemView.findViewById(R.id.detailsText);
        }
    }
}
