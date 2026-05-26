package com.example.billz;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddStaffActivity extends AppCompatActivity {

    private EditText editStaffName, editStaffEmail, editStaffMobile, editSalary, editShiftHours, editJoiningDate;
    private TextView textTrackAttendance, textAllowAppUse;
    private CheckBox checkTrackAttendance, checkAllowAppUse, checkSelfAttendance;
    
    // Permission Checkboxes
    private CheckBox permCreateReceipt, permViewAllReceipt, permEditReceipt, permReturnReceipt, permReceiptAdmin;
    private CheckBox permDiscount, permGift, permFree, permAddCharges, permAddTax, permGiveCredit, permViewRunningTables;
    private CheckBox permInvViewOnly, permInvCreate, permInvEdit, permInvAdmin;
    private CheckBox permStaffAttendance, permStaffPayroll, permStaffManagement, permStaffAdmin;
    private CheckBox permCustViewOnly, permCustCreate, permCustEdit, permCustAdmin;

    private LinearLayout tabMonthly, tabDayWise, tabHourly;
    private CardView cardMonthly, cardDayWise, cardHourly;
    private ImageView imageCheckMonthly, imageCheckDayWise, imageCheckHourly;
    
    private CardView cardRolePartner, cardRoleManager, cardRoleHelper, cardRoleCustom;
    private LinearLayout tabRolePartner, tabRoleManager, tabRoleHelper, tabRoleCustom;
    private ImageView imageCheckPartner, imageCheckManager, imageCheckHelper, imageCheckCustom;

    private String selectedCalculationType = "Monthly";
    private String selectedRole = "Helper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_staff);

        View toolbar = findViewById(R.id.toolbarStaff);
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveStaff());

        initViews();
        setupListeners();
        selectRole("Helper"); 
        selectTab("Monthly");
    }

    private void initViews() {
        editStaffName = findViewById(R.id.editStaffName);
        editStaffEmail = findViewById(R.id.editStaffEmail);
        editStaffMobile = findViewById(R.id.editStaffMobile);
        editSalary = findViewById(R.id.editSalary);
        editShiftHours = findViewById(R.id.editShiftHours);
        editJoiningDate = findViewById(R.id.editJoiningDate);

        textTrackAttendance = findViewById(R.id.textTrackAttendance);
        textAllowAppUse = findViewById(R.id.textAllowAppUse);

        checkTrackAttendance = findViewById(R.id.checkTrackAttendance);
        checkAllowAppUse = findViewById(R.id.checkAllowAppUse);
        checkSelfAttendance = findViewById(R.id.checkSelfAttendance);

        // Permissions - Receipt
        permCreateReceipt = findViewById(R.id.permCreateReceipt);
        permViewAllReceipt = findViewById(R.id.permViewAllReceipt);
        permEditReceipt = findViewById(R.id.permEditReceipt);
        permReturnReceipt = findViewById(R.id.permReturnReceipt);
        permReceiptAdmin = findViewById(R.id.permReceiptAdmin);
        
        // Permissions - Sales
        permDiscount = findViewById(R.id.permDiscount);
        permGift = findViewById(R.id.permGift);
        permFree = findViewById(R.id.permFree);
        permAddCharges = findViewById(R.id.permAddCharges);
        permAddTax = findViewById(R.id.permAddTax);
        permGiveCredit = findViewById(R.id.permGiveCredit);
        permViewRunningTables = findViewById(R.id.permViewRunningTables);

        // Permissions - Inventory
        permInvViewOnly = findViewById(R.id.permInvViewOnly);
        permInvCreate = findViewById(R.id.permInvCreate);
        permInvEdit = findViewById(R.id.permInvEdit);
        permInvAdmin = findViewById(R.id.permInvAdmin);

        // Permissions - Staff
        permStaffAttendance = findViewById(R.id.permStaffAttendance);
        permStaffPayroll = findViewById(R.id.permStaffPayroll);
        permStaffManagement = findViewById(R.id.permStaffManagement);
        permStaffAdmin = findViewById(R.id.permStaffAdmin);

        // Permissions - Customer
        permCustViewOnly = findViewById(R.id.permCustViewOnly);
        permCustCreate = findViewById(R.id.permCustCreate);
        permCustEdit = findViewById(R.id.permCustEdit);
        permCustAdmin = findViewById(R.id.permCustAdmin);

        // Calculation Tabs
        tabMonthly = findViewById(R.id.tabMonthly);
        tabDayWise = findViewById(R.id.tabDayWise);
        tabHourly = findViewById(R.id.tabHourly);
        cardMonthly = findViewById(R.id.cardMonthly);
        cardDayWise = findViewById(R.id.cardDayWise);
        cardHourly = findViewById(R.id.cardHourly);
        imageCheckMonthly = findViewById(R.id.imageCheckMonthly);
        imageCheckDayWise = findViewById(R.id.imageCheckDayWise);
        imageCheckHourly = findViewById(R.id.imageCheckHourly);

        // Roles
        cardRolePartner = findViewById(R.id.cardRolePartner);
        cardRoleManager = findViewById(R.id.cardRoleManager);
        cardRoleHelper = findViewById(R.id.cardRoleHelper);
        cardRoleCustom = findViewById(R.id.cardRoleCustom);
        tabRolePartner = findViewById(R.id.tabRolePartner);
        tabRoleManager = findViewById(R.id.tabRoleManager);
        tabRoleHelper = findViewById(R.id.tabRoleHelper);
        tabRoleCustom = findViewById(R.id.tabRoleCustom);
        imageCheckPartner = findViewById(R.id.imageCheckPartner);
        imageCheckManager = findViewById(R.id.imageCheckManager);
        imageCheckHelper = findViewById(R.id.imageCheckHelper);
        imageCheckCustom = findViewById(R.id.imageCheckCustom);
    }

    private void setupListeners() {
        tabMonthly.setOnClickListener(v -> selectTab("Monthly"));
        tabDayWise.setOnClickListener(v -> selectTab("Day Wise"));
        tabHourly.setOnClickListener(v -> selectTab("Hourly"));

        cardRolePartner.setOnClickListener(v -> selectRole("Partner"));
        cardRoleManager.setOnClickListener(v -> selectRole("Manager"));
        cardRoleHelper.setOnClickListener(v -> selectRole("Helper"));
        cardRoleCustom.setOnClickListener(v -> selectRole("Custom"));

        editJoiningDate.setOnClickListener(v -> showDatePickerDialog());

        editStaffName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString().trim();
                if (name.isEmpty()) name = "staff";
                textTrackAttendance.setText(getString(R.string.track_attendance_salary, name));
                textAllowAppUse.setText(getString(R.string.allow_staff_use_app, name));
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void selectTab(String type) {
        selectedCalculationType = type;
        tabMonthly.setBackgroundColor(type.equals("Monthly") ? 0xFFE8F0FE : Color.WHITE);
        tabDayWise.setBackgroundColor(type.equals("Day Wise") ? 0xFFE8F0FE : Color.WHITE);
        tabHourly.setBackgroundColor(type.equals("Hourly") ? 0xFFE8F0FE : Color.WHITE);
        float density = getResources().getDisplayMetrics().density;
        cardMonthly.setCardElevation((type.equals("Monthly") ? 4 : 1) * density);
        cardDayWise.setCardElevation((type.equals("Day Wise") ? 4 : 1) * density);
        cardHourly.setCardElevation((type.equals("Hourly") ? 4 : 1) * density);
        imageCheckMonthly.setVisibility(type.equals("Monthly") ? View.VISIBLE : View.GONE);
        imageCheckDayWise.setVisibility(type.equals("Day Wise") ? View.VISIBLE : View.GONE);
        imageCheckHourly.setVisibility(type.equals("Hourly") ? View.VISIBLE : View.GONE);
    }

    private void selectRole(String role) {
        selectedRole = role;
        tabRolePartner.setBackgroundColor(role.equals("Partner") ? 0xFFE8F0FE : Color.WHITE);
        tabRoleManager.setBackgroundColor(role.equals("Manager") ? 0xFFE8F0FE : Color.WHITE);
        tabRoleHelper.setBackgroundColor(role.equals("Helper") ? 0xFFE8F0FE : Color.WHITE);
        tabRoleCustom.setBackgroundColor(role.equals("Custom") ? 0xFFE8F0FE : Color.WHITE);
        float density = getResources().getDisplayMetrics().density;
        cardRolePartner.setCardElevation((role.equals("Partner") ? 4 : 1) * density);
        cardRoleManager.setCardElevation((role.equals("Manager") ? 4 : 1) * density);
        cardRoleHelper.setCardElevation((role.equals("Helper") ? 4 : 1) * density);
        cardRoleCustom.setCardElevation((role.equals("Custom") ? 4 : 1) * density);
        imageCheckPartner.setVisibility(role.equals("Partner") ? View.VISIBLE : View.GONE);
        imageCheckManager.setVisibility(role.equals("Manager") ? View.VISIBLE : View.GONE);
        imageCheckHelper.setVisibility(role.equals("Helper") ? View.VISIBLE : View.GONE);
        imageCheckCustom.setVisibility(role.equals("Custom") ? View.VISIBLE : View.GONE);
        applyRolePermissions(role);
    }

    private void applyRolePermissions(String role) {
        boolean isPartner = role.equals("Partner");
        boolean isManager = role.equals("Manager");
        boolean isHelper = role.equals("Helper");
        boolean isCustom = role.equals("Custom");

        permCreateReceipt.setChecked(true);
        permCreateReceipt.setEnabled(isCustom); 
        permViewAllReceipt.setChecked(isPartner || isManager || isHelper);
        permEditReceipt.setChecked(isPartner || isManager);
        permReturnReceipt.setChecked(isPartner || isManager);
        permReceiptAdmin.setChecked(isPartner);

        permDiscount.setChecked(isPartner || isManager || isHelper);
        permGift.setChecked(isPartner || isManager);
        permFree.setChecked(isPartner || isManager);
        permAddCharges.setChecked(true);
        permAddTax.setChecked(isPartner || isManager);
        permGiveCredit.setChecked(isPartner || isManager || isHelper);
        permViewRunningTables.setChecked(isPartner || isManager);
        
        if (permInvViewOnly != null) {
            permInvViewOnly.setChecked(isPartner || isManager || isHelper);
            permInvCreate.setChecked(isPartner || isManager);
            permInvEdit.setChecked(isPartner || isManager);
            permInvAdmin.setChecked(isPartner);
        }
        
        if (permStaffAttendance != null) {
            permStaffAttendance.setChecked(isPartner || isManager);
            permStaffPayroll.setChecked(isPartner);
            permStaffManagement.setChecked(isPartner || isManager);
            permStaffAdmin.setChecked(isPartner);
        }
        
        if (permCustViewOnly != null) {
            permCustViewOnly.setChecked(isPartner || isManager || isHelper);
            permCustCreate.setChecked(isPartner || isManager || isHelper);
            permCustEdit.setChecked(isPartner || isManager);
            permCustAdmin.setChecked(isPartner);
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
            editJoiningDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveStaff() {
        String name = editStaffName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter staff name", Toast.LENGTH_SHORT).show();
            return;
        }

        double salary = 0;
        try { salary = Double.parseDouble(editSalary.getText().toString()); } catch (Exception ignored) {}
        int shift = 0;
        try { shift = Integer.parseInt(editShiftHours.getText().toString()); } catch (Exception ignored) {}

        Staff staff = new Staff(name, editStaffEmail.getText().toString(), editStaffMobile.getText().toString(),
                checkTrackAttendance.isChecked(), checkAllowAppUse.isChecked(), selectedCalculationType,
                salary, shift, editJoiningDate.getText().toString(), checkSelfAttendance.isChecked());
        
        staff.role = selectedRole;
        staff.permCreateReceipt = permCreateReceipt.isChecked();
        staff.permViewAllReceipt = permViewAllReceipt.isChecked();
        staff.permEditReceipt = permEditReceipt.isChecked();
        staff.permReturnReceipt = permReturnReceipt.isChecked();
        staff.permReceiptAdmin = permReceiptAdmin.isChecked();
        staff.permDiscount = permDiscount.isChecked();
        staff.permGift = permGift.isChecked();
        staff.permFree = permFree.isChecked();
        staff.permAddCharges = permAddCharges.isChecked();
        staff.permAddTax = permAddTax.isChecked();
        staff.permGiveCredit = permGiveCredit.isChecked();
        staff.permViewRunningTables = permViewRunningTables.isChecked();
        
        if (permInvViewOnly != null) {
            staff.permInvViewOnly = permInvViewOnly.isChecked();
            staff.permInvCreate = permInvCreate.isChecked();
            staff.permInvEdit = permInvEdit.isChecked();
            staff.permInvAdmin = permInvAdmin.isChecked();
        }
        
        if (permStaffAttendance != null) {
            staff.permStaffAttendance = permStaffAttendance.isChecked();
            staff.permStaffPayroll = permStaffPayroll.isChecked();
            staff.permStaffManagement = permStaffManagement.isChecked();
            staff.permStaffAdmin = permStaffAdmin.isChecked();
        }
        
        if (permCustViewOnly != null) {
            staff.permCustViewOnly = permCustViewOnly.isChecked();
            staff.permCustCreate = permCustCreate.isChecked();
            staff.permCustEdit = permCustEdit.isChecked();
            staff.permCustAdmin = permCustAdmin.isChecked();
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).staffDao().insert(staff);
            runOnUiThread(() -> {
                Toast.makeText(this, "Staff Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
