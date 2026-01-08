package com.khorshed.mybank.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.CustomerAccount;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerAccountAdapter extends RecyclerView.Adapter<CustomerAccountAdapter.ViewHolder> {

    private List<CustomerAccount> customerAccounts;
    private List<CustomerAccount> filteredAccounts;
    private OnCustomerClickListener listener;

    public interface OnCustomerClickListener {
        void onCustomerClick(CustomerAccount account);
    }

    public CustomerAccountAdapter(OnCustomerClickListener listener) {
        this.customerAccounts = new ArrayList<>();
        this.filteredAccounts = new ArrayList<>();
        this.listener = listener;
    }

    public void setCustomerAccounts(List<CustomerAccount> accounts) {
        this.customerAccounts = accounts;
        this.filteredAccounts = new ArrayList<>(accounts);
        notifyDataSetChanged();
    }

    public void filter(String query, String status) {
        filteredAccounts.clear();
        
        if (query == null) query = "";
        query = query.toLowerCase().trim();
        
        for (CustomerAccount account : customerAccounts) {
            boolean matchesQuery = query.isEmpty() || 
                (account.getCustomerName() != null && account.getCustomerName().toLowerCase().contains(query)) ||
                (account.getEmail() != null && account.getEmail().toLowerCase().contains(query)) ||
                (account.getPhone() != null && account.getPhone().contains(query)) ||
                (account.getAccountNumber() != null && account.getAccountNumber().contains(query));
            
            boolean matchesStatus = status == null || status.equals("All") || 
                (account.getStatus() != null && account.getStatus().equals(status));
            
            if (matchesQuery && matchesStatus) {
                filteredAccounts.add(account);
            }
        }
        
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_customer_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerAccount account = filteredAccounts.get(position);
        
        // Account number (show row number)
        holder.accountNumberText.setText(String.valueOf(position + 1));
        
        // Customer name
        if (account.getCustomerName() != null) {
            holder.customerNameText.setText(account.getCustomerName());
        } else {
            holder.customerNameText.setText("N/A");
        }
        
        // Email (truncate if needed)
        if (account.getEmail() != null) {
            String email = account.getEmail();
            if (email.length() > 25) {
                email = email.substring(0, 22) + "...";
            }
            holder.emailText.setText(email);
        } else {
            holder.emailText.setText("N/A");
        }
        
        // Phone
        if (account.getPhone() != null) {
            holder.phoneText.setText(account.getPhone());
        } else {
            holder.phoneText.setText("N/A");
        }
        
        // Account type
        if (account.getAccountType() != null) {
            holder.accountTypeText.setText(account.getAccountType());
        } else {
            holder.accountTypeText.setText("N/A");
        }
        
        // Balance
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "BD"));
        String balanceStr = "৳" + String.format(Locale.US, "%.2f", account.getBalance());
        holder.balanceText.setText(balanceStr);
        
        // Status badge
        String status = account.getStatus() != null ? account.getStatus() : "UNKNOWN";
        holder.statusBadge.setText(status);
        
        // Status color
        switch (status) {
            case "ACTIVE":
                holder.statusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "BLOCKED":
            case "FROZEN":
                holder.statusBadge.setBackgroundColor(Color.parseColor("#F44336")); // Red
                break;
            case "PENDING":
                holder.statusBadge.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                break;
            default:
                holder.statusBadge.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
                break;
        }
        
        // Created date
        if (account.getCreatedDate() != null) {
            String date = account.getCreatedDate();
            // Truncate to show only date and time (remove seconds if present)
            if (date.length() > 16) {
                date = date.substring(0, 16);
            }
            holder.createdDateText.setText(date);
        } else {
            holder.createdDateText.setText("N/A");
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCustomerClick(account);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredAccounts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView accountNumberText;
        TextView customerNameText;
        TextView emailText;
        TextView phoneText;
        TextView accountTypeText;
        TextView balanceText;
        TextView statusBadge;
        TextView createdDateText;

        ViewHolder(View itemView) {
            super(itemView);
            accountNumberText = itemView.findViewById(R.id.accountNumberText);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            emailText = itemView.findViewById(R.id.emailText);
            phoneText = itemView.findViewById(R.id.phoneText);
            accountTypeText = itemView.findViewById(R.id.accountTypeText);
            balanceText = itemView.findViewById(R.id.balanceText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            createdDateText = itemView.findViewById(R.id.createdDateText);
        }
    }
}
