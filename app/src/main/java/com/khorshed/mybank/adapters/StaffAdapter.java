package com.khorshed.mybank.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Staff;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<Staff> staffList;
    private List<Staff> staffListFull;
    private OnStaffClickListener listener;

    public interface OnStaffClickListener {
        void onStaffClick(Staff staff, int position);
        void onStaffSelectionChanged(List<Staff> selectedStaff);
    }

    public StaffAdapter(List<Staff> staffList, OnStaffClickListener listener) {
        this.staffList = staffList;
        this.staffListFull = new ArrayList<>(staffList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        Staff staff = staffList.get(position);
        holder.bind(staff, position);
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public void filter(String query) {
        staffList.clear();
        if (query.isEmpty()) {
            staffList.addAll(staffListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Staff staff : staffListFull) {
                if (staff.getUsername().toLowerCase().contains(lowerCaseQuery) ||
                    staff.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                    staff.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    staffList.add(staff);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        staffList.clear();
        if (status.equals("All") || status.isEmpty()) {
            staffList.addAll(staffListFull);
        } else {
            for (Staff staff : staffListFull) {
                if (staff.getStatus().equalsIgnoreCase(status)) {
                    staffList.add(staff);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<Staff> getSelectedStaff() {
        List<Staff> selected = new ArrayList<>();
        for (Staff staff : staffList) {
            if (staff.isSelected()) {
                selected.add(staff);
            }
        }
        return selected;
    }

    public void updateStaffList(List<Staff> newStaffList) {
        this.staffList = newStaffList;
        this.staffListFull = new ArrayList<>(newStaffList);
        notifyDataSetChanged();
    }

    class StaffViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private TextView idText, usernameText, fullNameText, emailText, roleText, statusText;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.staffCheckBox);
            idText = itemView.findViewById(R.id.staffIdText);
            usernameText = itemView.findViewById(R.id.staffUsernameText);
            fullNameText = itemView.findViewById(R.id.staffFullNameText);
            emailText = itemView.findViewById(R.id.staffEmailText);
            roleText = itemView.findViewById(R.id.staffRoleText);
            statusText = itemView.findViewById(R.id.staffStatusText);
        }

        public void bind(Staff staff, int position) {
            idText.setText(staff.getId());
            usernameText.setText(staff.getUsername());
            fullNameText.setText(staff.getFullName());
            emailText.setText(staff.getEmail());
            roleText.setText(staff.getRole());
            statusText.setText(staff.getStatus());

            // Set status color
            if (staff.getStatus().equalsIgnoreCase("Active")) {
                statusText.setTextColor(Color.parseColor("#4CAF50"));
            } else if (staff.getStatus().equalsIgnoreCase("Inactive")) {
                statusText.setTextColor(Color.parseColor("#F44336"));
            } else {
                statusText.setTextColor(Color.parseColor("#FF9800"));
            }

            checkBox.setChecked(staff.isSelected());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                staff.setSelected(isChecked);
                if (listener != null) {
                    listener.onStaffSelectionChanged(getSelectedStaff());
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStaffClick(staff, position);
                }
            });
        }
    }
}
