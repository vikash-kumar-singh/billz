package com.example.billz;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ItemGridAdapter extends RecyclerView.Adapter<ItemGridAdapter.ViewHolder> {

    public static class GridItem {
        public Item item;
        public Variant variant;

        public GridItem(Item item, Variant variant) {
            this.item = item;
            this.variant = variant;
        }
    }

    private List<GridItem> items;
    private List<GridItem> itemsFull;
    private int style; // 0: Tap to add, 1: Without Category
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item, Variant variant, int position);
    }

    public ItemGridAdapter(List<GridItem> items, int style, OnItemClickListener listener) {
        this.items = items;
        this.itemsFull = new ArrayList<>(items);
        this.style = style;
        this.listener = listener;
    }

    public void filter(String text) {
        items.clear();
        if (text == null || text.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            text = text.toLowerCase();
            for (GridItem gi : itemsFull) {
                boolean matchItem = gi.item.getName() != null && gi.item.getName().toLowerCase().contains(text);
                boolean matchVariant = gi.variant != null && gi.variant.getName() != null && gi.variant.getName().toLowerCase().contains(text);
                if (matchItem || matchVariant) {
                    items.add(gi);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_grid_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridItem gridItem = items.get(position);
        Item item = gridItem.item;
        Variant variant = gridItem.variant;

        // Display logic
        if (variant != null) {
            // It's a variant tile (only used in style 0)
            holder.textName.setText(variant.getName());
            holder.textPrice.setText("₹" + (int)variant.getSellingPrice());
            holder.textVariantCenter.setText(item.getName()); // Show product name above variant name
            
            if (item.getName() != null && !item.getName().isEmpty()) {
                holder.textInitial.setText(item.getName().substring(0, 1).toUpperCase());
            }
            
            // Out of stock logic
            boolean isOutOfStock = variant.getStockQuantity() <= 0;
            holder.textOutOfStock.setVisibility(isOutOfStock ? View.VISIBLE : View.GONE);
            holder.circleBackground.setAlpha(isOutOfStock ? 0.5f : 1.0f);
            holder.textInitial.setAlpha(isOutOfStock ? 0.3f : 1.0f);

            // Selection / Quantity logic
            int qty = 0;
            String itemId = item.getId();
            String variantId = variant.getId();
            for (CartItem ci : CartManager.getInstance().getCartItems()) {
                String ciItemId = ci.getItem().getId();
                String ciVariantId = (ci.getVariant() != null) ? ci.getVariant().getId() : null;

                if (itemId != null && itemId.equals(ciItemId) && variantId != null && variantId.equals(ciVariantId)) {
                    qty += ci.getQuantity();
                }
            }
            updateSelectionOverlay(holder, qty);

        } else {
            // It's a regular item tile
            holder.textName.setText(item.getName());
            holder.textPrice.setText("₹" + (int)item.getSellingPrice());
            
            String vName = item.getVariantName();
            if (vName == null || vName.isEmpty() || vName.equalsIgnoreCase("Default")) {
                vName = "NO VARIANT";
            }
            holder.textVariantCenter.setText(vName);

            if (item.getName() != null && !item.getName().isEmpty()) {
                holder.textInitial.setText(item.getName().substring(0, 1).toUpperCase());
            }

            // Out of stock logic
            boolean isOutOfStock = item.getStockQuantity() <= 0;
            holder.textOutOfStock.setVisibility(isOutOfStock ? View.VISIBLE : View.GONE);
            holder.circleBackground.setAlpha(isOutOfStock ? 0.5f : 1.0f);
            holder.textInitial.setAlpha(isOutOfStock ? 0.3f : 1.0f);

            // Selection / Quantity logic
            int qty = 0;
            for (CartItem ci : CartManager.getInstance().getCartItems()) {
                String ciItemId = ci.getItem().getId();
                if (ciItemId != null && ciItemId.equals(item.getId())) {
                    qty += ci.getQuantity();
                }
            }
            updateSelectionOverlay(holder, qty);
        }

        if (style == 0) {
            // Tap to add style
            holder.textVariantCenter.setVisibility(View.VISIBLE);
            holder.textVariantBanner.setVisibility(View.GONE);
            holder.indicatorDot.setVisibility(View.GONE);
        } else {
            // Without category style (View everything in a grid, show categories in banner)
            holder.textVariantCenter.setVisibility(View.GONE);
            holder.textVariantBanner.setVisibility(View.VISIBLE);
            holder.indicatorDot.setVisibility(View.VISIBLE);
            String bannerText = item.getCategory();
            if (bannerText == null || bannerText.isEmpty()) bannerText = "UNCATEGORIZED";
            holder.textVariantBanner.setText(bannerText.toUpperCase());
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            GridItem currentGridItem = items.get(currentPos);
            
            if (listener != null) {
                listener.onItemClick(currentGridItem.item, currentGridItem.variant, currentPos);
            } else {
                // Default fallback if no listener (shouldn't happen in ReportsActivity)
                Item ci = currentGridItem.item;
                Variant cv = currentGridItem.variant;
                int stock = (cv != null) ? cv.getStockQuantity() : ci.getStockQuantity();
                
                if (stock > 0) {
                    if (CartManager.getInstance().addItem(ci, cv)) {
                        notifyItemChanged(currentPos);
                    }
                } else {
                    Toast.makeText(v.getContext(), "Out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return false;
            GridItem currentGridItem = items.get(currentPos);

            String itemId = currentGridItem.item.getId();
            String variantId = (currentGridItem.variant != null) ? currentGridItem.variant.getId() : null;
            
            int currentQty = 0;
            for (CartItem ci : CartManager.getInstance().getCartItems()) {
                String ciItemId = ci.getItem().getId();
                boolean sameItem = ciItemId != null && ciItemId.equals(itemId);
                
                String ciVariantId = (ci.getVariant() != null) ? ci.getVariant().getId() : null;
                boolean sameVariant = (variantId == null && ciVariantId == null) || 
                                     (variantId != null && variantId.equals(ciVariantId));

                if (sameItem && sameVariant) {
                    currentQty = ci.getQuantity();
                    break;
                }
            }

            if (currentQty > 0) {
                CartManager.getInstance().updateQuantity(itemId, variantId, currentQty - 1);
                notifyItemChanged(currentPos);
                return true;
            }
            
            // If quantity is 0 or other fallback, open edit screen
            Intent intent = new Intent(v.getContext(), ManageItemActivity.class);
            intent.putExtra("item_id", currentGridItem.item.getId());
            v.getContext().startActivity(intent);
            return true;
        });
    }

    private void updateSelectionOverlay(ViewHolder holder, int qty) {
        if (qty > 0) {
            holder.viewSelectedOverlay.setVisibility(View.VISIBLE);
            holder.textQuantityOverlay.setVisibility(View.VISIBLE);
            holder.textQuantityOverlay.setText("x" + qty);
            holder.viewSelectedOverlay.setBackgroundColor(Color.parseColor("#602563EB")); // Brand Blue
        } else {
            holder.viewSelectedOverlay.setVisibility(View.GONE);
            holder.textQuantityOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textInitial, textVariantCenter, textVariantBanner, textQuantityOverlay, textOutOfStock;
        View circleBackground, viewSelectedOverlay, indicatorDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textInitial = itemView.findViewById(R.id.textInitial);
            textVariantCenter = itemView.findViewById(R.id.textVariantCenter);
            textVariantBanner = itemView.findViewById(R.id.textVariantBanner);
            textQuantityOverlay = itemView.findViewById(R.id.textQuantityOverlay);
            textOutOfStock = itemView.findViewById(R.id.textOutOfStock);
            circleBackground = itemView.findViewById(R.id.circleBackground);
            viewSelectedOverlay = itemView.findViewById(R.id.viewSelectedOverlay);
            indicatorDot = itemView.findViewById(R.id.indicatorDot);
        }
    }
}
