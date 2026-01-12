package com.khorshed.mybank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.Transaction;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.repository.AccountRepository;
import com.khorshed.mybank.repository.TransactionRepository;
import com.khorshed.mybank.repository.UserRepository;
import com.khorshed.mybank.utils.EmailNotificationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public CustomerViewModel() {
        userRepository = new UserRepository();
        accountRepository = new AccountRepository();
        transactionRepository = new TransactionRepository();
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<Account> getCurrentAccount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return accountRepository.getAccountByUserId(userId);
    }

    public LiveData<List<Transaction>> getTransactions(String accountId) {
        return transactionRepository.getTransactionsByAccount(accountId);
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

    public void deposit(Account account, double amount, String description) {
        if (account.isFrozen()) {
            errorMessage.setValue("Account is frozen. Please contact support.");
            return;
        }

        isLoading.setValue(true);
        double previousBalance = account.getBalance();
        double newBalance = account.getBalance() + amount;
        
        Transaction transaction = new Transaction(
            null, 
            "SYSTEM", 
            account.getAccountId(), 
            "DEPOSIT", 
            amount, 
            newBalance, 
            description != null ? description : "Deposit"
        );
        
        transactionRepository.createTransaction(transaction, new TransactionRepository.OnCompleteListener() {
            @Override
            public void onSuccess(String transactionId) {
                accountRepository.updateAccountBalance(account.getAccountId(), newBalance, 
                    new AccountRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            // Send email notification for deposit
                            EmailNotificationHelper.sendTransactionEmailWithUserInfo(
                                    "DEPOSIT", account.getAccountId(), amount, 
                                    previousBalance, newBalance, transactionId, description, null, null);
                            
                            isLoading.setValue(false);
                            operationSuccess.setValue(true);
                        }

                        @Override
                        public void onFailure(String error) {
                            isLoading.setValue(false);
                            errorMessage.setValue("Failed to update balance: " + error);
                        }
                    });
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to create transaction: " + error);
            }
        });
    }

    public void withdraw(Account account, double amount, String description) {
        if (account.isFrozen()) {
            errorMessage.setValue("Account is frozen. Please contact support.");
            return;
        }

        if (account.getBalance() < amount) {
            errorMessage.setValue("Insufficient funds");
            return;
        }

        isLoading.setValue(true);
        double previousBalance = account.getBalance();
        double newBalance = account.getBalance() - amount;
        
        Transaction transaction = new Transaction(
            null, 
            account.getAccountId(), 
            "SYSTEM", 
            "WITHDRAWAL", 
            amount, 
            newBalance, 
            description != null ? description : "Withdrawal"
        );
        
        transactionRepository.createTransaction(transaction, new TransactionRepository.OnCompleteListener() {
            @Override
            public void onSuccess(String transactionId) {
                accountRepository.updateAccountBalance(account.getAccountId(), newBalance, 
                    new AccountRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            // Send email notification for withdrawal
                            EmailNotificationHelper.sendTransactionEmailWithUserInfo(
                                    "WITHDRAWAL", account.getAccountId(), amount, 
                                    previousBalance, newBalance, transactionId, description, null, null);
                            
                            isLoading.setValue(false);
                            operationSuccess.setValue(true);
                        }

                        @Override
                        public void onFailure(String error) {
                            isLoading.setValue(false);
                            errorMessage.setValue("Failed to update balance: " + error);
                        }
                    });
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to create transaction: " + error);
            }
        });
    }

    public void transfer(Account fromAccount, String toAccountNumber, double amount, String description) {
        if (fromAccount.isFrozen()) {
            errorMessage.setValue("Your account is frozen. Please contact support.");
            return;
        }

        if (fromAccount.getBalance() < amount) {
            errorMessage.setValue("Insufficient funds");
            return;
        }

        isLoading.setValue(true);
        double previousBalance = fromAccount.getBalance();
        
        // First, get the recipient account
        accountRepository.getAccountByAccountNumber(toAccountNumber).observeForever(toAccount -> {
            if (toAccount == null) {
                isLoading.setValue(false);
                errorMessage.setValue("Recipient account not found");
                return;
            }

            if (toAccount.isFrozen()) {
                isLoading.setValue(false);
                errorMessage.setValue("Recipient account is frozen");
                return;
            }

            // Deduct from sender
            double newFromBalance = fromAccount.getBalance() - amount;
            double newToBalance = toAccount.getBalance() + amount;
            
            Transaction transaction = new Transaction(
                null, 
                fromAccount.getAccountId(), 
                toAccount.getAccountId(), 
                "TRANSFER", 
                amount, 
                newFromBalance, 
                description != null ? description : "Transfer to " + toAccountNumber
            );
            
            transactionRepository.createTransaction(transaction, new TransactionRepository.OnCompleteListener() {
                @Override
                public void onSuccess(String transactionId) {
                    // Update sender balance
                    accountRepository.updateAccountBalance(fromAccount.getAccountId(), newFromBalance, 
                        new AccountRepository.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                // Update receiver balance
                                accountRepository.updateAccountBalance(toAccount.getAccountId(), newToBalance, 
                                    new AccountRepository.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                            // Get beneficiary name and send email notification
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .whereEqualTo("userId", toAccount.getUserId())
                                                    .get()
                                                    .addOnSuccessListener(querySnapshot -> {
                                                        String beneficiaryName = "Unknown";
                                                        if (!querySnapshot.isEmpty()) {
                                                            beneficiaryName = querySnapshot.getDocuments().get(0).getString("name");
                                                        }
                                                        // Send email notification for transfer
                                                        EmailNotificationHelper.sendTransactionEmailWithUserInfo(
                                                                "TRANSFER", fromAccount.getAccountId(), amount,
                                                                previousBalance, newFromBalance, transactionId,
                                                                description, toAccountNumber, beneficiaryName);
                                                    });
                                            
                                            isLoading.setValue(false);
                                            operationSuccess.setValue(true);
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            isLoading.setValue(false);
                                            errorMessage.setValue("Failed to update recipient balance: " + error);
                                        }
                                    });
                            }

                            @Override
                            public void onFailure(String error) {
                                isLoading.setValue(false);
                                errorMessage.setValue("Failed to update your balance: " + error);
                            }
                        });
                }

                @Override
                public void onFailure(String error) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to create transaction: " + error);
                }
            });
        });
    }

    public void updateProfile(String userId, String name, String phone, String profileImageUrl) {
        isLoading.setValue(true);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        if (profileImageUrl != null) {
            updates.put("profileImageUrl", profileImageUrl);
        }
        
        userRepository.updateUser(userId, updates, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                operationSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update profile: " + error);
            }
        });
    }

    public void updateNotificationSettings(String userId, boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("notificationsEnabled", enabled);
        
        userRepository.updateUser(userId, updates, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                operationSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Failed to update settings: " + error);
            }
        });
    }

    public void updateBiometricSettings(String userId, boolean enabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("biometricEnabled", enabled);
        
        userRepository.updateUser(userId, updates, new UserRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                operationSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Failed to update settings: " + error);
            }
        });
    }
}
