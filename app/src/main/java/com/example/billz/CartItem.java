package com.example.billz;

public class CartItem {
    private Item item;
    private Variant variant;
    private int quantity;
    private double customPrice = -1; // -1 means use original price

    public CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public CartItem(Item item, Variant variant, int quantity) {
        this.item = item;
        this.variant = variant;
        this.quantity = quantity;
    }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void addQuantity(int delta) { this.quantity += delta; }

    public double getUnitPrice() {
        if (customPrice >= 0) return customPrice;
        return (variant != null) ? variant.getSellingPrice() : item.getSellingPrice();
    }

    public void setCustomPrice(double customPrice) {
        this.customPrice = customPrice;
    }

    public boolean hasCustomPrice() {
        return customPrice >= 0;
    }

    public double getTotalPrice() {
        return getUnitPrice() * quantity;
    }
}
