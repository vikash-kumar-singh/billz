package com.example.billz;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class DiscountSelectionAdapter extends RecyclerView.Adapter<DiscountSelectionAdapter.ViewHolder> {

    private final List<Discount> discounts;
    private final OnDiscountClickListener listener;
    private int selectedId = -1;

    public interface OnDiscountClickListener {
        void onDiscountClick(Discount discount);
    }

    public DiscountSelectionAdapter(List<Discount> discounts, OnDiscountClickListener listener) {
        this.discounts = discounts;
        this.listener = listener;
    }

    public void setSelectedId(int id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discount_selection_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discount discount = discounts.get(position);
        holder.textName.setText(discount.getName());
        String symbol = discount.isPercentage() ? "%" : "₹";
        holder.textValue.setText(String.format(Locale.getDefault(), "%s(%s%.0f)", discount.getName(), symbol, discount.getValue()));

        // All blue as per reference
        holder.layoutContent.setBackgroundColor(0xFF3F51B5);
        holder.textName.setTextColor(Color.WHITE);
        holder.textValue.setTextColor(0xCCFFFFFF);

        if (discount.getId() == selectedId) {
            holder.layoutContent.setBackgroundColor(0xFF2563EB); // Brighter blue for selected
        }

        holder.itemView.setOnClickListener(v -> listener.onDiscountClick(discount));
    }

    @Override
    public int getItemCount() {
        return discounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;
        LinearLayout layoutContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textDiscountName);
            textValue = itemView.findViewById(R.id.textDiscountValue);
            layoutContent = itemView.findViewById(R.id.layoutDiscountContent);
        }
    }
}