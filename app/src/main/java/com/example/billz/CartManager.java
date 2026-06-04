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

    public boolean addItem(Item item) {
        return addItem(item, null);
    }

    public boolean addItem(Item item, Variant variant) {
        int maxStock = (variant != null) ? variant.getStockQuantity() : item.getStockQuantity();

        for (CartItem ci : cartItems) {
            boolean sameItem = ci.getItem().getId() == item.getId();
            boolean sameVariant = (variant == null && ci.getVariant() == null) || 
                                 (variant != null && ci.getVariant() != null && variant.getId() == ci.getVariant().getId());
            
            if (sameItem && sameVariant) {
                if (ci.getQuantity() + 1 <= maxStock) {
                    ci.addQuantity(1);
                    notifyChanged();
                    return true;
                } else {
                    return false;
                }
            }
        }

        if (1 <= maxStock) {
            cartItems.add(new CartItem(item, variant, 1));
            notifyChanged();
            return true;
        }
        return false;
    }

    public void updateQuantity(int itemId, int quantity) {
        updateQuantity(itemId, -1, quantity);
    }

    public boolean updateQuantity(int itemId, int variantId, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem ci = cartItems.get(i);
            boolean sameItem = ci.getItem().getId() == itemId;
            boolean sameVariant = (variantId == -1 && ci.getVariant() == null) || 
                                 (ci.getVariant() != null && ci.getVariant().getId() == variantId);

            if (sameItem && sameVariant) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                    notifyChanged();
                    return true;
                } else {
                    int maxStock = (ci.getVariant() != null) ? ci.getVariant().getStockQuantity() : ci.getItem().getStockQuantity();
                    if (quantity <= maxStock) {
                        ci.setQuantity(quantity);
                        notifyChanged();
                        return true;
                    } else {
                        // Cap at max stock
                        ci.setQuantity(maxStock);
                        notifyChanged();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public boolean setVariantQuantity(Item item, Variant variant, int quantity) {
        int variantId = (variant != null) ? variant.getId() : -1;
        int maxStock = (variant != null) ? variant.getStockQuantity() : item.getStockQuantity();
        
        // Cap quantity at max stock
        if (quantity > maxStock) quantity = maxStock;

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
        return quantity < maxStock || quantity == 0; // return false if we hit the limit
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
