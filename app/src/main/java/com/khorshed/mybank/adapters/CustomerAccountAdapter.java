package com.khorshed.mybank.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.CustomerAccount;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerAccountAdapter extends RecyclerView.Adapter<CustomerAccountAdapter.AccountViewHolder> {

    private List<CustomerAccount> accountList;
    private List<CustomerAccount> filteredAccountList;

    public CustomerAccountAdapter(List<CustomerAccount> accountList) {
        this.accountList = accountList;
        this.filteredAccountList = new ArrayList<>(accountList);
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        CustomerAccount account = filteredAccountList.get(position);
        
        holder.accountNumberText.setText(account.getAccountNumber());
        holder.ownerNameText.setText(account.getCustomerName());
        holder.accountTypeText.setText(account.getAccountType());
        
        // Format balance with currency
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "BD"));
        String balanceFormatted = "৳" + String.format("%.2f", account.getBalance());
        holder.balanceText.setText(balanceFormatted);
        
        holder.statusText.setText(account.getStatus());
        
        // Set status color
        int statusColor;
        switch (account.getStatus().toLowerCase()) {
            case "active":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark);
                break;
            case "blocked":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark);
                break;
            case "pending":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark);
                break;
            default:
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray);
                break;
        }
        holder.statusText.setTextColor(statusColor);
        
        holder.createdDateText.setText(account.getCreatedDate());
        
        // Handle checkbox
        holder.accountCheckBox.setChecked(account.isSelected());
        holder.accountCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            account.setSelected(isChecked);
        });
        
        // Handle row click
        holder.itemView.setOnClickListener(v -> {
            account.setSelected(!account.isSelected());
            holder.accountCheckBox.setChecked(account.isSelected());
        });
    }

    @Override
    public int getItemCount() {
        return filteredAccountList.size();
    }

    // Filter by search query
    public void filter(String query) {
        filteredAccountList.clear();
        if (query.isEmpty()) {
            filteredAccountList.addAll(accountList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (CustomerAccount account : accountList) {
                if (account.getAccountNumber().toLowerCase().contains(lowerCaseQuery) ||
                    account.getCustomerName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredAccountList.add(account);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Filter by status
    public void filterByStatus(String status) {
        filteredAccountList.clear();
        if (status.equals("All") || status.isEmpty()) {
            filteredAccountList.addAll(accountList);
        } else {
            for (CustomerAccount account : accountList) {
                if (account.getStatus().equalsIgnoreCase(status)) {
                    filteredAccountList.add(account);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Get selected accounts
    public List<CustomerAccount> getSelectedAccounts() {
        List<CustomerAccount> selected = new ArrayList<>();
        for (CustomerAccount account : filteredAccountList) {
            if (account.isSelected()) {
                selected.add(account);
            }
        }
        return selected;
    }

    // Update data
    public void updateData(List<CustomerAccount> newAccountList) {
        this.accountList = newAccountList;
        this.filteredAccountList = new ArrayList<>(newAccountList);
        notifyDataSetChanged();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        CheckBox accountCheckBox;
        TextView accountNumberText;
        TextView ownerNameText;
        TextView accountTypeText;
        TextView balanceText;
        TextView statusText;
        TextView createdDateText;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountCheckBox = itemView.findViewById(R.id.accountCheckBox);
            accountNumberText = itemView.findViewById(R.id.accountNumberText);
            ownerNameText = itemView.findViewById(R.id.ownerNameText);
            accountTypeText = itemView.findViewById(R.id.accountTypeText);
            balanceText = itemView.findViewById(R.id.balanceText);
            statusText = itemView.findViewById(R.id.statusText);
            createdDateText = itemView.findViewById(R.id.createdDateText);
        }
    }
}
