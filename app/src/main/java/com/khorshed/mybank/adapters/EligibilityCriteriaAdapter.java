
package com.khorshed.mybank.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.ChequeEligibilityCriteria;

import java.util.List;
import java.util.Locale;

public class EligibilityCriteriaAdapter extends RecyclerView.Adapter<EligibilityCriteriaAdapter.CriteriaViewHolder> {

    private List<ChequeEligibilityCriteria> criteriaList;
    private OnCriteriaClickListener clickListener;
    private int selectedPosition = -1;

    public interface OnCriteriaClickListener {
        void onCriteriaClick(ChequeEligibilityCriteria criteria, int position);
    }

    public EligibilityCriteriaAdapter(List<ChequeEligibilityCriteria> criteriaList, OnCriteriaClickListener listener) {
        this.criteriaList = criteriaList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public CriteriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_eligibility_criteria_row, parent, false);
        return new CriteriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CriteriaViewHolder holder, int position) {
        ChequeEligibilityCriteria criteria = criteriaList.get(position);
        
        holder.accountTypeText.setText(criteria.getAccountType());
        holder.minBalanceText.setText(String.format(Locale.getDefault(), "%.1f", criteria.getMinBalance()));
        holder.minAgeDaysText.setText(String.valueOf(criteria.getMinAccountAgeDays()));
        holder.maxBooksText.setText(String.valueOf(criteria.getMaxBooksPerYear()));
        holder.leavesPerBookText.setText(String.valueOf(criteria.getLeavesPerBook()));
        
        // Active status with color
        holder.activeText.setText(criteria.isActive() ? "true" : "false");
        if (criteria.isActive()) {
            holder.activeText.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.activeText.setTextColor(Color.parseColor("#F44336"));
        }
        
        // Highlight selected row
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // Notify changes for previous and current selected items
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);
            
            if (clickListener != null) {
                clickListener.onCriteriaClick(criteria, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return criteriaList.size();
    }

    public void updateList(List<ChequeEligibilityCriteria> newList) {
        this.criteriaList = newList;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }
    
    public void clearSelection() {
        int previousPosition = selectedPosition;
        selectedPosition = -1;
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
    }
    
    public int getSelectedPosition() {
        return selectedPosition;
    }

    static class CriteriaViewHolder extends RecyclerView.ViewHolder {
        TextView accountTypeText;
        TextView minBalanceText;
        TextView minAgeDaysText;
        TextView maxBooksText;
        TextView leavesPerBookText;
        TextView activeText;

        public CriteriaViewHolder(@NonNull View itemView) {
            super(itemView);
            accountTypeText = itemView.findViewById(R.id.accountTypeText);
            minBalanceText = itemView.findViewById(R.id.minBalanceText);
            minAgeDaysText = itemView.findViewById(R.id.minAgeDaysText);
            maxBooksText = itemView.findViewById(R.id.maxBooksText);
            leavesPerBookText = itemView.findViewById(R.id.leavesPerBookText);
            activeText = itemView.findViewById(R.id.activeText);
        }
    }
}
