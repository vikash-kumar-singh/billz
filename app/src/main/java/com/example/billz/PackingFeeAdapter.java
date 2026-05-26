package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PackingFeeAdapter extends RecyclerView.Adapter<PackingFeeAdapter.PackingViewHolder> {

    public interface OnPackingFeeClickListener {
        void onPackingFeeClick(PackingFee fee);
    }

    private List<PackingFee> fees;
    private OnPackingFeeClickListener listener;

    public PackingFeeAdapter(List<PackingFee> fees, OnPackingFeeClickListener listener) {
        this.fees = fees;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_packing_fee, parent, false);
        return new PackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackingViewHolder holder, int position) {
        PackingFee fee = fees.get(position);
        holder.textName.setText(fee.getName());
        
        String formattedValue;
        if (fee.isPercentage()) {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_percent_format, fee.getValue());
        } else {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_flat_format, fee.getValue());
        }
        holder.textValue.setText(formattedValue);
        
        holder.itemView.setOnClickListener(v -> listener.onPackingFeeClick(fee));
    }

    @Override
    public int getItemCount() {
        return fees.size();
    }

    static class PackingViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;

        public PackingViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textPackingName);
            textValue = itemView.findViewById(R.id.textPackingValue);
        }
    }
}
