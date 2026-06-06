package com.example.billz;

public class CartItem {
    private Item item;
    private Variant variant;
    private int quantity;

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
    
    public double getTotalPrice() {
        double price = (variant != null) ? variant.getSellingPrice() : item.getSellingPrice();
        return price * quantity;
    }
}
