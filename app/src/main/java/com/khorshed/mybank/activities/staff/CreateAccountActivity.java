package com.khorshed.mybank.activities.staff;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.AccountApplication;

import java.util.Date;
import java.util.UUID;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput, addressInput;
    private EditText identityNumberInput, initialDepositInput;
    private Spinner identityTypeSpinner, accountTypeSpinner;
    private Button submitButton, backButton, chooseImageButton;
    private ImageView profileImageView;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .into(profileImageView);
                        Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initializeViews();
        setupFirebase();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        identityNumberInput = findViewById(R.id.identityNumberInput);
        initialDepositInput = findViewById(R.id.initialDepositInput);
        identityTypeSpinner = findViewById(R.id.identityTypeSpinner);
        accountTypeSpinner = findViewById(R.id.accountTypeSpinner);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupSpinners() {
        String[] identityTypes = {"National ID", "Passport", "Driving License"};
        ArrayAdapter<String> identityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, identityTypes);
        identityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identityTypeSpinner.setAdapter(identityAdapter);

        String[] accountTypes = {"SAVINGS", "CURRENT"};
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, accountTypes);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(accountAdapter);
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageThenSubmit();
            } else {
                submitApplication();
            }
        });

        backButton.setOnClickListener(v -> finish());

        chooseImageButton.setOnClickListener(v -> checkPermissionAndPickImage());
    }

    private void checkPermissionAndPickImage() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageThenSubmit() {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        String fileName = "customer_profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadedImageUrl = uri.toString();
                        submitApplication();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Image uploaded but URL failed. Submitting without image.",
                                Toast.LENGTH_SHORT).show();
                        submitApplication();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed. Submitting without profile picture.",
                            Toast.LENGTH_SHORT).show();
                    submitApplication();
                });
    }

    private void submitApplication() {
        if (!validateInputs()) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            return;
        }

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String identityType = identityTypeSpinner.getSelectedItem().toString();
        String identityNumber = identityNumberInput.getText().toString().trim();
        String accountType = accountTypeSpinner.getSelectedItem().toString();
        double initialDeposit = Double.parseDouble(initialDepositInput.getText().toString().trim());

        // Auto-generate password
        String autoPassword = "CUSTOMER_" + phone;

        // Create user in Firebase Auth
        auth.createUserWithEmailAndPassword(email, autoPassword)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    createUserProfile(userId, name, email, phone, address, identityType,
                            identityNumber, accountType, initialDeposit);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createUserProfile(String userId, String name, String email, String phone,
                                    String address, String identityType, String identityNumber,
                                    String accountType, double initialDeposit) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create user profile
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("userId", userId);
        userData.put("email", email);
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("role", "CUSTOMER");
        userData.put("isActive", true);
        userData.put("biometricEnabled", false);
        userData.put("notificationsEnabled", true);
        userData.put("createdAt", new Date());

        if (uploadedImageUrl != null) {
            userData.put("profileImageUrl", uploadedImageUrl);
        }

        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    createAccountApplication(userId, name, email, phone, address,
                            identityType, identityNumber, accountType, initialDeposit);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Profile creation failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createAccountApplication(String userId, String name, String email, String phone,
                                           String address, String identityType, String identityNumber,
                                           String accountType, double initialDeposit) {
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
        application.setStatus("PENDING");
        application.setSubmittedAt(new Date());

        db.collection("account_applications")
                .document(applicationId)
                .set(application)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Application submission failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs() {
        if (nameInput.getText().toString().trim().isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return false;
        }

        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (phoneInput.getText().toString().trim().isEmpty()) {
            phoneInput.setError("Phone is required");
            phoneInput.requestFocus();
            return false;
        }

        if (addressInput.getText().toString().trim().isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return false;
        }

        if (identityNumberInput.getText().toString().trim().isEmpty()) {
            identityNumberInput.setError("Identity number is required");
            identityNumberInput.requestFocus();
            return false;
        }

        if (initialDepositInput.getText().toString().trim().isEmpty()) {
            initialDepositInput.setError("Initial deposit is required");
            initialDepositInput.requestFocus();
            return false;
        }

        double deposit = Double.parseDouble(initialDepositInput.getText().toString().trim());
        if (deposit < 1000) {
            initialDepositInput.setError("Minimum deposit is ৳1000");
            initialDepositInput.requestFocus();
            return false;
        }

        return true;
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Application Submitted!")
                .setMessage("Customer account application has been submitted successfully.\n\n" +
                        "✅ Application is pending approval\n" +
                        "✅ Customer will receive email once approved\n\n" +
                        "Thank you!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
