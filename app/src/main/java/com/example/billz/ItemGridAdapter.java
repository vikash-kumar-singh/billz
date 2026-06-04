package com.example.billz;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGridAdapter extends RecyclerView.Adapter<ItemGridAdapter.ViewHolder> {

    private List<Item> items;
    private List<Item> itemsFull;
    private int style; // 0: Tap to add, 1: Without Category
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item, int position);
    }

    public ItemGridAdapter(List<Item> items, int style, OnItemClickListener listener) {
        this.items = items;
        this.itemsFull = new java.util.ArrayList<>(items);
        this.style = style;
        this.listener = listener;
    }

    public void filter(String text) {
        items.clear();
        if (text == null || text.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            text = text.toLowerCase();
            for (Item item : itemsFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    items.add(item);
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
        Item item = items.get(position);
        holder.textName.setText(item.getName());
        holder.textPrice.setText("₹" + (int)item.getSellingPrice());
        
        String variant = item.getVariantName();
        if (variant == null || variant.isEmpty() || variant.equalsIgnoreCase("Default")) {
            variant = "NO VARIANT";
        }

        if (style == 0) {
            // Tap to add style
            holder.textVariantCenter.setVisibility(View.VISIBLE);
            holder.textVariantBanner.setVisibility(View.GONE);
            holder.indicatorDot.setVisibility(View.GONE);
            holder.textVariantCenter.setText(variant);
        } else {
            // Without category style
            holder.textVariantCenter.setVisibility(View.GONE);
            holder.textVariantBanner.setVisibility(View.VISIBLE);
            holder.indicatorDot.setVisibility(View.VISIBLE);
            holder.textVariantBanner.setText(variant.toUpperCase());
        }

        if (item.getName() != null && !item.getName().isEmpty()) {
            holder.textInitial.setText(item.getName().substring(0, 1).toUpperCase());
        }

        // Out of stock logic
        boolean isOutOfStock = item.getStockQuantity() <= 0;
        holder.textOutOfStock.setVisibility(isOutOfStock ? View.VISIBLE : View.GONE);
        holder.circleBackground.setAlpha(isOutOfStock ? 0.5f : 1.0f);
        holder.textInitial.setAlpha(isOutOfStock ? 0.3f : 1.0f);

        // Selection / Quantity logic from CartManager
        int qty = 0;
        for (CartItem ci : CartManager.getInstance().getCartItems()) {
            if (ci.getItem().getId() == item.getId()) {
                qty += ci.getQuantity();
            }
        }

        if (qty > 0) {
            holder.viewSelectedOverlay.setVisibility(View.VISIBLE);
            holder.textQuantityOverlay.setVisibility(View.VISIBLE);
            holder.textQuantityOverlay.setText("x" + qty);
            if (style == 0) {
                holder.viewSelectedOverlay.setBackgroundColor(Color.parseColor("#80A1A1A1")); // Grey
            } else {
                holder.viewSelectedOverlay.setBackgroundColor(Color.parseColor("#602563EB")); // Blue
            }
        } else {
            holder.viewSelectedOverlay.setVisibility(View.GONE);
            holder.textQuantityOverlay.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isOutOfStock) return;
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            
            if (listener != null) {
                listener.onItemClick(items.get(currentPos), currentPos);
            } else {
                CartManager.getInstance().addItem(items.get(currentPos));
                notifyItemChanged(currentPos);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return false;
            
            Intent intent = new Intent(v.getContext(), ManageItemActivity.class);
            intent.putExtra("item_id", items.get(currentPos).getId());
            v.getContext().startActivity(intent);
            return true;
        });
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
