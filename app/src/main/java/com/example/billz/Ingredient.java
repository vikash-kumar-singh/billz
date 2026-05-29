package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingredients")
public class Ingredient {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double stock;

    public Ingredient(String name, double stock) {
        this.name = name;
        this.stock = stock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }
}
