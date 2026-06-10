package com.example.billz;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.ViewHolder> {

    private final List<Business> businessList;
    private final OnBusinessClickListener listener;

    public interface OnBusinessClickListener {
        void onBusinessClick(Business business);
    }

    public BusinessAdapter(List<Business> businessList, OnBusinessClickListener listener) {
        this.businessList = businessList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_business_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Business business = businessList.get(position);
        holder.textName.setText(business.getName());
        
        String category = (business.getCategory() != null && !business.getCategory().isEmpty()) ? business.getCategory() : "No Category";
        String plan = (business.getPlan() != null && !business.getPlan().isEmpty()) ? business.getPlan() : "FREE";
        
        String info = category + " • " + plan;
        holder.textRole.setText(info.toLowerCase());

        if (business.isSelected()) {
            holder.cardView.setStrokeColor(Color.parseColor("#1E40AF"));
            holder.cardView.setStrokeWidth(4);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E0E7FF")); 
            holder.textName.setTextColor(Color.parseColor("#1E40AF"));
            holder.textRole.setTextColor(Color.parseColor("#475569"));
            holder.cardLogoContainer.setCardBackgroundColor(Color.WHITE);
        } else {
            holder.cardView.setStrokeColor(Color.parseColor("#E2E8F0")); 
            holder.cardView.setStrokeWidth(0); 
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F8FAFC"));
            holder.textName.setTextColor(Color.parseColor("#334155"));
            holder.textRole.setTextColor(Color.parseColor("#94A3B8"));
            holder.cardLogoContainer.setCardBackgroundColor(Color.WHITE);
        }

        if (business.getLogoPath() != null && !business.getLogoPath().isEmpty()) {
            try {
                holder.imgLogo.setImageURI(Uri.parse(business.getLogoPath()));
                holder.imgLogo.setColorFilter(null);
            } catch (Exception e) {
                setDefaultLogo(holder);
            }
        } else {
            setDefaultLogo(holder);
        }

        holder.itemView.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                listener.onBusinessClick(business);
            }).start();
        });
    }

    private void setDefaultLogo(ViewHolder holder) {
        holder.imgLogo.setImageResource(R.drawable.ic_nav_reports);
        holder.imgLogo.setColorFilter(Color.parseColor("#1E40AF"));
    }

    @Override
    public int getItemCount() {
        return businessList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textRole;
        ImageView imgLogo;
        MaterialCardView cardView, cardLogoContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textName = itemView.findViewById(R.id.textBusinessName);
            textRole = itemView.findViewById(R.id.textBusinessRole);
            imgLogo = itemView.findViewById(R.id.imgBusinessLogo);
            cardLogoContainer = itemView.findViewById(R.id.cardLogoContainer);
        }
    }
}
