package com.khorshed.mybank.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ActivityLog {
    private String logId;
    private String staffMemberId;
    private String staffMemberName;
    private String action;
    private String targetAccount;
    private String details;
    @ServerTimestamp
    private Date timestamp;

    public ActivityLog() {
        // Required empty constructor
    }

    public ActivityLog(String staffMemberId, String staffMemberName, String action, 
                      String targetAccount, String details) {
        this.staffMemberId = staffMemberId;
        this.staffMemberName = staffMemberName;
        this.action = action;
        this.targetAccount = targetAccount;
        this.details = details;
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getStaffMemberId() {
        return staffMemberId;
    }

    public void setStaffMemberId(String staffMemberId) {
        this.staffMemberId = staffMemberId;
    }

    public String getStaffMemberName() {
        return staffMemberName;
    }

    public void setStaffMemberName(String staffMemberName) {
        this.staffMemberName = staffMemberName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
