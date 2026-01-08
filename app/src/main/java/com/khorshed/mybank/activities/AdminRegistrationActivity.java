package com.khorshed.mybank.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class AdminRegistrationActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText fullNameInput, usernameInput, emailInput, phoneInput;
    private EditText passwordInput, confirmPasswordInput;
    private Button uploadPictureButton, registerButton, backToLoginButton;
    
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String uploadedImageUrl;
    private ProgressDialog progressDialog;
    
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_registration);
        
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        
        initViews();
        setupImagePicker();
        setupClickListeners();
    }
    
    private void initViews() {
        profileImageView = findViewById(R.id.adminProfileImage);
        fullNameInput = findViewById(R.id.adminFullNameInput);
        usernameInput = findViewById(R.id.adminUsernameInput);
        emailInput = findViewById(R.id.adminEmailInput);
        phoneInput = findViewById(R.id.adminPhoneInput);
        passwordInput = findViewById(R.id.adminPasswordInput);
        confirmPasswordInput = findViewById(R.id.adminConfirmPasswordInput);
        uploadPictureButton = findViewById(R.id.adminUploadPictureButton);
        registerButton = findViewById(R.id.adminRegisterButton);
        backToLoginButton = findViewById(R.id.adminBackToLoginButton);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        loadSelectedImage();
                    }
                }
            }
        );
        
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void setupClickListeners() {
        uploadPictureButton.setOnClickListener(v -> checkPermissionAndPickImage());
        registerButton.setOnClickListener(v -> performRegistration());
        backToLoginButton.setOnClickListener(v -> finish());
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
            permissionLauncher.launch(permission);
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    private void loadSelectedImage() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
            
            // Check file size (limit to 2MB)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            
            float sizeMB = data.length / (1024f * 1024f);
            
            if (data.length > 2 * 1024 * 1024) {
                Toast.makeText(this, String.format("❌ Image size (%.2f MB) exceeds 2 MB limit", sizeMB), 
                    Toast.LENGTH_LONG).show();
                selectedImageBitmap = null;
                selectedImageUri = null;
                return;
            }
            
            profileImageView.setImageBitmap(selectedImageBitmap);
            Toast.makeText(this, String.format("✅ Image selected (%.2f MB)", sizeMB), Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to load image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void performRegistration() {
        String fullName = fullNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Full name is required");
            fullNameInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }
        
        if (username.length() < 4) {
            usernameInput.setError("Username must be at least 4 characters");
            usernameInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
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
        
        // Upload image if selected, then register
        if (selectedImageBitmap != null) {
            uploadImageAndRegister(fullName, username, email, phone, password);
        } else {
            registerAdmin(fullName, username, email, phone, password, null);
        }
    }
    
    private void uploadImageAndRegister(String fullName, String username, String email, 
                                       String phone, String password) {
        progressDialog.setMessage("Uploading profile picture...");
        progressDialog.show();
        
        String fileName = "admin_profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference().child(fileName);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();
        
        float sizeMB = data.length / (1024f * 1024f);
        progressDialog.setMessage(String.format("Uploading %.2f MB...", sizeMB));
        
        storageRef.putBytes(data)
            .addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressDialog.setMessage(String.format("Uploading: %.0f%%", progress));
            })
            .addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrl = uri.toString();
                    Toast.makeText(this, "✅ Profile picture uploaded!", Toast.LENGTH_SHORT).show();
                    registerAdmin(fullName, username, email, phone, password, uploadedImageUrl);
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image uploaded but URL retrieval failed. Registering without image.", 
                        Toast.LENGTH_SHORT).show();
                    registerAdmin(fullName, username, email, phone, password, null);
                });
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Image upload failed: " + e.getMessage() + ". Registering without image.", 
                    Toast.LENGTH_LONG).show();
                registerAdmin(fullName, username, email, phone, password, null);
            });
    }
    
    private void registerAdmin(String fullName, String username, String email, 
                              String phone, String password, String imageUrl) {
        progressDialog.setMessage("Creating admin account...");
        progressDialog.show();
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    String userId = task.getResult().getUser().getUid();
                    
                    // Create admin user document in Firestore with ACTIVE status
                    User adminUser = new User(userId, email, fullName, phone, "ADMIN");
                    adminUser.setActive(true); // Admin accounts are created as ACTIVE
                    adminUser.setUsername(username);
                    
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        adminUser.setProfileImageUrl(imageUrl);
                    }
                    
                    // Save to Firestore
                    firestore.collection("users")
                        .document(userId)
                        .set(adminUser)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            
                            // Send email verification
                            task.getResult().getUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        Toast.makeText(this, 
                                            "✅ Admin account created successfully! Please verify your email.", 
                                            Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, 
                                            "✅ Admin account created! (Email verification failed to send)", 
                                            Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                });
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, 
                                "❌ Failed to create admin profile: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                } else {
                    progressDialog.dismiss();
                    String errorMsg = task.getException() != null ? 
                        task.getException().getMessage() : "Registration failed";
                    Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
    }
}
