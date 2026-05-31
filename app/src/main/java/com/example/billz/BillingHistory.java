package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "billing_history")
public class BillingHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String invoiceNumber;
    private String planPurchased;
    private long purchaseDate;
    private double amount;
    private String currency = "INR";

    public BillingHistory(String invoiceNumber, String planPurchased, long purchaseDate, double amount) {
        this.invoiceNumber = invoiceNumber;
        this.planPurchased = planPurchased;
        this.purchaseDate = purchaseDate;
        this.amount = amount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getPlanPurchased() { return planPurchased; }
    public void setPlanPurchased(String planPurchased) { this.planPurchased = planPurchased; }

    public long getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
