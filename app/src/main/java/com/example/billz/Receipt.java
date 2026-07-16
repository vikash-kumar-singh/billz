package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "receipts")
public class Receipt {
    @PrimaryKey
    @NonNull
    private String id; // Use Firestore Document ID as Primary Key
    private String receiptNo;
    private String invoiceNumber;
    private String customerId;
    private String customerName;
    private String paymentMode;
    private double subtotal;
    private double discount;
    private double tax;
    private double totalAmount;
    private int itemCount;
    private long timestamp;
    private long createdAt;
    private long updatedAt;
    private String businessId; // store Firebase UID
    private String businessUuid; // store stable business UUID
    private boolean isReturned;
    private boolean syncPending;

    public Receipt() {
        // Required for Firestore
    }

    public Receipt(@NonNull String id, String receiptNo, String customerName, String paymentMode, double totalAmount, int itemCount, long timestamp, String businessId) {
        this.id = id;
        this.receiptNo = receiptNo;
        this.customerName = customerName;
        this.paymentMode = paymentMode;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.timestamp = timestamp;
        this.businessId = businessId;
        this.createdAt = timestamp;
        this.updatedAt = timestamp;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getBusinessUuid() { return businessUuid; }
    public void setBusinessUuid(String businessUuid) { this.businessUuid = businessUuid; }
    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean returned) { isReturned = returned; }
    public boolean isSyncPending() { return syncPending; }
    public void setSyncPending(boolean syncPending) { this.syncPending = syncPending; }
}
