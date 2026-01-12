package com.khorshed.mybank.models;

public class TransactionMonitoring {
    private String id;
    private String transactionId;
    private String accountNumber;
    private String accountId;
    private String type;
    private double amount;
    private String dateTime;
    private String description;
    private String status;
    private boolean isSelected;
    private Object timestamp;

    public TransactionMonitoring() {
        // Empty constructor for Firestore
    }

    public TransactionMonitoring(String id, String transactionId, String accountNumber, String accountId,
                                String type, double amount, String dateTime, String description, String status) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.dateTime = dateTime;
        this.description = description;
        this.status = status;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
