package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    private final List<Variant> variants;
    private final OnVariantClickListener listener;

    public interface OnVariantClickListener {
        void onVariantClick(Variant variant);
    }

    public VariantAdapter(List<Variant> variants, OnVariantClickListener listener) {
        this.variants = variants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Variant variant = variants.get(position);
        holder.text1.setText(variant.getName());
        holder.text1.setTextColor(0xFF000000);
        holder.text2.setText(String.format(Locale.getDefault(), "₹%.0f", variant.getSellingPrice()));
        holder.text2.setTextColor(0xFF3F51B5);
        
        holder.itemView.setOnClickListener(v -> listener.onVariantClick(variant));
    }

    @Override
    public int getItemCount() {
        return variants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}