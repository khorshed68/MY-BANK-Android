package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Admin {
    private String adminId;
    private String username;
    private String fullName;
    private String email;
    private String status; // ACTIVE, SUSPENDED, INACTIVE
    private String role; // SUPER_ADMIN, ADMIN
    private String createdBy;
    private String lastModifiedBy;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;
    private Date lastLogin;
    private String profileImageUrl;
    private boolean canCreateAdmin;
    private boolean canDeleteAdmin;
    private boolean canResetPassword;
    
    public Admin() {
        // Required empty constructor for Firestore
    }

    public Admin(String adminId, String username, String fullName, String email, String role) {
        this.adminId = adminId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = "ACTIVE";
        this.canCreateAdmin = false;
        this.canDeleteAdmin = false;
        this.canResetPassword = false;
    }

    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
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

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isCanCreateAdmin() {
        return canCreateAdmin;
    }

    public void setCanCreateAdmin(boolean canCreateAdmin) {
        this.canCreateAdmin = canCreateAdmin;
    }

    public boolean isCanDeleteAdmin() {
        return canDeleteAdmin;
    }

    public void setCanDeleteAdmin(boolean canDeleteAdmin) {
        this.canDeleteAdmin = canDeleteAdmin;
    }

    public boolean isCanResetPassword() {
        return canResetPassword;
    }

    public void setCanResetPassword(boolean canResetPassword) {
        this.canResetPassword = canResetPassword;
    }
}
