package com.khorshed.mybank.activities.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.khorshed.mybank.R;

public class StaffActivityLogActivity extends AppCompatActivity {

    private MaterialButton backButton;
    private TextView staffNameText;
    private RecyclerView activityRecyclerView;
    private FirebaseFirestore db;
    private String staffUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get staff info from intent
        staffUsername = getIntent().getStringExtra("staffUsername");
        String staffName = getIntent().getStringExtra("staffName");
        
        // For now, just show a toast - you can create a full layout later
        Toast.makeText(this, "Activity Log for " + staffName + " - Coming Soon", Toast.LENGTH_SHORT).show();
        finish();
    }
}
