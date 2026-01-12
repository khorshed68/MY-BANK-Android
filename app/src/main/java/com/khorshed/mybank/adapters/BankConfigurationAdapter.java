package com.khorshed.mybank.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.BankConfiguration;

import java.util.ArrayList;
import java.util.List;

public class BankConfigurationAdapter extends RecyclerView.Adapter<BankConfigurationAdapter.ConfigViewHolder> {

    private List<BankConfiguration> configList;
    private List<BankConfiguration> filteredConfigList;

    public BankConfigurationAdapter(List<BankConfiguration> configList) {
        this.configList = configList;
        this.filteredConfigList = new ArrayList<>(configList);
    }

    @NonNull
    @Override
    public ConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bank_config, parent, false);
        return new ConfigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfigViewHolder holder, int position) {
        BankConfiguration config = filteredConfigList.get(position);
        
        holder.keyText.setText(config.getKey());
        holder.valueEditText.setText(config.getValue());
        holder.categoryText.setText(config.getCategory());
        holder.descriptionText.setText(config.getDescription());

        // Add text watcher to update value when edited
        holder.valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                config.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public int getItemCount() {
        return filteredConfigList.size();
    }

    // Filter by category
    public void filterByCategory(String category) {
        filteredConfigList.clear();
        if (category.equals("All") || category.isEmpty()) {
            filteredConfigList.addAll(configList);
        } else {
            for (BankConfiguration config : configList) {
                if (config.getCategory().equalsIgnoreCase(category)) {
                    filteredConfigList.add(config);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Update data
    public void updateData(List<BankConfiguration> newConfigList) {
        this.configList = newConfigList;
        this.filteredConfigList = new ArrayList<>(newConfigList);
        notifyDataSetChanged();
    }

    // Get all configurations (for saving)
    public List<BankConfiguration> getAllConfigurations() {
        return configList;
    }

    static class ConfigViewHolder extends RecyclerView.ViewHolder {
        TextView keyText;
        EditText valueEditText;
        TextView categoryText;
        TextView descriptionText;

        public ConfigViewHolder(@NonNull View itemView) {
            super(itemView);
            keyText = itemView.findViewById(R.id.keyText);
            valueEditText = itemView.findViewById(R.id.valueEditText);
            categoryText = itemView.findViewById(R.id.categoryText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
        }
    }
}
