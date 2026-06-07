package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipts")
public class Receipt {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String receiptNo;
    private String customerName;
    private String paymentMode;
    private double totalAmount;
    private int itemCount;
    private long timestamp;
    private int businessId;
    private boolean isReturned;

    public Receipt(String receiptNo, String customerName, String paymentMode, double totalAmount, int itemCount, long timestamp, int businessId) {
        this.receiptNo = receiptNo;
        this.customerName = customerName;
        this.paymentMode = paymentMode;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.timestamp = timestamp;
        this.businessId = businessId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getBusinessId() { return businessId; }
    public void setBusinessId(int businessId) { this.businessId = businessId; }
    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean returned) { isReturned = returned; }
}
