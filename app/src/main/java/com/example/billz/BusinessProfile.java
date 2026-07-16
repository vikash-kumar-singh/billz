package com.example.billz;

public class BusinessProfile {
    private String businessName;
    private String address;
    private String category;
    private String email;
    private String mobile;
    private String plan;
    private String status;
    private String role;
    private String uid;
    private String country;
    private String timezone;
    private String businessType;
    private String currency;
    private String numberSystem;
    private int decimalPlaces;
    private String businessUuid;
    private boolean setupCompleted;

    public BusinessProfile() {}

    public String getBusinessUuid() { return businessUuid; }
    public void setBusinessUuid(String businessUuid) { this.businessUuid = businessUuid; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getNumberSystem() { return numberSystem; }
    public void setNumberSystem(String numberSystem) { this.numberSystem = numberSystem; }

    public int getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(int decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public boolean isSetupCompleted() { return setupCompleted; }
    public void setSetupCompleted(boolean setupCompleted) { this.setupCompleted = setupCompleted; }
}
