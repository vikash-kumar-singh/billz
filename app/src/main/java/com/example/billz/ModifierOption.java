package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "modifier_options")
public class ModifierOption {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int modifierSetId;
    private String name;
    private double price;

    public ModifierOption(int modifierSetId, String name, double price) {
        this.modifierSetId = modifierSetId;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getModifierSetId() { return modifierSetId; }
    public void setModifierSetId(int modifierSetId) { this.modifierSetId = modifierSetId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
