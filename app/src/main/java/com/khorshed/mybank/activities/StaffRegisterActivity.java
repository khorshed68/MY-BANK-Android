package com.khorshed.mybank.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.khorshed.mybank.R;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.viewmodel.AuthViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class StaffRegisterActivity extends AppCompatActivity {

    private EditText nameInput, usernameInput, emailInput, phoneInput;
    private EditText passwordInput, confirmPasswordInput;
    private AutoCompleteTextView roleInput;
    private MaterialButton registerButton, chooseImageButton, backToLoginButton;
    private ImageView profileImagePreview;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String base64ProfileImage; // Base64 encoded image string

    // Activity result launcher for image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    // Copy file to cache immediately to avoid URI permission issues
                    copyImageToCache();
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
        registerButton.setOnClickListener(v -> {
            // Convert image to Base64 if selected, then register
            if (selectedImageBitmap != null) {
                convertImageToBase64ThenRegister();
            } else {
                performRegistration();
            }
        });
        
        chooseImageButton.setOnClickListener(v -> {
            Toast.makeText(this, "Opening image picker...", Toast.LENGTH_SHORT).show();
            checkPermissionAndPickImage();
        });
        
        profileImagePreview.setOnClickListener(v -> checkPermissionAndPickImage());
        
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

        // Proceed with registration (image already converted to Base64 if selected)
        registerStaff(email, password, name, phone, role, username);
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

    /**
     * Copies the selected image to cache directory to avoid URI permission issues
     */
    private void copyImageToCache() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap != null) {
                // Save bitmap to cache
                File cacheFile = new File(getCacheDir(), "staff_profile_temp.jpg");
                FileOutputStream fos = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                
                // Load from cache
                selectedImageBitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                
                // Update UI
                Glide.with(this)
                    .load(selectedImageBitmap)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(profileImagePreview);
                    
                Toast.makeText(this, "✅ Image selected successfully!", Toast.LENGTH_SHORT).show();
                android.util.Log.d("StaffRegisterActivity", "Image loaded and cached successfully");
            }
        } catch (Exception e) {
            android.util.Log.e("StaffRegisterActivity", "Error loading image", e);
            Toast.makeText(this, "❌ Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Converts the selected image to Base64 string before registration
     * This method runs in background to avoid blocking UI
     */
    private void convertImageToBase64ThenRegister() {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        
        Toast.makeText(this, "Processing profile picture...", Toast.LENGTH_SHORT).show();
        android.util.Log.d("StaffRegisterActivity", "=== Converting image to Base64 ===");
        
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
                    android.util.Log.d("StaffRegisterActivity", "Compressed to quality " + quality + ": " + imageBytes.length + " bytes");
                }
                
                int finalSize = imageBytes.length;
                android.util.Log.d("StaffRegisterActivity", "Final image size: " + finalSize + " bytes");
                
                // Convert to Base64
                base64ProfileImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                int base64Size = base64ProfileImage.length();
                android.util.Log.d("StaffRegisterActivity", "Base64 string size: " + base64Size + " characters");
                
                // Check if Base64 string is within Firestore limits
                if (base64Size > 500 * 1024) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);
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
                    android.util.Log.d("StaffRegisterActivity", "✅ Image converted to Base64 successfully");
                    Toast.makeText(this, "✅ Image processed!", Toast.LENGTH_SHORT).show();
                    performRegistration();
                });
                
            } catch (OutOfMemoryError e) {
                android.util.Log.e("StaffRegisterActivity", "❌ Out of memory during compression", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
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
                android.util.Log.e("StaffRegisterActivity", "❌ Error converting image to Base64", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
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

    private void registerStaff(String email, String password, String name, 
                              String phone, String role, String username) {
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
                    if (base64ProfileImage != null && !base64ProfileImage.isEmpty()) {
                        staffUser.setProfileImageUrl(base64ProfileImage); // Store Base64 image
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
