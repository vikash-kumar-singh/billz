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
        addItem(item, null);
    }

    public void addItem(Item item, Variant variant) {
        for (CartItem ci : cartItems) {
            boolean sameItem = ci.getItem().getId() == item.getId();
            boolean sameVariant = (variant == null && ci.getVariant() == null) || 
                                 (variant != null && ci.getVariant() != null && variant.getId() == ci.getVariant().getId());
            
            if (sameItem && sameVariant) {
                ci.addQuantity(1);
                notifyChanged();
                return;
            }
        }
        cartItems.add(new CartItem(item, variant, 1));
        notifyChanged();
    }

    public void updateQuantity(int itemId, int quantity) {
        updateQuantity(itemId, -1, quantity);
    }

    public void updateQuantity(int itemId, int variantId, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem ci = cartItems.get(i);
            boolean sameItem = ci.getItem().getId() == itemId;
            boolean sameVariant = (variantId == -1 && ci.getVariant() == null) || 
                                 (ci.getVariant() != null && ci.getVariant().getId() == variantId);

            if (sameItem && sameVariant) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                } else {
                    ci.setQuantity(quantity);
                }
                notifyChanged();
                return;
            }
        }
    }

    public void setVariantQuantity(Item item, Variant variant, int quantity) {
        int variantId = (variant != null) ? variant.getId() : -1;
        boolean found = false;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem ci = cartItems.get(i);
            boolean sameItem = ci.getItem().getId() == item.getId();
            boolean sameVariant = (variantId == -1 && ci.getVariant() == null) || 
                                 (ci.getVariant() != null && ci.getVariant().getId() == variantId);

            if (sameItem && sameVariant) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                } else {
                    ci.setQuantity(quantity);
                }
                found = true;
                break;
            }
        }
        if (!found && quantity > 0) {
            cartItems.add(new CartItem(item, variant, quantity));
        }
        notifyChanged();
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
