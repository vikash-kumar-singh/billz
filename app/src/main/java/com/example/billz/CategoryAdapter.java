package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textName.setText(category.getName());
        
        if (category.isCustom()) {
            holder.layoutIcon.setBackgroundResource(R.drawable.bg_circle_blue);
            holder.textInitial.setVisibility(View.GONE);
            holder.imageIcon.setVisibility(View.VISIBLE);
        } else {
            holder.layoutIcon.setBackgroundResource(category.isExpense() ? R.drawable.bg_circle_red : R.drawable.bg_circle_green);
            holder.textInitial.setVisibility(View.VISIBLE);
            holder.imageIcon.setVisibility(View.GONE);
            holder.textInitial.setText(category.getInitial());
        }

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutIcon;
        TextView textInitial, textName;
        ImageView imageIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutIcon = itemView.findViewById(R.id.layoutIcon);
            textInitial = itemView.findViewById(R.id.textInitial);
            textName = itemView.findViewById(R.id.textCategoryName);
            imageIcon = itemView.findViewById(R.id.imageIcon);
        }
    }
}
