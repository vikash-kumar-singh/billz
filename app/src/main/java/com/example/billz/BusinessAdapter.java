package com.example.billz;

import android.graphics.Color;
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
        holder.textRole.setText(business.getRole());

        if (business.isSelected()) {
            holder.cardView.setStrokeColor(Color.parseColor("#3F51B5"));
            holder.cardView.setStrokeWidth(5); // Clean blue border
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F1F3FF")); 
            
            holder.textName.setTextColor(Color.parseColor("#3F51B5"));
            holder.textRole.setTextColor(Color.parseColor("#7986CB"));
            holder.imgLogo.setColorFilter(Color.parseColor("#3F51B5"));
            
            holder.imgPlaceholder.setVisibility(View.GONE);
            holder.cardLogo.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setStrokeColor(Color.parseColor("#CBD5E1")); 
            holder.cardView.setStrokeWidth(3); // Consistent grey border
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            
            holder.textName.setTextColor(Color.BLACK);
            holder.textRole.setTextColor(Color.parseColor("#94A3B8"));
            holder.imgLogo.setColorFilter(Color.parseColor("#3F51B5"));
            
            holder.imgPlaceholder.setVisibility(View.VISIBLE);
            holder.cardLogo.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onBusinessClick(business));
    }

    @Override
    public int getItemCount() {
        return businessList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textRole;
        ImageView imgPlaceholder, imgLogo;
        CardView cardLogo;
        MaterialCardView cardView;
        LinearLayout layoutItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textName = itemView.findViewById(R.id.textBusinessName);
            textRole = itemView.findViewById(R.id.textBusinessRole);
            imgPlaceholder = itemView.findViewById(R.id.imgPlaceholder);
            imgLogo = itemView.findViewById(R.id.imgBusinessLogo);
            cardLogo = itemView.findViewById(R.id.cardLogo);
            layoutItem = itemView.findViewById(R.id.layoutBusinessItem);
        }
    }
}
