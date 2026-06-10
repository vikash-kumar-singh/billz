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

public class TaxSettingsActivity extends AppCompatActivity {

    private TaxAdapter adapter;
    private final List<Tax> taxList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tax_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarTaxSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerTaxes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaxAdapter(taxList, this::showUpdateTaxDialog);
        recyclerView.setAdapter(adapter);

        View root = toolbar.getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            
            if (recyclerView != null) {
                recyclerView.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        loadTaxes();

        findViewById(R.id.fabAddTax).setOnClickListener(v -> showAddTaxDialog());
    }

    private void loadTaxes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;
            List<Tax> dbTaxes = AppDatabase.getInstance(this).taxDao().getAllTaxes(bId);
            if (dbTaxes.isEmpty()) {
                addInitialDummyTaxes(bId);
                dbTaxes = AppDatabase.getInstance(this).taxDao().getAllTaxes(bId);
            }
            final List<Tax> finalDbTaxes = dbTaxes;
            runOnUiThread(() -> {
                taxList.clear();
                taxList.addAll(finalDbTaxes);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void addInitialDummyTaxes(int bId) {
        TaxDao dao = AppDatabase.getInstance(this).taxDao();
        Tax t1 = new Tax("CGST on sales", 9.0, false);
        t1.setBusinessId(bId);
        dao.insert(t1);

        Tax t2 = new Tax("SGST+CGST", 18.0, false);
        t2.setBusinessId(bId);
        dao.insert(t2);

        Tax t3 = new Tax("SGST on sales", 9.0, false);
        t3.setBusinessId(bId);
        dao.insert(t3);

        Tax t4 = new Tax("TRANSPORTATION", 50.0, false);
        t4.setBusinessId(bId);
        dao.insert(t4);
    }

    private void showAddTaxDialog() {
        showTaxDialog(null);
    }

    private void showUpdateTaxDialog(Tax tax) {
        showTaxDialog(tax);
    }

    private void showTaxDialog(Tax tax) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_tax);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editName = dialog.findViewById(R.id.editTaxName);
        EditText editValue = dialog.findViewById(R.id.editTaxValue);
        CheckBox checkDefault = dialog.findViewById(R.id.checkDefaultTax);
        View btnUpdate = dialog.findViewById(R.id.btnUpdateTax);
        View btnSave = dialog.findViewById(R.id.btnSaveTax);
        View btnDelete = dialog.findViewById(R.id.btnDeleteTax);

        if (tax != null) {
            editName.setText(tax.getName());
            editValue.setText(String.valueOf(tax.getValue()));
            checkDefault.setChecked(tax.isDefault());
            btnUpdate.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnUpdate.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
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
            if (tax != null) {
                tax.setName(name);
                tax.setValue(value);
                tax.setDefault(checkDefault.isChecked());
                updateTax(tax);
            } else {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
                    int bId = (active != null) ? active.getId() : -1;
                    Tax newTax = new Tax(name, value, checkDefault.isChecked());
                    newTax.setBusinessId(bId);
                    saveTax(newTax);
                });
            }
            dialog.dismiss();
        };

        btnSave.setOnClickListener(saveAction);
        btnUpdate.setOnClickListener(saveAction);
        btnDelete.setOnClickListener(v -> {
            deleteTax(tax);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveTax(Tax tax) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).taxDao().insert(tax);
            loadTaxes();
        });
    }

    private void updateTax(Tax tax) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).taxDao().update(tax);
            loadTaxes();
        });
    }

    private void deleteTax(Tax tax) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).taxDao().delete(tax);
            loadTaxes();
        });
    }
}
