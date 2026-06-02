package com.example.billz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ManageItemActivity extends AppCompatActivity {

    private EditText editItemName;
    private TextView textCategory, textSellBy;
    private ImageView imgCategory;
    private View layoutCategorySelector;
    private RecyclerView recyclerVariants;
    private View topBarManage;
    private int itemId;
    private AppDatabase db;
    private Item currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_item);

        View root = findViewById(R.id.manageItemRoot);
        topBarManage = findViewById(R.id.topBarManage);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            if (topBarManage != null) {
                topBarManage.setPadding(0, systemBars.top, 0, 0);
            }
            return insets;
        });

        db = AppDatabase.getInstance(this);
        itemId = getIntent().getIntExtra("item_id", -1);

        editItemName = findViewById(R.id.editItemName);
        textCategory = findViewById(R.id.textCategory);
        textSellBy = findViewById(R.id.textSellBy);
        imgCategory = findViewById(R.id.imgCategory);
        layoutCategorySelector = findViewById(R.id.layoutCategorySelector);
        recyclerVariants = findViewById(R.id.recyclerVariants);
        recyclerVariants.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteItem());
        
        layoutCategorySelector.setOnClickListener(v -> showCategorySelectionDialog());

        findViewById(R.id.btnAddVariant).setOnClickListener(v -> showAddVariantDialog());

        loadItemData();
    }

    private void showAddVariantDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_variant, null);
        dialog.setContentView(view);

        EditText editName = view.findViewById(R.id.editVariantName);
        EditText editSelling = view.findViewById(R.id.editSellingPrice);
        EditText editCost = view.findViewById(R.id.editCostPrice);
        EditText editStock = view.findViewById(R.id.editStock);

        view.findViewById(R.id.btnSaveVariant).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter variant name", Toast.LENGTH_SHORT).show();
                return;
            }

            double selling = 0, cost = 0;
            int stock = 0;
            try { selling = Double.parseDouble(editSelling.getText().toString()); } catch (Exception ignored) {}
            try { cost = Double.parseDouble(editCost.getText().toString()); } catch (Exception ignored) {}
            try { stock = Integer.parseInt(editStock.getText().toString()); } catch (Exception ignored) {}

            Variant variant = new Variant(itemId, name, selling, cost, stock);
            Executors.newSingleThreadExecutor().execute(() -> {
                // Get current variant count for sort order
                int count = db.variantDao().getVariantsForItem(itemId).size();
                variant.setSortOrder(count);
                
                db.variantDao().insert(variant);
                
                // Update total stock quantity
                if (currentItem != null) {
                    currentItem.setStockQuantity(currentItem.getStockQuantity() + variant.getStockQuantity());
                    
                    // If this is the first variant, also update the display name and price
                    if (count == 0) {
                        currentItem.setSellingPrice(variant.getSellingPrice());
                        currentItem.setCostPrice(variant.getCostPrice());
                        currentItem.setVariantName(variant.getName());
                    }
                    db.itemDao().update(currentItem);
                }
                runOnUiThread(() -> {
                    dialog.dismiss();
                    setResult(RESULT_OK); // Notify parent to refresh list
                    loadItemData();
                    Toast.makeText(this, "Variant added", Toast.LENGTH_SHORT).show();
                });
            });
        });

        dialog.show();
    }

    private void loadItemData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentItem = db.itemDao().getById(itemId);
            List<Variant> variants = db.variantDao().getVariantsForItem(itemId);

            if (currentItem != null) {
                // Fetch category image
                String categoryName = currentItem.getCategory();
                Category category = db.categoryDao().getByName(categoryName);

                runOnUiThread(() -> {
                    editItemName.setText(currentItem.getName());
                    textCategory.setText(categoryName != null ? categoryName : "No Category");
                    textSellBy.setText("Sell by " + (currentItem.getSellBy() != null ? currentItem.getSellBy() : "Unit"));
                    
                    if (category != null && category.getImageUri() != null) {
                        imgCategory.setImageURI(android.net.Uri.parse(category.getImageUri()));
                        imgCategory.setImageTintList(null);
                    } else {
                        imgCategory.setImageResource(R.drawable.ic_nav_reports);
                        int color = androidx.core.content.ContextCompat.getColor(this, R.color.reports_tab_selected);
                        imgCategory.setImageTintList(android.content.res.ColorStateList.valueOf(color));
                    }

                    VariantAdapter adapter = new VariantAdapter(variants);
                    recyclerVariants.setAdapter(adapter);
                });
            }
        });
    }

    private void showCategorySelectionDialog() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = db.categoryDao().getAllCategories();
            runOnUiThread(() -> {
                String[] names = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) names[i] = categories.get(i).getName();

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setTitle("Select Category");
                builder.setItems(names, (dialog, which) -> {
                    updateCategory(names[which]);
                });
                builder.show();
            });
        });
    }

    private void updateCategory(String newCategory) {
        if (currentItem != null) {
            currentItem.setCategory(newCategory);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.itemDao().update(currentItem);
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    loadItemData(); // Refresh UI
                });
            });
        }
    }

    private void deleteItem() {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.itemDao().deleteById(itemId); // I need to add this method to ItemDao
            db.variantDao().deleteVariantsForItem(itemId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {
        private List<Variant> variants;

        VariantAdapter(List<Variant> variants) {
            this.variants = variants;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_variant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Variant variant = variants.get(position);
            holder.textVariantName.setText(variant.getName());
            holder.textSellingPrice.setText("₹" + (int) variant.getSellingPrice());
            holder.textCostPrice.setText(variant.getCostPrice() > 0 ? "₹" + (int) variant.getCostPrice() : "-");
            holder.textStock.setText(String.valueOf(variant.getStockQuantity()));

            holder.btnMoveUp.setOnClickListener(v -> moveVariant(holder.getAdapterPosition(), -1));
            holder.btnMoveDown.setOnClickListener(v -> moveVariant(holder.getAdapterPosition(), 1));

            holder.btnMoveUp.setAlpha(position == 0 ? 0.3f : 1.0f);
            holder.btnMoveUp.setEnabled(position > 0);
            holder.btnMoveDown.setAlpha(position == variants.size() - 1 ? 0.3f : 1.0f);
            holder.btnMoveDown.setEnabled(position < variants.size() - 1);
        }

        private void moveVariant(int fromPosition, int direction) {
            int toPosition = fromPosition + direction;
            if (toPosition < 0 || toPosition >= variants.size()) return;

            java.util.Collections.swap(variants, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            notifyItemChanged(fromPosition);
            notifyItemChanged(toPosition);

            // Persist order and update main item info to match the new top variant
            Executors.newSingleThreadExecutor().execute(() -> {
                for (int i = 0; i < variants.size(); i++) {
                    variants.get(i).setSortOrder(i);
                    db.variantDao().update(variants.get(i));
                }
                
                // Update Item record with the info of the top variant (index 0)
                if (currentItem != null && !variants.isEmpty()) {
                    Variant topVariant = variants.get(0);
                    currentItem.setSellingPrice(topVariant.getSellingPrice());
                    currentItem.setCostPrice(topVariant.getCostPrice());
                    currentItem.setVariantName(topVariant.getName());
                    db.itemDao().update(currentItem);
                    runOnUiThread(() -> setResult(RESULT_OK)); // Ensure parent refreshes
                }
            });
        }

        @Override
        public int getItemCount() {
            return variants.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textVariantName, textSellingPrice, textCostPrice, textStock;
            View btnMoveUp, btnMoveDown;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textVariantName = itemView.findViewById(R.id.textVariantName);
                textSellingPrice = itemView.findViewById(R.id.textSellingPrice);
                textCostPrice = itemView.findViewById(R.id.textCostPrice);
                textStock = itemView.findViewById(R.id.textStock);
                btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
                btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
            }
        }
    }
}
