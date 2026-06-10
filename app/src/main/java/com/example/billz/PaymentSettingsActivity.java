package com.example.billz;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PaymentSettingsActivity extends AppCompatActivity {

    private PaymentModeAdapter suggestionAdapter, addedAdapter;
    private final List<PaymentMode> suggestionList = new ArrayList<>();
    private final List<PaymentMode> addedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarPaymentSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new PaymentModeAdapter(suggestionList, new PaymentModeAdapter.OnPaymentModeClickListener() {
            @Override
            public void onActionClick(PaymentMode mode) {
                addPaymentMode(mode);
            }

            @Override
            public void onConfigClick(PaymentMode mode) {}
        });
        recyclerSuggestions.setAdapter(suggestionAdapter);

        RecyclerView recyclerAdded = findViewById(R.id.recyclerAddedModes);
        recyclerAdded.setLayoutManager(new LinearLayoutManager(this));
        addedAdapter = new PaymentModeAdapter(addedList, new PaymentModeAdapter.OnPaymentModeClickListener() {
            @Override
            public void onActionClick(PaymentMode mode) {}

            @Override
            public void onConfigClick(PaymentMode mode) {
                android.util.Log.d("PaymentSettings", "Config clicked for: " + mode.getName());
                showUpiConfigDialog();
            }
        });
        recyclerAdded.setAdapter(addedAdapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            findViewById(android.R.id.content).setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        loadPaymentModes();

        findViewById(R.id.btnAddPaymentMode).setOnClickListener(v -> showAddPaymentModeDialog());
    }

    private void showAddPaymentModeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_payment_mode);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editName = dialog.findViewById(R.id.editPaymentModeName);

        dialog.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter payment mode name", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
                int bId = (active != null) ? active.getId() : -1;
                PaymentMode newMode = new PaymentMode(name, true, false);
                newMode.setBusinessId(bId);
                saveNewPaymentMode(newMode);
            });
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showUpiConfigDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upi_config);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // Set flags to allow interaction with input methods
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText editId = dialog.findViewById(R.id.editUpiId);
        EditText editName = dialog.findViewById(R.id.editUpiFullName);
        EditText editMerchant = dialog.findViewById(R.id.editMerchantId);

        Executors.newSingleThreadExecutor().execute(() -> {
            UpiConfig config = AppDatabase.getInstance(this).upiConfigDao().getUpiConfig();
            if (config != null) {
                runOnUiThread(() -> {
                    editId.setText(config.getUpiId());
                    editName.setText(config.getFullName());
                    editMerchant.setText(config.getMerchantId());
                });
            }
        });

        dialog.findViewById(R.id.btnSaveUpi).setOnClickListener(v -> {
            String upiId = editId.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String merchant = editMerchant.getText().toString().trim();

            if (upiId.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill required fields (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            UpiConfig newConfig = new UpiConfig(upiId, name, merchant);
            saveUpiConfig(newConfig);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveUpiConfig(UpiConfig config) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).upiConfigDao().insertOrUpdate(config);
            runOnUiThread(() -> Toast.makeText(this, "UPI Config Saved", Toast.LENGTH_SHORT).show());
        });
    }

    private void saveNewPaymentMode(PaymentMode mode) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).paymentModeDao().insert(mode);
            loadPaymentModes();
        });
    }

    private void loadPaymentModes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;
            List<PaymentMode> allModes = AppDatabase.getInstance(this).paymentModeDao().getAllPaymentModes(bId);
            if (allModes.isEmpty()) {
                addInitialPaymentModes(bId);
                allModes = AppDatabase.getInstance(this).paymentModeDao().getAllPaymentModes(bId);
            }
            
            final List<PaymentMode> suggestions = new ArrayList<>();
            final List<PaymentMode> added = new ArrayList<>();
            
            for (PaymentMode m : allModes) {
                if (m.isAdded()) added.add(m);
                else suggestions.add(m);
            }
            
            runOnUiThread(() -> {
                suggestionList.clear();
                suggestionList.addAll(suggestions);
                suggestionAdapter.notifyDataSetChanged();
                
                addedList.clear();
                addedList.addAll(added);
                addedAdapter.notifyDataSetChanged();
            });
        });
    }

    private void addInitialPaymentModes(int bId) {
        PaymentModeDao dao = AppDatabase.getInstance(this).paymentModeDao();
        // Suggestions
        PaymentMode m1 = new PaymentMode("Store Credit", false, false);
        m1.setBusinessId(bId);
        dao.insert(m1);

        PaymentMode m2 = new PaymentMode("Google Pay", false, false);
        m2.setBusinessId(bId);
        dao.insert(m2);
        
        // Added modes
        PaymentMode m3 = new PaymentMode("Cash", true, false);
        m3.setBusinessId(bId);
        dao.insert(m3);

        PaymentMode m4 = new PaymentMode("Debit Card", true, false);
        m4.setBusinessId(bId);
        dao.insert(m4);

        PaymentMode m5 = new PaymentMode("Credit Card", true, false);
        m5.setBusinessId(bId);
        dao.insert(m5);

        PaymentMode m6 = new PaymentMode("Credit", true, false);
        m6.setBusinessId(bId);
        dao.insert(m6);

        PaymentMode m7 = new PaymentMode("UPI / BHIM", true, true);
        m7.setBusinessId(bId);
        dao.insert(m7);
    }

    private void addPaymentMode(PaymentMode mode) {
        mode.setAdded(true);
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).paymentModeDao().update(mode);
            loadPaymentModes();
        });
    }
}
