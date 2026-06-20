package com.example.billz;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "variants")
public class Variant {
    @PrimaryKey
    @androidx.annotation.NonNull
    private String id = ""; // Use Firestore Document ID
    private String itemId; // Foreign key-like reference to Item.id (String now)
    private String name;
    private double sellingPrice;
    private double costPrice;
    private int stockQuantity;
    private int sortOrder;
    private String imageUri;

    public Variant() {
    }

    @Ignore
    public Variant(String itemId, String name, double sellingPrice, double costPrice, int stockQuantity) {
        this.itemId = itemId;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
        this.stockQuantity = stockQuantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
