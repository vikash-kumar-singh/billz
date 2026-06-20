package com.example.billz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private List<Item> items;
    private List<Item> itemsFull;

    public ProductListAdapter(List<Item> items) {
        this.items = items;
        this.itemsFull = new java.util.ArrayList<>(items);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.textName.setText(item.getName());
        holder.textPrice.setText("₹" + (int)item.getSellingPrice());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ManageItemActivity.class);
            intent.putExtra("item_id", item.getId());
            v.getContext().startActivity(intent);
        });
        
        int qty = 0;
        for (CartItem ci : CartManager.getInstance().getCartItems()) {
            if (ci.getItem().getId() != null && ci.getItem().getId().equals(item.getId())) {
                qty += ci.getQuantity();
            }
        }
        holder.editQuantity.setText(String.valueOf(qty));

        // If Advance Mode, the inline buttons should probably trigger variant selector 
        // or be disabled/hidden if they don't make sense for multi-variants.
        // For consistency with Grid view, let's make them trigger variant selector if Advance.
        
        holder.btnPlus.setOnClickListener(v -> {
            if (item.isAdvanceMode()) {
                // Trigger variant selector via a callback or event?
                // For now, let's just use the grid's logic if possible.
                // But ProductListAdapter is separate.
                // Best to have a listener.
                if (listener != null) listener.onPlusClick(item, position);
            } else {
                int current = 0;
                try { current = Integer.parseInt(holder.editQuantity.getText().toString()); } catch (Exception e) {}
                if (current + 1 <= item.getStockQuantity()) {
                    if (CartManager.getInstance().addItem(item)) {
                        notifyItemChanged(position);
                    }
                } else {
                    android.widget.Toast.makeText(v.getContext(), "Maximum stock reached", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.isAdvanceMode()) {
                if (listener != null) listener.onMinusClick(item, position);
            } else {
                int current = 0;
                try { current = Integer.parseInt(holder.editQuantity.getText().toString()); } catch (Exception e) {}
                if (current > 0) {
                    CartManager.getInstance().updateQuantity(item.getId(), current - 1);
                    notifyItemChanged(position);
                }
            }
        });
    }

    private OnProductActionListener listener;
    public interface OnProductActionListener {
        void onPlusClick(Item item, int position);
        void onMinusClick(Item item, int position);
    }
    public void setListener(OnProductActionListener listener) { this.listener = listener; }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice;
        View btnPlus, btnMinus;
        TextView editQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            editQuantity = itemView.findViewById(R.id.editQuantity);
        }
    }
}
