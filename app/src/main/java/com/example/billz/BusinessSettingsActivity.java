package com.example.billz;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Random;
import java.util.concurrent.Executors;

public class BusinessSettingsActivity extends AppCompatActivity {

    private ReceiptSettings settings;
    private TextView textPrefixValue, textTaxDesc, textDiscountDesc, textServiceDesc, textOtherDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarBusinessSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        textPrefixValue = findViewById(R.id.textReceiptIdPrefixValue);
        textTaxDesc = findViewById(R.id.textTaxSettingsDesc);
        textDiscountDesc = findViewById(R.id.textDiscountSettingsDesc);
        textServiceDesc = findViewById(R.id.textServiceFeeSettingsDesc);
        textOtherDesc = findViewById(R.id.textOtherFeeSettingsDesc);

        findViewById(R.id.itemReceiptIdPrefix).setOnClickListener(v -> showBillPrefixDialog());
        
        findViewById(R.id.itemTaxSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, TaxSettingsActivity.class));
        });

        findViewById(R.id.itemDiscountSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, DiscountSettingsActivity.class));
        });

        findViewById(R.id.itemPaymentModeSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, PaymentSettingsActivity.class));
        });

        findViewById(R.id.itemDeliveryFeeSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, DeliveryFeeSettingsActivity.class));
        });

        findViewById(R.id.itemPackingFeeSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, PackingFeeSettingsActivity.class));
        });

        findViewById(R.id.itemServiceFeeSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, ServiceFeeSettingsActivity.class));
        });

        findViewById(R.id.itemOtherFeeSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, OtherFeeSettingsActivity.class));
        });

        findViewById(R.id.itemDeleteReset).setOnClickListener(v -> showDeleteResetDialog());

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            View mainContent = findViewById(android.R.id.content);
            if (mainContent != null) {
                mainContent.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        loadSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }

    private void loadSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : 1;

            settings = AppDatabase.getInstance(this).receiptSettingsDao().getSettingsByBusiness(bId);
            if (settings == null) {
                settings = new ReceiptSettings();
                settings.setId(bId);
            }
            runOnUiThread(() -> {
                updateUI();
            });
        });
    }

    private void updateUI() {
        if (settings != null) {
            textPrefixValue.setText(getString(R.string.receipt_id_format, 
                    settings.getReceiptIdPrefix(), settings.getCurrentBillNo()));
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;

            int count = AppDatabase.getInstance(this).taxDao().getAllTaxes(bId).size();
            int dCount = AppDatabase.getInstance(this).discountDao().getAllDiscounts(bId).size();
            int sCount = AppDatabase.getInstance(this).serviceFeeDao().getAllServiceFees(bId).size();
            int oCount = AppDatabase.getInstance(this).otherFeeDao().getAllOtherFees(bId).size();
            runOnUiThread(() -> {
                textTaxDesc.setText(getString(R.string.tax_settings_desc, count));
                textDiscountDesc.setText(getString(R.string.discount_settings_desc, dCount));
                textServiceDesc.setText(getString(R.string.service_fee_settings_desc, sCount));
                textOtherDesc.setText(getString(R.string.others_fee_settings_desc, oCount));
            });
        });
    }

    private void showBillPrefixDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bill_prefix);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            // Forces the dialog to be wide (95% of screen)
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        EditText editPrefix = dialog.findViewById(R.id.editPrefix);
        EditText editBillNo = dialog.findViewById(R.id.editBillNo);
        TextView textGenerateRandom = dialog.findViewById(R.id.textGenerateRandom);

        if (settings != null) {
            editPrefix.setText(settings.getReceiptIdPrefix());
            editBillNo.setText(String.valueOf(settings.getCurrentBillNo()));
        }

        textGenerateRandom.setOnClickListener(v -> {
            Random r = new Random();
            int low = 1000;
            int high = 9999;
            int result = r.nextInt(high - low) + low;
            editBillNo.setText(String.valueOf(result));
        });

        dialog.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String prefix = editPrefix.getText().toString().trim();
            String billNoStr = editBillNo.getText().toString().trim();

            if (prefix.isEmpty() || billNoStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            settings.setReceiptIdPrefix(prefix);
            settings.setCurrentBillNo(Integer.parseInt(billNoStr));

            saveSettings();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteResetDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_reset);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.findViewById(R.id.btnResetItems).setOnClickListener(v -> {
            Toast.makeText(this, "Items Reset requested", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnResetCategory).setOnClickListener(v -> {
            Toast.makeText(this, "Category Reset requested", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).receiptSettingsDao().insert(settings);
            runOnUiThread(() -> {
                updateUI();
                Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
