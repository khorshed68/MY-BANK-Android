package com.khorshed.mybank.activities.staff;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.R;
import com.khorshed.mybank.adapter.ActivityLogAdapter;
import com.khorshed.mybank.models.ActivityLog;
import com.khorshed.mybank.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StaffActivityLogsActivity extends AppCompatActivity {

    private AutoCompleteTextView staffFilterSpinner, actionFilterSpinner;
    private TextInputEditText fromDateInput, toDateInput;
    private Button refreshButton, backToDashboardButton;
    private RecyclerView activityLogsRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    
    private ActivityLogAdapter adapter;
    private FirebaseFirestore db;
    private Calendar fromDate, toDate;
    private SimpleDateFormat dateFormat;
    
    private String currentStaffFilter = "All Staff";
    private String currentActionFilter = "All Actions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_logs);
        
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);
        
        initializeViews();
        setupRecyclerView();
        setupDatePickers();
        setupListeners();
        
        // Set default dates (last 7 days)
        fromDate = Calendar.getInstance();
        fromDate.add(Calendar.DAY_OF_MONTH, -7);
        toDate = Calendar.getInstance();
        updateDateFields();
        
        loadStaffMembers();
        loadActions();
        loadActivityLogs();
    }
    
    private void initializeViews() {
        staffFilterSpinner = findViewById(R.id.staffFilterSpinner);
        actionFilterSpinner = findViewById(R.id.actionFilterSpinner);
        fromDateInput = findViewById(R.id.fromDateInput);
        toDateInput = findViewById(R.id.toDateInput);
        refreshButton = findViewById(R.id.refreshButton);
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        activityLogsRecyclerView = findViewById(R.id.activityLogsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }
    
    private void setupRecyclerView() {
        adapter = new ActivityLogAdapter();
        activityLogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityLogsRecyclerView.setAdapter(adapter);
    }
    
    private void setupDatePickers() {
        fromDateInput.setOnClickListener(v -> showDatePicker(true));
        toDateInput.setOnClickListener(v -> showDatePicker(false));
    }
    
    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = isFromDate ? fromDate : toDate;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                updateDateFields();
                loadActivityLogs();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void updateDateFields() {
        fromDateInput.setText(dateFormat.format(fromDate.getTime()));
        toDateInput.setText(dateFormat.format(toDate.getTime()));
    }
    
    private void setupListeners() {
        backToDashboardButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> {
            currentStaffFilter = "All Staff";
            currentActionFilter = "All Actions";
            staffFilterSpinner.setText("All Staff", false);
            actionFilterSpinner.setText("All Actions", false);
            loadActivityLogs();
        });
        
        staffFilterSpinner.setOnItemClickListener((parent, view, position, id) -> {
            currentStaffFilter = parent.getItemAtPosition(position).toString();
            applyFilters();
        });
        
        actionFilterSpinner.setOnItemClickListener((parent, view, position, id) -> {
            currentActionFilter = parent.getItemAtPosition(position).toString();
            applyFilters();
        });
    }
    
    private void loadStaffMembers() {
        db.collection("users")
            .whereIn("role", List.of("STAFF", "MANAGER", "TELLER", "OFFICER"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Set<String> staffNames = new HashSet<>();
                staffNames.add("All Staff");
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    User user = doc.toObject(User.class);
                    if (user.getName() != null) {
                        staffNames.add(user.getName());
                    }
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    new ArrayList<>(staffNames)
                );
                staffFilterSpinner.setAdapter(adapter);
            });
    }
    
    private void loadActions() {
        String[] actions = {
            "All Actions",
            "LOGIN",
            "LOGOUT",
            "CREATE_ACCOUNT",
            "APPROVE_APPLICATION",
            "REJECT_APPLICATION",
            "SUSPEND_ACCOUNT",
            "UNBLOCK_ACCOUNT",
            "PROCESS_DEPOSIT",
            "PROCESS_WITHDRAWAL",
            "PROCESS_TRANSFER",
            "VIEW_CUSTOMER",
            "GENERATE_REPORT"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            actions
        );
        actionFilterSpinner.setAdapter(adapter);
    }
    
    private void loadActivityLogs() {
        showLoading(true);
        
        // Query activity logs within date range
        Date startDate = fromDate.getTime();
        Date endDate = toDate.getTime();
        
        // Set end date to end of day
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endDate = endCal.getTime();
        
        db.collection("activity_logs")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(500)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<ActivityLog> logs = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    ActivityLog log = doc.toObject(ActivityLog.class);
                    log.setLogId(doc.getId());
                    logs.add(log);
                }
                
                showLoading(false);
                
                if (logs.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    adapter.setActivityLogs(logs);
                    applyFilters();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading activity logs: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void applyFilters() {
        adapter.filter(currentStaffFilter, currentActionFilter);
        showEmptyState(adapter.getItemCount() == 0);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        activityLogsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        activityLogsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    // Static method to log activity (call this from other activities)
    public static void logActivity(String action, String targetAccount, String details) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            
            // Get staff member name
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        String staffName = user != null ? user.getName() : "Unknown";
                        
                        // Create activity log
                        ActivityLog log = new ActivityLog(userId, staffName, action, 
                            targetAccount, details);
                        
                        // Save to Firestore
                        db.collection("activity_logs")
                            .add(log)
                            .addOnFailureListener(e -> {
                                // Silently fail - don't interrupt user flow
                            });
                    }
                });
        }
    }
}
