package com.example.billz;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> items;
    private List<InventoryItem> itemsFull;
    private Context context;

    public InventoryAdapter(List<InventoryItem> items, Context context) {
        this.items = items;
        this.itemsFull = new ArrayList<>(items);
        this.context = context;
    }

    public void filter(String text) {
        items.clear();
        if (text.isEmpty()) {
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.textInitial.setText(item.getInitial());
        holder.textItemName.setText(item.getName());
        holder.textItemPrice.setText(item.getPrice());
        holder.textStockStatus.setText(item.getStockStatus());

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textInitial, textItemName, textItemPrice, textStockStatus;
        LinearLayout layoutTags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInitial = itemView.findViewById(R.id.textInitial);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemPrice = itemView.findViewById(R.id.textItemPrice);
            textStockStatus = itemView.findViewById(R.id.textStockStatus);
            layoutTags = itemView.findViewById(R.id.layoutTags);
        }
    }
}
