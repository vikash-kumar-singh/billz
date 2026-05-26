package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeliveryFeeAdapter extends RecyclerView.Adapter<DeliveryFeeAdapter.DeliveryViewHolder> {

    public interface OnDeliveryFeeClickListener {
        void onDeliveryFeeClick(DeliveryFee fee);
    }

    private List<DeliveryFee> fees;
    private OnDeliveryFeeClickListener listener;

    public DeliveryFeeAdapter(List<DeliveryFee> fees, OnDeliveryFeeClickListener listener) {
        this.fees = fees;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_fee, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        DeliveryFee fee = fees.get(position);
        holder.textName.setText(fee.getName());
        
        String formattedValue;
        if (fee.isPercentage()) {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_percent_format, fee.getValue());
        } else {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_flat_format, fee.getValue());
        }
        holder.textValue.setText(formattedValue);
        
        holder.itemView.setOnClickListener(v -> listener.onDeliveryFeeClick(fee));
    }

    @Override
    public int getItemCount() {
        return fees.size();
    }

    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textDeliveryName);
            textValue = itemView.findViewById(R.id.textDeliveryValue);
        }
    }
}
