package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String mobile;
    private String name;
    private String email;
    private String gender;
    private String dob;
    private String anniversary;
    private String gstin;
    private String address;
    private String notes;
    private int ordersCount = 1;
    private String lastOrderTime = "JUST NOW";

    public Customer(String mobile, String name, String email, String gender, String dob, String anniversary, String gstin, String address, String notes) {
        this.mobile = mobile;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.dob = dob;
        this.anniversary = anniversary;
        this.gstin = gstin;
        this.address = address;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMobile() { return mobile; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public String getDob() { return dob; }
    public String getAnniversary() { return anniversary; }
    public String getGstin() { return gstin; }
    public String getAddress() { return address; }
    public String getNotes() { return notes; }
    public int getOrdersCount() { return ordersCount; }
    public void setOrdersCount(int ordersCount) { this.ordersCount = ordersCount; }
    public String getLastOrderTime() { return lastOrderTime; }
    public void setLastOrderTime(String lastOrderTime) { this.lastOrderTime = lastOrderTime; }
}
