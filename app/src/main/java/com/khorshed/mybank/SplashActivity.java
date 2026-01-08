package com.khorshed.mybank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.khorshed.mybank.activities.LoginActivity;
import com.khorshed.mybank.activities.customer.CustomerDashboardActivity;
import com.khorshed.mybank.activities.staff.StaffDashboardActivity;
import com.khorshed.mybank.activities.admin.AdminDashboardActivity;
import com.khorshed.mybank.models.User;
import com.khorshed.mybank.repository.UserRepository;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private FirebaseAuth auth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndNavigate, SPLASH_DELAY);
    }

    private void checkUserAndNavigate() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            userRepository.getCurrentUser().observe(this, user -> {
                if (user == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }

                Intent intent;
                switch (user.getRole()) {
                    case "CUSTOMER":
                        intent = new Intent(this, CustomerDashboardActivity.class);
                        break;
                    case "STAFF":
                        intent = new Intent(this, StaffDashboardActivity.class);
                        break;
                    case "ADMIN":
                        intent = new Intent(this, AdminDashboardActivity.class);
                        break;
                    default:
                        intent = new Intent(this, LoginActivity.class);
                        break;
                }
                startActivity(intent);
                finish();
            });
        }
    }
}
