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

    private EditText editName, editEmail;
    private BusinessProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditProfile);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        toolbar.setNavigationOnClickListener(v -> finish());

        profileRepository = new BusinessProfileRepository(this);
        loadProfileData();

        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        BusinessProfile cached = profileRepository.getCachedProfile();
        editName.setText(cached.getBusinessName());
        editEmail.setText(cached.getEmail());

        profileRepository.loadBusinessProfile(new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                android.util.Log.d("SETUP", "EDIT_PROFILE_LOADED");
                runOnUiThread(() -> {
                    editName.setText(profile.getBusinessName());
                    editEmail.setText(profile.getEmail());
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, "Load failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("businessName", name);
        data.put("email", email);

        profileRepository.saveBusinessProfile(data, new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Update failed: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
