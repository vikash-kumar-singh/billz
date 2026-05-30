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
    private ReceiptSettings settings;

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

        loadProfileData();

        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            settings = AppDatabase.getInstance(this).receiptSettingsDao().getSettings();
            runOnUiThread(() -> {
                if (settings != null) {
                    editName.setText(settings.getBusinessName());
                    editEmail.setText(settings.getEmail());
                }
            });
        });
    }

    private void saveProfileData() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (settings == null) {
                settings = new ReceiptSettings();
            }
            settings.setBusinessName(name);
            settings.setEmail(email);
            db.receiptSettingsDao().insert(settings);

            runOnUiThread(() -> {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
