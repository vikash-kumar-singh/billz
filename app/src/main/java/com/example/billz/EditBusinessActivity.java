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

import java.util.concurrent.Executors;

public class EditBusinessActivity extends AppCompatActivity {

    private EditText editBusinessName, editBusinessMobile;
    private ReceiptSettings currentSettings;

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
        
        loadSettings();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentSettings = AppDatabase.getInstance(this).receiptSettingsDao().getSettings();
            if (currentSettings == null) {
                currentSettings = new ReceiptSettings();
            }
            runOnUiThread(() -> {
                editBusinessName.setText(currentSettings.getBusinessName());
                editBusinessMobile.setText(currentSettings.getPhoneNumber());
            });
        });
    }

    private void saveSettings() {
        String name = editBusinessName.getText().toString().trim();
        String mobile = editBusinessMobile.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Business Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSettings.setBusinessName(name);
        currentSettings.setPhoneNumber(mobile);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).receiptSettingsDao().insert(currentSettings);
            runOnUiThread(() -> {
                Toast.makeText(this, "Business saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
