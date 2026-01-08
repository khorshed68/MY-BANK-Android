package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChequeBook {
    private String chequeBookNumber;
    private String accountId;
    private String accountNumber;
    private String userId;
    private int numberOfLeaves;
    private int usedLeaves;
    private String issuedBy;
    private String status; // ACTIVE, COMPLETED, BLOCKED
    @ServerTimestamp
    private Date issuedDate;
    
    // For display in table
    private long startChequeNumber;
    private long endChequeNumber;

    public ChequeBook() {
        // Required empty constructor
    }

    // Getters and Setters
    public String getChequeBookNumber() {
        return chequeBookNumber;
    }

    public void setChequeBookNumber(String chequeBookNumber) {
        this.chequeBookNumber = chequeBookNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getNumberOfLeaves() {
        return numberOfLeaves;
    }

    public void setNumberOfLeaves(int numberOfLeaves) {
        this.numberOfLeaves = numberOfLeaves;
    }

    public int getUsedLeaves() {
        return usedLeaves;
    }

    public void setUsedLeaves(int usedLeaves) {
        this.usedLeaves = usedLeaves;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public long getStartChequeNumber() {
        return startChequeNumber;
    }

    public void setStartChequeNumber(long startChequeNumber) {
        this.startChequeNumber = startChequeNumber;
    }

    public long getEndChequeNumber() {
        return endChequeNumber;
    }

    public void setEndChequeNumber(long endChequeNumber) {
        this.endChequeNumber = endChequeNumber;
    }
    
    public int getRemainingLeaves() {
        return numberOfLeaves - usedLeaves;
    }
}
