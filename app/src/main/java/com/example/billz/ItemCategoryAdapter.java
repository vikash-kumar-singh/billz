package com.example.billz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ItemCategoryAdapter extends RecyclerView.Adapter<ItemCategoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public static class CategoryWithCount {
        String name;
        int count;

        public CategoryWithCount(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    private List<CategoryWithCount> categories;
    private List<CategoryWithCount> categoriesFull;
    private int expandedPosition = -1;
    private OnItemClickListener itemClickListener;

    public ItemCategoryAdapter(List<CategoryWithCount> categories, OnItemClickListener listener) {
        this.categories = categories;
        this.categoriesFull = new ArrayList<>(categories);
        this.itemClickListener = listener;
    }

    public void filter(String text) {
        categories.clear();
        if (text == null || text.isEmpty()) {
            categories.addAll(categoriesFull);
        } else {
            text = text.toLowerCase();
            for (CategoryWithCount category : categoriesFull) {
                if (category.name.toLowerCase().contains(text)) {
                    categories.add(category);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryWithCount category = categories.get(position);
        holder.textNameWithCount.setText(category.name + " (" + category.count + ")");
        
        boolean isExpanded = position == expandedPosition;
        holder.recyclerProducts.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.imageArrow.setRotation(isExpanded ? 180 : 0);

        if (isExpanded) {
            loadItemsForCategory(holder, category.name);
        }

        holder.layoutHeader.setOnClickListener(v -> {
            int previousExpanded = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            
            if (previousExpanded != -1) {
                notifyItemChanged(previousExpanded);
            }
            notifyItemChanged(position);
        });
    }

    private void loadItemsForCategory(ViewHolder holder, String categoryName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());
            List<Item> items = db.itemDao().getItemsByCategory(categoryName);
            
            holder.itemView.post(() -> {
                holder.recyclerProducts.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 3));
                holder.recyclerProducts.setAdapter(new ProductAdapter(items, categoryName, itemClickListener));
            });
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNameWithCount;
        View layoutHeader;
        ImageView imageArrow;
        RecyclerView recyclerProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNameWithCount = itemView.findViewById(R.id.textCategoryNameWithCount);
            layoutHeader = itemView.findViewById(R.id.layoutCategoryHeader);
            imageArrow = itemView.findViewById(R.id.imageArrow);
            recyclerProducts = itemView.findViewById(R.id.recyclerProducts);
        }
    }

    // Nested Adapter for Product Cards
    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Item> items;
        private String categoryName;
        private OnItemClickListener clickListener;

        ProductAdapter(List<Item> items, String categoryName, OnItemClickListener clickListener) {
            this.items = items;
            this.categoryName = categoryName;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            if (position < items.size()) {
                Item item = items.get(position);
                holder.layoutNewItem.setVisibility(View.GONE);
                
                holder.itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onItemClick(item);
                    }
                });

                holder.itemView.setOnLongClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ManageItemActivity.class);
                    intent.putExtra("item_id", item.getId());
                    v.getContext().startActivity(intent);
                    return true;
                });

                holder.textName.setText(item.getName());
                holder.textPrice.setText("₹" + (int)item.getSellingPrice());
                holder.textInitial.setText(item.getName().substring(0, 1).toUpperCase());
                String variant = item.getVariantName();
                holder.textVariant.setText(variant != null && !variant.isEmpty() ? variant.toUpperCase() : "DEFAULT");
            } else {
                holder.layoutNewItem.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(null); // Clear item click listener
                holder.layoutNewItem.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), AddItemActivity.class);
                    intent.putExtra("category", categoryName);
                    v.getContext().startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size() + 1; // +1 for "New Item" button
        }

        static class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textPrice, textInitial, textVariant;
            View layoutNewItem;

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textProductName);
                textPrice = itemView.findViewById(R.id.textProductPrice);
                textInitial = itemView.findViewById(R.id.textProductInitial);
                textVariant = itemView.findViewById(R.id.textProductVariant);
                layoutNewItem = itemView.findViewById(R.id.layoutNewItem);
            }
        }
    }
}
