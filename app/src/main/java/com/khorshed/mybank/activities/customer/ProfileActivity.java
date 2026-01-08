package com.khorshed.mybank.activities.customer;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.khorshed.mybank.R;
import com.khorshed.mybank.viewmodel.AuthViewModel;
import com.khorshed.mybank.viewmodel.CustomerViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private CustomerViewModel customerViewModel;
    private AuthViewModel authViewModel;
    
    private TextView tvAccountNumber, tvName, tvEmail, tvPhone;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView ivCurrentPicture, ivNewPicture;
    private TextView tvNoNewImage;
    private Button btnChangePassword, btnChoosePicture, btnSaveChanges, btnRemovePicture, btnBack;
    
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private ProgressDialog progressDialog;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initializeViews();
        initializeViewModels();
        setupImagePicker();
        loadUserProfile();
        setupClickListeners();
    }
    
    private void initializeViews() {
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        ivCurrentPicture = findViewById(R.id.ivCurrentPicture);
        ivNewPicture = findViewById(R.id.ivNewPicture);
        tvNoNewImage = findViewById(R.id.tvNoNewImage);
        
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChoosePicture = findViewById(R.id.btnChoosePicture);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnRemovePicture = findViewById(R.id.btnRemovePicture);
        btnBack = findViewById(R.id.btnBack);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }
    
    private void initializeViewModels() {
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Observe AuthViewModel states
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });
        
        authViewModel.getIsSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                clearPasswordFields();
            }
        });
        
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
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
                    Toast.makeText(this, "❌ Permission denied. Cannot select image.", Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    private void loadUserProfile() {
        customerViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                tvName.setText(user.getName());
                tvEmail.setText(user.getEmail());
                tvPhone.setText(user.getPhone());
                
                // Load profile picture if exists
                loadProfilePicture(user.getUserId());
            }
        });
        
        customerViewModel.getCurrentAccount().observe(this, account -> {
            if (account != null) {
                tvAccountNumber.setText(account.getAccountNumber());
            }
        });
    }
    
    private void setupClickListeners() {
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnChoosePicture.setOnClickListener(v -> chooseNewPicture());
        btnSaveChanges.setOnClickListener(v -> saveProfilePicture());
        btnRemovePicture.setOnClickListener(v -> removeProfilePicture());
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "Please enter current password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("Are you sure you want to change your password?")
            .setPositiveButton("Yes", (dialog, which) -> {
                authViewModel.changePassword(currentPassword, newPassword);
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void clearPasswordFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }
    
    private void chooseNewPicture() {
        checkPermissionAndPickImage();
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
        Toast.makeText(this, "📷 Opening image picker...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, String.format("❌ Image size (%.2f MB) exceeds 2 MB limit", sizeMB), Toast.LENGTH_LONG).show();
                resetImagePreview();
                return;
            }
            
            ivNewPicture.setImageBitmap(selectedImageBitmap);
            tvNoNewImage.setVisibility(TextView.GONE);
            btnSaveChanges.setEnabled(true);
            Toast.makeText(this, String.format("✅ Image selected (%.2f MB)", sizeMB), Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to load image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetImagePreview();
        }
    }
    
    private void saveProfilePicture() {
        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Please choose an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.setMessage("Uploading profile picture...");
        progressDialog.show();
        
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
            .child("profile_pictures/" + user.getUid() + ".jpg");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();
        
        // Show upload size
        float sizeMB = data.length / (1024f * 1024f);
        progressDialog.setMessage(String.format("Uploading %.2f MB...", sizeMB));
        
        storageRef.putBytes(data)
            .addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressDialog.setMessage(String.format("Uploading: %.0f%%", progress));
            })
            .addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(this, "✅ Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                ivCurrentPicture.setImageBitmap(selectedImageBitmap);
                resetImagePreview();
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "❌ Failed to upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void removeProfilePicture() {
        new AlertDialog.Builder(this)
            .setTitle("🗑️ Remove Profile Picture")
            .setMessage("Are you sure you want to remove your profile picture?")
            .setPositiveButton("Yes", (dialog, which) -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;
                
                progressDialog.setMessage("Removing profile picture...");
                progressDialog.show();
                
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_pictures/" + user.getUid() + ".jpg");
                
                storageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "✅ Profile picture removed successfully", Toast.LENGTH_SHORT).show();
                        ivCurrentPicture.setImageResource(R.drawable.ic_person);
                        resetImagePreview();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        // Don't show error if file doesn't exist - just reset to default
                        if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
                            Toast.makeText(this, "Profile picture already removed", Toast.LENGTH_SHORT).show();
                            ivCurrentPicture.setImageResource(R.drawable.ic_person);
                            resetImagePreview();
                        } else {
                            Toast.makeText(this, "❌ Failed to remove: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void loadProfilePicture(String userId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
            .child("profile_pictures/" + userId + ".jpg");
        
        storageRef.getBytes(2 * 1024 * 1024)
            .addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ivCurrentPicture.setImageBitmap(bitmap);
            })
            .addOnFailureListener(e -> {
                // Keep default image if profile picture doesn't exist
            });
    }
    
    private void resetImagePreview() {
        selectedImageUri = null;
        selectedImageBitmap = null;
        ivNewPicture.setImageResource(R.drawable.ic_person);
        tvNoNewImage.setVisibility(TextView.VISIBLE);
        btnSaveChanges.setEnabled(false);
    }
}
