package com.example.billz;

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
        
        String lastOrder = customer.getLastOrderTime();
        if ("JUST NOW".equals(lastOrder)) {
            lastOrder = holder.itemView.getContext().getString(R.string.just_now);
        }
        holder.badgeTime.setText(lastOrder);
        
        // Show NEW badge for first item as a demo
        holder.badgeNew.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

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
        TextView textInitial, textName, textMobile, badgeOrders, badgeTime, badgeNew;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInitial = itemView.findViewById(R.id.textInitial);
            textName = itemView.findViewById(R.id.textCustomerName);
            textMobile = itemView.findViewById(R.id.textCustomerMobile);
            badgeOrders = itemView.findViewById(R.id.badgeOrders);
            badgeTime = itemView.findViewById(R.id.badgeTime);
            badgeNew = itemView.findViewById(R.id.badgeNew);
        }
    }
}
