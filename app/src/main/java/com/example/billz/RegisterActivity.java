package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etBusinessName, etEmail, etMobile, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etFullName = findViewById(R.id.etFullName);
        etBusinessName = findViewById(R.id.etBusinessName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
        
        // Add Google Sign In Button if it exists in layout, or use the one from Login logic
        View btnGoogle = findViewById(R.id.btnGoogleSignUp);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> signInWithGoogle());
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                android.util.Log.e("RegisterActivity", "Google sign in failed. Code: " + e.getStatusCode(), e);
                Toast.makeText(this, "Google signup failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = FirebaseHelper.getCurrentUid();
                        // For Google, we might not have business name yet, so we'll pass null and handle it in setup
                        checkAndSaveGoogleProfile(uid, mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getEmail());
                    } else {
                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndSaveGoogleProfile(String uid, String name, String email) {
        db.collection("users").document(uid).collection("profile").document("info").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // New user via Google
                        saveUserProfile(uid, "", email, "");
                    } else {
                        // Existing user logging in via Register screen (redirect to login flow)
                        new BusinessProfileRepository(this).loadBusinessProfile(new BusinessProfileRepository.ProfileCallback() {
                            @Override
                            public void onProfileLoaded(BusinessProfile profile) {
                                navigateBasedOnProfile(profile);
                            }

                            @Override
                            public void onError(String message) {
                                startActivity(new Intent(RegisterActivity.this, BusinessSetupActivity.class));
                                finishAffinity();
                            }
                        });
                    }
                });
    }

    private void navigateBasedOnProfile(BusinessProfile profile) {
        PreferenceManager preferenceManager = new PreferenceManager(this);
        
        // Ensure local DB is in sync
        saveToLocalDatabase(profile.getUid(), profile.getBusinessName(), profile.getEmail(), profile.getMobile());
        
        if (profile.isSetupCompleted()) {
            preferenceManager.setBusinessSetupCompleted(true);
            startActivity(new Intent(RegisterActivity.this, ReportsActivity.class));
        } else {
            preferenceManager.setBusinessSetupCompleted(false);
            startActivity(new Intent(RegisterActivity.this, BusinessSetupActivity.class));
        }
        finishAffinity();
    }

    private void saveToLocalDatabase(String uid, String businessName, String email, String mobile) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase localDb = AppDatabase.getInstance(this);
            
            // Sync to Business table
            Business b = localDb.businessDao().getSelectedBusiness();
            if (b == null) {
                b = new Business(businessName != null ? businessName : "My Business", mobile, "OWNER", true);
            } else {
                b.setName(businessName != null ? businessName : b.getName());
                b.setPhoneNumber(mobile != null ? mobile : b.getPhoneNumber());
                b.setEmail(email != null ? email : b.getEmail());
            }
            localDb.businessDao().insert(b);

            // Sync to ReceiptSettings
            ReceiptSettings rs = localDb.receiptSettingsDao().getSettingsByBusiness(b.getId());
            if (rs == null) {
                rs = new ReceiptSettings();
                rs.setId(b.getId());
                rs.setBusinessName(b.getName());
                rs.setPhoneNumber(b.getPhoneNumber());
                rs.setEmail(b.getEmail());
                localDb.receiptSettingsDao().insert(rs);
            }
        });
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
                        
                        // Local Database Sync
                        saveToLocalDatabase(uid, businessName, email, mobile);
                        
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
