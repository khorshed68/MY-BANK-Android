package com.khorshed.mybank.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.khorshed.mybank.models.AuditLog;

import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {
    private final FirebaseFirestore db;

    public AuditLogRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<AuditLog>> getAllAuditLogs() {
        MutableLiveData<List<AuditLog>> logsLiveData = new MutableLiveData<>();
        
        db.collection("audit_logs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(500)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    logsLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<AuditLog> logs = value.toObjects(AuditLog.class);
                    logsLiveData.setValue(logs);
                }
            });
        
        return logsLiveData;
    }

    public LiveData<List<AuditLog>> getAuditLogsByAction(String action) {
        MutableLiveData<List<AuditLog>> logsLiveData = new MutableLiveData<>();
        
        db.collection("audit_logs")
            .whereEqualTo("action", action)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    logsLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<AuditLog> logs = value.toObjects(AuditLog.class);
                    logsLiveData.setValue(logs);
                }
            });
        
        return logsLiveData;
    }

    public void createAuditLog(AuditLog auditLog, OnCompleteListener listener) {
        String logId = db.collection("audit_logs").document().getId();
        auditLog.setLogId(logId);
        
        db.collection("audit_logs")
            .document(logId)
            .set(auditLog)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void searchAuditLogs(String query, OnSearchListener listener) {
        db.collection("audit_logs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(500)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<AuditLog> results = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    AuditLog log = doc.toObject(AuditLog.class);
                    if (log != null) {
                        String searchQuery = query.toLowerCase();
                        if (log.getAction().toLowerCase().contains(searchQuery) ||
                            log.getUserName().toLowerCase().contains(searchQuery) ||
                            (log.getTargetUserName() != null && log.getTargetUserName().toLowerCase().contains(searchQuery)) ||
                            log.getDescription().toLowerCase().contains(searchQuery)) {
                            results.add(log);
                        }
                    }
                }
                listener.onSearchComplete(results);
            })
            .addOnFailureListener(e -> listener.onSearchComplete(new ArrayList<>()));
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnSearchListener {
        void onSearchComplete(List<AuditLog> logs);
    }
}
