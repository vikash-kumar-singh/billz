package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaxAdapter extends RecyclerView.Adapter<TaxAdapter.TaxViewHolder> {

    public interface OnTaxClickListener {
        void onTaxClick(Tax tax);
    }

    private List<Tax> taxes;
    private OnTaxClickListener listener;

    public TaxAdapter(List<Tax> taxes, OnTaxClickListener listener) {
        this.taxes = taxes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tax, parent, false);
        return new TaxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxViewHolder holder, int position) {
        Tax tax = taxes.get(position);
        holder.textName.setText(tax.getName());
        holder.textValue.setText(holder.itemView.getContext().getString(R.string.tax_value_format, tax.getValue()));
        holder.itemView.setOnClickListener(v -> listener.onTaxClick(tax));
    }

    @Override
    public int getItemCount() {
        return taxes.size();
    }

    public void updateData(List<Tax> newTaxes) {
        this.taxes = newTaxes;
        notifyDataSetChanged();
    }

    static class TaxViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;

        public TaxViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTaxName);
            textValue = itemView.findViewById(R.id.textTaxValue);
        }
    }
}
