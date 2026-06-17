package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_history")
public class StockHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int ingredientId = -1;
    private int itemId = -1;
    private int variantId = -1;
    private double amount;
    private boolean isAddition;
    private long timestamp;
    private String source;

    public StockHistory(int ingredientId, double amount, boolean isAddition, long timestamp, String source) {
        this.ingredientId = ingredientId;
        this.amount = amount;
        this.isAddition = isAddition;
        this.timestamp = timestamp;
        this.source = source;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public boolean isAddition() { return isAddition; }
    public void setAddition(boolean addition) { isAddition = addition; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
