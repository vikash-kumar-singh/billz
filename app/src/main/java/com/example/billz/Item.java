package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String category;
    private double sellingPrice;
    private double costPrice;
    private int stockQuantity;
    private String variantName;
    private String sellBy; // Unit or Fraction
    private boolean isAdvanceMode;

    public Item(String name, String category, double sellingPrice, double costPrice, int stockQuantity, String variantName, String sellBy, boolean isAdvanceMode) {
        this.name = name;
        this.category = category;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
        this.stockQuantity = stockQuantity;
        this.variantName = variantName;
        this.sellBy = sellBy;
        this.isAdvanceMode = isAdvanceMode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public String getSellBy() { return sellBy; }
    public void setSellBy(String sellBy) { this.sellBy = sellBy; }
    public boolean isAdvanceMode() { return isAdvanceMode; }
    public void setAdvanceMode(boolean advanceMode) { isAdvanceMode = advanceMode; }
}
