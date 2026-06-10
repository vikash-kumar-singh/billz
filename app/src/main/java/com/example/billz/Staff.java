package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "staff")
public class Staff {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int businessId;
    
    public String name;
    public String email;
    public String mobile;
    public boolean trackAttendance;
    public boolean allowAppUse;
    public String calculationType; // Monthly, Day Wise, Hourly
    public double salary;
    public int shiftHours;
    public String joiningDate;
    public boolean allowSelfAttendance;

    public String role; // Partner, Manager, Helper, Custom

    // Receipt Management
    public boolean permCreateReceipt;
    public boolean permViewAllReceipt;
    public boolean permEditReceipt;
    public boolean permReturnReceipt;
    public boolean permReceiptAdmin;

    // Sales Counter
    public boolean permDiscount;
    public boolean permGift;
    public boolean permFree;
    public boolean permAddCharges;
    public boolean permAddTax;
    public boolean permGiveCredit;
    public boolean permViewRunningTables;

    // Inventory
    public boolean permInvViewOnly;
    public boolean permInvCreate;
    public boolean permInvEdit;
    public boolean permInvAdmin;

    // Staff
    public boolean permStaffAttendance;
    public boolean permStaffPayroll;
    public boolean permStaffManagement;
    public boolean permStaffAdmin;

    // Customer
    public boolean permCustViewOnly;
    public boolean permCustCreate;
    public boolean permCustEdit;
    public boolean permCustAdmin;

    // StoreFront
    public boolean permManageBanners;
    public boolean permStoreSettings;
    public boolean permPublishItems;
    public boolean permManageOrders;

    // Reports
    public boolean permReportLowStocks;
    public boolean permReportExpiredStocks;
    public boolean permReportProfits;
    public boolean permReportSales;
    public boolean permReportPaymentModes;
    public boolean permReportSoldBy;
    public boolean permReportTopCustomers;
    public boolean permReportAdmin;

    // Expense
    public boolean permAddExpense;
    public boolean permModifyExpense;

    // Business and Settings
    public boolean permManageBusiness;
    public boolean permManageSettings;
    public boolean permCreateBusiness;

    public Staff() {} // Room needs this

    public Staff(String name, String email, String mobile, boolean trackAttendance, 
                 boolean allowAppUse, String calculationType, double salary, 
                 int shiftHours, String joiningDate, boolean allowSelfAttendance) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.trackAttendance = trackAttendance;
        this.allowAppUse = allowAppUse;
        this.calculationType = calculationType;
        this.salary = salary;
        this.shiftHours = shiftHours;
        this.joiningDate = joiningDate;
        this.allowSelfAttendance = allowSelfAttendance;
    }
}
