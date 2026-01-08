package com.khorshed.mybank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.Transaction;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.repository.AccountRepository;
import com.khorshed.mybank.repository.TransactionRepository;
import com.khorshed.mybank.repository.UserRepository;

import java.util.List;

public class StaffViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public StaffViewModel() {
        userRepository = new UserRepository();
        accountRepository = new AccountRepository();
        transactionRepository = new TransactionRepository();
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

    public LiveData<List<Transaction>> getTodayTransactions() {
        return transactionRepository.getTodayTransactions();
    }

    public void searchCustomers(String query, UserRepository.OnSearchListener listener) {
        isLoading.setValue(true);
        userRepository.searchUsers(query, users -> {
            isLoading.setValue(false);
            // Filter only customers
            List<User> customers = users.stream()
                .filter(u -> "CUSTOMER".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
            listener.onSearchComplete(customers);
        });
    }

    public LiveData<User> getCustomerById(String customerId) {
        return userRepository.getUserById(customerId);
    }

    public LiveData<Account> getCustomerAccount(String userId) {
        return accountRepository.getAccountByUserId(userId);
    }

    public LiveData<List<Transaction>> getCustomerTransactions(String accountId) {
        return transactionRepository.getTransactionsByAccount(accountId);
    }

    public void processDeposit(Account account, double amount, String description) {
        if (account.isFrozen()) {
            errorMessage.setValue("Account is frozen");
            return;
        }

        isLoading.setValue(true);
        double newBalance = account.getBalance() + amount;
        
        String staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        Transaction transaction = new Transaction(
            null, 
            "SYSTEM", 
            account.getAccountId(), 
            "DEPOSIT", 
            amount, 
            newBalance, 
            description != null ? description : "Staff Deposit"
        );
        transaction.setProcessedBy(staffId);
        
        transactionRepository.createTransaction(transaction, new TransactionRepository.OnCompleteListener() {
            @Override
            public void onSuccess(String transactionId) {
                accountRepository.updateAccountBalance(account.getAccountId(), newBalance, 
                    new AccountRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
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

    public void processWithdrawal(Account account, double amount, String description) {
        if (account.isFrozen()) {
            errorMessage.setValue("Account is frozen");
            return;
        }

        if (account.getBalance() < amount) {
            errorMessage.setValue("Insufficient funds");
            return;
        }

        isLoading.setValue(true);
        double newBalance = account.getBalance() - amount;
        
        String staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        Transaction transaction = new Transaction(
            null, 
            account.getAccountId(), 
            "SYSTEM", 
            "WITHDRAWAL", 
            amount, 
            newBalance, 
            description != null ? description : "Staff Withdrawal"
        );
        transaction.setProcessedBy(staffId);
        
        transactionRepository.createTransaction(transaction, new TransactionRepository.OnCompleteListener() {
            @Override
            public void onSuccess(String transactionId) {
                accountRepository.updateAccountBalance(account.getAccountId(), newBalance, 
                    new AccountRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
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
}
