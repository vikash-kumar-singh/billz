package com.example.billz;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class AddBusinessActivity extends AppCompatActivity {

    private EditText editBusinessName, editMobile;
    private TextView textType, textCountry, textTimeZone, textCurrency, textNumberSystem, textDecimalPlaces, textSeparatorFormat;
    private ImageView checkName, checkType, checkCountry, checkTimeZone, checkCurrency, checkNumberSystem, checkDecimalPlaces, checkSeparatorFormat;
    private CheckBox checkAgree;
    private boolean isUpdate = false;
    private ReceiptSettings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_business);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddBusiness);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnDelete = findViewById(R.id.btnDeleteBusiness);
        View layoutAgree = findViewById(R.id.layoutAgree);
        View layoutLegal = findViewById(R.id.layoutLegal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarAddBusinessLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        toolbar.setNavigationOnClickListener(v -> finish());

        editBusinessName = findViewById(R.id.editBusinessName);
        editMobile = findViewById(R.id.editMobile);
        textType = findViewById(R.id.textType);
        textCountry = findViewById(R.id.textCountry);
        textTimeZone = findViewById(R.id.textTimeZone);
        textCurrency = findViewById(R.id.textCurrency);
        textNumberSystem = findViewById(R.id.textNumberSystem);
        textDecimalPlaces = findViewById(R.id.textDecimalPlaces);
        textSeparatorFormat = findViewById(R.id.textSeparatorFormat);

        checkName = findViewById(R.id.checkName);
        checkType = findViewById(R.id.checkType);
        checkCountry = findViewById(R.id.checkCountry);
        checkTimeZone = findViewById(R.id.checkTimeZone);
        checkCurrency = findViewById(R.id.checkCurrency);
        checkNumberSystem = findViewById(R.id.checkNumberSystem);
        checkDecimalPlaces = findViewById(R.id.checkDecimalPlaces);
        checkSeparatorFormat = findViewById(R.id.checkSeparatorFormat);

        checkAgree = findViewById(R.id.checkAgree);

        if (getIntent().hasExtra("business_name")) {
            isUpdate = true;
            String name = getIntent().getStringExtra("business_name");
            
            toolbar.setTitle("UPDATE");
            btnSave.setText("Update");
            btnDelete.setVisibility(View.VISIBLE);
            layoutAgree.setVisibility(View.GONE);
            layoutLegal.setVisibility(View.GONE);

            // Fetch current settings from DB
            Executors.newSingleThreadExecutor().execute(() -> {
                currentSettings = AppDatabase.getInstance(this).receiptSettingsDao().getSettings();
                runOnUiThread(() -> {
                    if (currentSettings != null) {
                        editBusinessName.setText(currentSettings.getBusinessName());
                        editMobile.setText(currentSettings.getPhoneNumber());
                    } else {
                        // Fallback to dummy data if DB is empty
                        editBusinessName.setText(name);
                        editMobile.setText("7903598844");
                    }
                    
                    // Set all checkmarks to blue for Update mode
                    int blue = Color.parseColor("#3F51B5");
                    checkName.setColorFilter(blue);
                    checkType.setColorFilter(blue);
                    checkCountry.setColorFilter(blue);
                    checkTimeZone.setColorFilter(blue);
                    checkCurrency.setColorFilter(blue);
                    checkNumberSystem.setColorFilter(blue);
                    checkDecimalPlaces.setColorFilter(blue);
                    checkSeparatorFormat.setColorFilter(blue);

                    textType.setText("Retail");
                    textType.setTextColor(Color.BLACK);
                    textDecimalPlaces.setText("2");
                    textSeparatorFormat.setText("₹ 99,99,999.00");
                });
            });
        }

        btnSave.setOnClickListener(v -> saveBusiness());
        btnDelete.setOnClickListener(v -> deleteBusiness());
    }

    private void deleteBusiness() {
        String name = editBusinessName.getText().toString().trim();
        if (name.isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.businessDao().deleteByName(name);
            
            // Check if any businesses are left
            List<Business> remaining = db.businessDao().getAllBusinesses();
            if (!remaining.isEmpty()) {
                // Switch to the first remaining business
                Business switchTarget = remaining.get(0);
                db.businessDao().deselectAll();
                db.businessDao().selectBusiness(switchTarget.getName());
                
                // Update active ReceiptSettings to reflect the switch
                ReceiptSettings settings = db.receiptSettingsDao().getSettings();
                if (settings == null) settings = new ReceiptSettings();
                settings.setBusinessName(switchTarget.getName());
                settings.setPhoneNumber(switchTarget.getPhoneNumber());
                db.receiptSettingsDao().insert(settings);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Business Deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void saveBusiness() {
        String name = editBusinessName.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter business name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isUpdate && checkAgree != null && !checkAgree.isChecked()) {
            Toast.makeText(this, "Please agree to Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Update/Insert in Business table
            AppDatabase db = AppDatabase.getInstance(this);
            if (!isUpdate) {
                db.businessDao().deselectAll();
            }
            Business newBusiness = new Business(name, mobile, "OWNER", true);
            db.businessDao().insert(newBusiness);

            // Update active ReceiptSettings
            ReceiptSettings settings = db.receiptSettingsDao().getSettings();
            if (settings == null) {
                settings = new ReceiptSettings();
            }
            settings.setBusinessName(name);
            settings.setPhoneNumber(mobile);
            db.receiptSettingsDao().insert(settings);

            runOnUiThread(() -> {
                String msg = isUpdate ? "Business updated successfully" : "Business created successfully";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
