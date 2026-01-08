package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class DepositedCheque {
    private String depositId;
    private String chequeNumber;
    private String accountNumber;
    private String payerName;
    private double amount;
    private double accountBalance;
    private String depositedBy;
    private String depositedByStaff;
    private String status; // PENDING, CLEARED, BOUNCED
    private boolean signatureVerified;
    private String clearanceRemarks;
    private String bounceReason;
    private String clearedBy;
    private String bouncedBy;
    @ServerTimestamp
    private Date issueDate;
    @ServerTimestamp
    private Date depositDate;
    @ServerTimestamp
    private Date clearedDate;
    @ServerTimestamp
    private Date bouncedDate;

    public DepositedCheque() {
        // Required empty constructor
    }

    // Getters and Setters
    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
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

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getDepositedBy() {
        return depositedBy;
    }

    public void setDepositedBy(String depositedBy) {
        this.depositedBy = depositedBy;
    }

    public String getDepositedByStaff() {
        return depositedByStaff;
    }

    public void setDepositedByStaff(String depositedByStaff) {
        this.depositedByStaff = depositedByStaff;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSignatureVerified() {
        return signatureVerified;
    }

    public void setSignatureVerified(boolean signatureVerified) {
        this.signatureVerified = signatureVerified;
    }

    public String getClearanceRemarks() {
        return clearanceRemarks;
    }

    public void setClearanceRemarks(String clearanceRemarks) {
        this.clearanceRemarks = clearanceRemarks;
    }

    public String getBounceReason() {
        return bounceReason;
    }

    public void setBounceReason(String bounceReason) {
        this.bounceReason = bounceReason;
    }

    public String getClearedBy() {
        return clearedBy;
    }

    public void setClearedBy(String clearedBy) {
        this.clearedBy = clearedBy;
    }

    public String getBouncedBy() {
        return bouncedBy;
    }

    public void setBouncedBy(String bouncedBy) {
        this.bouncedBy = bouncedBy;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(Date depositDate) {
        this.depositDate = depositDate;
    }

    public Date getClearedDate() {
        return clearedDate;
    }

    public void setClearedDate(Date clearedDate) {
        this.clearedDate = clearedDate;
    }

    public Date getBouncedDate() {
        return bouncedDate;
    }

    public void setBouncedDate(Date bouncedDate) {
        this.bouncedDate = bouncedDate;
    }
}
