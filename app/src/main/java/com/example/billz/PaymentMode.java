package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "payment_modes")
public class PaymentMode {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int businessId;
    private String name;
    private boolean isAdded; // True if it's in the \"Add / Remove\" section, false for \"Suggestion\"
    private boolean hasConfig; // True for modes like UPI/BHIM that might have settings

    public PaymentMode(String name, boolean isAdded, boolean hasConfig) {
        this.name = name;
        this.isAdded = isAdded;
        this.hasConfig = hasConfig;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBusinessId() { return businessId; }
    public void setBusinessId(int businessId) { this.businessId = businessId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isAdded() { return isAdded; }
    public void setAdded(boolean added) { isAdded = added; }

    public boolean isHasConfig() { return hasConfig; }
    public void setHasConfig(boolean hasConfig) { this.hasConfig = hasConfig; }
}
