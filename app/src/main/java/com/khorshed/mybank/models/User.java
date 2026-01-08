package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String username; // Optional username field (mainly for admin/staff)
    private String role; // CUSTOMER, STAFF, ADMIN
    private String profileImageUrl;
    private boolean isActive;
    private boolean biometricEnabled;
    private boolean notificationsEnabled;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String userId, String email, String name, String phone, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.isActive = true;
        this.biometricEnabled = false;
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isBiometricEnabled() {
        return biometricEnabled;
    }

    public void setBiometricEnabled(boolean biometricEnabled) {
        this.biometricEnabled = biometricEnabled;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
