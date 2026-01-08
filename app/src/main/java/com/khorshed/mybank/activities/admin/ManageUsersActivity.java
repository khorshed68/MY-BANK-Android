package com.khorshed.mybank.activities.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.khorshed.mybank.R;

public class ManageUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Toast.makeText(this, "Manage Users - Coming Soon", Toast.LENGTH_SHORT).show();
    }
}
