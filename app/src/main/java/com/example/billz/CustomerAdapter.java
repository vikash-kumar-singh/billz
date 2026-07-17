package com.example.billz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private List<Customer> customers;

    public CustomerAdapter(List<Customer> customers) {
        this.customers = customers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.textName.setText(customer.getName().isEmpty() ? 
                holder.itemView.getContext().getString(R.string.unknown) : customer.getName());
        holder.textMobile.setText(customer.getMobile());
        
        String initial = "C";
        if (!customer.getName().isEmpty()) {
            initial = customer.getName().substring(0, 1).toUpperCase();
        }
        holder.textInitial.setText(initial);

        holder.badgeOrders.setText(holder.itemView.getContext().getString(R.string.orders_suffix, customer.getOrdersCount()));
        
        String timeDisplay = "JUST NOW";
        if (customer.getLastPurchaseTimestamp() > 0) {
            long now = System.currentTimeMillis();
            long diff = now - customer.getLastPurchaseTimestamp();
            if (diff < 60000) { // Less than 1 minute
                timeDisplay = holder.itemView.getContext().getString(R.string.just_now);
            } else {
                timeDisplay = android.text.format.DateUtils.getRelativeTimeSpanString(
                        customer.getLastPurchaseTimestamp(),
                        now,
                        android.text.format.DateUtils.MINUTE_IN_MILLIS).toString().toUpperCase();
            }
        }
        holder.badgeTime.setText(timeDisplay);
        
        long oneDayMillis = 24 * 60 * 60 * 1000;
        boolean isNew = (System.currentTimeMillis() - customer.getCreatedAt()) < oneDayMillis;
        holder.badgeNew.setVisibility(isNew ? View.VISIBLE : View.GONE);

        if (customer.getDueAmount() > 0) {
            holder.textDueAmount.setVisibility(View.VISIBLE);
            holder.textDueAmount.setText(String.format(java.util.Locale.getDefault(), "₹-%,.0f", customer.getDueAmount()));
        } else {
            holder.textDueAmount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CustomerDetailsActivity.class);
            intent.putExtra("customer_id", customer.getId());
            v.getContext().startActivity(intent);
        });

        // Set entry animation
        setAnimation(holder.itemView, position);
    }

    private int lastPosition = -1;
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.fade_in);
            animation.setDuration(400);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textInitial, textName, textMobile, badgeOrders, badgeTime, badgeNew, textDueAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInitial = itemView.findViewById(R.id.textInitial);
            textName = itemView.findViewById(R.id.textCustomerName);
            textMobile = itemView.findViewById(R.id.textCustomerMobile);
            badgeOrders = itemView.findViewById(R.id.badgeOrders);
            badgeTime = itemView.findViewById(R.id.badgeTime);
            badgeNew = itemView.findViewById(R.id.badgeNew);
            textDueAmount = itemView.findViewById(R.id.textDueAmount);
        }
    }
}
