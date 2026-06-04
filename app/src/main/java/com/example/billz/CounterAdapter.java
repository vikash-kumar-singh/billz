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

    private List<CartItem> items;
    private OnItemEditListener editListener;

    public CounterAdapter(List<CartItem> items, OnItemEditListener editListener) {
        this.items = items;
        this.editListener = editListener;
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

        String variant = item.getVariantName();
        holder.textName.setText((variant != null && !variant.equals("Default") ? variant + " " : "") + item.getName());
        holder.textDetails.setText(cartItem.getQuantity() + " x " + (int)item.getSellingPrice());
        holder.textTotal.setText(String.valueOf((int)cartItem.getTotalPrice()));

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditItem(cartItem);
        });
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
