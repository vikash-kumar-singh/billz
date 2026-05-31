package com.example.billz;

import android.content.Context;

import java.util.concurrent.atomic.AtomicInteger;

public class UsageManager {
    private static UsageManager instance;
    private final AppDatabase db;

    private UsageManager(Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    public static synchronized UsageManager getInstance(Context context) {
        if (instance == null) {
            instance = new UsageManager(context);
        }
        return instance;
    }

    public int getCustomerCount() {
        return db.customerDao().getAllCustomers().size();
    }

    public int getProductCount() {
        return db.categoryDao().getAllCategories().size(); // Placeholder for products
    }

    public int getStaffCount() {
        return db.staffDao().getAllStaff().size();
    }

    public boolean canAddCustomer(Subscription sub) {
        if ("FREE".equalsIgnoreCase(sub.getPlanType())) {
            return getCustomerCount() < 100;
        }
        return true;
    }

    public boolean canAddProduct(Subscription sub) {
        if ("FREE".equalsIgnoreCase(sub.getPlanType())) {
            return getProductCount() < 100;
        }
        return true;
    }

    public boolean canAddStaff(Subscription sub) {
        if ("FREE".equalsIgnoreCase(sub.getPlanType())) {
            return getStaffCount() < 1;
        }
        return true;
    }
}
