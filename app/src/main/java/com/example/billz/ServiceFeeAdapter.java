package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceFeeAdapter extends RecyclerView.Adapter<ServiceFeeAdapter.ServiceViewHolder> {

    public interface OnServiceFeeClickListener {
        void onServiceFeeClick(ServiceFee fee);
    }

    private List<ServiceFee> fees;
    private OnServiceFeeClickListener listener;

    public ServiceFeeAdapter(List<ServiceFee> fees, OnServiceFeeClickListener listener) {
        this.fees = fees;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_fee, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceFee fee = fees.get(position);
        holder.textName.setText(fee.getName());
        
        String formattedValue;
        if (fee.isPercentage()) {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_percent_format, fee.getValue());
        } else {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_flat_format, fee.getValue());
        }
        holder.textValue.setText(formattedValue);
        
        holder.itemView.setOnClickListener(v -> listener.onServiceFeeClick(fee));
    }

    @Override
    public int getItemCount() {
        return fees.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textServiceName);
            textValue = itemView.findViewById(R.id.textServiceValue);
        }
    }
}
