package com.khorshed.mybank.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Admin;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    private List<Admin> adminList;
    private OnAdminClickListener listener;
    private int selectedPosition = -1;

    public interface OnAdminClickListener {
        void onAdminClick(Admin admin);
    }

    public AdminAdapter(List<Admin> adminList, OnAdminClickListener listener) {
        this.adminList = adminList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Admin admin = adminList.get(position);
        
        // Display ID (position + 1)
        holder.adminIdText.setText(String.valueOf(position + 1));
        
        // Display username
        holder.usernameText.setText(admin.getUsername() != null ? admin.getUsername() : "N/A");
        
        // Display full name
        holder.fullNameText.setText(admin.getFullName() != null ? admin.getFullName() : "N/A");
        
        // Display email
        holder.emailText.setText(admin.getEmail() != null ? admin.getEmail() : "N/A");
        
        // Display status with color
        String status = admin.getStatus() != null ? admin.getStatus() : "UNKNOWN";
        holder.statusText.setText(status);
        if ("ACTIVE".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("SUSPENDED".equals(status)) {
            holder.statusText.setTextColor(Color.parseColor("#FF9800"));
        } else {
            holder.statusText.setTextColor(Color.parseColor("#9E9E9E"));
        }
        
        // Display role with color
        String role = admin.getRole() != null ? admin.getRole() : "ADMIN";
        holder.roleText.setText(role.replace("_", " "));
        if ("SUPER_ADMIN".equals(role)) {
            holder.roleText.setTextColor(Color.parseColor("#6A1B9A"));
            holder.roleText.setTextSize(11);
        } else {
            holder.roleText.setTextColor(Color.parseColor("#1976D2"));
            holder.roleText.setTextSize(11);
        }
        
        // Highlight selected item
        if (selectedPosition == position) {
            holder.itemLayout.setBackgroundColor(Color.parseColor("#E1BEE7"));
        } else {
            holder.itemLayout.setBackgroundColor(Color.WHITE);
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // Notify changes
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onAdminClick(admin);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public void clearSelection() {
        int previous = selectedPosition;
        selectedPosition = -1;
        if (previous != -1) {
            notifyItemChanged(previous);
        }
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView adminIdText;
        TextView usernameText;
        TextView fullNameText;
        TextView emailText;
        TextView statusText;
        TextView roleText;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            adminIdText = itemView.findViewById(R.id.adminIdText);
            usernameText = itemView.findViewById(R.id.usernameText);
            fullNameText = itemView.findViewById(R.id.fullNameText);
            emailText = itemView.findViewById(R.id.emailText);
            statusText = itemView.findViewById(R.id.statusText);
            roleText = itemView.findViewById(R.id.roleText);
        }
    }
}
