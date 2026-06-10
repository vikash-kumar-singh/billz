package com.example.billz;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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

public class DeliveryFeeSettingsActivity extends AppCompatActivity {

    private DeliveryFeeAdapter adapter;
    private final List<DeliveryFee> feeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_fee_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDeliveryFeeSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerDeliveryFees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeliveryFeeAdapter(feeList, this::showUpdateDeliveryFeeDialog);
        recyclerView.setAdapter(adapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            recyclerView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        loadDeliveryFees();

        findViewById(R.id.fabAddDeliveryFee).setOnClickListener(v -> showAddDeliveryFeeDialog());
    }

    private void loadDeliveryFees() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;
            List<DeliveryFee> dbFees = AppDatabase.getInstance(this).deliveryFeeDao().getAllDeliveryFees(bId);
            final List<DeliveryFee> finalDbFees = dbFees;
            runOnUiThread(() -> {
                feeList.clear();
                feeList.addAll(finalDbFees);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddDeliveryFeeDialog() {
        showDeliveryFeeDialog(null);
    }

    private void showUpdateDeliveryFeeDialog(DeliveryFee fee) {
        showDeliveryFeeDialog(fee);
    }

    private void showDeliveryFeeDialog(DeliveryFee fee) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_delivery_fee);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editName = dialog.findViewById(R.id.editDeliveryName);
        EditText editValue = dialog.findViewById(R.id.editDeliveryValue);
        TextView labelValue = dialog.findViewById(R.id.labelDeliveryValue);
        CheckBox checkIsPercentage = dialog.findViewById(R.id.checkIsPercentage);
        CheckBox checkDefault = dialog.findViewById(R.id.checkDefaultDelivery);
        View btnSave = dialog.findViewById(R.id.btnSaveDelivery);
        View btnDelete = dialog.findViewById(R.id.btnDeleteDelivery);

        checkIsPercentage.setOnCheckedChangeListener((buttonView, isChecked) ->
            labelValue.setText(getString(isChecked ? R.string.delivery_value_percent_label : R.string.delivery_value_label)));

        if (fee != null) {
            editName.setText(fee.getName());
            editValue.setText(String.valueOf(fee.getValue()));
            checkIsPercentage.setChecked(fee.isPercentage());
            checkDefault.setChecked(fee.isDefault());
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String valueStr = editValue.getText().toString().trim();

            if (name.isEmpty() || valueStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double value = Double.parseDouble(valueStr);
            boolean isPercentage = checkIsPercentage.isChecked();
            boolean isDefault = checkDefault.isChecked();
            
            if (fee != null) {
                fee.setName(name);
                fee.setValue(value);
                fee.setPercentage(isPercentage);
                fee.setDefault(isDefault);
                updateDeliveryFee(fee);
            } else {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
                    int bId = (active != null) ? active.getId() : -1;
                    DeliveryFee newFee = new DeliveryFee(name, value, isPercentage, isDefault);
                    newFee.setBusinessId(bId);
                    saveDeliveryFee(newFee);
                });
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deleteDeliveryFee(fee);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveDeliveryFee(DeliveryFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).deliveryFeeDao().insert(fee);
            loadDeliveryFees();
        });
    }

    private void updateDeliveryFee(DeliveryFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).deliveryFeeDao().update(fee);
            loadDeliveryFees();
        });
    }

    private void deleteDeliveryFee(DeliveryFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).deliveryFeeDao().delete(fee);
            loadDeliveryFees();
        });
    }
}
