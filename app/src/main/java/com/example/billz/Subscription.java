package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscriptions")
public class Subscription {
    @PrimaryKey
    private int id = 1; // Single active subscription

    private String planType; // FREE, PREMIUM, ENTERPRISE
    private String status; // ACTIVE, EXPIRED, SUSPENDED
    private long startDate;
    private long expiryDate;
    private boolean trialActive;
    private long trialStartDate;
    private long trialEndDate;

    public Subscription() {
        this.planType = "FREE";
        this.status = "ACTIVE";
        this.startDate = System.currentTimeMillis();
        // Default trial logic
        this.trialActive = true;
        this.trialStartDate = System.currentTimeMillis();
        this.trialEndDate = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000); // 7 days
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public boolean isTrialActive() { return trialActive; }
    public void setTrialActive(boolean trialActive) { this.trialActive = trialActive; }

    public long getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(long trialStartDate) { this.trialStartDate = trialStartDate; }

    public long getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(long trialEndDate) { this.trialEndDate = trialEndDate; }
}
