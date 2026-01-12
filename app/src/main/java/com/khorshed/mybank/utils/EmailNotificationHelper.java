package com.khorshed.mybank.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.services.EmailService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class for sending email notifications for various banking operations
 */
public class EmailNotificationHelper {
    
    private static final SimpleDateFormat dateFormatter = 
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    
    /**
     * Send balance check notification
     */
    public static void sendBalanceCheckEmail(String userEmail, String userName, 
                                              String accountNumber, double balance) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .balance(balance)
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.CHECK_BALANCE, emailData);
    }
    
    /**
     * Send deposit notification
     */
    public static void sendDepositEmail(String userEmail, String userName, String accountNumber,
                                        double amount, double previousBalance, double newBalance,
                                        String transactionId, String description) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .amount(amount)
                .previousBalance(previousBalance)
                .balance(newBalance)
                .transactionId(transactionId)
                .description(description != null ? description : "Deposit")
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.DEPOSIT, emailData);
    }
    
    /**
     * Send withdrawal notification
     */
    public static void sendWithdrawEmail(String userEmail, String userName, String accountNumber,
                                         double amount, double previousBalance, double newBalance,
                                         String transactionId, String description) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .amount(amount)
                .previousBalance(previousBalance)
                .balance(newBalance)
                .transactionId(transactionId)
                .description(description != null ? description : "Withdrawal")
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.WITHDRAW, emailData);
    }
    
    /**
     * Send fund transfer notification
     */
    public static void sendTransferEmail(String userEmail, String userName, String fromAccountNumber,
                                         String toAccountNumber, String beneficiaryName, double amount,
                                         double previousBalance, double newBalance, String transactionId,
                                         String description) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(fromAccountNumber)
                .toAccountNumber(toAccountNumber)
                .beneficiaryName(beneficiaryName)
                .amount(amount)
                .previousBalance(previousBalance)
                .balance(newBalance)
                .transactionId(transactionId)
                .description(description != null ? description : "Fund Transfer")
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.FUND_TRANSFER, emailData);
    }
    
    /**
     * Send cheque book request notification
     */
    public static void sendChequeRequestEmail(String userEmail, String userName, String accountNumber,
                                              String requestId, int numberOfLeaves) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .requestId(requestId)
                .numberOfLeaves(numberOfLeaves)
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.CHEQUE_REQUEST, emailData);
    }
    
    /**
     * Send cheque book approval notification
     */
    public static void sendChequeApprovedEmail(String userEmail, String userName, String accountNumber,
                                               String chequeBookNumber, int numberOfLeaves,
                                               String startingChequeNumber, String endingChequeNumber,
                                               String issuedBy) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .chequeBookNumber(chequeBookNumber)
                .numberOfLeaves(numberOfLeaves)
                .startingChequeNumber(startingChequeNumber)
                .endingChequeNumber(endingChequeNumber)
                .issuedBy(issuedBy)
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.CHEQUE_APPROVED, emailData);
    }
    
    /**
     * Send password change notification
     */
    public static void sendPasswordChangeEmail(String userEmail, String userName, String accountNumber) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.PASSWORD_CHANGE, emailData);
    }
    
    /**
     * Send profile picture change notification
     */
    public static void sendProfilePictureChangeEmail(String userEmail, String userName, String accountNumber) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.PROFILE_PICTURE_CHANGE, emailData);
    }
    
    /**
     * Send security alert notification
     */
    public static void sendSecurityAlertEmail(String userEmail, String userName, String accountNumber,
                                              String activityType, String description) {
        EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                .customerName(userName)
                .accountNumber(accountNumber)
                .activityType(activityType)
                .description(description)
                .timestamp(dateFormatter.format(new Date()))
                .build();
        
        EmailService.sendEmail(userEmail, EmailService.NotificationType.SECURITY_ALERT, emailData);
    }
    
    /**
     * Get current user information and send email for transaction
     */
    public static void sendTransactionEmailWithUserInfo(String transactionType, String accountId,
                                                        double amount, double previousBalance,
                                                        double newBalance, String transactionId,
                                                        String description, String toAccountNumber,
                                                        String beneficiaryName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.getString("name");
                    String userEmail = userDoc.getString("email");
                    
                    FirebaseFirestore.getInstance()
                            .collection("accounts")
                            .document(accountId)
                            .get()
                            .addOnSuccessListener(accountDoc -> {
                                String accountNumber = accountDoc.getString("accountNumber");
                                
                                switch (transactionType) {
                                    case "DEPOSIT":
                                        sendDepositEmail(userEmail, userName, accountNumber, amount,
                                                previousBalance, newBalance, transactionId, description);
                                        break;
                                    case "WITHDRAWAL":
                                        sendWithdrawEmail(userEmail, userName, accountNumber, amount,
                                                previousBalance, newBalance, transactionId, description);
                                        break;
                                    case "TRANSFER":
                                        sendTransferEmail(userEmail, userName, accountNumber, toAccountNumber,
                                                beneficiaryName, amount, previousBalance, newBalance,
                                                transactionId, description);
                                        break;
                                }
                            });
                });
    }
}
