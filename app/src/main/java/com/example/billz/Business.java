package com.example.billz;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "businesses")
public class Business {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uuid = java.util.UUID.randomUUID().toString();
    
    @NonNull
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean isSelected;
    private String logoPath;
    private String category;
    private String status;
    private String plan;

    public Business(@NonNull String name, String phoneNumber, String role, boolean isSelected) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isSelected = isSelected;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
}
