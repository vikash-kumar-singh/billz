package com.example.billz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.Executors;

public class AddBusinessActivity extends AppCompatActivity {

    private EditText editBusinessName, editMobile;
    private TextView textType, textCountry, textTimeZone, textCurrency, textNumberSystem, textDecimalPlaces, textSeparatorFormat;
    private ImageView checkName, checkType, checkCountry, checkTimeZone, checkCurrency, checkNumberSystem, checkDecimalPlaces, checkSeparatorFormat;
    private CheckBox checkAgree;
    private DrawerLayout drawerLayout;
    private boolean isUpdate = false;
    private int businessId = -1;
    private Business currentBusiness;

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

        setupSidebar();

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

        if (getIntent().hasExtra("business_id")) {
            isUpdate = true;
            businessId = getIntent().getIntExtra("business_id", -1);
            
            toolbar.setTitle("UPDATE");
            btnSave.setText("Update");
            btnDelete.setVisibility(View.VISIBLE);
            layoutAgree.setVisibility(View.GONE);
            layoutLegal.setVisibility(View.GONE);

            Executors.newSingleThreadExecutor().execute(() -> {
                currentBusiness = AppDatabase.getInstance(this).businessDao().getById(businessId);
                runOnUiThread(() -> {
                    if (currentBusiness != null) {
                        editBusinessName.setText(currentBusiness.getName());
                        editMobile.setText(currentBusiness.getPhoneNumber());
                    }
                    
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

    private void setupSidebar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddBusiness);
        
        if (drawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });

            // Sidebar Item Click Listeners
            findViewById(R.id.btnSwitchBusiness).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Switch Business coming soon", Toast.LENGTH_SHORT).show();
            });

            findViewById(R.id.btnCreateBusiness).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                if (isUpdate) {
                    startActivity(new Intent(this, AddBusinessActivity.class));
                }
            });

            findViewById(R.id.nav_help_chat).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, HelpChatActivity.class));
            });

            findViewById(R.id.nav_inventory).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, InventoryManagementActivity.class));
            });

            findViewById(R.id.nav_add_expense).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, CashFlowActivity.class));
            });

            findViewById(R.id.nav_receipts).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, ReceiptsActivity.class));
            });

            findViewById(R.id.nav_customers).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, CustomerManagementActivity.class));
            });

            findViewById(R.id.nav_staff).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, StaffManagementActivity.class));
            });

            findViewById(R.id.nav_printer_setup).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, PrinterSetupActivity.class));
            });

            findViewById(R.id.nav_device_details).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, DeviceDetailsActivity.class));
            });

            findViewById(R.id.nav_language).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, LanguageSelectionActivity.class);
                intent.putExtra("isFromSettings", true);
                startActivity(intent);
            });

            findViewById(R.id.nav_logout).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void deleteBusiness() {
        if (businessId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.businessDao().deleteById(businessId);
            
            List<Business> remaining = db.businessDao().getAllBusinesses();
            if (!remaining.isEmpty()) {
                Business switchTarget = remaining.get(0);
                db.businessDao().deselectAll();
                db.businessDao().selectBusiness(switchTarget.getId());
                
                ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(switchTarget.getId());
                if (settings == null) {
                    settings = new ReceiptSettings();
                    settings.setId(switchTarget.getId());
                }
                settings.setBusinessName(switchTarget.getName());
                settings.setPhoneNumber(switchTarget.getPhoneNumber());
                settings.setEmail(switchTarget.getEmail());
                settings.setBusinessLogoPath(switchTarget.getLogoPath());
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
            AppDatabase db = AppDatabase.getInstance(this);
            
            Business businessToSave;
            if (isUpdate && currentBusiness != null) {
                businessToSave = currentBusiness;
                businessToSave.setName(name);
                businessToSave.setPhoneNumber(mobile);
            } else {
                db.businessDao().deselectAll();
                businessToSave = new Business(name, mobile, "OWNER", true);
            }
            
            long savedId = db.businessDao().insert(businessToSave);
            int finalBId = isUpdate ? businessToSave.getId() : (int) savedId;

            if (businessToSave.isSelected()) {
                ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(finalBId);
                if (settings == null) {
                    settings = new ReceiptSettings();
                    settings.setId(finalBId);
                }
                settings.setBusinessName(name);
                settings.setPhoneNumber(mobile);
                db.receiptSettingsDao().insert(settings);

                // Sync with Firestore
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("businessName", name);
                data.put("businessUuid", businessToSave.getUuid());
                data.put("mobile", mobile);
                new BusinessProfileRepository(this).saveBusinessProfile(data, null);
            }

            runOnUiThread(() -> {
                String msg = isUpdate ? "Business updated successfully" : "Business created successfully";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
