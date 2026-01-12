package com.khorshed.mybank.activities.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khorshed.mybank.R;

import java.util.HashMap;
import java.util.Map;

public class CreateStaffActivity extends AppCompatActivity {

    private TextInputEditText usernameInput, fullNameInput, emailInput, passwordInput, roleInput;
    private MaterialButton backButton, createButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For now, just show a toast - you can create a full layout later
        Toast.makeText(this, "Create Staff - Coming Soon", Toast.LENGTH_SHORT).show();
        finish();
    }
}
