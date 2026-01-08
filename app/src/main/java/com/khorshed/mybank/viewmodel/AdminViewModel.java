package com.khorshed.mybank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.AuditLog;
import com.khorshed.mybank.models.SystemStats;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.repository.AccountRepository;
import com.khorshed.mybank.repository.AuditLogRepository;
import com.khorshed.mybank.repository.TransactionRepository;
import com.khorshed.mybank.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final FirebaseAuth auth;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<SystemStats> systemStats = new MutableLiveData<>();

    public AdminViewModel() {
        userRepository = new UserRepository();
        accountRepository = new AccountRepository();
        transactionRepository = new TransactionRepository();
        auditLogRepository = new AuditLogRepository();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<SystemStats> getSystemStats() {
        return systemStats;
    }

    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public LiveData<List<User>> getUsersByRole(String role) {
        return userRepository.getUsersByRole(role);
    }

    public LiveData<List<Account>> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }

    public LiveData<List<AuditLog>> getAuditLogs() {
        return auditLogRepository.getAllAuditLogs();
    }

    public void loadSystemStats() {
        isLoading.setValue(true);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Get total users
        db.collection("users").get().addOnSuccessListener(userSnapshot -> {
            int totalUsers = userSnapshot.size();
            int activeUsers = (int) userSnapshot.getDocuments().stream()
                .filter(doc -> {
                    User user = doc.toObject(User.class);
                    return user != null && user.isActive();
                })
                .count();
            
            // Get total accounts
            db.collection("accounts").get().addOnSuccessListener(accountSnapshot -> {
                int totalAccounts = accountSnapshot.size();
                double totalMoney = accountSnapshot.getDocuments().stream()
                    .mapToDouble(doc -> {
                        Account account = doc.toObject(Account.class);
                        return account != null ? account.getBalance() : 0.0;
                    })
                    .sum();
                
                // Get transaction count
                transactionRepository.getTransactionCount(totalTransactions -> {
                    transactionRepository.getTodayTransactions().observeForever(todayTrans -> {
                        int todayCount = todayTrans != null ? todayTrans.size() : 0;
                        
                        SystemStats stats = new SystemStats(
                            totalUsers, 
                            totalAccounts, 
                            totalTransactions, 
                            totalMoney, 
                            todayCount, 
                            activeUsers
                        );
                        
                        systemStats.setValue(stats);
                        isLoading.setValue(false);
                    });
                });
            });
        });
    }

    public void createUser(String email, String password, String name, String phone, String role) {
        isLoading.setValue(true);
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    String userId = task.getResult().getUser().getUid();
                    User user = new User(userId, email, name, phone, role);
                    
                    userRepository.createUser(user, new UserRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            if ("CUSTOMER".equals(role)) {
                                // Create account for customer
                                accountRepository.createAccount(userId, "SAVINGS", 
                                    new AccountRepository.OnAccountCreateListener() {
                                        @Override
                                        public void onSuccess(Account account) {
                                            logAudit("USER_CREATED", userId, name, 
                                                "Created new " + role + " user: " + email);
                                            isLoading.setValue(false);
                                            operationSuccess.setValue(true);
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            isLoading.setValue(false);
                                            errorMessage.setValue("User created but failed to create account: " + error);
                                        }
                                    });
                            } else {
                                logAudit("USER_CREATED", userId, name, 
                                    "Created new " + role + " user: " + email);
                                isLoading.setValue(false);
                                operationSuccess.setValue(true);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            isLoading.setValue(false);
                            errorMessage.setValue("Failed to create user profile: " + error);
                        }
                    });
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue(task.getException() != null ? 
                        task.getException().getMessage() : "Failed to create user");
                }
            });
    }

    public void updateUser(String userId, String name, String phone, String role, boolean isActive) {
        isLoading.setValue(true);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("role", role);
        updates.put("active", isActive);
        
        userRepository.updateUser(userId, updates, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                logAudit("USER_UPDATED", userId, name, "Updated user information");
                isLoading.setValue(false);
                operationSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update user: " + error);
            }
        });
    }

    public void deleteUser(String userId, String userName) {
        isLoading.setValue(true);
        
        userRepository.deleteUser(userId, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                logAudit("USER_DELETED", userId, userName, "Deleted user");
                isLoading.setValue(false);
                operationSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to delete user: " + error);
            }
        });
    }

    public void toggleAccountFreeze(String accountId, boolean freeze, String accountNumber) {
        isLoading.setValue(true);
        
        accountRepository.freezeAccount(accountId, freeze, new AccountRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                String action = freeze ? "ACCOUNT_FROZEN" : "ACCOUNT_UNFROZEN";
                logAudit(action, null, null, 
                    (freeze ? "Froze" : "Unfroze") + " account " + accountNumber);
                isLoading.setValue(false);
                operationSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to " + (freeze ? "freeze" : "unfreeze") + " account: " + error);
            }
        });
    }

    public void searchAuditLogs(String query, AuditLogRepository.OnSearchListener listener) {
        auditLogRepository.searchAuditLogs(query, listener);
    }

    private void logAudit(String action, String targetUserId, String targetUserName, String description) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "SYSTEM";
        
        userRepository.getUserById(currentUserId).observeForever(currentUser -> {
            String currentUserName = currentUser != null ? currentUser.getName() : "System";
            
            AuditLog log = new AuditLog(
                null, 
                currentUserId, 
                currentUserName, 
                action, 
                targetUserId, 
                targetUserName, 
                description
            );
            
            auditLogRepository.createAuditLog(log, new AuditLogRepository.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    // Audit log created successfully
                }

                @Override
                public void onFailure(String error) {
                    // Failed to create audit log (non-critical)
                }
            });
        });
    }
}
