package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipt_items")
public class ReceiptItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String receiptId;
    private String itemName;
    private String variantName;
    private double price;
    private int quantity;

    public ReceiptItem(String receiptId, String itemName, String variantName, double price, int quantity) {
        this.receiptId = receiptId;
        this.itemName = itemName;
        this.variantName = variantName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
