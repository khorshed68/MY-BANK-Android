package com.khorshed.mybank.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.AuditLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.AuditLogViewHolder> {

    private List<AuditLog> auditLogs;
    private List<AuditLog> auditLogsFiltered;

    public AuditLogAdapter(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
        this.auditLogsFiltered = new ArrayList<>(auditLogs);
    }

    @NonNull
    @Override
    public AuditLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audit_log, parent, false);
        return new AuditLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditLogViewHolder holder, int position) {
        AuditLog log = auditLogsFiltered.get(position);

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = "";
        if (log.getTimestamp() != null) {
            Date date = log.getTimestamp().toDate();
            formattedDate = sdf.format(date);
        }
        holder.timestampText.setText(formattedDate);

        holder.userTypeText.setText(log.getUserType() != null ? log.getUserType() : "");
        holder.usernameText.setText(log.getUsername() != null ? log.getUsername() : "");
        holder.actionText.setText(log.getAction() != null ? log.getAction() : "");
        holder.moduleText.setText(log.getModule() != null ? log.getModule() : "");
        holder.detailsText.setText(log.getDetails() != null ? log.getDetails() : "");
        holder.statusText.setText(log.getStatus() != null ? log.getStatus() : "");

        // Set status color
        if (log.getStatus() != null) {
            if (log.getStatus().equals("SUCCESS")) {
                holder.statusText.setTextColor(0xFF4CAF50); // Green
            } else if (log.getStatus().equals("FAILED")) {
                holder.statusText.setTextColor(0xFFF44336); // Red
            }
        }

        // Set user type color
        if (log.getUserType() != null) {
            if (log.getUserType().equals("ADMIN")) {
                holder.userTypeText.setTextColor(0xFF673AB7); // Purple
            } else if (log.getUserType().equals("STAFF")) {
                holder.userTypeText.setTextColor(0xFF2196F3); // Blue
            } else if (log.getUserType().equals("CUSTOMER")) {
                holder.userTypeText.setTextColor(0xFF4CAF50); // Green
            }
        }
    }

    @Override
    public int getItemCount() {
        return auditLogsFiltered.size();
    }

    public void filter(String query, String filterType) {
        auditLogsFiltered.clear();

        if (query.isEmpty() && filterType.equals("All")) {
            auditLogsFiltered.addAll(auditLogs);
        } else {
            for (AuditLog log : auditLogs) {
                boolean matchesQuery = false;
                boolean matchesFilter = false;

                // Check query match
                if (query.isEmpty()) {
                    matchesQuery = true;
                } else {
                    String lowerCaseQuery = query.toLowerCase();
                    if ((log.getUsername() != null && log.getUsername().toLowerCase().contains(lowerCaseQuery)) ||
                        (log.getDetails() != null && log.getDetails().toLowerCase().contains(lowerCaseQuery)) ||
                        (log.getAction() != null && log.getAction().toLowerCase().contains(lowerCaseQuery))) {
                        matchesQuery = true;
                    }
                }

                // Check filter match
                if (filterType.equals("All")) {
                    matchesFilter = true;
                } else {
                    // Check if filter matches userType, action, or status
                    if ((log.getUserType() != null && log.getUserType().equals(filterType)) ||
                        (log.getAction() != null && log.getAction().equals(filterType)) ||
                        (log.getStatus() != null && log.getStatus().equals(filterType))) {
                        matchesFilter = true;
                    }
                }

                if (matchesQuery && matchesFilter) {
                    auditLogsFiltered.add(log);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void updateData(List<AuditLog> newLogs) {
        this.auditLogs = newLogs;
        this.auditLogsFiltered = new ArrayList<>(newLogs);
        notifyDataSetChanged();
    }

    static class AuditLogViewHolder extends RecyclerView.ViewHolder {
        TextView timestampText, userTypeText, usernameText, actionText;
        TextView moduleText, detailsText, statusText;

        public AuditLogViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampText = itemView.findViewById(R.id.timestampText);
            userTypeText = itemView.findViewById(R.id.userTypeText);
            usernameText = itemView.findViewById(R.id.usernameText);
            actionText = itemView.findViewById(R.id.actionText);
            moduleText = itemView.findViewById(R.id.moduleText);
            detailsText = itemView.findViewById(R.id.detailsText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}
