package com.khorshed.mybank.activities.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.khorshed.mybank.R;
import com.khorshed.mybank.adapters.AdminAdapter;
import com.khorshed.mybank.models.Admin;
import com.khorshed.mybank.models.AuditLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManagementActivity extends AppCompatActivity implements AdminAdapter.OnAdminClickListener {

    private RecyclerView adminsRecyclerView;
    private AdminAdapter adminAdapter;
    private ProgressBar loadingProgressBar;
    private LinearLayout noAdminsLayout;
    private Button createAdminButton, updateAdminButton, deleteAdminButton, resetPasswordButton;
    private ImageView backButton, refreshButton;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Admin> adminList;
    private Admin selectedAdmin;
    private String currentUserId;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        adminList = new ArrayList<>();
        
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }
        
        initializeViews();
        checkUserRole();
        setupRecyclerView();
        setupClickListeners();
        loadAdmins();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        adminsRecyclerView = findViewById(R.id.adminsRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        noAdminsLayout = findViewById(R.id.noAdminsText);
        createAdminButton = findViewById(R.id.createAdminButton);
        updateAdminButton = findViewById(R.id.updateAdminButton);
        deleteAdminButton = findViewById(R.id.deleteAdminButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        
        // Initially disable update/delete/reset buttons
        updateAdminButton.setEnabled(false);
        deleteAdminButton.setEnabled(false);
        resetPasswordButton.setEnabled(false);
    }

    private void checkUserRole() {
        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUserRole = documentSnapshot.getString("role");
                    
                    // Only SUPER_ADMIN or ADMIN can access this page
                    if (!"ADMIN".equals(currentUserRole) && !"SUPER_ADMIN".equals(currentUserRole)) {
                        Toast.makeText(this, "Access Denied: Super Admin Only", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
    }

    private void setupRecyclerView() {
        adminAdapter = new AdminAdapter(adminList, this);
        adminsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminsRecyclerView.setAdapter(adminAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> {
            selectedAdmin = null;
            updateButtonStates();
            loadAdmins();
        });
        
        createAdminButton.setOnClickListener(v -> showCreateAdminDialog());
        
        updateAdminButton.setOnClickListener(v -> {
            if (selectedAdmin != null) {
                showUpdateAdminDialog(selectedAdmin);
            }
        });
        
        deleteAdminButton.setOnClickListener(v -> {
            if (selectedAdmin != null) {
                showDeleteConfirmationDialog(selectedAdmin);
            }
        });
        
        resetPasswordButton.setOnClickListener(v -> {
            if (selectedAdmin != null) {
                showResetPasswordDialog(selectedAdmin);
            }
        });
    }

    private void loadAdmins() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        noAdminsLayout.setVisibility(View.GONE);
        adminsRecyclerView.setVisibility(View.GONE);
        
        // Query users collection where role is ADMIN
        db.collection("users")
            .whereEqualTo("role", "ADMIN")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                adminList.clear();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Convert User document to Admin object
                    String adminId = document.getId();
                    String username = document.getString("username");
                    String name = document.getString("name");
                    String email = document.getString("email");
                    Boolean isActive = document.getBoolean("isActive");
                    com.google.firebase.Timestamp createdAt = document.getTimestamp("createdAt");
                    
                    Admin admin = new Admin();
                    admin.setAdminId(adminId);
                    admin.setUsername(username != null ? username : "N/A");
                    admin.setFullName(name != null ? name : "N/A");
                    admin.setEmail(email != null ? email : "N/A");
                    admin.setStatus(isActive != null && isActive ? "ACTIVE" : "SUSPENDED");
                    admin.setRole("ADMIN");
                    if (createdAt != null) {
                        admin.setCreatedAt(createdAt.toDate());
                    }
                    
                    adminList.add(admin);
                }
                
                loadingProgressBar.setVisibility(View.GONE);
                
                if (adminList.isEmpty()) {
                    noAdminsLayout.setVisibility(View.VISIBLE);
                    adminsRecyclerView.setVisibility(View.GONE);
                } else {
                    noAdminsLayout.setVisibility(View.GONE);
                    adminsRecyclerView.setVisibility(View.VISIBLE);
                }
                
                adminAdapter.notifyDataSetChanged();
                android.util.Log.d("AdminManagement", "✅ Loaded " + adminList.size() + " admins");
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load admins: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("AdminManagement", "❌ Failed to load admins", e);
            });
    }

    private void showCreateAdminDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_admin);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        EditText usernameInput = dialog.findViewById(R.id.usernameInput);
        EditText fullNameInput = dialog.findViewById(R.id.fullNameInput);
        EditText emailInput = dialog.findViewById(R.id.emailInput);
        EditText passwordInput = dialog.findViewById(R.id.passwordInput);
        RadioGroup roleRadioGroup = dialog.findViewById(R.id.roleRadioGroup);
        Button createButton = dialog.findViewById(R.id.createButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        
        createButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            
            // Validate inputs
            if (TextUtils.isEmpty(username)) {
                usernameInput.setError("Username required");
                return;
            }
            if (TextUtils.isEmpty(fullName)) {
                fullNameInput.setError("Full name required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Password required");
                return;
            }
            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                return;
            }
            
            String role = roleRadioGroup.getCheckedRadioButtonId() == R.id.superAdminRadio ? 
                "SUPER_ADMIN" : "ADMIN";
            
            createAdmin(username, fullName, email, password, role, dialog);
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void createAdmin(String username, String fullName, String email, String password, String role, Dialog dialog) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        // Create Firebase Auth user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String adminId = authResult.getUser().getUid();
                
                // Create user record with admin role
                Map<String, Object> user = new HashMap<>();
                user.put("userId", adminId);
                user.put("username", username);
                user.put("name", fullName);
                user.put("email", email);
                user.put("role", "ADMIN");
                user.put("isActive", true);
                user.put("createdAt", new Date());
                user.put("createdBy", currentUserId);
                user.put("status", "ACTIVE");
                user.put("adminRole", role); // ADMIN or SUPER_ADMIN
                
                db.collection("users")
                    .document(adminId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        logAction("CREATE_ADMIN", "Created new admin: " + username);
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Admin created successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadAdmins();
                    })
                    .addOnFailureListener(e -> {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to create admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to create auth user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showUpdateAdminDialog(Admin admin) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_admin);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        EditText usernameInput = dialog.findViewById(R.id.usernameInput);
        EditText fullNameInput = dialog.findViewById(R.id.fullNameInput);
        EditText emailInput = dialog.findViewById(R.id.emailInput);
        RadioGroup statusRadioGroup = dialog.findViewById(R.id.statusRadioGroup);
        RadioGroup roleRadioGroup = dialog.findViewById(R.id.roleRadioGroup);
        Button updateButton = dialog.findViewById(R.id.updateButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        
        // Pre-fill current values
        usernameInput.setText(admin.getUsername());
        fullNameInput.setText(admin.getFullName());
        emailInput.setText(admin.getEmail());
        
        if ("ACTIVE".equals(admin.getStatus())) {
            statusRadioGroup.check(R.id.activeRadio);
        } else {
            statusRadioGroup.check(R.id.suspendedRadio);
        }
        
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            roleRadioGroup.check(R.id.superAdminRadio);
        } else {
            roleRadioGroup.check(R.id.adminRadio);
        }
        
        updateButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String status = statusRadioGroup.getCheckedRadioButtonId() == R.id.activeRadio ? 
                "ACTIVE" : "SUSPENDED";
            String role = roleRadioGroup.getCheckedRadioButtonId() == R.id.superAdminRadio ? 
                "SUPER_ADMIN" : "ADMIN";
            
            updateAdmin(admin.getAdminId(), username, fullName, email, status, role, dialog);
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void updateAdmin(String adminId, String username, String fullName, String email, String status, String role, Dialog dialog) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("name", fullName);
        updates.put("email", email);
        updates.put("status", status);
        updates.put("adminRole", role);
        updates.put("isActive", "ACTIVE".equals(status));
        updates.put("lastModifiedBy", currentUserId);
        updates.put("updatedAt", new Date());
        
        db.collection("users")
            .document(adminId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                logAction("UPDATE_ADMIN", "Updated admin: " + username);
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Admin updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                selectedAdmin = null;
                updateButtonStates();
                loadAdmins();
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to update admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showDeleteConfirmationDialog(Admin admin) {
        // Check if trying to delete own account
        if (admin.getAdminId().equals(currentUserId)) {
            Toast.makeText(this, "Cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete Admin")
            .setMessage("Are you sure you want to delete admin '" + admin.getUsername() + "'?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete", (dialogInterface, i) -> deleteAdmin(admin))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteAdmin(Admin admin) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        db.collection("users")
            .document(admin.getAdminId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                logAction("DELETE_ADMIN", "Deleted admin: " + admin.getUsername());
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Admin deleted successfully", Toast.LENGTH_SHORT).show();
                selectedAdmin = null;
                updateButtonStates();
                loadAdmins();
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to delete admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showResetPasswordDialog(Admin admin) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reset_password);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView adminNameText = dialog.findViewById(R.id.adminNameText);
        EditText newPasswordInput = dialog.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialog.findViewById(R.id.confirmPasswordInput);
        Button resetButton = dialog.findViewById(R.id.resetButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        
        adminNameText.setText("Reset password for: " + admin.getUsername());
        
        resetButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(newPassword)) {
                newPasswordInput.setError("Password required");
                return;
            }
            if (newPassword.length() < 6) {
                newPasswordInput.setError("Password must be at least 6 characters");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                return;
            }
            
            resetPassword(admin, newPassword, dialog);
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void resetPassword(Admin admin, String newPassword, Dialog dialog) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        // Note: This requires Firebase Admin SDK on the backend
        // For now, we'll send a password reset email
        auth.sendPasswordResetEmail(admin.getEmail())
            .addOnSuccessListener(aVoid -> {
                logAction("RESET_PASSWORD", "Sent password reset email to: " + admin.getUsername());
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Password reset email sent to " + admin.getEmail(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            })
            .addOnFailureListener(e -> {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void logAction(String action, String description) {
        AuditLog log = new AuditLog();
        log.setUserId(currentUserId);
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(Timestamp.now());
        log.setUserType("ADMIN");
        log.setModule("ADMIN_MANAGEMENT");
        
        db.collection("auditLogs")
            .add(log)
            .addOnFailureListener(e -> {
                // Silent fail for audit logs
            });
    }

    @Override
    public void onAdminClick(Admin admin) {
        selectedAdmin = admin;
        updateButtonStates();
        Toast.makeText(this, "Selected: " + admin.getUsername(), Toast.LENGTH_SHORT).show();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedAdmin != null;
        updateAdminButton.setEnabled(hasSelection);
        deleteAdminButton.setEnabled(hasSelection);
        resetPasswordButton.setEnabled(hasSelection);
    }
}
