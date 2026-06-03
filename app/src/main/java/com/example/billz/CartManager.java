package com.example.billz;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems = new ArrayList<>();
    private OnCartChangedListener listener;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(Item item) {
        for (CartItem ci : cartItems) {
            if (ci.getItem().getId() == item.getId()) {
                ci.addQuantity(1);
                notifyChanged();
                return;
            }
        }
        cartItems.add(new CartItem(item, 1));
        notifyChanged();
    }

    public void updateQuantity(int itemId, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getItem().getId() == itemId) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                } else {
                    cartItems.get(i).setQuantity(quantity);
                }
                notifyChanged();
                return;
            }
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getTotalUnits() {
        int total = 0;
        for (CartItem ci : cartItems) total += ci.getQuantity();
        return total;
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem ci : cartItems) total += ci.getTotalPrice();
        return total;
    }

    public void clearCart() {
        cartItems.clear();
        notifyChanged();
    }

    public void setListener(OnCartChangedListener listener) {
        this.listener = listener;
    }

    private void notifyChanged() {
        if (listener != null) listener.onCartChanged();
    }
}
