package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentModeAdapter extends RecyclerView.Adapter<PaymentModeAdapter.PaymentModeViewHolder> {

    public interface OnPaymentModeClickListener {
        void onActionClick(PaymentMode mode);
        void onConfigClick(PaymentMode mode);
    }

    private List<PaymentMode> modes;
    private OnPaymentModeClickListener listener;

    public PaymentModeAdapter(List<PaymentMode> modes, OnPaymentModeClickListener listener) {
        this.modes = modes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentModeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_mode, parent, false);
        return new PaymentModeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentModeViewHolder holder, int position) {
        PaymentMode mode = modes.get(position);
        holder.textName.setText(mode.getName());
        holder.textInitial.setText(mode.getName().substring(0, 1).toUpperCase());
        
        if (mode.isAdded()) {
            holder.imageAction.setVisibility(View.GONE);
            if (mode.isHasConfig()) {
                holder.imageConfig.setVisibility(View.VISIBLE);
                holder.textNoConfig.setVisibility(View.VISIBLE);
            } else {
                holder.imageConfig.setVisibility(View.GONE);
                holder.textNoConfig.setVisibility(View.GONE);
            }
        } else {
            holder.imageAction.setVisibility(View.VISIBLE);
            holder.imageAction.setImageResource(android.R.drawable.ic_input_add);
            holder.imageConfig.setVisibility(View.GONE);
            holder.textNoConfig.setVisibility(View.GONE);
        }

        holder.imageAction.setOnClickListener(v -> listener.onActionClick(mode));
        holder.imageConfig.setOnClickListener(v -> listener.onConfigClick(mode));
    }

    @Override
    public int getItemCount() {
        return modes.size();
    }

    static class PaymentModeViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textInitial, textNoConfig;
        ImageView imageAction, imageConfig;

        public PaymentModeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textModeName);
            textInitial = itemView.findViewById(R.id.textInitial);
            textNoConfig = itemView.findViewById(R.id.textNoConfig);
            imageAction = itemView.findViewById(R.id.imageAction);
            imageConfig = itemView.findViewById(R.id.imageConfig);
        }
    }
}
