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

public class PackingFeeSettingsActivity extends AppCompatActivity {

    private PackingFeeAdapter adapter;
    private final List<PackingFee> feeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_packing_fee_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarPackingFeeSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerPackingFees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PackingFeeAdapter(feeList, this::showUpdatePackingFeeDialog);
        recyclerView.setAdapter(adapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            recyclerView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        loadPackingFees();

        findViewById(R.id.fabAddPackingFee).setOnClickListener(v -> showAddPackingFeeDialog());
    }

    private void loadPackingFees() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;
            List<PackingFee> dbFees = AppDatabase.getInstance(this).packingFeeDao().getAllPackingFees(bId);
            final List<PackingFee> finalDbFees = dbFees;
            runOnUiThread(() -> {
                feeList.clear();
                feeList.addAll(finalDbFees);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddPackingFeeDialog() {
        showPackingFeeDialog(null);
    }

    private void showUpdatePackingFeeDialog(PackingFee fee) {
        showPackingFeeDialog(fee);
    }

    private void showPackingFeeDialog(PackingFee fee) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_packing_fee);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editName = dialog.findViewById(R.id.editPackingName);
        EditText editValue = dialog.findViewById(R.id.editPackingValue);
        TextView labelValue = dialog.findViewById(R.id.labelPackingValue);
        CheckBox checkIsPercentage = dialog.findViewById(R.id.checkIsPercentage);
        CheckBox checkDefault = dialog.findViewById(R.id.checkDefaultPacking);
        View btnSave = dialog.findViewById(R.id.btnSavePacking);
        View btnDelete = dialog.findViewById(R.id.btnDeletePacking);

        checkIsPercentage.setOnCheckedChangeListener((buttonView, isChecked) ->
            labelValue.setText(getString(isChecked ? R.string.packing_value_percent_label : R.string.packing_value_label)));

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
                updatePackingFee(fee);
            } else {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
                    int bId = (active != null) ? active.getId() : -1;
                    PackingFee newFee = new PackingFee(name, value, isPercentage, isDefault);
                    newFee.setBusinessId(bId);
                    savePackingFee(newFee);
                });
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deletePackingFee(fee);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void savePackingFee(PackingFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).packingFeeDao().insert(fee);
            loadPackingFees();
        });
    }

    private void updatePackingFee(PackingFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).packingFeeDao().update(fee);
            loadPackingFees();
        });
    }

    private void deletePackingFee(PackingFee fee) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).packingFeeDao().delete(fee);
            loadPackingFees();
        });
    }
}
