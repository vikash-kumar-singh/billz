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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AttendanceManagementActivity extends AppCompatActivity {

    private int staffId;
    private TextView textStaffInfo, textStaffInitials, textCurrentDate;
    private View layoutActions, cardDropdown;
    private ImageView imageDropdown;
    private boolean isExpanded = false;

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

        setCurrentDate();
        
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

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("'Today' : dd MMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        textCurrentDate.setText(currentDate);
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
            Staff staff = AppDatabase.getInstance(this).staffDao().getById(staffId);
            if (staff != null) {
                runOnUiThread(() -> {
                    String info = staff.name + " - " + (staff.email != null ? staff.email : "-");
                    textStaffInfo.setText(info);
                    if (staff.name != null && !staff.name.isEmpty()) {
                        textStaffInitials.setText(String.valueOf(staff.name.charAt(0)).toUpperCase());
                    }
                });
            }
        });
    }
}
