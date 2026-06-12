package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BusinessSetupActivity extends AppCompatActivity {

    private TextInputEditText etBusinessName, etBusinessAddress, etBusinessCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_setup);

        etBusinessName = findViewById(R.id.etBusinessName);
        etBusinessAddress = findViewById(R.id.etBusinessAddress);
        etBusinessCategory = findViewById(R.id.etBusinessCategory);

        findViewById(R.id.btnCompleteSetup).setOnClickListener(v -> completeSetup());
    }

    private void completeSetup() {
        String name = etBusinessName.getText().toString().trim();
        String address = etBusinessAddress.getText().toString().trim();
        String category = etBusinessCategory.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etBusinessName.setError("Business name is required");
            return;
        }

        findViewById(R.id.btnCompleteSetup).setEnabled(false);

        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        java.util.Map<String, Object> businessData = new java.util.HashMap<>();
        businessData.put("businessName", name);
        businessData.put("address", address);
        businessData.put("category", category);
        businessData.put("email", currentUserEmail);
        businessData.put("role", "OWNER");
        businessData.put("plan", "FREE");
        businessData.put("status", "ACTIVE");
        businessData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        businessData.put("setupCompleted", true);

        new BusinessProfileRepository(this).saveBusinessProfile(businessData, new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(BusinessSetupActivity.this);
                    db.businessDao().deselectAll();
                    
                    Business localBusiness = new Business(name, "", "OWNER", true);
                    localBusiness.setCategory(category);
                    localBusiness.setEmail(currentUserEmail);
                    db.businessDao().insert(localBusiness);

                    runOnUiThread(() -> {
                        PreferenceManager preferenceManager = new PreferenceManager(BusinessSetupActivity.this);
                        preferenceManager.setBusinessSetupCompleted(true);
                        preferenceManager.setFirstLaunch(false);

                        startActivity(new Intent(BusinessSetupActivity.this, ReportsActivity.class));
                        finishAffinity();
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    findViewById(R.id.btnCompleteSetup).setEnabled(true);
                    Toast.makeText(BusinessSetupActivity.this, "Setup failed: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
