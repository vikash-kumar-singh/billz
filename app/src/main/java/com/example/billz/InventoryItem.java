package com.example.billz;

import java.util.List;

public class InventoryItem {
    private String name;
    private String price;
    private String stockStatus;
    private boolean isOutOfStock;
    private int stockQuantity;
    private List<String> tags;

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
}
