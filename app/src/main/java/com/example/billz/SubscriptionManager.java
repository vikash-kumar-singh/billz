package com.example.billz;

import android.content.Context;
import java.util.List;
import java.util.concurrent.Executors;

public class SubscriptionManager {
    private static SubscriptionManager instance;
    private final AppDatabase db;
    private Subscription activeSubscription;

    private SubscriptionManager(Context context) {
        this.db = AppDatabase.getInstance(context);
        loadSubscription();
    }

    public static synchronized SubscriptionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SubscriptionManager(context);
        }
        return instance;
    }

    private void loadSubscription() {
        Executors.newSingleThreadExecutor().execute(() -> {
            activeSubscription = db.subscriptionDao().getActiveSubscription();
            if (activeSubscription == null) {
                activeSubscription = new Subscription();
                db.subscriptionDao().insert(activeSubscription);
            }
        });
    }

    public Subscription getActiveSubscription() {
        return activeSubscription;
    }

    public boolean hasPremium() {
        if (activeSubscription == null) return false;
        return "PREMIUM".equalsIgnoreCase(activeSubscription.getPlanType()) || 
               "ENTERPRISE".equalsIgnoreCase(activeSubscription.getPlanType());
    }

    public boolean hasEnterprise() {
        if (activeSubscription == null) return false;
        return "ENTERPRISE".equalsIgnoreCase(activeSubscription.getPlanType());
    }

    public boolean isTrialActive() {
        if (activeSubscription == null) return false;
        return activeSubscription.isTrialActive() && 
               System.currentTimeMillis() < activeSubscription.getTrialEndDate();
    }

    public int getTrialDaysLeft() {
        if (!isTrialActive()) return 0;
        long diff = activeSubscription.getTrialEndDate() - System.currentTimeMillis();
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public void upgradeToPremium() {
        if (activeSubscription != null) {
            activeSubscription.setPlanType("PREMIUM");
            activeSubscription.setTrialActive(false);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.subscriptionDao().update(activeSubscription);
                
                // Sync with ReceiptSettings and Business
                ReceiptSettings rs = db.receiptSettingsDao().getSettings();
                if (rs != null) {
                    rs.setPlanType("PREMIUM");
                    db.receiptSettingsDao().insert(rs);
                }
                
                List<Business> businesses = db.businessDao().getAllBusinesses();
                for (Business b : businesses) {
                    if (b.isSelected()) {
                        b.setPlanType("PREMIUM");
                        db.businessDao().update(b);
                        break;
                    }
                }

                // Log billing history
                db.subscriptionDao().insertBillingHistory(new BillingHistory(
                    "INV-" + System.currentTimeMillis() / 1000,
                    "PREMIUM",
                    System.currentTimeMillis(),
                    299.0
                ));
            });
        }
    }
}
