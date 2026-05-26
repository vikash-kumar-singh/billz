package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OtherFeeAdapter extends RecyclerView.Adapter<OtherFeeAdapter.OtherViewHolder> {

    public interface OnOtherFeeClickListener {
        void onOtherFeeClick(OtherFee fee);
    }

    private List<OtherFee> fees;
    private OnOtherFeeClickListener listener;

    public OtherFeeAdapter(List<OtherFee> fees, OnOtherFeeClickListener listener) {
        this.fees = fees;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OtherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_fee, parent, false);
        return new OtherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherViewHolder holder, int position) {
        OtherFee fee = fees.get(position);
        holder.textName.setText(fee.getName());
        
        String formattedValue;
        if (fee.isPercentage()) {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_percent_format, fee.getValue());
        } else {
            formattedValue = holder.itemView.getContext().getString(R.string.discount_value_flat_format, fee.getValue());
        }
        holder.textValue.setText(formattedValue);
        
        holder.itemView.setOnClickListener(v -> listener.onOtherFeeClick(fee));
    }

    @Override
    public int getItemCount() {
        return fees.size();
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textValue;

        public OtherViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textOtherName);
            textValue = itemView.findViewById(R.id.textOtherValue);
        }
    }
}
