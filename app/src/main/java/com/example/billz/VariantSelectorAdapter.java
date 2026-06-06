package com.example.billz;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VariantSelectorAdapter extends RecyclerView.Adapter<VariantSelectorAdapter.ViewHolder> {

    private final List<Variant> variants;
    private final Map<Integer, Integer> variantQuantities = new HashMap<>();
    private final OnQuantityChangedListener listener;

    public interface OnQuantityChangedListener {
        void onQuantityChanged(Variant variant, int quantity);
    }

    public VariantSelectorAdapter(List<Variant> variants, List<CartItem> currentCart, OnQuantityChangedListener listener) {
        this.variants = variants;
        this.listener = listener;
        for (CartItem ci : currentCart) {
            if (ci.getVariant() != null) {
                variantQuantities.put(ci.getVariant().getId(), ci.getQuantity());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_variant_selector_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Variant variant = variants.get(position);
        int qty = variantQuantities.containsKey(variant.getId()) ? variantQuantities.get(variant.getId()) : 0;

        holder.textPrice.setText(String.format(Locale.getDefault(), "₹%.0f", variant.getSellingPrice()));
        holder.textName.setText(variant.getName());
        
        String stockInfo = variant.getStockQuantity() <= 0 ? "Out of stock ( 0 )" : variant.getStockQuantity() + " Left";
        holder.textStock.setText(stockInfo);
        holder.textQuantity.setText(String.valueOf(qty));

        if (qty > 0) {
            holder.card.setCardBackgroundColor(0xFF3F51B5);
            holder.textPrice.setTextColor(Color.WHITE);
            holder.textName.setTextColor(Color.WHITE);
            holder.textStock.setTextColor(0xCCFFFFFF);
            holder.btnMinus.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            holder.btnPlus.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            holder.textQuantity.setTextColor(Color.WHITE);
            holder.textQuantity.setBackgroundResource(R.drawable.bg_quantity_box_white);
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.textPrice.setTextColor(0xFF3F51B5);
            holder.textName.setTextColor(0xFF334155);
            holder.textStock.setTextColor(0xFF94A3B8);
            holder.btnMinus.setImageTintList(ColorStateList.valueOf(0xFF3F51B5));
            holder.btnPlus.setImageTintList(ColorStateList.valueOf(0xFF3F51B5));
            holder.textQuantity.setTextColor(0xFF334155);
            holder.textQuantity.setBackgroundResource(R.drawable.bg_quantity_box);
        }

        holder.btnPlus.setOnClickListener(v -> {
            if (qty + 1 <= variant.getStockQuantity()) {
                int newQty = qty + 1;
                variantQuantities.put(variant.getId(), newQty);
                notifyItemChanged(position);
                listener.onQuantityChanged(variant, newQty);
            } else {
                android.widget.Toast.makeText(v.getContext(), "Maximum stock reached: " + variant.getStockQuantity(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (qty > 0) {
                int newQty = qty - 1;
                variantQuantities.put(variant.getId(), newQty);
                notifyItemChanged(position);
                listener.onQuantityChanged(variant, newQty);
            }
        });
    }

    @Override
    public int getItemCount() {
        return variants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView textPrice, textName, textStock, textQuantity;
        ImageView btnMinus, btnPlus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardVariant);
            textPrice = itemView.findViewById(R.id.textVariantPrice);
            textName = itemView.findViewById(R.id.textVariantName);
            textStock = itemView.findViewById(R.id.textVariantStock);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}