package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editMobile;
    private BusinessProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editMobile = findViewById(R.id.editMobile);
        View btnUpdate = findViewById(R.id.btnUpdate);

        // Email is not editable as per business mail requirement
        editEmail.setEnabled(false);
        editEmail.setFocusable(false);
        editEmail.setClickable(false);

        profileRepository = new BusinessProfileRepository(this);
        loadProfileData();

        btnUpdate.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        // 1. Show immediate cached data for better UX
        BusinessProfile cached = profileRepository.getCachedProfile();
        populateFields(cached);

        // 2. Load latest from Firestore to ensure accuracy
        profileRepository.loadBusinessProfile(new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                runOnUiThread(() -> populateFields(profile));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, "Failed to sync: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(BusinessProfile profile) {
        if (profile == null) return;
        editName.setText(profile.getBusinessName());
        editEmail.setText(profile.getEmail());
        editMobile.setText(profile.getMobile());
    }

    private void saveProfileData() {
        String name = editName.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Name is required");
            return;
        }

        // Prepare update map (Email is excluded to keep it constant)
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("businessName", name);
        data.put("mobile", mobile);

        findViewById(R.id.btnUpdate).setEnabled(false);

        profileRepository.saveBusinessProfile(data, new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    findViewById(R.id.btnUpdate).setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Update failed: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
