package com.example.billz;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.content.Intent;
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

import com.google.android.material.appbar.MaterialToolbar;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddStaffActivity extends AppCompatActivity {

    private EditText editStaffName, editStaffEmail, editStaffMobile, editSalary, editShiftHours, editJoiningDate;
    private TextView textTrackAttendance, textAllowAppUse, textSalaryLabel;
    private MaterialCheckBox checkTrackAttendance, checkAllowAppUse, checkSelfAttendance;

    // Permission Checkboxes
    private MaterialCheckBox permCreateReceipt, permViewAllReceipt, permEditReceipt, permReturnReceipt, permReceiptAdmin;
    private MaterialCheckBox permDiscount, permGift, permFree, permAddCharges, permAddTax, permGiveCredit, permViewRunningTables;
    private MaterialCheckBox permInvViewOnly, permInvCreate, permInvEdit, permInvAdmin;
    private MaterialCheckBox permStaffAttendance, permStaffPayroll, permStaffManagement, permStaffAdmin;
    private MaterialCheckBox permCustViewOnly, permCustCreate, permCustEdit, permCustAdmin;

    // StoreFront
    private MaterialCheckBox permManageBanners, permStoreSettings, permPublishItems, permManageOrders;
    
    // Reports
    private MaterialCheckBox permReportLowStocks, permReportExpiredStocks, permReportProfits, permReportSales, permReportPaymentModes, permReportSoldBy, permReportTopCustomers, permReportAdmin;
    
    // Expense
    private MaterialCheckBox permAddExpense, permModifyExpense;
    
    // Business and Settings
    private MaterialCheckBox permManageBusiness, permManageSettings, permCreateBusiness;

    private LinearLayout tabMonthly, tabDayWise, tabHourly, layoutShiftHours;
    private CardView cardMonthly, cardDayWise, cardHourly;
    private ImageView imageCheckMonthly, imageCheckDayWise, imageCheckHourly;
    
    private CardView cardRolePartner, cardRoleManager, cardRoleHelper, cardRoleCustom;
    private LinearLayout tabRolePartner, tabRoleManager, tabRoleHelper, tabRoleCustom;

    private String selectedCalculationType = "Monthly";
    private String selectedRole = "Helper";
    private int staffId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_staff);

        MaterialToolbar toolbar = findViewById(R.id.toolbarStaff);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarStaffLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        toolbar.setNavigationOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveStaff());

        initViews();
        setupListeners();
        
        staffId = getIntent().getIntExtra("staff_id", -1);
        if (staffId != -1) {
            toolbar.setTitle("UPDATE STAFF");
            loadStaffData();
        } else {
            selectRole("Helper");
            selectTab("Monthly");
        }
    }

    private void loadStaffData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Staff staff = AppDatabase.getInstance(this).staffDao().getById(staffId);
            if (staff != null) {
                runOnUiThread(() -> {
                    editStaffName.setText(staff.name);
                    editStaffEmail.setText(staff.email);
                    editStaffMobile.setText(staff.mobile);
                    editSalary.setText(String.valueOf(staff.salary));
                    editShiftHours.setText(String.valueOf(staff.shiftHours));
                    editJoiningDate.setText(staff.joiningDate);
                    
                    updateLabels(staff.name);
                    
                    checkTrackAttendance.setChecked(staff.trackAttendance);
                    checkAllowAppUse.setChecked(staff.allowAppUse);
                    checkSelfAttendance.setChecked(staff.allowSelfAttendance);

                    selectTab(staff.calculationType);
                    selectRole(staff.role);

                    // After selectRole, some might have been overwritten, so set them manually
                    applyStoredPermissions(staff);
                });
            }
        });
    }

    private void applyStoredPermissions(Staff staff) {
        permCreateReceipt.setChecked(staff.permCreateReceipt);
        permViewAllReceipt.setChecked(staff.permViewAllReceipt);
        permEditReceipt.setChecked(staff.permEditReceipt);
        permReturnReceipt.setChecked(staff.permReturnReceipt);
        permReceiptAdmin.setChecked(staff.permReceiptAdmin);

        permDiscount.setChecked(staff.permDiscount);
        permGift.setChecked(staff.permGift);
        permFree.setChecked(staff.permFree);
        permAddCharges.setChecked(staff.permAddCharges);
        permAddTax.setChecked(staff.permAddTax);
        permGiveCredit.setChecked(staff.permGiveCredit);
        permViewRunningTables.setChecked(staff.permViewRunningTables);
        
        permInvViewOnly.setChecked(staff.permInvViewOnly);
        permInvCreate.setChecked(staff.permInvCreate);
        permInvEdit.setChecked(staff.permInvEdit);
        permInvAdmin.setChecked(staff.permInvAdmin);
        
        permStaffAttendance.setChecked(staff.permStaffAttendance);
        permStaffPayroll.setChecked(staff.permStaffPayroll);
        permStaffManagement.setChecked(staff.permStaffManagement);
        permStaffAdmin.setChecked(staff.permStaffAdmin);
        
        permCustViewOnly.setChecked(staff.permCustViewOnly);
        permCustCreate.setChecked(staff.permCustCreate);
        permCustEdit.setChecked(staff.permCustEdit);
        permCustAdmin.setChecked(staff.permCustAdmin);

        permManageBanners.setChecked(staff.permManageBanners);
        permStoreSettings.setChecked(staff.permStoreSettings);
        permPublishItems.setChecked(staff.permPublishItems);
        permManageOrders.setChecked(staff.permManageOrders);

        permReportLowStocks.setChecked(staff.permReportLowStocks);
        permReportExpiredStocks.setChecked(staff.permReportExpiredStocks);
        permReportProfits.setChecked(staff.permReportProfits);
        permReportSales.setChecked(staff.permReportSales);
        permReportPaymentModes.setChecked(staff.permReportPaymentModes);
        permReportSoldBy.setChecked(staff.permReportSoldBy);
        permReportTopCustomers.setChecked(staff.permReportTopCustomers);
        permReportAdmin.setChecked(staff.permReportAdmin);

        permAddExpense.setChecked(staff.permAddExpense);
        permModifyExpense.setChecked(staff.permModifyExpense);

        permManageBusiness.setChecked(staff.permManageBusiness);
        permManageSettings.setChecked(staff.permManageSettings);
        permCreateBusiness.setChecked(staff.permCreateBusiness);
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
        textSalaryLabel = findViewById(R.id.textSalaryLabel);

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

        // Permissions - StoreFront
        permManageBanners = findViewById(R.id.permManageBanners);
        permStoreSettings = findViewById(R.id.permStoreSettings);
        permPublishItems = findViewById(R.id.permPublishItems);
        permManageOrders = findViewById(R.id.permManageOrders);

        // Permissions - Reports
        permReportLowStocks = findViewById(R.id.permReportLowStocks);
        permReportExpiredStocks = findViewById(R.id.permReportExpiredStocks);
        permReportProfits = findViewById(R.id.permReportProfits);
        permReportSales = findViewById(R.id.permReportSales);
        permReportPaymentModes = findViewById(R.id.permReportPaymentModes);
        permReportSoldBy = findViewById(R.id.permReportSoldBy);
        permReportTopCustomers = findViewById(R.id.permReportTopCustomers);
        permReportAdmin = findViewById(R.id.permReportAdmin);

        // Permissions - Expense
        permAddExpense = findViewById(R.id.permAddExpense);
        permModifyExpense = findViewById(R.id.permModifyExpense);

        // Permissions - Business and Settings
        permManageBusiness = findViewById(R.id.permManageBusiness);
        permManageSettings = findViewById(R.id.permManageSettings);
        permCreateBusiness = findViewById(R.id.permCreateBusiness);

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

        layoutShiftHours = findViewById(R.id.layoutShiftHours);

        // Roles
        cardRolePartner = findViewById(R.id.cardRolePartner);
        cardRoleManager = findViewById(R.id.cardRoleManager);
        cardRoleHelper = findViewById(R.id.cardRoleHelper);
        cardRoleCustom = findViewById(R.id.cardRoleCustom);
        tabRolePartner = findViewById(R.id.tabRolePartner);
        tabRoleManager = findViewById(R.id.tabRoleManager);
        tabRoleHelper = findViewById(R.id.tabRoleHelper);
        tabRoleCustom = findViewById(R.id.tabRoleCustom);
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
                updateLabels(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateLabels(String name) {
        String displayName = (name == null || name.isEmpty()) ? "staff" : name;
        textTrackAttendance.setText(getString(R.string.track_attendance_salary, displayName));
        textAllowAppUse.setText(getString(R.string.allow_staff_use_app, displayName));
        textSalaryLabel.setText(getString(R.string.enter_staff_salary_name, displayName));
    }

    private void selectTab(String type) {
        selectedCalculationType = type;
        
        if (layoutShiftHours != null) {
            layoutShiftHours.setVisibility(type.equals("Hourly") ? View.GONE : View.VISIBLE);
        }

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
        applyRolePermissions(role);
    }

    private void applyRolePermissions(String role) {
        boolean isPartner = role.equals("Partner");
        boolean isManager = role.equals("Manager");
        boolean isHelper = role.equals("Helper");
        boolean isCustom = role.equals("Custom");

        if (isCustom) {
            // Receipt
            setPerm(permCreateReceipt, true, false); // Grey checked
            setPerm(permViewAllReceipt, false, true);
            setPerm(permEditReceipt, false, true);
            setPerm(permReturnReceipt, false, true);
            setPerm(permReceiptAdmin, false, true);

            // Sales
            setPerm(permDiscount, false, true);
            setPerm(permGift, false, true);
            setPerm(permFree, false, true);
            setPerm(permAddCharges, false, true);
            setPerm(permAddTax, false, true);
            setPerm(permGiveCredit, true, true); // Blue checked
            setPerm(permViewRunningTables, false, true);
            
            // Inventory
            setPerm(permInvViewOnly, false, true);
            setPerm(permInvCreate, false, true);
            setPerm(permInvEdit, false, true);
            setPerm(permInvAdmin, false, true);
            
            // Staff
            setPerm(permStaffAttendance, true, true); // Blue checked
            setPerm(permStaffPayroll, true, true);    // Blue checked
            setPerm(permStaffManagement, false, true);
            setPerm(permStaffAdmin, false, true);
            
            // Customer
            setPerm(permCustViewOnly, false, true);
            setPerm(permCustCreate, false, true);
            setPerm(permCustEdit, false, true);
            setPerm(permCustAdmin, false, true);

            // StoreFront
            setPerm(permManageBanners, false, true);
            setPerm(permStoreSettings, false, true);
            setPerm(permPublishItems, false, true);
            setPerm(permManageOrders, false, true);

            // Reports
            setPerm(permReportLowStocks, false, true);
            setPerm(permReportExpiredStocks, false, true);
            setPerm(permReportProfits, false, true);
            setPerm(permReportSales, false, true);
            setPerm(permReportPaymentModes, false, true);
            setPerm(permReportSoldBy, false, true);
            setPerm(permReportTopCustomers, false, true);
            setPerm(permReportAdmin, false, true);

            // Expense
            setPerm(permAddExpense, false, true);
            setPerm(permModifyExpense, false, true);

            // Business and Settings
            setPerm(permManageBusiness, false, true);
            setPerm(permManageSettings, false, true);
            setPerm(permCreateBusiness, false, true);
            return;
        }

        // Logic based on images: 
        // Grey = Required/Fixed for Role (Locked)
        // Blue = Specific to Role (Adjustable)

        if (isPartner) {
            // Receipt
            setPerm(permCreateReceipt, true, false);
            setPerm(permViewAllReceipt, true, false);
            setPerm(permEditReceipt, true, false);
            setPerm(permReturnReceipt, true, false);
            setPerm(permReceiptAdmin, true, true);

            // Sales
            setPerm(permDiscount, true, true);
            setPerm(permGift, true, true);
            setPerm(permFree, true, true);
            setPerm(permAddCharges, true, true);
            setPerm(permAddTax, true, true);
            setPerm(permGiveCredit, true, true);
            setPerm(permViewRunningTables, false, true);
            
            // Inventory
            setPerm(permInvViewOnly, true, false);
            setPerm(permInvCreate, true, false);
            setPerm(permInvEdit, true, false);
            setPerm(permInvAdmin, true, true);
            
            // Staff
            setPerm(permStaffAttendance, true, true);
            setPerm(permStaffPayroll, true, true);
            setPerm(permStaffManagement, true, true);
            setPerm(permStaffAdmin, true, true);
            
            // Customer
            setPerm(permCustViewOnly, true, false);
            setPerm(permCustCreate, true, false);
            setPerm(permCustEdit, true, false);
            setPerm(permCustAdmin, true, true);

            // StoreFront
            setPerm(permManageBanners, true, true);
            setPerm(permStoreSettings, true, true);
            setPerm(permPublishItems, true, true);
            setPerm(permManageOrders, true, true);

            // Reports
            setPerm(permReportLowStocks, true, false);
            setPerm(permReportExpiredStocks, true, false);
            setPerm(permReportProfits, true, false);
            setPerm(permReportSales, true, false);
            setPerm(permReportPaymentModes, true, false);
            setPerm(permReportSoldBy, true, false);
            setPerm(permReportTopCustomers, true, false);
            setPerm(permReportAdmin, true, true);

            // Expense
            setPerm(permAddExpense, true, false);
            setPerm(permModifyExpense, true, true);

            // Business and Settings
            setPerm(permManageBusiness, true, true);
            setPerm(permManageSettings, true, true);
            setPerm(permCreateBusiness, true, true);
            
        } else if (isManager) {
            // Receipt
            setPerm(permCreateReceipt, true, false);
            setPerm(permViewAllReceipt, true, false);
            setPerm(permEditReceipt, true, true);
            setPerm(permReturnReceipt, true, true);
            setPerm(permReceiptAdmin, false, true);

            // Sales
            setPerm(permDiscount, true, true);
            setPerm(permGift, true, true);
            setPerm(permFree, true, true);
            setPerm(permAddCharges, true, true);
            setPerm(permAddTax, true, true);
            setPerm(permGiveCredit, true, true);
            setPerm(permViewRunningTables, false, true);
            
            // Inventory
            setPerm(permInvViewOnly, true, false);
            setPerm(permInvCreate, true, false);
            setPerm(permInvEdit, true, false);
            setPerm(permInvAdmin, true, true);
            
            // Staff
            setPerm(permStaffAttendance, true, true);
            setPerm(permStaffPayroll, false, true);
            setPerm(permStaffManagement, false, true);
            setPerm(permStaffAdmin, false, true);
            
            // Customer
            setPerm(permCustViewOnly, true, false);
            setPerm(permCustCreate, true, false);
            setPerm(permCustEdit, true, false);
            setPerm(permCustAdmin, true, true);

            // StoreFront
            setPerm(permManageBanners, true, true);
            setPerm(permStoreSettings, false, true);
            setPerm(permPublishItems, true, true);
            setPerm(permManageOrders, true, true);

            // Reports
            setPerm(permReportLowStocks, true, false);
            setPerm(permReportExpiredStocks, true, false);
            setPerm(permReportProfits, true, false);
            setPerm(permReportSales, true, false);
            setPerm(permReportPaymentModes, true, false);
            setPerm(permReportSoldBy, true, false);
            setPerm(permReportTopCustomers, true, false);
            setPerm(permReportAdmin, true, true);

            // Expense
            setPerm(permAddExpense, true, true);
            setPerm(permModifyExpense, false, true);

            // Business and Settings
            setPerm(permManageBusiness, true, true);
            setPerm(permManageSettings, true, true);
            setPerm(permCreateBusiness, false, true);
        } else if (isHelper) {
            // Receipt
            setPerm(permCreateReceipt, true, false); // Grey
            setPerm(permViewAllReceipt, true, true); // Blue
            setPerm(permEditReceipt, false, true);
            setPerm(permReturnReceipt, false, true);
            setPerm(permReceiptAdmin, false, true);

            // Sales
            setPerm(permDiscount, true, true);
            setPerm(permGift, false, true);
            setPerm(permFree, false, true);
            setPerm(permAddCharges, true, true);
            setPerm(permAddTax, false, true);
            setPerm(permGiveCredit, true, true);
            setPerm(permViewRunningTables, false, true);

            // Inventory - All Unchecked
            setPerm(permInvViewOnly, false, true);
            setPerm(permInvCreate, false, true);
            setPerm(permInvEdit, false, true);
            setPerm(permInvAdmin, false, true);

            // Staff - All Unchecked
            setPerm(permStaffAttendance, false, true);
            setPerm(permStaffPayroll, false, true);
            setPerm(permStaffManagement, false, true);
            setPerm(permStaffAdmin, false, true);

            // Customer - All Unchecked
            setPerm(permCustViewOnly, false, true);
            setPerm(permCustCreate, false, true);
            setPerm(permCustEdit, false, true);
            setPerm(permCustAdmin, false, true);

            // StoreFront - All Unchecked
            setPerm(permManageBanners, false, true);
            setPerm(permStoreSettings, false, true);
            setPerm(permPublishItems, false, true);
            setPerm(permManageOrders, false, true);

            // Reports - All Unchecked
            setPerm(permReportLowStocks, false, true);
            setPerm(permReportExpiredStocks, false, true);
            setPerm(permReportProfits, false, true);
            setPerm(permReportSales, false, true);
            setPerm(permReportPaymentModes, false, true);
            setPerm(permReportSoldBy, false, true);
            setPerm(permReportTopCustomers, false, true);
            setPerm(permReportAdmin, false, true);

            // Expense
            setPerm(permAddExpense, true, true);
            setPerm(permModifyExpense, false, true);

            // Business and Settings - All Unchecked
            setPerm(permManageBusiness, false, true);
            setPerm(permManageSettings, false, true);
            setPerm(permCreateBusiness, false, true);
        }
    }

    private void setPerm(MaterialCheckBox cb, boolean checked, boolean isToggleable) {
        if (cb == null) return;
        cb.setChecked(checked);
        cb.setEnabled(isToggleable);
        int color = isToggleable ? Color.parseColor("#3F51B5") : Color.parseColor("#94A3B8");
        cb.setButtonTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void resetTints(int color) {
        android.content.res.ColorStateList list = android.content.res.ColorStateList.valueOf(color);
        permCreateReceipt.setButtonTintList(list);
        permViewAllReceipt.setButtonTintList(list);
        permEditReceipt.setButtonTintList(list);
        permReturnReceipt.setButtonTintList(list);
        permReceiptAdmin.setButtonTintList(list);
        permDiscount.setButtonTintList(list);
        permGift.setButtonTintList(list);
        permFree.setButtonTintList(list);
        permAddCharges.setButtonTintList(list);
        permAddTax.setButtonTintList(list);
        permGiveCredit.setButtonTintList(list);
        permViewRunningTables.setButtonTintList(list);
        permInvViewOnly.setButtonTintList(list);
        permInvCreate.setButtonTintList(list);
        permInvEdit.setButtonTintList(list);
        permInvAdmin.setButtonTintList(list);
        permStaffAttendance.setButtonTintList(list);
        permStaffPayroll.setButtonTintList(list);
        permStaffManagement.setButtonTintList(list);
        permStaffAdmin.setButtonTintList(list);
        permCustViewOnly.setButtonTintList(list);
        permCustCreate.setButtonTintList(list);
        permCustEdit.setButtonTintList(list);
        permCustAdmin.setButtonTintList(list);
        permManageBanners.setButtonTintList(list);
        permStoreSettings.setButtonTintList(list);
        permPublishItems.setButtonTintList(list);
        permManageOrders.setButtonTintList(list);
        permReportLowStocks.setButtonTintList(list);
        permReportExpiredStocks.setButtonTintList(list);
        permReportProfits.setButtonTintList(list);
        permReportSales.setButtonTintList(list);
        permReportPaymentModes.setButtonTintList(list);
        permReportSoldBy.setButtonTintList(list);
        permReportTopCustomers.setButtonTintList(list);
        permReportAdmin.setButtonTintList(list);
        permAddExpense.setButtonTintList(list);
        permModifyExpense.setButtonTintList(list);
        permManageBusiness.setButtonTintList(list);
        permManageSettings.setButtonTintList(list);
        permCreateBusiness.setButtonTintList(list);
    }

    private void enablePermissionCheckboxes(boolean enabled) {
        permCreateReceipt.setEnabled(enabled);
        permViewAllReceipt.setEnabled(enabled);
        permEditReceipt.setEnabled(enabled);
        permReturnReceipt.setEnabled(enabled);
        permReceiptAdmin.setEnabled(enabled);
        permDiscount.setEnabled(enabled);
        permGift.setEnabled(enabled);
        permFree.setEnabled(enabled);
        permAddCharges.setEnabled(enabled);
        permAddTax.setEnabled(enabled);
        permGiveCredit.setEnabled(enabled);
        permViewRunningTables.setEnabled(enabled);
        permInvViewOnly.setEnabled(enabled);
        permInvCreate.setEnabled(enabled);
        permInvEdit.setEnabled(enabled);
        permInvAdmin.setEnabled(enabled);
        permStaffAttendance.setEnabled(enabled);
        permStaffPayroll.setEnabled(enabled);
        permStaffManagement.setEnabled(enabled);
        permStaffAdmin.setEnabled(enabled);
        permCustViewOnly.setEnabled(enabled);
        permCustCreate.setEnabled(enabled);
        permCustEdit.setEnabled(enabled);
        permCustAdmin.setEnabled(enabled);
        permManageBanners.setEnabled(enabled);
        permStoreSettings.setEnabled(enabled);
        permPublishItems.setEnabled(enabled);
        permManageOrders.setEnabled(enabled);
        permReportLowStocks.setEnabled(enabled);
        permReportExpiredStocks.setEnabled(enabled);
        permReportProfits.setEnabled(enabled);
        permReportSales.setEnabled(enabled);
        permReportPaymentModes.setEnabled(enabled);
        permReportSoldBy.setEnabled(enabled);
        permReportTopCustomers.setEnabled(enabled);
        permReportAdmin.setEnabled(enabled);
        permAddExpense.setEnabled(enabled);
        permModifyExpense.setEnabled(enabled);
        permManageBusiness.setEnabled(enabled);
        permManageSettings.setEnabled(enabled);
        permCreateBusiness.setEnabled(enabled);
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            editJoiningDate.setText(day + "/" + (month + 1) + "/" + year);
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
        
        if (staffId != -1) {
            staff.id = staffId;
        }

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

        // StoreFront
        if (permManageBanners != null) {
            staff.permManageBanners = permManageBanners.isChecked();
            staff.permStoreSettings = permStoreSettings.isChecked();
            staff.permPublishItems = permPublishItems.isChecked();
            staff.permManageOrders = permManageOrders.isChecked();
        }

        // Reports
        if (permReportLowStocks != null) {
            staff.permReportLowStocks = permReportLowStocks.isChecked();
            staff.permReportExpiredStocks = permReportExpiredStocks.isChecked();
            staff.permReportProfits = permReportProfits.isChecked();
            staff.permReportSales = permReportSales.isChecked();
            staff.permReportPaymentModes = permReportPaymentModes.isChecked();
            staff.permReportSoldBy = permReportSoldBy.isChecked();
            staff.permReportTopCustomers = permReportTopCustomers.isChecked();
            staff.permReportAdmin = permReportAdmin.isChecked();
        }

        // Expense
        if (permAddExpense != null) {
            staff.permAddExpense = permAddExpense.isChecked();
            staff.permModifyExpense = permModifyExpense.isChecked();
        }

        // Business and Settings
        if (permManageBusiness != null) {
            staff.permManageBusiness = permManageBusiness.isChecked();
            staff.permManageSettings = permManageSettings.isChecked();
            staff.permCreateBusiness = permCreateBusiness.isChecked();
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Business active = db.businessDao().getSelectedBusiness();
            if (active != null) {
                staff.businessId = active.getId();
            }
            
            if (staffId != -1) {
                db.staffDao().update(staff);
            } else {
                db.staffDao().insert(staff);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, staffId != -1 ? "Staff Updated Successfully" : "Staff Saved Successfully", Toast.LENGTH_SHORT).show();
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_staff_name", name);
                resultIntent.putExtra("new_staff_email", editStaffEmail.getText().toString());
                setResult(RESULT_OK, resultIntent);

                finish();
            });
        });
    }
}
