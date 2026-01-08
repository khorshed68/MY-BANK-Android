package com.khorshed.mybank.models;

public class SystemStats {
    private int totalUsers;
    private int totalAccounts;
    private long totalTransactions;
    private double totalMoneyInSystem;
    private int todayTransactions;
    private int activeUsers;

    public SystemStats() {
        // Default constructor
    }

    public SystemStats(int totalUsers, int totalAccounts, long totalTransactions, 
                      double totalMoneyInSystem, int todayTransactions, int activeUsers) {
        this.totalUsers = totalUsers;
        this.totalAccounts = totalAccounts;
        this.totalTransactions = totalTransactions;
        this.totalMoneyInSystem = totalMoneyInSystem;
        this.todayTransactions = todayTransactions;
        this.activeUsers = activeUsers;
    }

    // Getters and Setters
    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public double getTotalMoneyInSystem() {
        return totalMoneyInSystem;
    }

    public void setTotalMoneyInSystem(double totalMoneyInSystem) {
        this.totalMoneyInSystem = totalMoneyInSystem;
    }

    public int getTodayTransactions() {
        return todayTransactions;
    }

    public void setTodayTransactions(int todayTransactions) {
        this.todayTransactions = todayTransactions;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }
}
