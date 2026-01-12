package com.khorshed.mybank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.models.Account;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.repository.AccountRepository;
import com.khorshed.mybank.repository.UserRepository;
import com.khorshed.mybank.services.EmailService;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();

    public AuthViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
        accountRepository = new AccountRepository();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsSuccess() {
        return isSuccess;
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public void register(String email, String password, String name, String phone) {
        isLoading.setValue(true);
        
        // For customers, use a standardized password pattern based on phone number
        String customerPassword = "CUSTOMER_" + phone;
        
        auth.createUserWithEmailAndPassword(email, customerPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {
                        User user = new User(firebaseUser.getUid(), email, name, phone, "CUSTOMER");
                        
                        userRepository.createUser(user, new UserRepository.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                // Create account for the user
                                accountRepository.createAccount(user.getUserId(), "SAVINGS", 
                                    new AccountRepository.OnAccountCreateListener() {
                                        @Override
                                        public void onSuccess(Account account) {
                                            // Send verification email
                                            firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    isLoading.setValue(false);
                                                    isSuccess.setValue(true);
                                                });
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            isLoading.setValue(false);
                                            errorMessage.setValue("Failed to create account: " + error);
                                        }
                                    });
                            }

                            @Override
                            public void onFailure(String error) {
                                isLoading.setValue(false);
                                errorMessage.setValue("Failed to create user profile: " + error);
                            }
                        });
                    }
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue(task.getException() != null ? 
                        task.getException().getMessage() : "Registration failed");
                }
            });
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful()) {
                    isSuccess.setValue(true);
                } else {
                    errorMessage.setValue(task.getException() != null ? 
                        task.getException().getMessage() : "Login failed");
                }
            });
    }

    public void loginWithPhone(String phone, String password) {
        isLoading.setValue(true);
        
        // First, find user by phone number to get their email
        userRepository.getUserByPhone(phone, new UserRepository.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getEmail() != null) {
                    // Now login with email and password
                    auth.signInWithEmailAndPassword(user.getEmail(), password)
                        .addOnCompleteListener(task -> {
                            isLoading.setValue(false);
                            if (task.isSuccessful()) {
                                isSuccess.setValue(true);
                            } else {
                                errorMessage.setValue(task.getException() != null ? 
                                    task.getException().getMessage() : "Login failed");
                            }
                        });
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("No account found with this phone number");
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Login failed: " + error);
            }
        });
    }

    // New method for password-less customer login (phone number only)
    public void loginWithPhoneOnly(String phone) {
        isLoading.setValue(true);
        
        // Find user by phone number
        userRepository.getUserByPhone(phone, new UserRepository.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getEmail() != null && "CUSTOMER".equals(user.getRole())) {
                    // For customers, create a temporary session using their phone number as identifier
                    // We'll sign in anonymously and then link to their account
                    // Or use a default password pattern
                    String defaultPassword = "CUSTOMER_" + phone; // Default pattern for customer accounts
                    
                    auth.signInWithEmailAndPassword(user.getEmail(), defaultPassword)
                        .addOnCompleteListener(task -> {
                            isLoading.setValue(false);
                            if (task.isSuccessful()) {
                                isSuccess.setValue(true);
                            } else {
                                errorMessage.setValue("Login failed. Please contact support.");
                            }
                        });
                } else if (user != null && !"CUSTOMER".equals(user.getRole())) {
                    isLoading.setValue(false);
                    errorMessage.setValue("This login method is only for customers. Please use the appropriate portal.");
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("No customer account found with this phone number");
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Login failed: " + error);
            }
        });
    }

    public void loginWithUsername(String username, String password) {
        isLoading.setValue(true);
        
        // First, find user by username to get their email
        userRepository.getUserByUsername(username, new UserRepository.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getEmail() != null) {
                    // Now login with email and password
                    auth.signInWithEmailAndPassword(user.getEmail(), password)
                        .addOnCompleteListener(task -> {
                            isLoading.setValue(false);
                            if (task.isSuccessful()) {
                                isSuccess.setValue(true);
                            } else {
                                String error = task.getException() != null ? 
                                    task.getException().getMessage() : "Login failed";
                                // Check if it's a wrong password
                                if (error != null && error.contains("password")) {
                                    errorMessage.setValue("Incorrect password. Please try again.");
                                } else {
                                    errorMessage.setValue(error);
                                }
                            }
                        });
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("No account found with username: " + username + ". Please check your username or register.");
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Login failed: " + error);
            }
        });
    }

    public void logout() {
        auth.signOut();
        isSuccess.setValue(true);
    }

    public void resetPassword(String email) {
        isLoading.setValue(true);
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful()) {
                    isSuccess.setValue(true);
                } else {
                    errorMessage.setValue(task.getException() != null ? 
                        task.getException().getMessage() : "Failed to send reset email");
                }
            });
    }

    public void changePassword(String currentPassword, String newPassword) {
        isLoading.setValue(true);
        FirebaseUser user = auth.getCurrentUser();
        
        if (user != null && user.getEmail() != null) {
            // Re-authenticate first
            auth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener(updateTask -> {
                                isLoading.setValue(false);
                                if (updateTask.isSuccessful()) {
                                    // Send password change email notification
                                    db.collection("users").document(user.getUid())
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            String userName = documentSnapshot.getString("name");
                                            String accountNumber = documentSnapshot.getString("accountNumber");
                                            
                                            if (user.getEmail() != null) {
                                                EmailService.EmailData emailData = new EmailService.EmailData.Builder()
                                                        .customerName(userName != null ? userName : "Customer")
                                                        .accountNumber(accountNumber != null ? accountNumber : "N/A")
                                                        .timestamp(new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date()))
                                                        .build();
                                                
                                                EmailService.sendEmail(user.getEmail(), EmailService.NotificationType.PASSWORD_CHANGE, emailData);
                                            }
                                        });
                                    
                                    isSuccess.setValue(true);
                                } else {
                                    errorMessage.setValue(updateTask.getException() != null ? 
                                        updateTask.getException().getMessage() : "Failed to update password");
                                }
                            });
                    } else {
                        isLoading.setValue(false);
                        errorMessage.setValue("Current password is incorrect");
                    }
                });
        } else {
            isLoading.setValue(false);
            errorMessage.setValue("User not authenticated");
        }
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
