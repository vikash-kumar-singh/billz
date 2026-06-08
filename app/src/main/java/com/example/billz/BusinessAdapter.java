package com.example.billz;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        
        // Display: Business Name Category Plan
        String category = (business.getCategory() != null && !business.getCategory().isEmpty()) ? business.getCategory() : "No Category";
        String plan = (business.getPlan() != null && !business.getPlan().isEmpty()) ? business.getPlan() : "FREE";
        
        String info = category + " • " + plan;
        holder.textRole.setText(info);

        if (business.isSelected()) {
            holder.cardView.setStrokeColor(Color.parseColor("#3F51B5"));
            holder.cardView.setStrokeWidth(0); 
            holder.cardView.setCardBackgroundColor(Color.parseColor("#3F51B5")); 
            holder.textName.setTextColor(Color.WHITE);
            holder.textRole.setTextColor(Color.parseColor("#E0E0E0"));
            holder.cardLogoContainer.setCardBackgroundColor(Color.WHITE);
        } else {
            holder.cardView.setStrokeColor(Color.parseColor("#CBD5E1")); 
            holder.cardView.setStrokeWidth(2); 
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.textName.setTextColor(Color.parseColor("#64748B"));
            holder.textRole.setTextColor(Color.parseColor("#94A3B8"));
            holder.cardLogoContainer.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
        }

        if (business.getLogoPath() != null) {
            holder.imgLogo.setImageURI(Uri.parse(business.getLogoPath()));
            holder.imgLogo.setColorFilter(null);
        } else {
            holder.imgLogo.setImageResource(R.drawable.ic_nav_reports);
            holder.imgLogo.setColorFilter(Color.parseColor("#3F51B5"));
        }

        holder.itemView.setOnClickListener(v -> listener.onBusinessClick(business));
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
