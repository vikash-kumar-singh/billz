package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

    public interface OnDiscountClickListener {
        void onDiscountClick(Discount discount);
    }

    private List<Discount> discounts;
    private OnDiscountClickListener listener;

    public DiscountAdapter(List<Discount> discounts, OnDiscountClickListener listener) {
        this.discounts = discounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discount, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        Discount discount = discounts.get(position);
        holder.textName.setText(discount.getName());
        
        String formattedValue;
        if (discount.isPercentage()) {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_percent_format, discount.getValue());
        } else {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_flat_format, discount.getValue());
        }
        holder.textValue.setText(formattedValue);
        
        holder.itemView.setOnClickListener(v -> listener.onDiscountClick(discount));
    }

    @Override
    public int getItemCount() {
        return discounts.size();
    }

    static class DiscountViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textDiscountName);
            textValue = itemView.findViewById(R.id.textDiscountValue);
        }
    }
}
