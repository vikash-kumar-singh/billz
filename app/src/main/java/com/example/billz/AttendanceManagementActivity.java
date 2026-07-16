package com.example.billz;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AttendanceManagementActivity extends AppCompatActivity {

    private int staffId;
    private TextView textStaffInfo, textStaffInitials, textCurrentDate;
    private View layoutActions, cardDropdown;
    private ImageView imageDropdown, btnPrevDate, btnNextDate;
    private boolean isExpanded = false;
    private Calendar selectedDate = Calendar.getInstance();
    private Staff currentStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAttendance);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        textStaffInfo = findViewById(R.id.textStaffInfo);
        textStaffInitials = findViewById(R.id.textStaffInitials);
        textCurrentDate = findViewById(R.id.textCurrentDate);
        layoutActions = findViewById(R.id.layoutActions);
        cardDropdown = findViewById(R.id.cardDropdown);
        btnPrevDate = findViewById(R.id.btnPrevDate);
        btnNextDate = findViewById(R.id.btnNextDate);

        updateDateDisplay();

        btnPrevDate.setOnClickListener(v -> {
            Calendar prev = (Calendar) selectedDate.clone();
            prev.add(Calendar.DAY_OF_MONTH, -1);

            Calendar joiningDate = parseJoiningDate(currentStaff != null ? currentStaff.joiningDate : null);
            if (joiningDate != null && prev.before(joiningDate)) {
                Toast.makeText(this, "Cannot go before joining date", Toast.LENGTH_SHORT).show();
            } else {
                selectedDate = prev;
                updateDateDisplay();
            }
        });

        btnNextDate.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
        });

        findViewById(R.id.cardDateSelector).setOnClickListener(v -> showDatePicker());

        // Find the image inside the cardDropdown
        if (cardDropdown instanceof android.view.ViewGroup) {
            imageDropdown = (ImageView) ((android.view.ViewGroup) cardDropdown).getChildAt(0);
        }

        cardDropdown.setOnClickListener(v -> toggleActions());
        findViewById(R.id.layoutStaffHeader).setOnClickListener(v -> toggleActions());

        staffId = getIntent().getIntExtra("staff_id", -1);
        if (staffId != -1) {
            loadStaffData();
        }
    }

    private void updateDateDisplay() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf;
        if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            sdf = new SimpleDateFormat("'Today' : dd MMM", Locale.getDefault());
        } else {
            sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        }
        textCurrentDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void showDatePicker() {
        Calendar joiningDate = parseJoiningDate(currentStaff != null ? currentStaff.joiningDate : null);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, year, month, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, month, dayOfMonth);
                    if (joiningDate != null && newDate.before(joiningDate)) {
                        Toast.makeText(this, "Selected date is before joining date", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        if (joiningDate != null) {
            datePickerDialog.getDatePicker().setMinDate(joiningDate.getTimeInMillis());
        }

        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        datePickerDialog.show();
    }

    private void toggleActions() {
        isExpanded = !isExpanded;
        android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) layoutActions.getParent());
        layoutActions.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        if (imageDropdown != null) {
            imageDropdown.animate().rotation(isExpanded ? 180 : 0).setDuration(200).start();
        }
    }

    private void loadStaffData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentStaff = AppDatabase.getInstance(this).staffDao().getById(staffId);
            if (currentStaff != null) {
                runOnUiThread(() -> {
                    String info = currentStaff.name + " - " + (currentStaff.email != null ? currentStaff.email : "-");
                    textStaffInfo.setText(info);
                    if (currentStaff.name != null && !currentStaff.name.isEmpty()) {
                        textStaffInitials.setText(String.valueOf(currentStaff.name.charAt(0)).toUpperCase());
                    }

                    Calendar joiningDate = parseJoiningDate(currentStaff.joiningDate);
                    if (joiningDate != null && selectedDate.before(joiningDate)) {
                        selectedDate = (Calendar) joiningDate.clone();
                        updateDateDisplay();
                    }
                });
            }
        });
    }

    private Calendar parseJoiningDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
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
