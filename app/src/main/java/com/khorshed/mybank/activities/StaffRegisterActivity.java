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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.viewmodel.AuthViewModel;

import java.util.UUID;

public class StaffRegisterActivity extends AppCompatActivity {

    private EditText nameInput, usernameInput, emailInput, phoneInput;
    private EditText passwordInput, confirmPasswordInput;
    private AutoCompleteTextView roleInput;
    private MaterialButton registerButton, chooseImageButton, backToLoginButton;
    private ImageView profileImagePreview;
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
                    Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(profileImagePreview);
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
        setContentView(R.layout.activity_staff_register);

        initViews();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupDropdowns();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.staffNameInput);
        usernameInput = findViewById(R.id.staffUsernameInput);
        emailInput = findViewById(R.id.staffEmailInput);
        phoneInput = findViewById(R.id.staffPhoneInput);
        roleInput = findViewById(R.id.staffRoleInput);
        passwordInput = findViewById(R.id.staffPasswordInput);
        confirmPasswordInput = findViewById(R.id.staffConfirmPasswordInput);
        registerButton = findViewById(R.id.staffRegisterButton);
        chooseImageButton = findViewById(R.id.staffChooseImageButton);
        backToLoginButton = findViewById(R.id.staffBackToLoginButton);
        profileImagePreview = findViewById(R.id.staffProfileImagePreview);
        progressBar = findViewById(R.id.staffProgressBar);
    }

    private void setupDropdowns() {
        String[] roles = {"TELLER", "OFFICER", "MANAGER"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            roles
        );
        roleInput.setAdapter(roleAdapter);
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
                Toast.makeText(this, 
                    "✅ Registration successful! Your account is now active. You can login immediately.", 
                    Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> performRegistration());
        
        chooseImageButton.setOnClickListener(v -> checkPermissionAndPickImage());
        
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = nameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String role = roleInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
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

        if (role.isEmpty()) {
            roleInput.setError("Role is required");
            roleInput.requestFocus();
            return;
        }

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

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Profile picture is optional - upload if selected, otherwise register without image
        if (selectedImageUri != null) {
            uploadImageAndRegister(email, password, name, phone, role, username);
        } else {
            // Register without profile picture (optional)
            registerStaff(email, password, name, phone, role, username, null);
        }
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageAndRegister(String email, String password, String name, 
                                       String phone, String role, String username) {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        String fileName = "staff_profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrl = uri.toString();
                    Toast.makeText(this, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show();
                    registerStaff(email, password, name, phone, role, username, uploadedImageUrl);
                }).addOnFailureListener(e -> {
                    // If getting URL fails, register without image
                    Toast.makeText(this, "Image uploaded but URL retrieval failed. Registering without image.", 
                        Toast.LENGTH_SHORT).show();
                    registerStaff(email, password, name, phone, role, username, null);
                });
            })
            .addOnFailureListener(e -> {
                // If upload fails, continue registration without image
                Toast.makeText(this, "Image upload failed: " + e.getMessage() + ". Registering without profile picture.", 
                    Toast.LENGTH_LONG).show();
                registerStaff(email, password, name, phone, role, username, null);
            });
    }

    private void registerStaff(String email, String password, String name, 
                              String phone, String role, String username, String imageUrl) {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        
        // Create Firebase Authentication account
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    String userId = task.getResult().getUser().getUid();
                    
                    // Create user document in Firestore with STAFF role
                    User staffUser = new User(userId, email, name, phone, role.toUpperCase());
                    staffUser.setUsername(username); // Set username for login
                    staffUser.setActive(true); // Staff accounts are immediately active - no approval needed
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        staffUser.setProfileImageUrl(imageUrl);
                    }
                    
                    // Save to Firestore
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(staffUser)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(this, 
                                "Staff account created successfully! You can now login.", 
                                Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(this, 
                                "Failed to create staff profile: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                } else {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    String errorMsg = task.getException() != null ? 
                        task.getException().getMessage() : "Registration failed";
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
            });
    }
}
