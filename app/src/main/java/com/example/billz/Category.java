package com.example.billz;

public class Category {
    private String name;
    private boolean isExpense;
    private boolean isCustom;

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

    public String getName() { return name; }
    public boolean isExpense() { return isExpense; }
    public boolean isCustom() { return isCustom; }
    
    public String getInitial() {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase();
    }
}
