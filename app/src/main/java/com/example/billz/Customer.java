package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int businessId;
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

    public Customer() {
        // Required for Firestore
    }

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
    public int getBusinessId() { return businessId; }
    public void setBusinessId(int businessId) { this.businessId = businessId; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getAnniversary() { return anniversary; }
    public void setAnniversary(String anniversary) { this.anniversary = anniversary; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getOrdersCount() { return ordersCount; }
    public void setOrdersCount(int ordersCount) { this.ordersCount = ordersCount; }
    public String getLastOrderTime() { return lastOrderTime; }
    public void setLastOrderTime(String lastOrderTime) { this.lastOrderTime = lastOrderTime; }
}
