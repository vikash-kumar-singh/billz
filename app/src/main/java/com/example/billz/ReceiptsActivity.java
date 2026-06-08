package com.example.billz;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReceiptsActivity extends AppCompatActivity {

    private RecyclerView recyclerReceipts;
    private TextView textDateFrom, textDateTo;
    private Calendar calendarFrom, calendarTo;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private AppDatabase db;
    private int currentBusinessId = 0;
    private String currentFilter = "ALL";
    private com.google.android.material.button.MaterialButton btnFilterAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);

        // Security Check: Ensure user is logged in
        if (FirebaseHelper.getCurrentUid() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipts);

        db = AppDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarReceipts);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Handle window insets for header padding (pushed heading down)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerContainer), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Apply bottom padding to Filter button
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnFilterAll), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = systemBars.bottom + (int)(16 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(lp);
            return insets;
        });

        recyclerReceipts = findViewById(R.id.recyclerReceipts);
        recyclerReceipts.setLayoutManager(new LinearLayoutManager(this));

        textDateFrom = findViewById(R.id.textDateFrom);
        textDateTo = findViewById(R.id.textDateTo);

        calendarFrom = Calendar.getInstance();
        calendarFrom.add(Calendar.DAY_OF_YEAR, -2); // Match image (3 Jun to 5 Jun)
        calendarTo = Calendar.getInstance();

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterAll.setOnClickListener(v -> showFilterDialog());

        updateDateLabels();

        findViewById(R.id.layoutDateFrom).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.layoutDateTo).setOnClickListener(v -> showDatePicker(false));

        loadCurrentBusinessAndReceipts();
    }

    private void updateDateLabels() {
        textDateFrom.setText(displayFormat.format(calendarFrom.getTime()));
        textDateTo.setText(displayFormat.format(calendarTo.getTime()));
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = isFrom ? calendarFrom : calendarTo;
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabels();
            loadReceipts();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_receipt_filter);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RadioGroup group = dialog.findViewById(R.id.radioGroupFilter);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<PaymentMode> allModes = db.paymentModeDao().getAllPaymentModes();
            List<String> activeModes = new ArrayList<>();
            activeModes.add("All");
            for (PaymentMode mode : allModes) {
                if (mode.isAdded()) activeModes.add(mode.getName());
            }

            runOnUiThread(() -> {
                group.removeAllViews();
                for (String modeName : activeModes) {
                    RadioButton rb = new RadioButton(this);
                    rb.setText(modeName);
                    rb.setTextSize(16);
                    rb.setTextColor(0xFF64748B);
                    rb.setLayoutParams(new RadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 
                            (int)(48 * getResources().getDisplayMetrics().density)));
                    
                    group.addView(rb);

                    if (modeName.equalsIgnoreCase(currentFilter)) {
                        rb.setChecked(true);
                    } else if (currentFilter.equals("ALL") && modeName.equalsIgnoreCase("All")) {
                        rb.setChecked(true);
                    }
                }

                group.setOnCheckedChangeListener((rg, checkedId) -> {
                    RadioButton selected = dialog.findViewById(checkedId);
                    if (selected != null) {
                        String name = selected.getText().toString();
                        currentFilter = name.equalsIgnoreCase("All") ? "ALL" : name;
                        btnFilterAll.setText("FILTER : " + currentFilter.toUpperCase());
                        loadReceipts();
                        dialog.dismiss();
                    }
                });
            });
        });

        dialog.show();
    }

    private void loadCurrentBusinessAndReceipts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business selected = db.businessDao().getSelectedBusiness();
            if (selected != null) {
                currentBusinessId = selected.getId();
            }
            loadReceipts();
        });
    }

    private void loadReceipts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Set range
            Calendar start = (Calendar) calendarFrom.clone();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);

            Calendar end = (Calendar) calendarTo.clone();
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);

            List<Receipt> list;
            if (currentFilter.equals("ALL")) {
                list = db.receiptDao().getReceiptsByDateRange(currentBusinessId, start.getTimeInMillis(), end.getTimeInMillis());
            } else {
                list = db.receiptDao().getReceiptsByFilter(currentBusinessId, currentFilter, start.getTimeInMillis(), end.getTimeInMillis());
            }

            runOnUiThread(() -> {
                recyclerReceipts.setAdapter(new ReceiptAdapter(list));
            });
        });
    }
}
