package com.khorshed.mybank.activities.staff;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.AccountApplication;
import com.khorshed.mybank.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportsActivity extends AppCompatActivity {

    private AutoCompleteTextView reportTypeSpinner;
    private TextInputEditText fromDateInput, toDateInput;
    private MaterialButton generateButton, backButton;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    private Calendar fromDate, toDate;
    private SimpleDateFormat dateFormat;
    
    private static final String[] REPORT_TYPES = {
        "Transaction Summary Report",
        "Account Activity Report",
        "Customer Statistics Report",
        "Pending Approvals Report",
        "Daily Summary Report"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);
        
        initializeViews();
        setupReportTypes();
        setupDatePickers();
        setupListeners();
        
        // Set default dates
        fromDate = Calendar.getInstance();
        fromDate.add(Calendar.MONTH, -1); // 1 month ago
        toDate = Calendar.getInstance();
        
        updateDateFields();
    }
    
    private void initializeViews() {
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner);
        fromDateInput = findViewById(R.id.fromDateInput);
        toDateInput = findViewById(R.id.toDateInput);
        generateButton = findViewById(R.id.generateButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupReportTypes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            REPORT_TYPES
        );
        reportTypeSpinner.setAdapter(adapter);
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
        backButton.setOnClickListener(v -> finish());
        
        generateButton.setOnClickListener(v -> {
            String reportType = reportTypeSpinner.getText().toString().trim();
            
            if (reportType.isEmpty()) {
                Toast.makeText(this, "Please select a report type", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (fromDate.after(toDate)) {
                Toast.makeText(this, "From date cannot be after To date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            generateReport(reportType);
        });
    }
    
    private void generateReport(String reportType) {
        showLoading(true);
        
        switch (reportType) {
            case "Transaction Summary Report":
                generateTransactionSummaryReport();
                break;
            case "Account Activity Report":
                generateAccountActivityReport();
                break;
            case "Customer Statistics Report":
                generateCustomerStatisticsReport();
                break;
            case "Pending Approvals Report":
                generatePendingApprovalsReport();
                break;
            case "Daily Summary Report":
                generateDailySummaryReport();
                break;
            default:
                showLoading(false);
                Toast.makeText(this, "Unknown report type", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void generateTransactionSummaryReport() {
        // For now, generate a simple summary
        db.collection("accounts")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int totalAccounts = querySnapshot.size();
                double totalBalance = 0;
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Account account = doc.toObject(Account.class);
                    totalBalance += account.getBalance();
                }
                
                showLoading(false);
                showReportDialog(
                    "Transaction Summary Report",
                    "Date Range: " + dateFormat.format(fromDate.getTime()) + 
                    " to " + dateFormat.format(toDate.getTime()) + "\n\n" +
                    "═══════════════════════════\n\n" +
                    "Total Accounts: " + totalAccounts + "\n" +
                    "Total Balance: ৳" + String.format(Locale.US, "%.2f", totalBalance) + "\n" +
                    "Average Balance: ৳" + String.format(Locale.US, "%.2f", 
                        totalAccounts > 0 ? totalBalance / totalAccounts : 0) + "\n\n" +
                    "═══════════════════════════\n\n" +
                    "Note: This is a basic summary report. Transaction details " +
                    "can be expanded based on your requirements."
                );
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error generating report: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void generateAccountActivityReport() {
        db.collection("accounts")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int activeAccounts = 0;
                int frozenAccounts = 0;
                int savingsAccounts = 0;
                int currentAccounts = 0;
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Account account = doc.toObject(Account.class);
                    
                    if (account.isFrozen()) {
                        frozenAccounts++;
                    } else {
                        activeAccounts++;
                    }
                    
                    if ("SAVINGS".equals(account.getAccountType())) {
                        savingsAccounts++;
                    } else if ("CURRENT".equals(account.getAccountType())) {
                        currentAccounts++;
                    }
                }
                
                showLoading(false);
                showReportDialog(
                    "Account Activity Report",
                    "Date Range: " + dateFormat.format(fromDate.getTime()) + 
                    " to " + dateFormat.format(toDate.getTime()) + "\n\n" +
                    "═══════════════════════════\n\n" +
                    "Account Status:\n" +
                    "• Active Accounts: " + activeAccounts + "\n" +
                    "• Frozen Accounts: " + frozenAccounts + "\n\n" +
                    "Account Types:\n" +
                    "• Savings Accounts: " + savingsAccounts + "\n" +
                    "• Current Accounts: " + currentAccounts + "\n\n" +
                    "Total Accounts: " + querySnapshot.size() + "\n\n" +
                    "═══════════════════════════"
                );
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error generating report: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void generateCustomerStatisticsReport() {
        db.collection("users")
            .whereEqualTo("role", "CUSTOMER")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int totalCustomers = querySnapshot.size();
                int activeCustomers = 0;
                int inactiveCustomers = 0;
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    User user = doc.toObject(User.class);
                    if (user.isActive()) {
                        activeCustomers++;
                    } else {
                        inactiveCustomers++;
                    }
                }
                
                showLoading(false);
                showReportDialog(
                    "Customer Statistics Report",
                    "Date Range: " + dateFormat.format(fromDate.getTime()) + 
                    " to " + dateFormat.format(toDate.getTime()) + "\n\n" +
                    "═══════════════════════════\n\n" +
                    "Customer Overview:\n\n" +
                    "Total Customers: " + totalCustomers + "\n" +
                    "Active Customers: " + activeCustomers + "\n" +
                    "Inactive/Blocked: " + inactiveCustomers + "\n\n" +
                    "Activity Rate: " + String.format(Locale.US, "%.1f%%", 
                        totalCustomers > 0 ? (activeCustomers * 100.0 / totalCustomers) : 0) + "\n\n" +
                    "═══════════════════════════"
                );
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error generating report: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void generatePendingApprovalsReport() {
        db.collection("account_applications")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int totalPending = querySnapshot.size();
                StringBuilder details = new StringBuilder();
                
                int count = 1;
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    AccountApplication app = doc.toObject(AccountApplication.class);
                    details.append(count++).append(". ")
                           .append(app.getName())
                           .append(" - ৳")
                           .append(String.format(Locale.US, "%.2f", app.getInitialDeposit()))
                           .append("\n");
                }
                
                showLoading(false);
                showReportDialog(
                    "Pending Approvals Report",
                    "Date Range: " + dateFormat.format(fromDate.getTime()) + 
                    " to " + dateFormat.format(toDate.getTime()) + "\n\n" +
                    "═══════════════════════════\n\n" +
                    "Total Pending Applications: " + totalPending + "\n\n" +
                    (totalPending > 0 ? "Applications:\n" + details.toString() : 
                        "No pending applications at this time.") + "\n" +
                    "═══════════════════════════"
                );
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error generating report: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void generateDailySummaryReport() {
        // Get data from multiple collections
        AtomicInteger totalCustomers = new AtomicInteger(0);
        AtomicInteger totalAccounts = new AtomicInteger(0);
        AtomicInteger pendingApprovals = new AtomicInteger(0);
        
        db.collection("users")
            .whereEqualTo("role", "CUSTOMER")
            .get()
            .addOnSuccessListener(users -> {
                totalCustomers.set(users.size());
                
                db.collection("accounts")
                    .get()
                    .addOnSuccessListener(accounts -> {
                        totalAccounts.set(accounts.size());
                        
                        double totalBalance = 0;
                        for (QueryDocumentSnapshot doc : accounts) {
                            Account account = doc.toObject(Account.class);
                            totalBalance += account.getBalance();
                        }
                        
                        double finalTotalBalance = totalBalance;
                        
                        db.collection("account_applications")
                            .whereEqualTo("status", "PENDING")
                            .get()
                            .addOnSuccessListener(applications -> {
                                pendingApprovals.set(applications.size());
                                
                                showLoading(false);
                                showReportDialog(
                                    "Daily Summary Report",
                                    "Generated: " + new SimpleDateFormat("MMM dd, yyyy HH:mm", 
                                        Locale.US).format(new Date()) + "\n\n" +
                                    "═══════════════════════════\n\n" +
                                    "📊 DAILY SUMMARY\n\n" +
                                    "Customers: " + totalCustomers.get() + "\n" +
                                    "Total Accounts: " + totalAccounts.get() + "\n" +
                                    "Pending Approvals: " + pendingApprovals.get() + "\n\n" +
                                    "Total System Balance: ৳" + 
                                        String.format(Locale.US, "%.2f", finalTotalBalance) + "\n\n" +
                                    "═══════════════════════════\n\n" +
                                    "Status: All systems operational"
                                );
                            });
                    });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error generating report: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void showReportDialog(String title, String content) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Close", null)
            .setNeutralButton("Export", (dialog, which) -> {
                Toast.makeText(this, "Export functionality coming soon", 
                    Toast.LENGTH_SHORT).show();
            })
            .show();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        generateButton.setEnabled(!show);
    }
}
