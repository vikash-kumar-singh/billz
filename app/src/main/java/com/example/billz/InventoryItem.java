package com.example.billz;

import java.util.List;

public class InventoryItem {
    private String name;
    private String price;
    private String stockStatus;
    private boolean isOutOfStock;
    private int stockQuantity;
    private List<String> tags;
    private String imageUri;
    private int backgroundColor = -1;
    private int type = 0; // 0: Item, 1: Category, 2: Modifier, 3: Ingredient
    private int databaseId = -1;

    public InventoryItem(String name, String price, String stockStatus, boolean isOutOfStock, int stockQuantity, List<String> tags) {
        this.name = name;
        this.price = price;
        this.stockStatus = stockStatus;
        this.isOutOfStock = isOutOfStock;
        this.stockQuantity = stockQuantity;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public String getInitial() {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase();
    }

    public String getPrice() {
        return price;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public boolean isOutOfStock() {
        return isOutOfStock;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }
}
