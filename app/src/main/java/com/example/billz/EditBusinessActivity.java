package com.example.billz;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executors;

public class EditBusinessActivity extends AppCompatActivity {

    private EditText editBusinessName, editBusinessMobile, editBusinessAddress, editBusinessCategory, editBusinessEmail;
    private BusinessProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_business);

        View toolbar = findViewById(R.id.toolbarEditBusiness);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        editBusinessName = findViewById(R.id.editBusinessName);
        editBusinessMobile = findViewById(R.id.editBusinessMobile);
        editBusinessAddress = findViewById(R.id.editBusinessAddress);
        editBusinessCategory = findViewById(R.id.editBusinessCategory);
        editBusinessEmail = findViewById(R.id.editBusinessEmail);

        profileRepository = new BusinessProfileRepository(this);
        
        loadBusinessProfile();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
    }

    private void loadBusinessProfile() {
        // Show immediate cached data
        BusinessProfile cached = profileRepository.getCachedProfile();
        populateFields(cached);

        // Load latest from Firestore
        profileRepository.loadBusinessProfile(new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                runOnUiThread(() -> populateFields(profile));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditBusinessActivity.this, "Failed to load profile: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(BusinessProfile profile) {
        if (profile == null) return;
        android.util.Log.d("SETUP", "EDIT_BUSINESS_LOADED");
        android.util.Log.d("SETUP", "BUSINESS_NAME = " + profile.getBusinessName());

        if (profile.getBusinessName() != null && !profile.getBusinessName().isEmpty())
            editBusinessName.setText(profile.getBusinessName());
        
        if (profile.getMobile() != null && !profile.getMobile().isEmpty())
            editBusinessMobile.setText(profile.getMobile());

        if (profile.getAddress() != null && !profile.getAddress().isEmpty())
            editBusinessAddress.setText(profile.getAddress());

        if (profile.getCategory() != null && !profile.getCategory().isEmpty())
            editBusinessCategory.setText(profile.getCategory());

        if (profile.getEmail() != null && !profile.getEmail().isEmpty())
            editBusinessEmail.setText(profile.getEmail());
    }

    private void saveProfile() {
        String name = editBusinessName.getText().toString().trim();
        String mobile = editBusinessMobile.getText().toString().trim();
        String address = editBusinessAddress.getText().toString().trim();
        String category = editBusinessCategory.getText().toString().trim();
        String email = editBusinessEmail.getText().toString().trim();
        
        if (name.isEmpty()) {
            editBusinessName.setError("Business name is required");
            return;
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("businessName", name);
        data.put("mobile", mobile);
        data.put("address", address);
        data.put("category", category);
        data.put("email", email);

        profileRepository.saveBusinessProfile(data, new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                runOnUiThread(() -> {
                    Toast.makeText(EditBusinessActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(EditBusinessActivity.this, "Update failed: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
