package com.example.billz;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> items;
    private List<InventoryItem> itemsFull;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(List<InventoryItem> items, Context context) {
        this.items = items;
        this.itemsFull = new ArrayList<>(items);
        this.context = context;
    }

    public InventoryAdapter(List<InventoryItem> items, Context context, OnItemClickListener listener) {
        this.items = items;
        this.itemsFull = new ArrayList<>(items);
        this.context = context;
        this.listener = listener;
    }

    public void filter(String text) {
        items.clear();
        if (text == null || text.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            text = text.toLowerCase();
            for (InventoryItem item : itemsFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByTags(List<String> selectedTags) {
        items.clear();
        if (selectedTags == null || selectedTags.isEmpty() || selectedTags.contains("Brand All Item")) {
            items.addAll(itemsFull);
        } else {
            List<String> lowerTags = new ArrayList<>();
            for (String tag : selectedTags) lowerTags.add(tag.toLowerCase());

            for (InventoryItem item : itemsFull) {
                boolean matches = false;
                for (String itemTag : item.getTags()) {
                    for (String filterTag : lowerTags) {
                        if (itemTag.toLowerCase().contains(filterTag)) {
                            matches = true;
                            break;
                        }
                    }
                    if (matches) break;
                }
                if (matches) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_modifier, parent, false);
            return new ModifierViewHolder(view);
        } else if (viewType == 3) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_ingredient, parent, false);
            return new IngredientViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        if (holder instanceof ModifierViewHolder) {
            ModifierViewHolder mHolder = (ModifierViewHolder) holder;
            mHolder.textModifierName.setText(item.getName());
            mHolder.textModifierOptions.setText(item.getStockStatus()); // We use stockStatus as options string for modifiers
        } else if (holder instanceof IngredientViewHolder) {
            IngredientViewHolder iHolder = (IngredientViewHolder) holder;
            iHolder.textIngredientName.setText(item.getName());
            iHolder.textIngredientStock.setText(item.getStockStatus()); // We use stockStatus as stock value for ingredients
        } else {
            holder.textInitial.setText(item.getInitial());
            holder.textItemName.setText(item.getName());
            holder.textItemPrice.setText(item.getPrice());
            holder.textStockStatus.setText(item.getStockStatus());

            if (item.getImageUri() != null) {
                holder.imgItem.setVisibility(View.VISIBLE);
                holder.textInitial.setVisibility(View.GONE);
                Glide.with(context)
                        .load(item.getImageUri())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imgItem);
            } else if (item.getBackgroundColor() != -1) {
                holder.imgItem.setVisibility(View.GONE);
                holder.textInitial.setVisibility(View.VISIBLE);
                holder.cardItemImage.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(item.getBackgroundColor()));
                holder.textInitial.setBackground(null); // Remove default circular background
            } else {
                holder.imgItem.setVisibility(View.GONE);
                holder.textInitial.setVisibility(View.VISIBLE);
                holder.cardItemImage.setCardBackgroundColor(Color.TRANSPARENT);
                holder.textInitial.setBackgroundResource(R.drawable.bg_inventory_initial);
            }

            if (item.isOutOfStock()) {
                holder.textStockStatus.setTextColor(Color.parseColor("#EF4444"));
            } else {
                holder.textStockStatus.setTextColor(Color.parseColor("#10B981")); // Green for in stock
            }

            holder.layoutTags.removeAllViews();
            for (int i = 0; i < item.getTags().size(); i++) {
                String tag = item.getTags().get(i);
                TextView tagView = new TextView(context);
                tagView.setText(tag);
                tagView.setTextSize(12);
                tagView.setPadding(24, 12, 24, 12);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 16, 0);
                tagView.setLayoutParams(params);

                if (i == 0) {
                    tagView.setBackgroundResource(R.drawable.bg_inventory_tag);
                    tagView.getBackground().setTint(ContextCompat.getColor(context, R.color.reports_tab_selected));
                    tagView.setTextColor(Color.WHITE);
                } else {
                    tagView.setBackgroundResource(R.drawable.bg_inventory_tag);
                    tagView.getBackground().setTint(Color.parseColor("#F1F5F9"));
                    tagView.setTextColor(Color.parseColor("#475569"));
                }
                holder.layoutTags.addView(tagView);
            }
        }

        // Set entry animation
        setAnimation(holder.itemView, position);
    }

    private int lastPosition = -1;
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.fade_in);
            animation.setDuration(400);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class IngredientViewHolder extends ViewHolder {
        TextView textIngredientName, textIngredientStock;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            textIngredientName = itemView.findViewById(R.id.textIngredientName);
            textIngredientStock = itemView.findViewById(R.id.textIngredientStock);
        }
    }

    public static class ModifierViewHolder extends ViewHolder {
        TextView textModifierName, textModifierOptions;

        public ModifierViewHolder(@NonNull View itemView) {
            super(itemView);
            textModifierName = itemView.findViewById(R.id.textModifierName);
            textModifierOptions = itemView.findViewById(R.id.textModifierOptions);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textInitial, textItemName, textItemPrice, textStockStatus;
        LinearLayout layoutTags;
        ImageView imgItem;
        com.google.android.material.card.MaterialCardView cardItemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInitial = itemView.findViewById(R.id.textInitial);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemPrice = itemView.findViewById(R.id.textItemPrice);
            textStockStatus = itemView.findViewById(R.id.textStockStatus);
            layoutTags = itemView.findViewById(R.id.layoutTags);
            imgItem = itemView.findViewById(R.id.imgItem);
            cardItemImage = itemView.findViewById(R.id.cardItemImage);
        }
    }
}
