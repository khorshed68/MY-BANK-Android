package com.khorshed.mybank.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.khorshed.mybank.models.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TransactionRepository {
    private final FirebaseFirestore db;

    public TransactionRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Transaction>> getTransactionsByAccount(String accountId) {
        MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
        
        // Get all transactions where this account is either sender or receiver
        db.collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    transactionsLiveData.setValue(new ArrayList<>());
                    return;
                }
                
                List<Transaction> transactions = new ArrayList<>();
                if (value != null) {
                    // Filter transactions that involve this account
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        if (transaction != null) {
                            String fromAccount = transaction.getFromAccountId();
                            String toAccount = transaction.getToAccountId();
                            
                            // Include transaction if this account is involved
                            if ((fromAccount != null && fromAccount.equals(accountId)) ||
                                (toAccount != null && toAccount.equals(accountId))) {
                                transactions.add(transaction);
                            }
                        }
                    }
                }
                transactionsLiveData.setValue(transactions);
            });
        
        return transactionsLiveData;
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
        
        db.collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1000)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    transactionsLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<Transaction> transactions = value.toObjects(Transaction.class);
                    transactionsLiveData.setValue(transactions);
                }
            });
        
        return transactionsLiveData;
    }

    public LiveData<List<Transaction>> getTodayTransactions() {
        MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("createdAt", startOfDay)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    transactionsLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<Transaction> transactions = value.toObjects(Transaction.class);
                    transactionsLiveData.setValue(transactions);
                }
            });
        
        return transactionsLiveData;
    }

    public void createTransaction(Transaction transaction, OnCompleteListener listener) {
        String transactionId = db.collection("transactions").document().getId();
        transaction.setTransactionId(transactionId);
        
        db.collection("transactions")
            .document(transactionId)
            .set(transaction)
            .addOnSuccessListener(aVoid -> listener.onSuccess(transactionId))
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void getTransactionCount(OnCountListener listener) {
        db.collection("transactions")
            .get()
            .addOnSuccessListener(querySnapshot -> listener.onCount(querySnapshot.size()))
            .addOnFailureListener(e -> listener.onCount(0));
    }

    public interface OnCompleteListener {
        void onSuccess(String transactionId);
        void onFailure(String error);
    }

    public interface OnCountListener {
        void onCount(int count);
    }
}
