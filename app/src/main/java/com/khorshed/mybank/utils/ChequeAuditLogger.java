package com.khorshed.mybank.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.models.ChequeAuditLog;

/**
 * Utility class for logging cheque-related transactions and status changes
 * to maintain a complete audit trail
 */
public class ChequeAuditLogger {
    
    private static final String COLLECTION_NAME = "cheque_audit_logs";
    
    /**
     * Log a cheque transaction or status change
     * 
     * @param chequeNumber Cheque number
     * @param accountNumber Account number
     * @param type Type of transaction (ISSUE, DEPOSIT, CLEAR, BOUNCE, CANCEL, STATUS_CHANGE)
     * @param oldStatus Previous status (can be null for new cheques)
     * @param newStatus New status
     * @param amount Cheque amount
     * @param performedBy User ID who performed the action
     * @param performedByName Name of the person who performed the action
     * @param userType Type of user (CUSTOMER, STAFF, ADMIN)
     */
    public static void logTransaction(String chequeNumber, String accountNumber, String type,
                                     String oldStatus, String newStatus, double amount,
                                     String performedBy, String performedByName, String userType) {
        logTransaction(chequeNumber, accountNumber, type, oldStatus, newStatus, amount,
                      performedBy, performedByName, userType, null);
    }
    
    /**
     * Log a cheque transaction or status change with remarks
     * 
     * @param chequeNumber Cheque number
     * @param accountNumber Account number
     * @param type Type of transaction (ISSUE, DEPOSIT, CLEAR, BOUNCE, CANCEL, STATUS_CHANGE)
     * @param oldStatus Previous status (can be null for new cheques)
     * @param newStatus New status
     * @param amount Cheque amount
     * @param performedBy User ID who performed the action
     * @param performedByName Name of the person who performed the action
     * @param userType Type of user (CUSTOMER, STAFF, ADMIN)
     * @param remarks Additional remarks or notes
     */
    public static void logTransaction(String chequeNumber, String accountNumber, String type,
                                     String oldStatus, String newStatus, double amount,
                                     String performedBy, String performedByName, String userType,
                                     String remarks) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        ChequeAuditLog log = new ChequeAuditLog(
            chequeNumber, accountNumber, type, oldStatus, newStatus, amount,
            performedBy, performedByName, userType
        );
        
        if (remarks != null && !remarks.isEmpty()) {
            log.setRemarks(remarks);
        }
        
        // Generate a unique ID for the log entry
        String logId = db.collection(COLLECTION_NAME).document().getId();
        log.setLogId(logId);
        
        // Save to Firestore
        db.collection(COLLECTION_NAME)
            .document(logId)
            .set(log)
            .addOnSuccessListener(aVoid -> {
                // Log saved successfully
            })
            .addOnFailureListener(e -> {
                // Log failed to save - could add error handling here
                e.printStackTrace();
            });
    }
    
    /**
     * Log a cheque issue event
     */
    public static void logChequeIssue(String chequeNumber, String accountNumber, double amount,
                                     String performedBy, String performedByName, String userType) {
        logTransaction(chequeNumber, accountNumber, "ISSUE", null, "ISSUED", amount,
                      performedBy, performedByName, userType);
    }
    
    /**
     * Log a cheque deposit event
     */
    public static void logChequeDeposit(String chequeNumber, String accountNumber, double amount,
                                       String performedBy, String performedByName, String userType) {
        logTransaction(chequeNumber, accountNumber, "DEPOSIT", "ISSUED", "DEPOSITED", amount,
                      performedBy, performedByName, userType);
    }
    
    /**
     * Log a cheque clearance event
     */
    public static void logChequeClear(String chequeNumber, String accountNumber, double amount,
                                     String performedBy, String performedByName, String userType,
                                     String remarks) {
        logTransaction(chequeNumber, accountNumber, "CLEAR", "PENDING_CLEARANCE", "CLEARED", amount,
                      performedBy, performedByName, userType, remarks);
    }
    
    /**
     * Log a cheque bounce event
     */
    public static void logChequeBounce(String chequeNumber, String accountNumber, double amount,
                                      String performedBy, String performedByName, String userType,
                                      String bounceReason) {
        logTransaction(chequeNumber, accountNumber, "BOUNCE", "PENDING_CLEARANCE", "BOUNCED", amount,
                      performedBy, performedByName, userType, bounceReason);
    }
    
    /**
     * Log a cheque cancellation event
     */
    public static void logChequeCancel(String chequeNumber, String accountNumber, double amount,
                                      String oldStatus, String performedBy, String performedByName,
                                      String userType, String remarks) {
        logTransaction(chequeNumber, accountNumber, "CANCEL", oldStatus, "CANCELLED", amount,
                      performedBy, performedByName, userType, remarks);
    }
}
