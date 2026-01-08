package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Cheque {
    private String chequeId;
    private String chequeNumber;
    private String chequeBookNumber;
    private String accountId;
    private String accountNumber;
    private String userId;
    private double amount;
    private String payeeName;
    private String status; // ISSUED, DEPOSITED, PENDING_CLEARANCE, CLEARED, BOUNCED, CANCELLED
    @ServerTimestamp
    private Date issuedDate;
    private Date depositedDate;
    private Date clearanceDate;
    private String clearedBy;
    private String bouncedBy;
    private String bounceReason;
    private String depositedBy;
    private String depositedByStaff;
    private boolean signatureVerified;
    private String clearanceRemarks;

    public Cheque() {
        // Required empty constructor
    }

    // Getters and Setters
    public String getChequeId() {
        return chequeId;
    }

    public void setChequeId(String chequeId) {
        this.chequeId = chequeId;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
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

    public Date getDepositedDate() {
        return depositedDate;
    }

    public void setDepositedDate(Date depositedDate) {
        this.depositedDate = depositedDate;
    }

    public Date getClearanceDate() {
        return clearanceDate;
    }

    public void setClearanceDate(Date clearanceDate) {
        this.clearanceDate = clearanceDate;
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

    public String getBounceReason() {
        return bounceReason;
    }

    public void setBounceReason(String bounceReason) {
        this.bounceReason = bounceReason;
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
}
