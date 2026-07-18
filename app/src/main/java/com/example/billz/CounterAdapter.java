package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CounterAdapter extends RecyclerView.Adapter<CounterAdapter.ViewHolder> {

    public interface OnItemEditListener {
        void onEditItem(CartItem item);
    }

    public interface OnPriceEditListener {
        void onEditPrice(CartItem item);
    }

    private List<CartItem> items;
    private OnItemEditListener editListener;
    private OnPriceEditListener priceEditListener;

    public CounterAdapter(List<CartItem> items, OnItemEditListener editListener, OnPriceEditListener priceEditListener) {
        this.items = items;
        this.editListener = editListener;
        this.priceEditListener = priceEditListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = items.get(position);
        Item item = cartItem.getItem();
        Variant variant = cartItem.getVariant();

        // Use Variant object's name if available, fallback to Item's variantName field
        String vName = (variant != null) ? variant.getName() : item.getVariantName();
        
        String variantPrefix = "";
        if (vName != null && !vName.isEmpty() && !vName.equalsIgnoreCase("Default") && !vName.equalsIgnoreCase("NO VARIANT")) {
            variantPrefix = vName + " ";
        }
        holder.textName.setText(variantPrefix + item.getName());
        
        double price = cartItem.getUnitPrice();
        holder.textDetails.setText(cartItem.getQuantity() + " x " + (int)price);
        holder.textTotal.setText(String.valueOf((int)cartItem.getTotalPrice()));

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditItem(cartItem);
        });

        View.OnClickListener priceClickListener = v -> {
            if (priceEditListener != null) priceEditListener.onEditPrice(cartItem);
        };

        holder.textTotal.setOnClickListener(priceClickListener);
        holder.textDetails.setOnClickListener(priceClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDetails, textTotal;
        View btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textCounterItemName);
            textDetails = itemView.findViewById(R.id.textCounterItemDetails);
            textTotal = itemView.findViewById(R.id.textCounterItemTotal);
            btnEdit = itemView.findViewById(R.id.btnEditCartItem);
        }
    }
}
