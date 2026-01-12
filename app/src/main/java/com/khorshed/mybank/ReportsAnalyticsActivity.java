package com.khorshed.mybank;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportsAnalyticsActivity extends AppCompatActivity {

    private MaterialButton backButton, generateReportButton, exportPdfButton, exportCsvButton;
    private Spinner reportTypeSpinner;
    private LinearLayout emptyStateLayout, reportContentLayout;
    private TextView reportTitleText, reportDataText;

    private FirebaseFirestore db;
    private String selectedReportType = "";
    private String currentReportData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_analytics);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Setup report type spinner
        setupReportTypeSpinner();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        generateReportButton = findViewById(R.id.generateReportButton);
        exportPdfButton = findViewById(R.id.exportPdfButton);
        exportCsvButton = findViewById(R.id.exportCsvButton);
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        reportContentLayout = findViewById(R.id.reportContentLayout);
        reportTitleText = findViewById(R.id.reportTitleText);
        reportDataText = findViewById(R.id.reportDataText);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        generateReportButton.setOnClickListener(v -> generateReport());
        exportPdfButton.setOnClickListener(v -> exportToPdf());
        exportCsvButton.setOnClickListener(v -> exportToCsv());
    }

    private void setupReportTypeSpinner() {
        String[] reportTypes = {
            "Daily Transaction Reports",
            "Weekly Activity Reports",
            "Monthly Financial Reports",
            "Account Status Reports",
            "Staff Activity Reports",
            "All Transactions",
            "User Summary"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, reportTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);

        reportTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReportType = reportTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void generateReport() {
        if (selectedReportType.isEmpty()) {
            Toast.makeText(this, "Please select a report type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Generating report...", Toast.LENGTH_SHORT).show();

        switch (selectedReportType) {
            case "Daily Transaction Reports":
                generateDailyTransactionReport();
                break;
            case "Weekly Activity Reports":
                generateWeeklyActivityReport();
                break;
            case "Monthly Financial Reports":
                generateMonthlyFinancialReport();
                break;
            case "Account Status Reports":
                generateAccountStatusReport();
                break;
            case "Staff Activity Reports":
                generateStaffActivityReport();
                break;
            case "All Transactions":
                generateAllTransactionsReport();
                break;
            case "User Summary":
                generateUserSummaryReport();
                break;
            default:
                Toast.makeText(this, "Report type not implemented", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void generateDailyTransactionReport() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTransactions = 0;
                    double totalAmount = 0.0;
                    int deposits = 0;
                    int withdrawals = 0;
                    int transfers = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Object timestampObj = doc.get("timestamp");
                        String transactionDate = formatDate(timestampObj);
                        
                        if (transactionDate.startsWith(today)) {
                            totalTransactions++;
                            Double amount = doc.getDouble("amount");
                            if (amount != null) {
                                totalAmount += amount;
                            }
                            
                            String type = doc.getString("type");
                            if ("Deposit".equalsIgnoreCase(type)) deposits++;
                            else if ("Withdraw".equalsIgnoreCase(type)) withdrawals++;
                            else if ("Transfer".equalsIgnoreCase(type)) transfers++;
                        }
                    }

                    StringBuilder report = new StringBuilder();
                    report.append("Report Date: ").append(today).append("\n\n");
                    report.append("SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Total Transactions: ").append(totalTransactions).append("\n");
                    report.append("Total Amount: ৳").append(String.format("%.2f", totalAmount)).append("\n\n");
                    report.append("BREAKDOWN BY TYPE\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Deposits: ").append(deposits).append("\n");
                    report.append("Withdrawals: ").append(withdrawals).append("\n");
                    report.append("Transfers: ").append(transfers).append("\n");

                    currentReportData = report.toString();
                    displayReport("Daily Transaction Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateWeeklyActivityReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        String weekAgo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTransactions = queryDocumentSnapshots.size();
                    
                    StringBuilder report = new StringBuilder();
                    report.append("Period: ").append(weekAgo).append(" to ").append(today).append("\n\n");
                    report.append("WEEKLY ACTIVITY SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Total Transactions (Last 7 Days): ").append(totalTransactions).append("\n");
                    report.append("Average per Day: ").append(totalTransactions / 7).append("\n");

                    currentReportData = report.toString();
                    displayReport("Weekly Activity Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateMonthlyFinancialReport() {
        Calendar calendar = Calendar.getInstance();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.getTime());

        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0.0;
                    double totalExpense = 0.0;
                    int transactionCount = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Object timestampObj = doc.get("timestamp");
                        String transactionDate = formatDate(timestampObj);
                        
                        if (transactionDate.startsWith(currentMonth)) {
                            transactionCount++;
                            Double amount = doc.getDouble("amount");
                            String type = doc.getString("type");
                            
                            if (amount != null) {
                                if ("Deposit".equalsIgnoreCase(type)) {
                                    totalIncome += amount;
                                } else {
                                    totalExpense += amount;
                                }
                            }
                        }
                    }

                    StringBuilder report = new StringBuilder();
                    report.append("Month: ").append(currentMonth).append("\n\n");
                    report.append("MONTHLY FINANCIAL SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Total Transactions: ").append(transactionCount).append("\n");
                    report.append("Total Income: ৳").append(String.format("%.2f", totalIncome)).append("\n");
                    report.append("Total Expenses: ৳").append(String.format("%.2f", totalExpense)).append("\n");
                    report.append("Net Balance: ৳").append(String.format("%.2f", totalIncome - totalExpense)).append("\n");

                    currentReportData = report.toString();
                    displayReport("Monthly Financial Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateAccountStatusReport() {
        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAccounts = 0;
                    int activeAccounts = 0;
                    int blockedAccounts = 0;
                    int pendingAccounts = 0;
                    double totalBalance = 0.0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        totalAccounts++;
                        String status = doc.getString("status");
                        Double balance = doc.getDouble("balance");
                        
                        if (balance != null) {
                            totalBalance += balance;
                        }
                        
                        if ("Active".equalsIgnoreCase(status)) activeAccounts++;
                        else if ("Blocked".equalsIgnoreCase(status)) blockedAccounts++;
                        else if ("Pending".equalsIgnoreCase(status)) pendingAccounts++;
                    }

                    StringBuilder report = new StringBuilder();
                    report.append("ACCOUNT STATUS SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Total Accounts: ").append(totalAccounts).append("\n");
                    report.append("Active Accounts: ").append(activeAccounts).append("\n");
                    report.append("Blocked Accounts: ").append(blockedAccounts).append("\n");
                    report.append("Pending Accounts: ").append(pendingAccounts).append("\n\n");
                    report.append("Total Balance: ৳").append(String.format("%.2f", totalBalance)).append("\n");

                    currentReportData = report.toString();
                    displayReport("Account Status Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateStaffActivityReport() {
        db.collection("users")
                .whereEqualTo("role", "STAFF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalStaff = 0;
                    int activeStaff = 0;
                    int inactiveStaff = 0;

                    StringBuilder report = new StringBuilder();
                    report.append("STAFF ACTIVITY SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        totalStaff++;
                        String status = doc.getString("status");
                        Boolean isActive = doc.getBoolean("isActive");
                        
                        if ("Active".equalsIgnoreCase(status) || (isActive != null && isActive)) {
                            activeStaff++;
                        } else {
                            inactiveStaff++;
                        }
                    }

                    report.append("Total Staff Members: ").append(totalStaff).append("\n");
                    report.append("Active Staff: ").append(activeStaff).append("\n");
                    report.append("Inactive Staff: ").append(inactiveStaff).append("\n");

                    currentReportData = report.toString();
                    displayReport("Staff Activity Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateAllTransactionsReport() {
        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalCount = queryDocumentSnapshots.size();
                    double totalAmount = 0.0;
                    
                    Map<String, Integer> typeCount = new HashMap<>();
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Double amount = doc.getDouble("amount");
                        if (amount != null) {
                            totalAmount += amount;
                        }
                        
                        String type = doc.getString("type");
                        if (type != null) {
                            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
                        }
                    }

                    StringBuilder report = new StringBuilder();
                    report.append("ALL TRANSACTIONS SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Total Transactions: ").append(totalCount).append("\n");
                    report.append("Total Amount: ৳").append(String.format("%.2f", totalAmount)).append("\n\n");
                    report.append("BREAKDOWN BY TYPE\n");
                    report.append("═══════════════════════════════\n\n");
                    
                    for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                        report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }

                    currentReportData = report.toString();
                    displayReport("All Transactions Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateUserSummaryReport() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = 0;
                    int admins = 0;
                    int staff = 0;
                    int customers = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        totalUsers++;
                        String role = doc.getString("role");
                        
                        if ("ADMIN".equalsIgnoreCase(role)) admins++;
                        else if ("STAFF".equalsIgnoreCase(role)) staff++;
                        else customers++;
                    }

                    StringBuilder report = new StringBuilder();
                    report.append("USER SUMMARY\n");
                    report.append("═══════════════════════════════\n\n");
                    report.append("Total Users: ").append(totalUsers).append("\n");
                    report.append("Administrators: ").append(admins).append("\n");
                    report.append("Staff Members: ").append(staff).append("\n");
                    report.append("Customers: ").append(customers).append("\n");

                    currentReportData = report.toString();
                    displayReport("User Summary Report", currentReportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generating report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void displayReport(String title, String data) {
        emptyStateLayout.setVisibility(View.GONE);
        reportContentLayout.setVisibility(View.VISIBLE);
        reportTitleText.setText(title);
        reportDataText.setText(data);
    }

    private void exportToPdf() {
        if (currentReportData.isEmpty()) {
            Toast.makeText(this, "Please generate a report first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "PDF Export - Feature coming soon!\nReport data is ready for export",
                Toast.LENGTH_LONG).show();
    }

    private void exportToCsv() {
        if (currentReportData.isEmpty()) {
            Toast.makeText(this, "Please generate a report first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "CSV Export - Feature coming soon!\nReport data is ready for export",
                Toast.LENGTH_LONG).show();
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return "";
        }
        
        try {
            if (dateObj instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) dateObj).toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.format(date);
            } else if (dateObj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.format((Date) dateObj);
            } else if (dateObj instanceof Long) {
                Date date = new Date((Long) dateObj);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "";
    }
}
