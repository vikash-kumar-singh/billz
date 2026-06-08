package com.example.billz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // User is logged in
                PreferenceManager preferenceManager = new PreferenceManager(this);
                if (preferenceManager.isBusinessSetupCompleted()) {
                    startActivity(new Intent(SplashActivity.this, ReportsActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, BusinessSetupActivity.class));
                }
            } else {
                // User is not logged in
                PreferenceManager preferenceManager = new PreferenceManager(this);
                if (preferenceManager.isFirstLaunch()) {
                    // First time launch - go to Language Selection
                    startActivity(new Intent(SplashActivity.this, LanguageSelectionActivity.class));
                } else {
                    // Not first time, but logged out - go to LoginActivity (Requirement 8)
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }
            finish();
        }, 1500); // 1.5 seconds delay
    }
}
