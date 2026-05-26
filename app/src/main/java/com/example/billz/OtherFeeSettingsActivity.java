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

public class OtherFeeSettingsActivity extends AppCompatActivity {

    private OtherFeeAdapter adapter;
    private final List<OtherFee> feeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_other_fee_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarOtherFeeSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerOtherFees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OtherFeeAdapter(feeList, this::showUpdateOtherFeeDialog);
        recyclerView.setAdapter(adapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            recyclerView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        loadOtherFees();

        findViewById(R.id.fabAddOtherFee).setOnClickListener(v -> showAddOtherFeeDialog());
    }

    private void loadOtherFees() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<OtherFee> dbFees = AppDatabase.getInstance(this).otherFeeDao().getAllOtherFees();
            final List<OtherFee> finalDbFees = dbFees;
            runOnUiThread(() -> {
                feeList.clear();
                feeList.addAll(finalDbFees);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddOtherFeeDialog() {
        showOtherFeeDialog(null);
    }

    private void showUpdateOtherFeeDialog(OtherFee fee) {
        showOtherFeeDialog(fee);
    }

    private void showOtherFeeDialog(OtherFee fee) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_other_fee);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editName = dialog.findViewById(R.id.editOtherName);
        EditText editValue = dialog.findViewById(R.id.editOtherValue);
        TextView labelValue = dialog.findViewById(R.id.labelOtherValue);
        CheckBox checkIsPercentage = dialog.findViewById(R.id.checkIsPercentage);
        CheckBox checkDefault = dialog.findViewById(R.id.checkDefaultOther);
        View btnSave = dialog.findViewById(R.id.btnSaveOther);
        View btnDelete = dialog.findViewById(R.id.btnDeleteOther);

        checkIsPercentage.setOnCheckedChangeListener((buttonView, isChecked) -> 
            labelValue.setText(getString(isChecked ? R.string.other_value_percent_label : R.string.other_value_label)));

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
            
            if (fee != null) {
                fee.setName(name);
                fee.setValue(value);
                fee.setPercentage(isPercentage);
                fee.setDefault(checkDefault.isChecked());
                updateOtherFee(fee);
            } else {
                saveOtherFee(new OtherFee(name, value, isPercentage, checkDefault.isChecked()));
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deleteOtherFee(fee);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveOtherFee(OtherFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).otherFeeDao().insert(fee);
            loadOtherFees();
        });
    }

    private void updateOtherFee(OtherFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).otherFeeDao().update(fee);
            loadOtherFees();
        });
    }

    private void deleteOtherFee(OtherFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).otherFeeDao().delete(fee);
            loadOtherFees();
        });
    }
}
