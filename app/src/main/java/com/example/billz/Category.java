package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String imageUri;
    private int backgroundColor;
    private boolean isExpense;
    private boolean isCustom;

    public Category() {}

    public Category(String name, String imageUri, int backgroundColor) {
        this.name = name;
        this.imageUri = imageUri;
        this.backgroundColor = backgroundColor;
    }

    public Category(String name, boolean isExpense) {
        this.name = name;
        this.isExpense = isExpense;
        this.isCustom = false;
    }

    public Category(String name, boolean isExpense, boolean isCustom) {
        this.name = name;
        this.isExpense = isExpense;
        this.isCustom = isCustom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }
    public boolean isExpense() { return isExpense; }
    public void setExpense(boolean expense) { isExpense = expense; }
    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }
    
    public String getInitial() {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase();
    }
}
