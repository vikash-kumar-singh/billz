package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CheckoutPaymentAdapter extends RecyclerView.Adapter<CheckoutPaymentAdapter.ViewHolder> {

    private final List<PaymentMode> modes;
    private final OnPaymentModeClickListener listener;

    public interface OnPaymentModeClickListener {
        void onPaymentClick(PaymentMode mode);
        void onAddNewClick();
    }

    public CheckoutPaymentAdapter(List<PaymentMode> modes, OnPaymentModeClickListener listener) {
        this.modes = modes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_mode_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == modes.size()) {
            // Add New button
            holder.textName.setText("Add New");
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_add);
            holder.imgIcon.setColorFilter(0xFF000000);
            holder.imgIcon.setVisibility(View.VISIBLE);
            holder.textInitial.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> listener.onAddNewClick());
            return;
        }

        PaymentMode mode = modes.get(position);
        holder.textName.setText(mode.getName());
        
        // Icons based on name for dummy data matching the image
        setupIcon(holder, mode.getName());

        holder.itemView.setOnClickListener(v -> listener.onPaymentClick(mode));
    }

    private void setupIcon(ViewHolder holder, String name) {
        holder.imgIcon.setColorFilter(null);
        holder.textInitial.setVisibility(View.GONE);
        holder.imgIcon.setVisibility(View.VISIBLE);
        holder.imgIcon.setPadding(0, 0, 0, 0);

        String lowName = name.toLowerCase();
        if (lowName.equals("cash")) {
            holder.imgIcon.setImageResource(R.drawable.ic_cash);
        } else if (lowName.contains("card")) {
            holder.imgIcon.setImageResource(R.drawable.ic_payment_card);
        } else if (lowName.equals("credit") || lowName.equals("store credit")) {
            holder.imgIcon.setImageResource(R.drawable.ic_credit_calendar);
        } else if (lowName.equals("upi")) {
            holder.imgIcon.setImageResource(R.drawable.ic_upi_bhmi);
        } else if (lowName.equals("google pay")) {
            holder.imgIcon.setImageResource(R.drawable.ic_google_pay);
        } else if (lowName.equals("online") || lowName.equals("sample rate") || lowName.equals("exchange")) {
            holder.imgIcon.setVisibility(View.GONE);
            holder.textInitial.setVisibility(View.VISIBLE);
            holder.textInitial.setText(name.substring(0, 1).toUpperCase());
            holder.textInitial.setBackgroundColor(0xFF3F51B5);
            holder.textInitial.setTextColor(0xFFFFFFFF);
            holder.textInitial.setTextSize(24);
            holder.textInitial.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.imgIcon.setVisibility(View.GONE);
            holder.textInitial.setVisibility(View.VISIBLE);
            holder.textInitial.setText(name.substring(0, 1).toUpperCase());
            holder.textInitial.setBackgroundColor(0xFF3F51B5);
            holder.textInitial.setTextColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return modes.size() + 1; // +1 for Add New
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textInitial;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textPaymentName);
            textInitial = itemView.findViewById(R.id.textInitial);
            imgIcon = itemView.findViewById(R.id.imgPaymentIcon);
        }
    }
}