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

public class TaxSelectionAdapter extends RecyclerView.Adapter<TaxSelectionAdapter.ViewHolder> {

    private final List<Tax> taxes;
    private final OnTaxClickListener listener;
    private int selectedId = -1;

    public interface OnTaxClickListener {
        void onTaxClick(Tax tax);
    }

    public TaxSelectionAdapter(List<Tax> taxes, OnTaxClickListener listener) {
        this.taxes = taxes;
        this.listener = listener;
    }

    public void setSelectedId(int id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tax_selection_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tax tax = taxes.get(position);
        holder.textName.setText(tax.getName());
        holder.textValue.setText(String.format(Locale.getDefault(), "%s(%.0f%%)", tax.getName(), tax.getValue()));

        // Make all cards blue as per the reference design
        holder.layoutContent.setBackgroundColor(0xFF3F51B5); 
        holder.textName.setTextColor(Color.WHITE);
        holder.textValue.setTextColor(0xCCFFFFFF);

        // Highlight the specifically selected one with a border or slightly different shade if needed
        // but for now let's just make them all look like the reference.
        if (tax.getId() == selectedId) {
            holder.layoutContent.setBackgroundColor(0xFF2563EB); // Slightly brighter blue for selection
        }

        holder.itemView.setOnClickListener(v -> listener.onTaxClick(tax));
    }

    @Override
    public int getItemCount() {
        return taxes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;
        LinearLayout layoutContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTaxName);
            textValue = itemView.findViewById(R.id.textTaxValue);
            layoutContent = itemView.findViewById(R.id.layoutTaxContent);
        }
    }
}