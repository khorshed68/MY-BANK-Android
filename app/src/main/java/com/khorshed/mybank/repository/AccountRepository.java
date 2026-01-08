package com.khorshed.mybank.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.khorshed.mybank.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AccountRepository {
    private final FirebaseFirestore db;

    public AccountRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<Account> getAccountByUserId(String userId) {
        MutableLiveData<Account> accountLiveData = new MutableLiveData<>();
        
        db.collection("accounts")
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null || value.isEmpty()) {
                    accountLiveData.setValue(null);
                    return;
                }
                Account account = value.getDocuments().get(0).toObject(Account.class);
                accountLiveData.setValue(account);
            });
        
        return accountLiveData;
    }

    public LiveData<Account> getAccountByAccountNumber(String accountNumber) {
        MutableLiveData<Account> accountLiveData = new MutableLiveData<>();
        
        db.collection("accounts")
            .whereEqualTo("accountNumber", accountNumber)
            .limit(1)
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null || value.isEmpty()) {
                    accountLiveData.setValue(null);
                    return;
                }
                Account account = value.getDocuments().get(0).toObject(Account.class);
                accountLiveData.setValue(account);
            });
        
        return accountLiveData;
    }

    public LiveData<List<Account>> getAllAccounts() {
        MutableLiveData<List<Account>> accountsLiveData = new MutableLiveData<>();
        
        db.collection("accounts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    accountsLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<Account> accounts = value.toObjects(Account.class);
                    accountsLiveData.setValue(accounts);
                }
            });
        
        return accountsLiveData;
    }

    public void createAccount(String userId, String accountType, OnAccountCreateListener listener) {
        // Get the count of existing accounts to generate sequential number
        db.collection("accounts")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int accountCount = querySnapshot.size();
                String accountNumber = String.valueOf(accountCount + 1);
                String accountId = db.collection("accounts").document().getId();
                
                Account account = new Account(accountId, userId, accountNumber, accountType);
                
                db.collection("accounts")
                    .document(accountId)
                    .set(account)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(account))
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateAccountBalance(String accountId, double newBalance, OnCompleteListener listener) {
        db.collection("accounts")
            .document(accountId)
            .update("balance", newBalance)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateAccount(String accountId, Map<String, Object> updates, OnCompleteListener listener) {
        db.collection("accounts")
            .document(accountId)
            .update(updates)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void freezeAccount(String accountId, boolean freeze, OnCompleteListener listener) {
        db.collection("accounts")
            .document(accountId)
            .update("frozen", freeze)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnAccountCreateListener {
        void onSuccess(Account account);
        void onFailure(String error);
    }
}
