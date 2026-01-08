package com.khorshed.mybank.activities.staff;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.khorshed.mybank.R;

public class ProcessDepositActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Toast.makeText(this, "Process Deposit - Coming Soon", Toast.LENGTH_SHORT).show();
    }
}
