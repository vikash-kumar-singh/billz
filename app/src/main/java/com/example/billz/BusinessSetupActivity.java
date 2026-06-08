package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
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

        Map<String, Object> businessData = new HashMap<>();
        businessData.put("businessName", name);
        businessData.put("address", address);
        businessData.put("category", category);
        businessData.put("setupCompleted", true);

        // Using helper for consistent document path: users/{uid}/profile/info
        FirebaseHelper.getUserProfileInfo().set(businessData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    PreferenceManager preferenceManager = new PreferenceManager(this);
                    preferenceManager.setBusinessSetupCompleted(true);
                    preferenceManager.setFirstLaunch(false);

                    startActivity(new Intent(BusinessSetupActivity.this, ReportsActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.btnCompleteSetup).setEnabled(true);
                    Toast.makeText(BusinessSetupActivity.this, "Setup failed. Check Firestore Rules: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.e("SETUP", "Firestore Task Failed", task.getException());
                    }
                });
    }
}
