package com.khorshed.mybank.models;

public class CustomerAccount {
    private String accountNumber;
    private String customerName;
    private String email;
    private String phone;
    private String accountType;
    private double balance;
    private String status;
    private String createdDate;
    private String userId;
    private String accountId;

    public CustomerAccount() {
        // Required empty constructor
    }

    public CustomerAccount(String accountNumber, String customerName, String email, 
                          String phone, String accountType, double balance, 
                          String status, String createdDate, String userId, String accountId) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdDate = createdDate;
        this.userId = userId;
        this.accountId = accountId;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
