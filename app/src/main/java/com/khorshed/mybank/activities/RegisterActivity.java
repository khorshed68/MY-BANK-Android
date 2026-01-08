package com.khorshed.mybank.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.AccountApplication;
import com.khorshed.mybank.viewmodel.AuthViewModel;

import java.util.Date;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private EditText addressInput, identityNumberInput, initialDepositInput;
    private EditText passwordInput, confirmPasswordInput;
    private AutoCompleteTextView identityTypeInput, accountTypeInput;
    private Button registerButton, chooseImageButton;
    private ImageView profileImageView;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    
    // Activity result launcher for image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    // Display selected image
                    Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_logo)
                        .into(profileImageView);
                    Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );
    
    // Permission launcher
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        identityTypeInput = findViewById(R.id.identityTypeInput);
        identityNumberInput = findViewById(R.id.identityNumberInput);
        accountTypeInput = findViewById(R.id.accountTypeInput);
        initialDepositInput = findViewById(R.id.initialDepositInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        progressBar = findViewById(R.id.progressBar);
        
        setupDropdowns();
    }
    
    private void setupDropdowns() {
        // Setup Identity Document Type dropdown
        String[] identityTypes = {"National ID", "Passport", "Driving License"};
        ArrayAdapter<String> identityAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            identityTypes
        );
        identityTypeInput.setAdapter(identityAdapter);
        
        // Setup Account Type dropdown
        String[] accountTypes = {"SAVINGS", "CURRENT"};
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            accountTypes
        );
        accountTypeInput.setAdapter(accountAdapter);
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            registerButton.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getIsSuccess().observe(this, success -> {
            if (success != null && success) {
                new AlertDialog.Builder(this)
                        .setTitle("Registration Successful!")
                        .setMessage("Your account application has been submitted successfully.\n\n" +
                                "✅ Your application is now pending approval\n" +
                                "✅ You will receive an email once approved\n" +
                                "✅ Use your phone number and password to login after approval\n\n" +
                                "Thank you for choosing MY BANK!")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> {
            // Upload image first if selected, then register
            if (selectedImageUri != null) {
                uploadImageThenRegister();
            } else {
                performRegistration();
            }
        });
        
        chooseImageButton.setOnClickListener(v -> {
            Toast.makeText(this, "Opening image picker...", Toast.LENGTH_SHORT).show();
            checkPermissionAndPickImage();
        });
        
        profileImageView.setOnClickListener(v -> {
            checkPermissionAndPickImage();
        });
        
        findViewById(R.id.loginLink).setOnClickListener(v -> finish());
    }
    
    private void uploadImageThenRegister() {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        
        String fileName = "customer_profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);
        
        storageRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrl = uri.toString();
                    performRegistration();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Image uploaded but URL failed. Registering without image.", 
                        Toast.LENGTH_SHORT).show();
                    performRegistration();
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Image upload failed. Registering without profile picture.", 
                    Toast.LENGTH_SHORT).show();
                performRegistration();
            });
    }

    private void performRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String identityType = identityTypeInput.getText().toString().trim();
        String identityNumber = identityNumberInput.getText().toString().trim();
        String accountType = accountTypeInput.getText().toString().trim();
        String initialDeposit = initialDepositInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone is required");
            phoneInput.requestFocus();
            return;
        }
        
        if (address.isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }
        
        if (identityType.isEmpty()) {
            identityTypeInput.setError("Identity document type is required");
            identityTypeInput.requestFocus();
            return;
        }
        
        if (identityNumber.isEmpty()) {
            identityNumberInput.setError("Identity number is required");
            identityNumberInput.requestFocus();
            return;
        }
        
        if (accountType.isEmpty()) {
            accountTypeInput.setError("Account type is required");
            accountTypeInput.requestFocus();
            return;
        }
        
        if (initialDeposit.isEmpty()) {
            initialDepositInput.setError("Initial deposit is required");
            initialDepositInput.requestFocus();
            return;
        }
        
        double depositAmount = Double.parseDouble(initialDeposit);
        if (depositAmount < 1000) {
            initialDepositInput.setError("Minimum deposit is TAKA 1000");
            initialDepositInput.requestFocus();
            return;
        }
        
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Submit application for approval (don't create Firebase Auth user yet)
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        
        // Generate a temporary userId for the application
        String tempUserId = UUID.randomUUID().toString();
        
        // Submit account application with password stored for later
        submitAccountApplication(tempUserId, name, email, phone, address, 
            identityType, identityNumber, accountType, depositAmount, password);
    }
    
    private void submitAccountApplication(String userId, String name, String email, 
                                         String phone, String address, String identityType, 
                                         String identityNumber, String accountType, 
                                         double initialDeposit, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Create account application
        String applicationId = UUID.randomUUID().toString();
        
        AccountApplication application = new AccountApplication();
        application.setApplicationId(applicationId);
        application.setUserId(userId);
        application.setName(name);
        application.setEmail(email);
        application.setPhone(phone);
        application.setAddress(address);
        application.setIdentityType(identityType);
        application.setIdentityNumber(identityNumber);
        application.setAccountType(accountType);
        application.setInitialDeposit(initialDeposit);
        application.setPassword(password); // Store password for later use
        application.setProfileImageUrl(uploadedImageUrl); // Store profile image URL
        application.setStatus("PENDING");
        application.setSubmittedAt(new Date());
        
        db.collection("account_applications").document(applicationId)
            .set(application)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                
                new AlertDialog.Builder(this)
                    .setTitle("Application Submitted Successfully!")
                    .setMessage("Thank you for choosing MY BANK!\n\n" +
                            "✅ Your account application has been submitted\n" +
                            "✅ Your application is pending staff approval\n" +
                            "✅ You will receive an email once approved\n" +
                            "✅ You can login after approval using:\n" +
                            "   - Phone: " + phone + "\n" +
                            "   - Your chosen password\n\n" +
                            "Please wait for approval notification.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(this, "Application submission failed: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
    
    private void checkPermissionAndPickImage() {
        String permission;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            Toast.makeText(this, "Requesting permission...", Toast.LENGTH_SHORT).show();
            permissionLauncher.launch(permission);
        }
    }
    
    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening image picker: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }
}
