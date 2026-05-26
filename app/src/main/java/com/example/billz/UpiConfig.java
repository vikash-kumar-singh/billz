package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "upi_config")
public class UpiConfig {
    @PrimaryKey
    private int id = 1;

    private String upiId;
    private String fullName;
    private String merchantId;

    public UpiConfig(String upiId, String fullName, String merchantId) {
        this.upiId = upiId;
        this.fullName = fullName;
        this.merchantId = merchantId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
}
