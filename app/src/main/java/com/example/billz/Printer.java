package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "printers")
public class Printer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int businessId;
    private String name;
    private String connectionType; // Bluetooth, USB, etc.
    private String model;
    private String paperSize; // 58mm, 80mm, A4, etc.
    private String printType; // Text, Image, etc.
    private boolean isAuto;
    private int iconResId;
    private String address; // MAC address for Bluetooth or IP for Network

    public Printer(String name, String connectionType, String model, String paperSize, String printType, boolean isAuto, int iconResId) {
        this.name = name;
        this.connectionType = connectionType;
        this.model = model;
        this.paperSize = paperSize;
        this.printType = printType;
        this.isAuto = isAuto;
        this.iconResId = iconResId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBusinessId() { return businessId; }
    public void setBusinessId(int businessId) { this.businessId = businessId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }
    public String getPrintType() { return printType; }
    public void setPrintType(String printType) { this.printType = printType; }
    public boolean isAuto() { return isAuto; }
    public void setAuto(boolean auto) { isAuto = auto; }
    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
