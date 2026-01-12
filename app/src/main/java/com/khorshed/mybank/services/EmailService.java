package com.khorshed.mybank.services;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email Service for sending real-time email notifications to users
 * Uses JavaMail API with Gmail SMTP server
 */
public class EmailService {
    
    private static final String TAG = "EmailService";
    
    // Email Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "mybank.bankmanagement@gmail.com";
    private static final String EMAIL_PASSWORD = "xphbmmqclkatozpe"; // Use App Password, not regular password
    
    // Email notification types
    public enum NotificationType {
        ACCOUNT_APPROVED,
        LOGIN,
        LOGOUT,
        CHECK_BALANCE,
        DEPOSIT,
        WITHDRAW,
        FUND_TRANSFER,
        CHEQUE_REQUEST,
        CHEQUE_APPROVED,
        PASSWORD_CHANGE,
        PROFILE_PICTURE_CHANGE,
        SECURITY_ALERT
    }
    
    /**
     * Send email notification asynchronously
     */
    public static void sendEmail(String toEmail, NotificationType type, EmailData data) {
        new SendEmailTask().execute(toEmail, type, data);
    }
    
    /**
     * AsyncTask to send email in background thread
     */
    private static class SendEmailTask extends AsyncTask<Object, Void, Boolean> {
        
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                String toEmail = (String) params[0];
                NotificationType type = (NotificationType) params[1];
                EmailData data = (EmailData) params[2];
                
                // Create email properties
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.ssl.trust", SMTP_HOST);
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                
                // Create authenticator
                Authenticator auth = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, EMAIL_PASSWORD);
                    }
                };
                
                // Create session
                Session session = Session.getInstance(props, auth);
                
                // Create message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL, "MY BANK"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(getEmailSubject(type, data));
                message.setText(getEmailBody(type, data));
                message.setSentDate(new Date());
                
                // Send email
                Transport.send(message);
                
                Log.d(TAG, "✅ Email sent successfully to: " + toEmail + " - Type: " + type);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send email", e);
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d(TAG, "Email delivery completed successfully");
            } else {
                Log.e(TAG, "Email delivery failed");
            }
        }
    }
    
    /**
     * Get email subject based on notification type
     */
    private static String getEmailSubject(NotificationType type, EmailData data) {
        switch (type) {
            case ACCOUNT_APPROVED:
                return "🎉 Account Approved - Welcome to MY BANK!";
            case LOGIN:
                return "🔐 Login Alert - MY BANK Account Access";
            case LOGOUT:
                return "👋 Logout Notification - MY BANK";
            case CHECK_BALANCE:
                return "💰 Balance Inquiry - MY BANK";
            case DEPOSIT:
                return "💵 Deposit Confirmation - MY BANK";
            case WITHDRAW:
                return "💸 Withdrawal Confirmation - MY BANK";
            case FUND_TRANSFER:
                return "💳 Fund Transfer Confirmation - MY BANK";
            case CHEQUE_REQUEST:
                return "📋 Cheque Book Request Received - MY BANK";
            case CHEQUE_APPROVED:
                return "✅ Cheque Book Approved - MY BANK";
            case PASSWORD_CHANGE:
                return "🔒 Password Changed Successfully - MY BANK";
            case PROFILE_PICTURE_CHANGE:
                return "📸 Profile Picture Updated - MY BANK";
            case SECURITY_ALERT:
                return "⚠️ SECURITY ALERT - Suspicious Activity Detected";
            default:
                return "MY BANK Notification";
        }
    }
    
    /**
     * Get email body based on notification type
     */
    private static String getEmailBody(NotificationType type, EmailData data) {
        StringBuilder body = new StringBuilder();
        
        switch (type) {
            case ACCOUNT_APPROVED:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Congratulations! Your account application has been approved.\n\n");
                body.append("Account Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Account Type: ").append(data.accountType).append("\n");
                body.append("Account Holder: ").append(data.customerName).append("\n");
                body.append("Phone Number: ").append(data.phoneNumber).append("\n");
                body.append("Initial Deposit: ৳ ").append(String.format("%.2f", data.amount)).append("\n");
                body.append("Status: ACTIVE\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("You can now login to your account using:\n");
                body.append("- Phone Number: ").append(data.phoneNumber).append("\n");
                body.append("- Your registered password\n\n");
                body.append("Thank you for choosing MY BANK!\n");
                break;
                
            case LOGIN:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your account was accessed successfully.\n\n");
                body.append("Login Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Login Time: ").append(data.timestamp).append("\n");
                body.append("Device: Android Mobile App\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("If this wasn't you, please contact our support immediately.\n");
                break;
                
            case LOGOUT:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("You have successfully logged out from your MY BANK account.\n\n");
                body.append("Logout Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Logout Time: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("Thank you for banking with us!\n");
                break;
                
            case CHECK_BALANCE:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your current account balance:\n\n");
                body.append("Account Balance:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Available Balance: ৳ ").append(String.format("%.2f", data.balance)).append("\n");
                body.append("Inquiry Time: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                break;
                
            case DEPOSIT:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your deposit has been processed successfully.\n\n");
                body.append("Transaction Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Transaction ID: ").append(data.transactionId).append("\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Deposit Amount: ৳ ").append(String.format("%.2f", data.amount)).append("\n");
                body.append("Previous Balance: ৳ ").append(String.format("%.2f", data.previousBalance)).append("\n");
                body.append("New Balance: ৳ ").append(String.format("%.2f", data.balance)).append("\n");
                body.append("Description: ").append(data.description).append("\n");
                body.append("Date & Time: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("Thank you for your deposit!\n");
                break;
                
            case WITHDRAW:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your withdrawal has been processed successfully.\n\n");
                body.append("Transaction Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Transaction ID: ").append(data.transactionId).append("\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Withdrawal Amount: ৳ ").append(String.format("%.2f", data.amount)).append("\n");
                body.append("Previous Balance: ৳ ").append(String.format("%.2f", data.previousBalance)).append("\n");
                body.append("New Balance: ৳ ").append(String.format("%.2f", data.balance)).append("\n");
                body.append("Description: ").append(data.description).append("\n");
                body.append("Date & Time: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("If you did not authorize this withdrawal, please contact us immediately.\n");
                break;
                
            case FUND_TRANSFER:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your fund transfer has been completed successfully.\n\n");
                body.append("Transfer Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Transaction ID: ").append(data.transactionId).append("\n");
                body.append("From Account: ").append(data.accountNumber).append("\n");
                body.append("To Account: ").append(data.toAccountNumber).append("\n");
                body.append("Beneficiary: ").append(data.beneficiaryName).append("\n");
                body.append("Transfer Amount: ৳ ").append(String.format("%.2f", data.amount)).append("\n");
                body.append("Previous Balance: ৳ ").append(String.format("%.2f", data.previousBalance)).append("\n");
                body.append("New Balance: ৳ ").append(String.format("%.2f", data.balance)).append("\n");
                body.append("Description: ").append(data.description).append("\n");
                body.append("Date & Time: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("Thank you for using MY BANK transfer service!\n");
                break;
                
            case CHEQUE_REQUEST:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your cheque book request has been received.\n\n");
                body.append("Request Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Request ID: ").append(data.requestId).append("\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Number of Leaves: ").append(data.numberOfLeaves).append("\n");
                body.append("Request Date: ").append(data.timestamp).append("\n");
                body.append("Status: PENDING APPROVAL\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("Your request is being processed and will be reviewed shortly.\n");
                body.append("You will receive a confirmation email once approved.\n");
                break;
                
            case CHEQUE_APPROVED:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your cheque book request has been approved!\n\n");
                body.append("Cheque Book Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Cheque Book Number: ").append(data.chequeBookNumber).append("\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Number of Leaves: ").append(data.numberOfLeaves).append("\n");
                body.append("Starting Cheque Number: ").append(data.startingChequeNumber).append("\n");
                body.append("Ending Cheque Number: ").append(data.endingChequeNumber).append("\n");
                body.append("Issued By: ").append(data.issuedBy).append("\n");
                body.append("Approval Date: ").append(data.timestamp).append("\n");
                body.append("Status: ACTIVE\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("Your cheque book is ready for use.\n");
                body.append("Please collect it from your nearest branch or it will be delivered to your registered address.\n");
                break;
                
            case PASSWORD_CHANGE:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your account password has been changed successfully.\n\n");
                body.append("Change Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Changed On: ").append(data.timestamp).append("\n");
                body.append("Changed Via: Android Mobile App\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("If you did not make this change, please contact our support immediately.\n");
                body.append("Your account security is our top priority.\n");
                break;
                
            case PROFILE_PICTURE_CHANGE:
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("Your profile picture has been updated successfully.\n\n");
                body.append("Update Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Updated On: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("Your new profile picture is now visible in your account.\n");
                break;
                
            case SECURITY_ALERT:
                body.append("SECURITY ALERT\n\n");
                body.append("Dear ").append(data.customerName).append(",\n\n");
                body.append("⚠️ We have detected suspicious activity on your account.\n\n");
                body.append("Alert Details:\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("Account Number: ").append(data.accountNumber).append("\n");
                body.append("Activity Type: ").append(data.activityType).append("\n");
                body.append("Description: ").append(data.description).append("\n");
                body.append("Detected Time: ").append(data.timestamp).append("\n");
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                body.append("IMMEDIATE ACTION REQUIRED:\n");
                body.append("1. If this was you, you can ignore this message.\n");
                body.append("2. If you did not authorize this activity:\n");
                body.append("   - Change your password immediately\n");
                body.append("   - Contact our support team\n");
                body.append("   - Review your recent transactions\n\n");
                body.append("For assistance, please contact:\n");
                body.append("Customer Support: +880-XXX-XXXXXX\n");
                body.append("Email: support@mybank.com\n");
                break;
        }
        
        body.append("\n\n");
        body.append("Best Regards,\n");
        body.append("MY BANK Team\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("This is an automated email. Please do not reply.\n");
        body.append("For support: mybank.bankmanagement@gmail.com\n");
        
        return body.toString();
    }
    
    /**
     * Email Data class to hold all email parameters
     */
    public static class EmailData {
        public String customerName;
        public String accountNumber;
        public String phoneNumber;
        public String accountType;
        public double amount;
        public double balance;
        public double previousBalance;
        public String transactionId;
        public String toAccountNumber;
        public String beneficiaryName;
        public String description;
        public String timestamp;
        public String requestId;
        public int numberOfLeaves;
        public String chequeBookNumber;
        public String startingChequeNumber;
        public String endingChequeNumber;
        public String issuedBy;
        public String activityType;
        
        public EmailData() {
            // Default constructor
        }
        
        // Builder pattern for easy EmailData creation
        public static class Builder {
            private EmailData data = new EmailData();
            
            public Builder customerName(String customerName) {
                data.customerName = customerName;
                return this;
            }
            
            public Builder accountNumber(String accountNumber) {
                data.accountNumber = accountNumber;
                return this;
            }
            
            public Builder phoneNumber(String phoneNumber) {
                data.phoneNumber = phoneNumber;
                return this;
            }
            
            public Builder accountType(String accountType) {
                data.accountType = accountType;
                return this;
            }
            
            public Builder amount(double amount) {
                data.amount = amount;
                return this;
            }
            
            public Builder balance(double balance) {
                data.balance = balance;
                return this;
            }
            
            public Builder previousBalance(double previousBalance) {
                data.previousBalance = previousBalance;
                return this;
            }
            
            public Builder transactionId(String transactionId) {
                data.transactionId = transactionId;
                return this;
            }
            
            public Builder toAccountNumber(String toAccountNumber) {
                data.toAccountNumber = toAccountNumber;
                return this;
            }
            
            public Builder beneficiaryName(String beneficiaryName) {
                data.beneficiaryName = beneficiaryName;
                return this;
            }
            
            public Builder description(String description) {
                data.description = description;
                return this;
            }
            
            public Builder timestamp(String timestamp) {
                data.timestamp = timestamp;
                return this;
            }
            
            public Builder requestId(String requestId) {
                data.requestId = requestId;
                return this;
            }
            
            public Builder numberOfLeaves(int numberOfLeaves) {
                data.numberOfLeaves = numberOfLeaves;
                return this;
            }
            
            public Builder chequeBookNumber(String chequeBookNumber) {
                data.chequeBookNumber = chequeBookNumber;
                return this;
            }
            
            public Builder startingChequeNumber(String startingChequeNumber) {
                data.startingChequeNumber = startingChequeNumber;
                return this;
            }
            
            public Builder endingChequeNumber(String endingChequeNumber) {
                data.endingChequeNumber = endingChequeNumber;
                return this;
            }
            
            public Builder issuedBy(String issuedBy) {
                data.issuedBy = issuedBy;
                return this;
            }
            
            public Builder activityType(String activityType) {
                data.activityType = activityType;
                return this;
            }
            
            public EmailData build() {
                return data;
            }
        }
    }
}
