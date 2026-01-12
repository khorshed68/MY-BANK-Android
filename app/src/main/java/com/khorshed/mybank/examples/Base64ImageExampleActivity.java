package com.khorshed.mybank.examples;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;
import com.khorshed.mybank.utils.ImageUtils;

/**
 * Complete Example: Profile Picture Upload using Base64 and Firebase Firestore
 * 
 * This activity demonstrates:
 * 1. Picking image from gallery with permission handling
 * 2. Converting image Uri to Base64 string
 * 3. Uploading Base64 string to Firebase Firestore
 * 4. Fetching Base64 string from Firestore
 * 5. Decoding Base64 to Bitmap
 * 6. Displaying Bitmap in ImageView
 */
public class Base64ImageExampleActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivProfilePicture;
    private Button btnPickImage;
    private Button btnUploadImage;
    private Button btnLoadImage;
    private Button btnRemoveImage;
    
    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    // Image handling
    private Uri selectedImageUri;
    private String base64ImageString;
    private ProgressDialog progressDialog;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base64_image_example);
        
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Initialize UI
        initializeViews();
        setupImagePickerLauncher();
        setupPermissionLauncher();
        setupClickListeners();
        
        // Load existing profile picture
        loadProfilePictureFromFirestore();
    }
    
    private void initializeViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        
        // Disable upload button initially
        btnUploadImage.setEnabled(false);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }
    
    /**
     * STEP 1: Setup Image Picker Launcher
     * Handles the result when user selects an image from gallery
     */
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    
                    if (selectedImageUri != null) {
                        convertUriToBase64();
                    }
                }
            }
        );
    }
    
    /**
     * STEP 2: Setup Permission Launcher
     * Handles permission request result
     */
    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, 
                        "Permission denied. Cannot access gallery.", 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    private void setupClickListeners() {
        btnPickImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnUploadImage.setOnClickListener(v -> uploadBase64ToFirestore());
        btnLoadImage.setOnClickListener(v -> loadProfilePictureFromFirestore());
        btnRemoveImage.setOnClickListener(v -> removeProfilePictureFromFirestore());
    }
    
    /**
     * STEP 3: Check Permission and Pick Image
     * Checks for appropriate permission based on Android version
     */
    private void checkPermissionAndPickImage() {
        String permission;
        
        // Android 13+ uses READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(this, permission) 
                == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }
    
    /**
     * STEP 4: Open Image Picker
     * Opens device gallery to select an image
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, 
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    /**
     * STEP 5: Convert URI to Base64
     * Converts selected image URI to Base64 string
     */
    private void convertUriToBase64() {
        progressDialog.setMessage("Converting image...");
        progressDialog.show();
        
        // Run conversion in background to avoid blocking UI
        new Thread(() -> {
            // Convert URI to Base64 with 75% quality
            base64ImageString = ImageUtils.convertUriToBase64(
                this, 
                selectedImageUri, 
                75  // Quality: 0-100
            );
            
            runOnUiThread(() -> {
                progressDialog.dismiss();
                
                if (base64ImageString != null) {
                    // Check size
                    float sizeKB = ImageUtils.getBase64SizeInKB(base64ImageString);
                    
                    // Recommended: Keep under 500KB for Firestore
                    if (sizeKB > 500) {
                        Toast.makeText(this, 
                            String.format("Warning: Image size (%.0f KB) is large. Consider using smaller image.", sizeKB),
                            Toast.LENGTH_LONG).show();
                    }
                    
                    // Convert Base64 back to Bitmap and display preview
                    Bitmap bitmap = ImageUtils.convertBase64ToBitmap(base64ImageString);
                    if (bitmap != null) {
                        ivProfilePicture.setImageBitmap(bitmap);
                        btnUploadImage.setEnabled(true);
                        
                        Toast.makeText(this, 
                            String.format("✅ Image converted (%.0f KB)", sizeKB),
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, 
                        "Failed to convert image", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    /**
     * STEP 6: Upload Base64 to Firestore
     * Stores Base64 string in Firebase Firestore database
     */
    private void uploadBase64ToFirestore() {
        if (base64ImageString == null || base64ImageString.isEmpty()) {
            Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.setMessage("Uploading to database...");
        progressDialog.show();
        
        // Upload Base64 string to Firestore
        firestore.collection("users")
            .document(user.getUid())
            .update("profileImageBase64", base64ImageString)
            .addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(this, 
                    "✅ Profile picture uploaded successfully!", 
                    Toast.LENGTH_SHORT).show();
                btnUploadImage.setEnabled(false);
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, 
                    "❌ Upload failed: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
    
    /**
     * STEP 7: Load Base64 from Firestore
     * Fetches Base64 string from Firebase Firestore and displays it
     */
    private void loadProfilePictureFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.setMessage("Loading profile picture...");
        progressDialog.show();
        
        // Fetch from Firestore
        firestore.collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                progressDialog.dismiss();
                
                if (documentSnapshot.exists() && 
                    documentSnapshot.contains("profileImageBase64")) {
                    
                    String base64Image = documentSnapshot.getString("profileImageBase64");
                    
                    if (base64Image != null && !base64Image.isEmpty()) {
                        // Decode Base64 to Bitmap
                        Bitmap bitmap = ImageUtils.convertBase64ToBitmap(base64Image);
                        
                        if (bitmap != null) {
                            ivProfilePicture.setImageBitmap(bitmap);
                            Toast.makeText(this, 
                                "✅ Profile picture loaded", 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, 
                                "Failed to decode image", 
                                Toast.LENGTH_SHORT).show();
                            setDefaultImage();
                        }
                    } else {
                        Toast.makeText(this, 
                            "No profile picture found", 
                            Toast.LENGTH_SHORT).show();
                        setDefaultImage();
                    }
                } else {
                    Toast.makeText(this, 
                        "No profile picture found", 
                        Toast.LENGTH_SHORT).show();
                    setDefaultImage();
                }
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, 
                    "❌ Failed to load: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
                setDefaultImage();
            });
    }
    
    /**
     * STEP 8: Remove Base64 from Firestore
     * Deletes profile picture from database
     */
    private void removeProfilePictureFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.setMessage("Removing profile picture...");
        progressDialog.show();
        
        // Set field to null to remove
        firestore.collection("users")
            .document(user.getUid())
            .update("profileImageBase64", null)
            .addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(this, 
                    "✅ Profile picture removed", 
                    Toast.LENGTH_SHORT).show();
                setDefaultImage();
                base64ImageString = null;
                btnUploadImage.setEnabled(false);
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, 
                    "❌ Failed to remove: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
    
    private void setDefaultImage() {
        ivProfilePicture.setImageResource(R.drawable.ic_person);
    }
}
