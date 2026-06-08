package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etBusinessName, etEmail, etMobile, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etBusinessName = findViewById(R.id.etBusinessName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBar = new ProgressBar(this); // Would be better in XML, but adding here for simplicity

        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = etFullName.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name is required");
            return;
        }
        if (TextUtils.isEmpty(businessName)) {
            etBusinessName.setError("Business name is required");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return;
        }
        if (TextUtils.isEmpty(mobile) || mobile.length() < 10) {
            etMobile.setError("Valid mobile number is required");
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Show loading
        findViewById(R.id.btnRegister).setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = FirebaseHelper.getCurrentUid();
                        saveUserProfile(uid, businessName, email, mobile);
                    } else {
                        findViewById(R.id.btnRegister).setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserProfile(String uid, String businessName, String email, String mobile) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("businessName", businessName);
        user.put("email", email);
        user.put("mobile", mobile);
        user.put("plan", "FREE");
        user.put("setupCompleted", false);
        user.put("createdAt", com.google.firebase.Timestamp.now());

        // We use a Transaction or a merged set to ensure everything is created
        // Path: users/{uid}
        db.collection("users").document(uid).set(new HashMap<String, Object>() {{
            put("uid", uid);
            put("createdAt", com.google.firebase.Timestamp.now());
        }}).addOnSuccessListener(v -> {
            // Path: users/{uid}/profile/info
            db.collection("users").document(uid)
                    .collection("profile").document("info")
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        PreferenceManager preferenceManager = new PreferenceManager(this);
                        preferenceManager.setBusinessSetupCompleted(false);
                        
                        startActivity(new Intent(RegisterActivity.this, BusinessSetupActivity.class));
                        finishAffinity();
                    })
                    .addOnFailureListener(e -> {
                        findViewById(R.id.btnRegister).setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Profile info failed. Check Firestore Rules: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }).addOnFailureListener(e -> {
            findViewById(R.id.btnRegister).setEnabled(true);
            Toast.makeText(RegisterActivity.this, "User doc failed. Check Firestore Rules: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }
}
