package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class EditBusinessActivity extends AppCompatActivity {

    private static final String TAG = "EditBusiness";

    private EditText editName, editAddress, editCategory, editMobile, editEmail;
    private AutoCompleteTextView autoCountry, autoTimezone, autoBusinessType, autoCurrency, autoNumberSystem, autoDecimalPlaces;
    private TextView textPlan, textRole, textStatus;
    private BusinessProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_business);

        initViews();
        setupDropdowns();
        
        profileRepository = new BusinessProfileRepository(this);
        loadProfile();

        findViewById(R.id.btnUpdateBottom).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btnDeleteBusiness).setOnClickListener(v -> showDeleteConfirmation());
        
        MaterialToolbar toolbar = findViewById(R.id.toolbarEditBusiness);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarEdit), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void initViews() {
        editName = findViewById(R.id.editBusinessName);
        editAddress = findViewById(R.id.editBusinessAddress);
        editCategory = findViewById(R.id.editBusinessCategory);
        editMobile = findViewById(R.id.editBusinessMobile);
        editEmail = findViewById(R.id.editBusinessEmail);

        autoCountry = findViewById(R.id.autoCountry);
        autoTimezone = findViewById(R.id.autoTimezone);
        autoBusinessType = findViewById(R.id.autoBusinessType);
        autoCurrency = findViewById(R.id.autoCurrency);
        autoNumberSystem = findViewById(R.id.autoNumberSystem);
        autoDecimalPlaces = findViewById(R.id.autoDecimalPlaces);

        textPlan = findViewById(R.id.textPlan);
        textRole = findViewById(R.id.textRole);
        textStatus = findViewById(R.id.textStatus);
    }

    private void setupDropdowns() {
        String[] countries = {"India", "USA", "UAE", "UK", "Australia"};
        String[] timezones = {"Asia/Kolkata", "UTC", "GMT", "America/New_York", "Europe/London"};
        String[] businessTypes = {"Retail", "Wholesale", "Service", "Restaurant", "Medical", "Gym", "Electronics", "Other"};
        String[] currencies = {"INR", "USD", "AED", "EUR", "GBP"};
        String[] numberSystems = {"Indian", "International"};
        String[] decimals = {"0", "1", "2", "3"};

        autoCountry.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries));
        autoTimezone.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, timezones));
        autoBusinessType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, businessTypes));
        autoCurrency.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencies));
        autoNumberSystem.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, numberSystems));
        autoDecimalPlaces.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, decimals));
    }

    private void loadProfile() {
        Log.d(TAG, "PROFILE_LOAD_STARTED");
        
        // Load from Room DB for the currently selected business
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Business selected = db.businessDao().getSelectedBusiness();
            
            runOnUiThread(() -> {
                if (selected != null) {
                    editName.setText(selected.getName());
                    editMobile.setText(selected.getPhoneNumber());
                    editEmail.setText(selected.getEmail());
                    editCategory.setText(selected.getCategory());
                    textPlan.setText(selected.getPlan() != null ? selected.getPlan() : "FREE");
                    textRole.setText(selected.getRole() != null ? selected.getRole() : "OWNER");
                    textStatus.setText(selected.getStatus() != null ? selected.getStatus() : "ACTIVE");

                    // Load ReceiptSettings for THIS specific business
                    Executors.newSingleThreadExecutor().execute(() -> {
                        ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(selected.getId());
                        runOnUiThread(() -> {
                            if (settings != null) {
                                editAddress.setText(settings.getBusinessAddress());
                            } else {
                                editAddress.setText(""); // Default if no settings for this business yet
                            }
                        });
                    });
                }
            });
        });

        // Still attempt Firestore pull to update local data if needed, but local is priority for switching
        profileRepository.loadBusinessProfile(new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                // If the firestore profile matches our local selected business name, update UI
                runOnUiThread(() -> {
                    if (editName.getText().toString().equals(profile.getBusinessName())) {
                        populateUI(profile);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "PROFILE_LOAD_FAILED: " + message);
            }
        });
    }

    private void populateUI(BusinessProfile profile) {
        if (profile == null) return;

        editName.setText(profile.getBusinessName());
        editAddress.setText(profile.getAddress());
        editCategory.setText(profile.getCategory());
        editMobile.setText(profile.getMobile());
        editEmail.setText(profile.getEmail());

        autoCountry.setText(profile.getCountry() != null ? profile.getCountry() : "India", false);
        autoTimezone.setText(profile.getTimezone() != null ? profile.getTimezone() : "Asia/Kolkata", false);
        autoBusinessType.setText(profile.getBusinessType() != null ? profile.getBusinessType() : "Retail", false);
        autoCurrency.setText(profile.getCurrency() != null ? profile.getCurrency() : "INR", false);
        autoNumberSystem.setText(profile.getNumberSystem() != null ? profile.getNumberSystem() : "Indian", false);
        autoDecimalPlaces.setText(String.valueOf(profile.getDecimalPlaces()), false);

        textPlan.setText(profile.getPlan() != null ? profile.getPlan() : "FREE");
        textRole.setText(profile.getRole() != null ? profile.getRole() : "OWNER");
        textStatus.setText(profile.getStatus() != null ? profile.getStatus() : "ACTIVE");
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) {
            editName.setError("Business Name is required");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("businessName", name);
        data.put("address", editAddress.getText().toString().trim());
        data.put("category", editCategory.getText().toString().trim());
        data.put("mobile", editMobile.getText().toString().trim());
        data.put("email", editEmail.getText().toString().trim());
        data.put("country", autoCountry.getText().toString());
        data.put("timezone", autoTimezone.getText().toString());
        data.put("businessType", autoBusinessType.getText().toString());
        data.put("currency", autoCurrency.getText().toString());
        data.put("numberSystem", autoNumberSystem.getText().toString());
        
        try {
            data.put("decimalPlaces", Integer.parseInt(autoDecimalPlaces.getText().toString()));
        } catch (Exception e) {
            data.put("decimalPlaces", 2);
        }

        findViewById(R.id.btnUpdateBottom).setEnabled(false);

        profileRepository.saveBusinessProfile(data, new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                // Also update local database and ReceiptSettings
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(EditBusinessActivity.this);
                    Business selected = db.businessDao().getSelectedBusiness();
                    if (selected != null) {
                        selected.setName(name);
                        selected.setPhoneNumber(editMobile.getText().toString().trim());
                        selected.setEmail(editEmail.getText().toString().trim());
                        selected.setCategory(editCategory.getText().toString().trim());
                        db.businessDao().update(selected);

                        // Update ReceiptSettings Address for THIS SPECIFIC business
                        ReceiptSettings rs = db.receiptSettingsDao().getSettingsByBusiness(selected.getId());
                        if (rs == null) {
                            rs = new ReceiptSettings();
                            rs.setId(selected.getId());
                        }
                        rs.setBusinessName(name);
                        rs.setPhoneNumber(editMobile.getText().toString().trim());
                        rs.setEmail(editEmail.getText().toString().trim());
                        rs.setBusinessAddress(editAddress.getText().toString().trim());
                        db.receiptSettingsDao().insert(rs);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(EditBusinessActivity.this, "Business profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    findViewById(R.id.btnUpdateBottom).setEnabled(true);
                    Toast.makeText(EditBusinessActivity.this, "Update failed: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Business")
                .setMessage("Are you sure you want to delete this business? This action cannot be undone.")
                .setPositiveButton("DELETE", (dialog, which) -> deleteBusiness())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void deleteBusiness() {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        findViewById(R.id.btnDeleteBusiness).setEnabled(false);

        // Delete from Firestore
        com.google.firebase.firestore.DocumentReference doc = FirebaseHelper.getUserDoc();
        if (doc == null) return;
        
        doc.delete()
                .addOnSuccessListener(aVoid -> {
                    // Clear local cache
                    new PreferenceManager(this).clear();
                    
                    // Clear Room
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(this).clearAllTables();
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Business deleted and session cleared", Toast.LENGTH_LONG).show();
                            // Redirect to Splash or Login
                            Intent intent = new Intent(this, SplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.btnDeleteBusiness).setEnabled(true);
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
