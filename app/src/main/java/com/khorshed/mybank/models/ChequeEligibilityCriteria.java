package com.khorshed.mybank.models;

public class ChequeEligibilityCriteria {
    private String criteriaId;
    private String accountType; // CURRENT, SALARY, SAVINGS, FIXED_DEPOSIT, RECURRING
    private double minBalance;
    private int minAccountAgeDays;
    private int maxBooksPerYear;
    private int leavesPerBook;
    private boolean active;

    public ChequeEligibilityCriteria() {
        // Required empty constructor
    }

    public ChequeEligibilityCriteria(String accountType, double minBalance, int minAccountAgeDays,
                                    int maxBooksPerYear, int leavesPerBook, boolean active) {
        this.accountType = accountType;
        this.minBalance = minBalance;
        this.minAccountAgeDays = minAccountAgeDays;
        this.maxBooksPerYear = maxBooksPerYear;
        this.leavesPerBook = leavesPerBook;
        this.active = active;
    }

    // Getters and Setters
    public String getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(String criteriaId) {
        this.criteriaId = criteriaId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getMinBalance() {
        return minBalance;
    }

    public void setMinBalance(double minBalance) {
        this.minBalance = minBalance;
    }

    public int getMinAccountAgeDays() {
        return minAccountAgeDays;
    }

    public void setMinAccountAgeDays(int minAccountAgeDays) {
        this.minAccountAgeDays = minAccountAgeDays;
    }

    public int getMaxBooksPerYear() {
        return maxBooksPerYear;
    }

    public void setMaxBooksPerYear(int maxBooksPerYear) {
        this.maxBooksPerYear = maxBooksPerYear;
    }

    public int getLeavesPerBook() {
        return leavesPerBook;
    }

    public void setLeavesPerBook(int leavesPerBook) {
        this.leavesPerBook = leavesPerBook;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
