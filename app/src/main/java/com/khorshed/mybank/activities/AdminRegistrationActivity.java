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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class AdminRegistrationActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText fullNameInput, usernameInput, emailInput, phoneInput;
    private EditText passwordInput, confirmPasswordInput;
    private Button uploadPictureButton, registerButton, backToLoginButton;
    
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String base64ProfileImage; // Base64 encoded image string
    private ProgressDialog progressDialog;
    
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_registration);
        
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
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
        registerButton.setOnClickListener(v -> {
            // Convert image to Base64 if selected, then register
            if (selectedImageBitmap != null) {
                convertImageToBase64ThenRegister();
            } else {
                performRegistration();
            }
        });
        profileImageView.setOnClickListener(v -> checkPermissionAndPickImage());
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
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap != null) {
                // Save bitmap to cache
                File cacheFile = new File(getCacheDir(), "admin_profile_temp.jpg");
                FileOutputStream fos = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                
                // Load from cache
                selectedImageBitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                
                // Update UI
                profileImageView.setImageBitmap(selectedImageBitmap);
                Toast.makeText(this, "✅ Image selected successfully!", Toast.LENGTH_SHORT).show();
                android.util.Log.d("AdminRegistrationActivity", "Image loaded and cached successfully");
            }
        } catch (Exception e) {
            android.util.Log.e("AdminRegistrationActivity", "Error loading image", e);
            Toast.makeText(this, "❌ Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        
        // Proceed with registration (image already converted to Base64 if selected)
        registerAdmin(fullName, username, email, phone, password);
    }
    
    /**
     * Converts the selected image to Base64 string before registration
     * This method runs in background to avoid blocking UI
     */
    private void convertImageToBase64ThenRegister() {
        progressDialog.setMessage("Processing profile picture...");
        progressDialog.show();
        
        android.util.Log.d("AdminRegistrationActivity", "=== Converting image to Base64 ===");
        
        // Run compression and Base64 conversion in background thread
        new Thread(() -> {
            try {
                // Compress bitmap to JPEG with quality adjustment
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int quality = 75; // Start with 75% quality
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                byte[] imageBytes = baos.toByteArray();
                
                // Reduce quality if image is too large (target: ~400 KB for Base64)
                while (imageBytes.length > 400 * 1024 && quality > 30) {
                    baos.reset();
                    quality -= 10;
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    imageBytes = baos.toByteArray();
                    android.util.Log.d("AdminRegistrationActivity", "Compressed to quality " + quality + ": " + imageBytes.length + " bytes");
                }
                
                int finalSize = imageBytes.length;
                android.util.Log.d("AdminRegistrationActivity", "Final image size: " + finalSize + " bytes");
                
                // Convert to Base64
                base64ProfileImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                int base64Size = base64ProfileImage.length();
                android.util.Log.d("AdminRegistrationActivity", "Base64 string size: " + base64Size + " characters");
                
                // Check if Base64 string is within Firestore limits
                if (base64Size > 500 * 1024) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        new AlertDialog.Builder(this)
                            .setTitle("Image Too Large")
                            .setMessage("Image file is too large even after compression. Please select a smaller image or continue without profile picture.")
                            .setPositiveButton("Continue Without Image", (d, w) -> {
                                base64ProfileImage = null;
                                selectedImageBitmap = null;
                                performRegistration();
                            })
                            .setNegativeButton("Try Another Image", null)
                            .show();
                    });
                    return;
                }
                
                // Success - proceed with registration on UI thread
                runOnUiThread(() -> {
                    android.util.Log.d("AdminRegistrationActivity", "✅ Image converted to Base64 successfully");
                    Toast.makeText(this, "✅ Image processed!", Toast.LENGTH_SHORT).show();
                    performRegistration();
                });
                
            } catch (OutOfMemoryError e) {
                android.util.Log.e("AdminRegistrationActivity", "❌ Out of memory during compression", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(this)
                        .setTitle("Memory Error")
                        .setMessage("Image is too large to process. Please select a smaller image or continue without profile picture.")
                        .setPositiveButton("Continue Without Image", (d, w) -> {
                            base64ProfileImage = null;
                            selectedImageBitmap = null;
                            performRegistration();
                        })
                        .setNegativeButton("Try Another Image", null)
                        .show();
                });
            } catch (Exception e) {
                android.util.Log.e("AdminRegistrationActivity", "❌ Error converting image to Base64", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(this)
                        .setTitle("Processing Error")
                        .setMessage("Failed to process image: " + e.getMessage() + "\n\nContinue without profile picture?")
                        .setPositiveButton("Continue Without Image", (d, w) -> {
                            base64ProfileImage = null;
                            selectedImageBitmap = null;
                            performRegistration();
                        })
                        .setNegativeButton("Try Another Image", null)
                        .show();
                });
            }
        }).start();
    }
    
    private void registerAdmin(String fullName, String username, String email, 
                              String phone, String password) {
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
                    
                    if (base64ProfileImage != null && !base64ProfileImage.isEmpty()) {
                        adminUser.setProfileImageUrl(base64ProfileImage); // Store Base64 image
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
