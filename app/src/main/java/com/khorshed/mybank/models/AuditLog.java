package com.khorshed.mybank.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class AuditLog {
    @DocumentId
    private String logId;
    private String userId;
    private String userName;
    private String action; // USER_CREATED, USER_UPDATED, USER_DELETED, ACCOUNT_FROZEN, etc.
    private String targetUserId;
    private String targetUserName;
    private String description;
    private String ipAddress;
    @ServerTimestamp
    private Date createdAt;

    public AuditLog() {
        // Required empty constructor for Firestore
    }

    public AuditLog(String logId, String userId, String userName, String action, 
                   String targetUserId, String targetUserName, String description) {
        this.logId = logId;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.targetUserId = targetUserId;
        this.targetUserName = targetUserName;
        this.description = description;
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
