package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPhone;
    private Spinner spinnerRole, spinnerPlan, spinnerStatus;
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
        editPhone = findViewById(R.id.editPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerPlan = findViewById(R.id.spinnerPlan);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        setupSpinners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        toolbar.setNavigationOnClickListener(v -> finish());

        loadProfileData();

        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void setupSpinners() {
        String[] roles = {"OWNER", "MANAGER", "STAFF"};
        String[] plans = {"FREE", "PREMIUM", "ENTERPRISE"};
        String[] statuses = {"ACTIVE", "EXPIRED", "SUSPENDED"};

        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles));
        spinnerPlan.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, plans));
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses));
    }

    private void loadProfileData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            settings = AppDatabase.getInstance(this).receiptSettingsDao().getSettings();
            runOnUiThread(() -> {
                if (settings != null) {
                    editName.setText(settings.getBusinessName());
                    editEmail.setText(settings.getEmail());
                    editPhone.setText(settings.getPhoneNumber());

                    setSpinnerSelection(spinnerRole, settings.getRole());
                    setSpinnerSelection(spinnerPlan, settings.getPlanType());
                    setSpinnerSelection(spinnerStatus, settings.getStatus());
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value.toUpperCase());
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void saveProfileData() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String plan = spinnerPlan.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

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
            settings.setPhoneNumber(phone);
            settings.setRole(role);
            settings.setPlanType(plan);
            settings.setStatus(status);
            db.receiptSettingsDao().insert(settings);

            // Also update the Business table if applicable
            Business selectedBusiness = null;
            List<Business> businesses = db.businessDao().getAllBusinesses();
            for (Business b : businesses) {
                if (b.isSelected()) {
                    b.setName(name);
                    b.setEmail(email);
                    b.setPhoneNumber(phone);
                    b.setRole(role);
                    b.setPlanType(plan);
                    b.setStatus(status);
                    db.businessDao().update(b);
                    break;
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
