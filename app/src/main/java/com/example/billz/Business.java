package com.example.billz;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "businesses")
public class Business {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String name;
    private String phoneNumber;
    private String role;
    private boolean isSelected;

    public Business(@NonNull String name, String phoneNumber, String role, boolean isSelected) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isSelected = isSelected;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
