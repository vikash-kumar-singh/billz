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
            if (ci.getItem().getId() == item.getId()) {
                qty = ci.getQuantity();
                break;
            }
        }
        holder.editQuantity.setText(String.valueOf(qty));

        holder.btnPlus.setOnClickListener(v -> {
            int current = 0;
            try { current = Integer.parseInt(holder.editQuantity.getText().toString()); } catch (Exception e) {}
            CartManager.getInstance().updateQuantity(item.getId(), current + 1);
            notifyItemChanged(position);
        });

        holder.btnMinus.setOnClickListener(v -> {
            int current = 0;
            try { current = Integer.parseInt(holder.editQuantity.getText().toString()); } catch (Exception e) {}
            if (current > 0) {
                CartManager.getInstance().updateQuantity(item.getId(), current - 1);
                notifyItemChanged(position);
            }
        });
    }

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
