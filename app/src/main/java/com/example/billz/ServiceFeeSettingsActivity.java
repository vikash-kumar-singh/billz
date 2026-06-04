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

public class ServiceFeeSettingsActivity extends AppCompatActivity {

    private ServiceFeeAdapter adapter;
    private final List<ServiceFee> feeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_fee_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarServiceFeeSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerServiceFees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceFeeAdapter(feeList, this::showUpdateServiceFeeDialog);
        recyclerView.setAdapter(adapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            recyclerView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        loadServiceFees();

        findViewById(R.id.fabAddServiceFee).setOnClickListener(v -> showAddServiceFeeDialog());
    }

    private void loadServiceFees() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ServiceFee> dbFees = AppDatabase.getInstance(this).serviceFeeDao().getAllServiceFees();
            final List<ServiceFee> finalDbFees = dbFees;
            runOnUiThread(() -> {
                feeList.clear();
                feeList.addAll(finalDbFees);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddServiceFeeDialog() {
        showServiceFeeDialog(null);
    }

    private void showUpdateServiceFeeDialog(ServiceFee fee) {
        showServiceFeeDialog(fee);
    }

    private void showServiceFeeDialog(ServiceFee fee) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_service_fee);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editName = dialog.findViewById(R.id.editServiceName);
        EditText editValue = dialog.findViewById(R.id.editServiceValue);
        TextView labelValue = dialog.findViewById(R.id.labelServiceValue);
        CheckBox checkIsPercentage = dialog.findViewById(R.id.checkIsPercentage);
        CheckBox checkDefault = dialog.findViewById(R.id.checkDefaultService);
        View btnSave = dialog.findViewById(R.id.btnSaveService);
        View btnDelete = dialog.findViewById(R.id.btnDeleteService);

        checkIsPercentage.setOnCheckedChangeListener((buttonView, isChecked) ->
            labelValue.setText(getString(isChecked ? R.string.service_value_percent_label : R.string.service_value_label)));

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
                updateServiceFee(fee);
            } else {
                saveServiceFee(new ServiceFee(name, value, isPercentage, isDefault));
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deleteServiceFee(fee);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveServiceFee(ServiceFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).serviceFeeDao().insert(fee);
            loadServiceFees();
        });
    }

    private void updateServiceFee(ServiceFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).serviceFeeDao().update(fee);
            loadServiceFees();
        });
    }

    private void deleteServiceFee(ServiceFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).serviceFeeDao().delete(fee);
            loadServiceFees();
        });
    }
}
