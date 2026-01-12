package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChequeAuditLog {
    private String logId;
    private String chequeNumber;
    private String accountNumber;
    private String type; // STATUS_CHANGE, ISSUE, DEPOSIT, CLEAR, BOUNCE, CANCEL
    private String oldStatus;
    private String newStatus;
    private double amount;
    @ServerTimestamp
    private Date timestamp;
    private String performedBy; // User ID who performed the action
    private String performedByName; // Name of the person who performed the action
    private String userType; // CUSTOMER, STAFF, ADMIN
    private String remarks;

    public ChequeAuditLog() {
        // Required empty constructor
    }

    public ChequeAuditLog(String chequeNumber, String accountNumber, String type, 
                         String oldStatus, String newStatus, double amount,
                         String performedBy, String performedByName, String userType) {
        this.chequeNumber = chequeNumber;
        this.accountNumber = accountNumber;
        this.type = type;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.amount = amount;
        this.performedBy = performedBy;
        this.performedByName = performedByName;
        this.userType = userType;
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
