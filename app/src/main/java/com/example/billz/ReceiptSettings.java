package com.example.billz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipt_settings")
public class ReceiptSettings {
    @PrimaryKey
    private int id = 1;

    private String businessName;
    private String email;
    private String phoneNumber;
    private boolean showPhoneNumber;
    private String businessAddress;
    private boolean showBusinessAddress;
    private String taxNoAndTitle;
    private boolean showTaxNo;
    private String website;
    private boolean showWebsite;
    private String receiptTitle;

    private String receiptIdPrefix = "NC";
    private int currentBillNo = 5086;

    // New fields
    private String businessLogoPath;
    private boolean showLogo;
    private boolean showListPrice;
    private boolean showRateInReceipt;
    private boolean showTotalMoneySaved;
    private boolean showCashierName;
    private boolean showCustomerPhone;
    private boolean showCustomerAddress;
    private String thankYouNote;

    private boolean useAppLanguage;
    private boolean showTotalItemCount;
    private boolean showChangeReturn;
    private boolean showPaymentDetails;
    private boolean showPoweredBy;
    private String orderItemsBy; // "Name" or "OrderAdded"

    private String messageTemplate;
    private String whatsappShareApp; // "WhatsApp" or "WhatsAppBusiness"

    private boolean roundOff;
    private boolean tableManagement;
    private boolean inventoryHealthScore;

    public ReceiptSettings() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isShowPhoneNumber() { return showPhoneNumber; }
    public void setShowPhoneNumber(boolean showPhoneNumber) { this.showPhoneNumber = showPhoneNumber; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public boolean isShowBusinessAddress() { return showBusinessAddress; }
    public void setShowBusinessAddress(boolean showBusinessAddress) { this.showBusinessAddress = showBusinessAddress; }

    public String getTaxNoAndTitle() { return taxNoAndTitle; }
    public void setTaxNoAndTitle(String taxNoAndTitle) { this.taxNoAndTitle = taxNoAndTitle; }

    public boolean isShowTaxNo() { return showTaxNo; }
    public void setShowTaxNo(boolean showTaxNo) { this.showTaxNo = showTaxNo; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public boolean isShowWebsite() { return showWebsite; }
    public void setShowWebsite(boolean showWebsite) { this.showWebsite = showWebsite; }

    public String getReceiptTitle() { return receiptTitle; }
    public void setReceiptTitle(String receiptTitle) { this.receiptTitle = receiptTitle; }

    public String getReceiptIdPrefix() { return receiptIdPrefix; }
    public void setReceiptIdPrefix(String receiptIdPrefix) { this.receiptIdPrefix = receiptIdPrefix; }

    public int getCurrentBillNo() { return currentBillNo; }
    public void setCurrentBillNo(int currentBillNo) { this.currentBillNo = currentBillNo; }

    public String getBusinessLogoPath() { return businessLogoPath; }
    public void setBusinessLogoPath(String businessLogoPath) { this.businessLogoPath = businessLogoPath; }

    public boolean isShowLogo() { return showLogo; }
    public void setShowLogo(boolean showLogo) { this.showLogo = showLogo; }

    public boolean isShowListPrice() { return showListPrice; }
    public void setShowListPrice(boolean showListPrice) { this.showListPrice = showListPrice; }

    public boolean isShowRateInReceipt() { return showRateInReceipt; }
    public void setShowRateInReceipt(boolean showRateInReceipt) { this.showRateInReceipt = showRateInReceipt; }

    public boolean isShowTotalMoneySaved() { return showTotalMoneySaved; }
    public void setShowTotalMoneySaved(boolean showTotalMoneySaved) { this.showTotalMoneySaved = showTotalMoneySaved; }

    public boolean isShowCashierName() { return showCashierName; }
    public void setShowCashierName(boolean showCashierName) { this.showCashierName = showCashierName; }

    public boolean isShowCustomerPhone() { return showCustomerPhone; }
    public void setShowCustomerPhone(boolean showCustomerPhone) { this.showCustomerPhone = showCustomerPhone; }

    public boolean isShowCustomerAddress() { return showCustomerAddress; }
    public void setShowCustomerAddress(boolean showCustomerAddress) { this.showCustomerAddress = showCustomerAddress; }

    public String getThankYouNote() { return thankYouNote; }
    public void setThankYouNote(String thankYouNote) { this.thankYouNote = thankYouNote; }

    public boolean isUseAppLanguage() { return useAppLanguage; }
    public void setUseAppLanguage(boolean useAppLanguage) { this.useAppLanguage = useAppLanguage; }

    public boolean isShowTotalItemCount() { return showTotalItemCount; }
    public void setShowTotalItemCount(boolean showTotalItemCount) { this.showTotalItemCount = showTotalItemCount; }

    public boolean isShowChangeReturn() { return showChangeReturn; }
    public void setShowChangeReturn(boolean showChangeReturn) { this.showChangeReturn = showChangeReturn; }

    public boolean isShowPaymentDetails() { return showPaymentDetails; }
    public void setShowPaymentDetails(boolean showPaymentDetails) { this.showPaymentDetails = showPaymentDetails; }

    public boolean isShowPoweredBy() { return showPoweredBy; }
    public void setShowPoweredBy(boolean showPoweredBy) { this.showPoweredBy = showPoweredBy; }

    public String getOrderItemsBy() { return orderItemsBy; }
    public void setOrderItemsBy(String orderItemsBy) { this.orderItemsBy = orderItemsBy; }

    public String getMessageTemplate() { return messageTemplate; }
    public void setMessageTemplate(String messageTemplate) { this.messageTemplate = messageTemplate; }

    public String getWhatsappShareApp() { return whatsappShareApp; }
    public void setWhatsappShareApp(String whatsappShareApp) { this.whatsappShareApp = whatsappShareApp; }

    public boolean isRoundOff() { return roundOff; }
    public void setRoundOff(boolean roundOff) { this.roundOff = roundOff; }

    public boolean isTableManagement() { return tableManagement; }
    public void setTableManagement(boolean tableManagement) { this.tableManagement = tableManagement; }

    public boolean isInventoryHealthScore() { return inventoryHealthScore; }
    public void setInventoryHealthScore(boolean inventoryHealthScore) { this.inventoryHealthScore = inventoryHealthScore; }
}

