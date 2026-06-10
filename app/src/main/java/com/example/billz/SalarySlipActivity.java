package com.example.billz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

import android.app.DatePickerDialog;
import android.graphics.Color;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.EditText;
import java.util.Random;

public class SalarySlipActivity extends AppCompatActivity {

    private int staffId;
    private TextView textDateRange, textBaseSalaryLabel, textBaseSalaryValue, textTotalSalary;
    private LinearLayout layoutAttendanceList;
    private View notificationContainer;
    private Staff currentStaff;
    private Calendar selectedEndDate = Calendar.getInstance();
    private double earnedSalary = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salary_slip);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSalary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        textDateRange = findViewById(R.id.textDateRange);
        textBaseSalaryLabel = findViewById(R.id.textBaseSalaryLabel);
        textBaseSalaryValue = findViewById(R.id.textBaseSalaryValue);
        textTotalSalary = findViewById(R.id.textTotalSalary);
        layoutAttendanceList = findViewById(R.id.layoutAttendanceList);
        notificationContainer = findViewById(R.id.notificationContainer);
        
        findViewById(R.id.layoutDateRange).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.textAdditions).setOnClickListener(v -> showDetailsBottomSheet("ADDITIONS"));
        findViewById(R.id.textDeductions).setOnClickListener(v -> showDetailsBottomSheet("DEDUCTIONS"));
        findViewById(R.id.btnGenerateSlip).setOnClickListener(v -> generateSlip());

        updateUI();
        
        staffId = getIntent().getIntExtra("staff_id", -1);
        if (staffId != -1) {
            loadStaffData();
        }
    }

    private void generateSlip() {
        if (earnedSalary <= 0) {
            showErrorNotification();
        } else {
            Toast.makeText(this, "Salary Slip Generated", Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorNotification() {
        notificationContainer.setVisibility(View.VISIBLE);
        notificationContainer.setAlpha(0f);
        notificationContainer.animate().alpha(1f).setDuration(300).start();
        
        new android.os.Handler().postDelayed(() -> {
            notificationContainer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                notificationContainer.setVisibility(View.GONE);
            }).start();
        }, 3000);
    }

    private void showDetailsBottomSheet(String type) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_enter_details_bottom_sheet, null);
        dialog.setContentView(view);

        TextView textAmountLabel = view.findViewById(R.id.textAmountLabel);
        EditText editAmount = view.findViewById(R.id.editAmount);

        if (type.equals("DEDUCTIONS")) {
            textAmountLabel.setText("Deduction Amount*");
        } else {
            textAmountLabel.setText("Amount*");
        }
        
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSaveDetails).setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                if (type.equals("DEDUCTIONS")) {
                    earnedSalary -= amount;
                } else {
                    earnedSalary += amount;
                }
                textTotalSalary.setText(String.format(Locale.getDefault(), "₹%.0f", earnedSalary));
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateUI() {
        setupDateRange();
        if (currentStaff != null) {
            generateAttendanceAndSalary();
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, year, month, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, month, dayOfMonth);
                    if (newDate.after(Calendar.getInstance())) {
                        Toast.makeText(this, "Cannot select future date", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedEndDate.set(Calendar.YEAR, year);
                        selectedEndDate.set(Calendar.MONTH, month);
                        selectedEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateUI();
                    }
                },
                selectedEndDate.get(Calendar.YEAR),
                selectedEndDate.get(Calendar.MONTH),
                selectedEndDate.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        datePickerDialog.show();
    }

    private void setupDateRange() {
        Calendar cal = (Calendar) selectedEndDate.clone();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String end = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -29); // To make it total 30 days
        String start = sdf.format(cal.getTime());
        textDateRange.setText(String.format("%s to %s", start, end));
    }

    private void loadStaffData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentStaff = AppDatabase.getInstance(this).staffDao().getById(staffId);
            if (currentStaff != null) {
                runOnUiThread(() -> {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(currentStaff.name);
                    }
                    generateAttendanceAndSalary();
                });
            }
        });
    }

    private void generateAttendanceAndSalary() {
        layoutAttendanceList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        
        Calendar cal = (Calendar) selectedEndDate.clone();
        cal.add(Calendar.DAY_OF_MONTH, -29);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar joiningDate = parseJoiningDate(currentStaff.joiningDate);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Random random = new Random();
        int presentDays = 0;
        boolean firstItemAdded = false;

        for (int i = 0; i < 30; i++) {
            // Check if current date is before joining date
            if (joiningDate != null && cal.before(joiningDate)) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            // Add divider before item if it's not the first one being displayed
            if (firstItemAdded) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
                divider.setBackgroundColor(0xFFF1F5F9);
                layoutAttendanceList.addView(divider);
            }

            boolean isPresent = random.nextInt(10) > 2; // ~70% chance of being present
            if (isPresent) presentDays++;

            View view = inflater.inflate(R.layout.item_salary_day, layoutAttendanceList, false);
            TextView textDate = view.findViewById(R.id.textDayDate);
            TextView textStatusText = view.findViewById(R.id.textDayStatus);
            TextView textBadge = view.findViewById(R.id.textStatusBadge);
            androidx.cardview.widget.CardView cardBadge = (androidx.cardview.widget.CardView) textBadge.getParent();

            textDate.setText(sdf.format(cal.getTime()));
            
            if (isPresent) {
                textStatusText.setText("(Present)");
                textStatusText.setTextColor(Color.parseColor("#4CAF50"));
                textBadge.setText("PRESENT");
                cardBadge.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            } else {
                textStatusText.setText("(Absent)");
                textStatusText.setTextColor(Color.parseColor("#94A3B8"));
                textBadge.setText("ABSENT");
                cardBadge.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFA726")));
            }

            layoutAttendanceList.addView(view);
            firstItemAdded = true;
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Calculate Salary
        double dailyRate = currentStaff.salary / 30.0;
        earnedSalary = dailyRate * presentDays;

        textBaseSalaryLabel.setText(String.format(Locale.getDefault(), "Base Salary (%d days)", presentDays));
        textBaseSalaryValue.setText(String.format(Locale.getDefault(), "₹%.0f", earnedSalary));
        textTotalSalary.setText(String.format(Locale.getDefault(), "₹%.0f", earnedSalary));
    }

    private Calendar parseJoiningDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Expected format d/M/yyyy from AddStaffActivity
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
                cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                cal.set(Calendar.YEAR, Integer.parseInt(parts[2]));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
