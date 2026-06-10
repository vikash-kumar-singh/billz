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

public class DiscountSettingsActivity extends AppCompatActivity {

    private DiscountAdapter adapter;
    private final List<Discount> discountList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_discount_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDiscountSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerDiscounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiscountAdapter(discountList, this::showUpdateDiscountDialog);
        recyclerView.setAdapter(adapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            recyclerView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        loadDiscounts();

        findViewById(R.id.fabAddDiscount).setOnClickListener(v -> showAddDiscountDialog());
    }

    private void loadDiscounts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;
            List<Discount> dbDiscounts = AppDatabase.getInstance(this).discountDao().getAllDiscounts(bId);
            if (dbDiscounts.isEmpty()) {
                addInitialDummyDiscounts(bId);
                dbDiscounts = AppDatabase.getInstance(this).discountDao().getAllDiscounts(bId);
            }
            final List<Discount> finalDbDiscounts = dbDiscounts;
            runOnUiThread(() -> {
                discountList.clear();
                discountList.addAll(finalDbDiscounts);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void addInitialDummyDiscounts(int bId) {
        DiscountDao dao = AppDatabase.getInstance(this).discountDao();
        
        Discount d1 = new Discount("DUES", 1800.0, false, false);
        d1.setBusinessId(bId);
        dao.insert(d1);

        Discount d2 = new Discount("PREVIOUS", 250.0, false, false);
        d2.setBusinessId(bId);
        dao.insert(d2);

        Discount d3 = new Discount("ONLINE", 250.0, false, false);
        d3.setBusinessId(bId);
        dao.insert(d3);

        Discount d4 = new Discount("RETURN", 650.0, false, false);
                d4.setBusinessId(bId);
        dao.insert(d4);

        Discount d5 = new Discount("RETURN GRIPPER", 250.0, false, false);
                d5.setBusinessId(bId);
        dao.insert(d5);

        Discount d6 = new Discount("CASH", 1500.0, false, false);
                d6.setBusinessId(bId);
        dao.insert(d6);
    }

    private void showAddDiscountDialog() {
        showDiscountDialog(null);
    }

    private void showUpdateDiscountDialog(Discount discount) {
        showDiscountDialog(discount);
    }

    private void showDiscountDialog(Discount discount) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_discount);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView textTitle = dialog.findViewById(R.id.textDialogTitle);
        EditText editName = dialog.findViewById(R.id.editDiscountName);
        EditText editValue = dialog.findViewById(R.id.editDiscountValue);
        TextView labelValue = dialog.findViewById(R.id.labelDiscountValue);
        CheckBox checkIsPercentage = dialog.findViewById(R.id.checkIsPercentage);
        CheckBox checkDefault = dialog.findViewById(R.id.checkDefaultDiscount);
        com.google.android.material.button.MaterialButton btnSave = dialog.findViewById(R.id.btnSaveDiscount);
        View btnDelete = dialog.findViewById(R.id.btnDeleteDiscount);

        checkIsPercentage.setOnCheckedChangeListener((buttonView, isChecked) -> 
            labelValue.setText(getString(isChecked ? R.string.discount_value_percent_label : R.string.discount_value_label)));

        if (discount != null) {
            textTitle.setText(R.string.update_discount_title);
            editName.setText(discount.getName());
            editValue.setText(String.valueOf(discount.getValue()));
            checkIsPercentage.setChecked(discount.isPercentage());
            checkDefault.setChecked(discount.isDefault());
            
            btnSave.setText("UPDATE");
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            textTitle.setText(R.string.add_discount_title);
            btnSave.setText("ADD");
            btnDelete.setVisibility(View.GONE);
        }

        View.OnClickListener saveAction = v -> {
            String name = editName.getText().toString().trim();
            String valueStr = editValue.getText().toString().trim();

            if (name.isEmpty() || valueStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double value = Double.parseDouble(valueStr);
            boolean isPercentage = checkIsPercentage.isChecked();
            boolean isDefault = checkDefault.isChecked();
            
            if (discount != null) {
                discount.setName(name);
                discount.setValue(value);
                discount.setPercentage(isPercentage);
                discount.setDefault(isDefault);
                updateDiscount(discount);
            } else {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
                    int bId = (active != null) ? active.getId() : -1;
                    Discount newDiscount = new Discount(name, value, isPercentage, isDefault);
                    newDiscount.setBusinessId(bId);
                    saveDiscount(newDiscount);
                });
            }
            dialog.dismiss();
        };

        btnSave.setOnClickListener(saveAction);
        btnDelete.setOnClickListener(v -> {
            deleteDiscount(discount);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveDiscount(Discount discount) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).discountDao().insert(discount);
            loadDiscounts();
        });
    }

    private void updateDiscount(Discount discount) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).discountDao().update(discount);
            loadDiscounts();
        });
    }

    private void deleteDiscount(Discount discount) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).discountDao().delete(discount);
            loadDiscounts();
        });
    }
}
