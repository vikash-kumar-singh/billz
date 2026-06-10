package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "taxes")
public class Tax {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int businessId;
    private String name;
    private double value;
    private boolean isDefault;

    public Tax(String name, double value, boolean isDefault) {
        this.name = name;
        this.value = value;
        this.isDefault = isDefault;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBusinessId() { return businessId; }
    public void setBusinessId(int businessId) { this.businessId = businessId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
