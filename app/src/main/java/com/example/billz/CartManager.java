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

        // Safety check for total stock if in Advance mode but adding without variant
        if (variant == null && item.isAdvanceMode()) {
            // This case shouldn't happen with the new selector logic, but we enforce it for safety
            return false;
        }

        for (CartItem ci : cartItems) {
            boolean sameItem = ci.getItem().getId() != null && ci.getItem().getId().equals(item.getId());
            boolean sameVariant = (variant == null && ci.getVariant() == null) || 
                                 (variant != null && ci.getVariant() != null && variant.getId().equals(ci.getVariant().getId()));
            
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

    public void updateQuantity(String itemId, int quantity) {
        updateQuantity(itemId, null, quantity);
    }

    public boolean updateQuantity(String itemId, String variantId, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem ci = cartItems.get(i);
            boolean sameItem = ci.getItem().getId() != null && ci.getItem().getId().equals(itemId);
            boolean sameVariant = (variantId == null && ci.getVariant() == null) || 
                                 (ci.getVariant() != null && ci.getVariant().getId().equals(variantId));

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
        String variantId = (variant != null) ? variant.getId() : null;
        int maxStock = (variant != null) ? variant.getStockQuantity() : item.getStockQuantity();
        
        boolean capped = false;
        int finalQuantity = quantity;
        if (finalQuantity > maxStock) {
            finalQuantity = maxStock;
            capped = true;
        }

        // Cleanup: If the item is in Advance mode, remove any existing "null variant" entries
        if (item.isAdvanceMode()) {
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem ci = cartItems.get(i);
                if (ci.getItem().getId() != null && ci.getItem().getId().equals(item.getId()) && ci.getVariant() == null) {
                    cartItems.remove(i);
                    i--;
                }
            }
        }

        boolean found = false;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem ci = cartItems.get(i);
            boolean sameItem = ci.getItem().getId() != null && ci.getItem().getId().equals(item.getId());
            boolean sameVariant = (variantId == null && ci.getVariant() == null) || 
                                 (ci.getVariant() != null && ci.getVariant().getId().equals(variantId));

            if (sameItem && sameVariant) {
                if (finalQuantity <= 0) {
                    cartItems.remove(i);
                } else {
                    ci.setQuantity(finalQuantity);
                }
                found = true;
                break;
            }
        }
        if (!found && finalQuantity > 0) {
            cartItems.add(new CartItem(item, variant, finalQuantity));
        }
        notifyChanged();
        return !capped;
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

    public void refreshItems(AppDatabase db) {
        for (CartItem ci : cartItems) {
            // Refresh Item
            Item freshItem = db.itemDao().getById(ci.getItem().getId());
            if (freshItem != null) {
                // Keep the last selected variant name on the Item object for grid sync
                freshItem.setVariantName(ci.getItem().getVariantName());
                ci.setItem(freshItem);
            }
            
            // Refresh Variant (Try ID match first, then Name match if IDs were rebuilt)
            if (ci.getVariant() != null) {
                Variant freshVariant = db.variantDao().getById(ci.getVariant().getId());
                if (freshVariant == null) {
                    // Try finding by name for the same item
                    List<Variant> itemVariants = db.variantDao().getVariantsForItem(ci.getItem().getId());
                    for (Variant v : itemVariants) {
                        if (v.getName().equalsIgnoreCase(ci.getVariant().getName())) {
                            freshVariant = v;
                            break;
                        }
                    }
                }

                if (freshVariant != null) {
                    ci.setVariant(freshVariant);
                }
            } else if (ci.getItem().isAdvanceMode()) {
                // If item is now Advance but cart has no variant, try to pick the first one as default
                List<Variant> itemVariants = db.variantDao().getVariantsForItem(ci.getItem().getId());
                if (!itemVariants.isEmpty()) {
                    ci.setVariant(itemVariants.get(0));
                    ci.getItem().setVariantName(itemVariants.get(0).getName());
                }
            }
        }
        notifyChanged();
    }

    private void notifyChanged() {
        if (listener != null) listener.onCartChanged();
    }
}
