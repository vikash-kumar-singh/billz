package com.example.billz;

import android.os.Bundle;
import android.view.View;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import android.view.inputmethod.InputMethodManager;
import android.transition.TransitionManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import android.widget.Toast;
import java.util.Locale;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyStateView;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemsList;
    private List<InventoryItem> categoriesList;
    private List<InventoryItem> modifiersList;
    private List<InventoryItem> ingredientsList;
    private List<InventoryItem> currentDisplayList;
    private List<String> selectedFilterTags = new ArrayList<>();
    private String activeQuickFilter = "All";
    private EditText editSearch;
    private boolean isSearchVisible = false;
    private ActivityResultLauncher<Intent> addCategoryLauncher;
    private ActivityResultLauncher<Intent> addModifierLauncher;
    private ActivityResultLauncher<Intent> addIngredientLauncher;
    private ActivityResultLauncher<Intent> addItemLauncher;

    private InventoryAdapter.OnItemClickListener itemClickListener = item -> {
        if (item.getType() == 0) { // Item
            Intent intent = new Intent(this, ManageItemActivity.class);
            intent.putExtra("item_id", item.getDatabaseId());
            addItemLauncher.launch(intent);
        } else if (item.getType() == 2) { // Modifier
            Intent intent = new Intent(this, CreateModifierActivity.class);
            intent.putExtra("modifier_set_id", item.getDatabaseId());
            intent.putExtra("modifier_set_name", item.getName());
            addModifierLauncher.launch(intent);
        } else if (item.getType() == 3) { // Ingredient
            Intent intent = new Intent(this, AddIngredientActivity.class);
            intent.putExtra("ingredient_id", item.getDatabaseId());
            addIngredientLauncher.launch(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory_management);

        addCategoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadCategoriesFromDB();
                    }
                }
        );

        addModifierLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadModifiersFromDB();
                    }
                }
        );

        addIngredientLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadIngredientsFromDB();
                    }
                }
        );

        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadItemsFromDB();
                        loadCategoriesFromDB(); // Real-time reflection of category counts
                    }
                }
        );

        MaterialToolbar toolbar = findViewById(R.id.toolbarInventory);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        View filterScrollView = findViewById(R.id.filterScrollView);
        View imageFilter = findViewById(R.id.imageFilter);
        TabLayout tabLayout = findViewById(R.id.tabLayoutInventory);
        emptyStateView = findViewById(R.id.emptyStateView);
        ViewGroup mainContainer = findViewById(android.R.id.content);

        editSearch = findViewById(R.id.editSearch);
        ImageView imageSearch = findViewById(R.id.imageSearch);
        View searchBarContainer = findViewById(R.id.searchBarContainer);
        ImageView imageClearSearch = findViewById(R.id.imageClearSearch);

        imageSearch.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(mainContainer);
            if (isSearchVisible) {
                searchBarContainer.setVisibility(View.GONE);
                editSearch.setText("");
                hideKeyboard();
            } else {
                searchBarContainer.setVisibility(View.VISIBLE);
                editSearch.requestFocus();
                showKeyboard();
            }
            isSearchVisible = !isSearchVisible;
        });

        imageClearSearch.setOnClickListener(v -> {
            if (editSearch.getText().length() > 0) {
                editSearch.setText("");
            } else {
                // If already empty, close the search bar
                searchBarContainer.setVisibility(View.GONE);
                isSearchVisible = false;
                // Restore original title
                toolbar.setTitle(R.string.inventory_title);
                hideKeyboard();
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    checkEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupFilterChips();
        
        imageFilter.setOnClickListener(v -> showFilterBottomSheet());

        MaterialToolbar toolbarView = findViewById(R.id.toolbarInventory);
        ViewCompat.setOnApplyWindowInsetsListener(toolbarView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupSwipeNavigation(tabLayout);

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            showAddMenuBottomSheet();
        });

        itemsList = new ArrayList<>();
        categoriesList = new ArrayList<>();
        modifiersList = new ArrayList<>();
        ingredientsList = new ArrayList<>();
        
        loadCategoriesFromDB();
        loadModifiersFromDB();
        loadIngredientsFromDB();
        loadItemsFromDB();

        currentDisplayList = new ArrayList<>(itemsList);
        adapter = new InventoryAdapter(currentDisplayList, this, itemClickListener);
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                
                if (position == 0) {
                    filterScrollView.setVisibility(View.VISIBLE);
                    imageFilter.setVisibility(View.VISIBLE);
                    updateDisplayList(itemsList);
                } else {
                    filterScrollView.setVisibility(View.GONE);
                    imageFilter.setVisibility(View.GONE);
                    
                    if (position == 1) updateDisplayList(categoriesList);
                    else if (position == 2) updateDisplayList(modifiersList);
                    else if (position == 3) updateDisplayList(ingredientsList);
                }
                checkEmptyState();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeNavigation(TabLayout tabLayout) {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        int currentTab = tabLayout.getSelectedTabPosition();
                        if (diffX < 0) {
                            // Swipe Left gesture (finger moves left): Move to next tab
                            if (currentTab < tabLayout.getTabCount() - 1) {
                                TabLayout.Tab nextTab = tabLayout.getTabAt(currentTab + 1);
                                if (nextTab != null) nextTab.select();
                            }
                        } else {
                            // Swipe Right gesture (finger moves right): Move to previous tab
                            if (currentTab > 0) {
                                TabLayout.Tab prevTab = tabLayout.getTabAt(currentTab - 1);
                                if (prevTab != null) prevTab.select();
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }
        });
    }

    private void setupFilterChips() {
        TextView chipAll = findViewById(R.id.chipAll);
        TextView chipLowStock = findViewById(R.id.chipLowStock);
        TextView chipExpired = findViewById(R.id.chipExpired);

        View.OnClickListener chipListener = v -> {
            // Reset colors for all chips
            int unselectedBg = Color.parseColor("#E2E8F0");
            int unselectedText = Color.parseColor("#475569");
            
            chipAll.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
            chipAll.setTextColor(unselectedText);
            chipLowStock.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
            chipLowStock.setTextColor(unselectedText);
            chipExpired.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
            chipExpired.setTextColor(unselectedText);

            // Highlight selected chip
            TextView selected = (TextView) v;
            selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E9D5FF")));
            selected.setTextColor(Color.parseColor("#7C3AED"));

            // Filter logic
            if (v.getId() == R.id.chipLowStock) {
                activeQuickFilter = "Low Stock";
            } else if (v.getId() == R.id.chipExpired) {
                activeQuickFilter = "Expired";
            } else {
                activeQuickFilter = "All";
            }
            applyAllFilters();
        };

        chipAll.setOnClickListener(chipListener);
        chipLowStock.setOnClickListener(chipListener);
        chipExpired.setOnClickListener(chipListener);
    }

    private void applyAllFilters() {
        List<InventoryItem> filtered = new ArrayList<>();
        
        // 1. Apply Quick Filter (All / Low Stock / Expired)
        List<InventoryItem> baseList;
        if (activeQuickFilter.equals("Low Stock")) {
            baseList = new ArrayList<>();
            for (InventoryItem item : itemsList) {
                if (!item.isOutOfStock() && item.getStockQuantity() > 0 && item.getStockQuantity() < 5) {
                    baseList.add(item);
                }
            }
        } else if (activeQuickFilter.equals("Expired")) {
            baseList = new ArrayList<>();
            for (InventoryItem item : itemsList) {
                if (item.getStockStatus().toLowerCase().contains("expired") || item.isOutOfStock()) {
                    baseList.add(item);
                }
            }
        } else {
            baseList = itemsList;
        }

        // 2. Apply Category Tags Filter
        if (selectedFilterTags.isEmpty() || selectedFilterTags.contains("Brand All Item")) {
            filtered.addAll(baseList);
        } else {
            List<String> lowerTags = new ArrayList<>();
            for (String tag : selectedFilterTags) lowerTags.add(tag.toLowerCase());

            for (InventoryItem item : baseList) {
                boolean matches = false;
                for (String itemTag : item.getTags()) {
                    for (String filterTag : lowerTags) {
                        if (itemTag.toLowerCase().contains(filterTag)) {
                            matches = true;
                            break;
                        }
                    }
                    if (matches) break;
                }
                if (matches) {
                    filtered.add(item);
                }
            }
        }

        updateDisplayList(filtered);
    }

    private void checkEmptyState() {
        if (adapter != null) {
            boolean isEmpty = adapter.getItemCount() == 0;
            emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }

    private void loadCategoriesFromDB() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> dbCategories = AppDatabase.getInstance(this).categoryDao().getAllCategories();
            ItemDao itemDao = AppDatabase.getInstance(this).itemDao();
            
            List<InventoryItem> newCategoriesList = new ArrayList<>();
            for (Category cat : dbCategories) {
                int count = itemDao.getItemCountByCategory(cat.getName());
                String countText = count + (count == 1 ? " Item" : " Items");
                
                InventoryItem item = new InventoryItem(cat.getName(), "", countText, false, 0, new ArrayList<>());
                if (cat.getImageUri() != null) item.setImageUri(cat.getImageUri());
                item.setBackgroundColor(cat.getBackgroundColor());
                item.setType(1); // Category
                newCategoriesList.add(item);
            }

            // Also check for items with no category or "Uncategorized"
            int uncategorizedCount = itemDao.getUncategorizedItemCount();
            
            if (uncategorizedCount > 0) {
                String countText = uncategorizedCount + (uncategorizedCount == 1 ? " Item" : " Items");
                InventoryItem uncategorizedItem = new InventoryItem("Uncategorized", "", countText, false, 0, new ArrayList<>());
                uncategorizedItem.setType(1); // Category
                uncategorizedItem.setBackgroundColor(Color.LTGRAY);
                newCategoriesList.add(uncategorizedItem);
            }

            runOnUiThread(() -> {
                categoriesList.clear();
                categoriesList.addAll(newCategoriesList);

                // Update UI if on category tab
                TabLayout tabLayout = findViewById(R.id.tabLayoutInventory);
                if (tabLayout != null && tabLayout.getSelectedTabPosition() == 1) {
                    updateDisplayList(categoriesList);
                }
            });
        });
    }

    private void loadModifiersFromDB() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            ModifierDao dao = AppDatabase.getInstance(this).modifierDao();
            List<ModifierSet> dbSets = dao.getAllModifierSets();
            runOnUiThread(() -> {
                modifiersList.clear();
                for (ModifierSet set : dbSets) {
                    // Fetch options for each set
                    List<ModifierOption> options = dao.getOptionsForSet(set.getId());
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < options.size(); i++) {
                        sb.append(options.get(i).getName());
                        if (i < options.size() - 1) sb.append(" , ");
                    }
                    
                    InventoryItem item = new InventoryItem(set.getName(), "", sb.toString(), false, 0, new ArrayList<>());
                    item.setType(2); // Modifier
                    item.setDatabaseId(set.getId());
                    modifiersList.add(item);
                }
                
                // Add remaining dummy modifiers
                InventoryItem dummy1 = new InventoryItem("Sugar Free", "₹0", "Health Option", false, 0, new ArrayList<>());
                dummy1.setType(2);
                modifiersList.add(dummy1);
                
                InventoryItem dummy2 = new InventoryItem("Extra Scoop", "₹50", "Add-on", false, 0, new ArrayList<>());
                dummy2.setType(2);
                modifiersList.add(dummy2);

                // Update UI if on modifiers tab
                TabLayout tabLayout = findViewById(R.id.tabLayoutInventory);
                if (tabLayout != null && tabLayout.getSelectedTabPosition() == 2) {
                    updateDisplayList(modifiersList);
                }
            });
        });
    }

    private void loadIngredientsFromDB() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Ingredient> dbIngredients = AppDatabase.getInstance(this).ingredientDao().getAllIngredients();
            runOnUiThread(() -> {
                ingredientsList.clear();
                for (Ingredient ing : dbIngredients) {
                    // For ingredients, we show Name and Stock value (no "kg Left" suffix as per image)
                    InventoryItem item = new InventoryItem(ing.getName(), "", String.format(Locale.getDefault(), "%.0f", ing.getStock()), false, (int) ing.getStock(), new ArrayList<>());
                    item.setType(3); // Ingredient
                    item.setDatabaseId(ing.getId());
                    ingredientsList.add(item);
                }
                
                // Dummy ingredients (matching the new style)
                InventoryItem ing1 = new InventoryItem("Whey Isolate", "", "10", false, 10, new ArrayList<>());
                ing1.setType(3);
                ingredientsList.add(ing1);
                
                InventoryItem ing2 = new InventoryItem("Cocoa Powder", "", "2", false, 2, new ArrayList<>());
                ing2.setType(3);
                ingredientsList.add(ing2);

                // Update UI if on ingredients tab
                TabLayout tabLayout = findViewById(R.id.tabLayoutInventory);
                if (tabLayout != null && tabLayout.getSelectedTabPosition() == 3) {
                    updateDisplayList(ingredientsList);
                }
            });
        });
    }

    private void updateDisplayList(List<InventoryItem> newList) {
        currentDisplayList.clear();
        currentDisplayList.addAll(newList);
        adapter = new InventoryAdapter(currentDisplayList, this, itemClickListener);
        recyclerView.setAdapter(adapter);
        
        // Apply existing search query if present
        if (editSearch != null && !editSearch.getText().toString().isEmpty()) {
            adapter.filter(editSearch.getText().toString());
        }

        checkEmptyState();
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_inventory_filter_bottom_sheet, null);
        dialog.setContentView(view);

        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chipGroupCategories);
        chipGroup.removeAllViews();

        // Color states for chips
        int[][] states = new int[][] {
            new int[] { android.R.attr.state_checked }, // checked
            new int[] { -android.R.attr.state_checked } // unchecked
        };
        int[] textColors = new int[] {
            Color.WHITE,
            Color.parseColor("#475569")
        };
        int[] bgColors = new int[] {
            Color.parseColor("#3F51B5"),
            Color.WHITE
        };
        ColorStateList textStateList = new ColorStateList(states, textColors);
        ColorStateList bgStateList = new ColorStateList(states, bgColors);

        // Fetch categories from DB to populate filter chips
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = AppDatabase.getInstance(this).categoryDao().getAllCategories();
            
            runOnUiThread(() -> {
                // Add "Brand All Item" first
                com.google.android.material.chip.Chip allChip = new com.google.android.material.chip.Chip(this);
                allChip.setText("Brand All Item");
                allChip.setCheckable(true);
                allChip.setChecked(selectedFilterTags.isEmpty() || selectedFilterTags.contains("Brand All Item"));
                allChip.setTextColor(textStateList);
                allChip.setChipBackgroundColor(bgStateList);
                allChip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
                allChip.setChipStrokeWidth(getResources().getDisplayMetrics().density * 1);
                chipGroup.addView(allChip);

                for (Category category : categories) {
                    com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
                    chip.setText(category.getName());
                    chip.setCheckable(true);
                    chip.setChecked(selectedFilterTags.contains(category.getName()));
                    
                    chip.setTextColor(textStateList);
                    chip.setChipBackgroundColor(bgStateList);
                    chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
                    chip.setChipStrokeWidth(getResources().getDisplayMetrics().density * 1);
                    
                    chipGroup.addView(chip);
                }
            });
        });
        
        view.findViewById(R.id.btnCloseFilter).setOnClickListener(v -> dialog.dismiss());
        
        view.findViewById(R.id.btnResetFilter).setOnClickListener(v -> {
            chipGroup.clearCheck();
            selectedFilterTags.clear();
            applyAllFilters();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            selectedFilterTags.clear();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    selectedFilterTags.add(chip.getText().toString());
                }
            }
            applyAllFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAddMenuBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_inventory_add_menu_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.menuAddItem).setOnClickListener(v -> {
            dialog.dismiss();
            addItemLauncher.launch(new Intent(this, AddItemActivity.class));
        });

        view.findViewById(R.id.menuAddCategory).setOnClickListener(v -> {
            dialog.dismiss();
            addCategoryLauncher.launch(new Intent(this, AddCategoryActivity.class));
        });

        view.findViewById(R.id.menuAddModifier).setOnClickListener(v -> {
            dialog.dismiss();
            addModifierLauncher.launch(new Intent(this, CreateModifierActivity.class));
        });

        view.findViewById(R.id.menuAddIngredient).setOnClickListener(v -> {
            dialog.dismiss();
            addIngredientLauncher.launch(new Intent(this, AddIngredientActivity.class));
        });

        view.findViewById(R.id.menuBulkEdit).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Bulk Edit clicked", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void loadItemsFromDB() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Item> dbItems = AppDatabase.getInstance(this).itemDao().getAllItems();
            runOnUiThread(() -> {
                itemsList.clear();
                for (Item item : dbItems) {
                    List<String> tags = new ArrayList<>();
                    if (item.getCategory() != null) tags.add(item.getCategory());
                    
                    String stockStatus = item.getStockQuantity() + " Left In Stock";
                    if (item.getStockQuantity() == 0) stockStatus = "Out of stock";
                    
                    InventoryItem uiItem = new InventoryItem(
                            item.getName(),
                            "₹" + item.getSellingPrice(),
                            stockStatus,
                            item.getStockQuantity() == 0,
                            item.getStockQuantity(),
                            tags
                    );
                    uiItem.setDatabaseId(item.getId());
                    itemsList.add(uiItem);
                }
                
                // Keep dummy data if we want to show it alongside real data
                populateDummyData();

                // Update UI if on items tab
                TabLayout tabLayout = findViewById(R.id.tabLayoutInventory);
                if (tabLayout != null && tabLayout.getSelectedTabPosition() == 0) {
                    updateDisplayList(itemsList);
                }
            });
        });
    }

    private void populateDummyData() {
        // Items
        itemsList.add(new InventoryItem(
                "Avvatar Iso Rich 1kg",
                "₹3,499",
                "1 Left In Stock",
                false,
                1,
                Arrays.asList("Whey ISOLATE", "Premium")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_fat_burner),
                "₹2,899",
                getString(R.string.out_of_stock),
                true,
                0,
                Arrays.asList("Fat-Cutter / Burner", "Stack")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_whey_protein),
                "₹2,109",
                getString(R.string.out_of_stock),
                true,
                0,
                Arrays.asList("Whey Protein 100%", "DAILY SUPPORT")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_oats),
                "₹499",
                getString(R.string.left_in_stock, 3),
                false,
                3,
                Arrays.asList("OATs", "PEANUT BUTTER")
        ));
        itemsList.add(new InventoryItem(
                "Muscle Gainer 5kg",
                "₹4,999",
                "10 Items",
                false,
                10,
                Arrays.asList("Muscle Gainer", "MASS GAINER")
        ));
        itemsList.add(new InventoryItem(
                "Gym Gloves",
                "₹299",
                "5 Left In Stock",
                false,
                5,
                Arrays.asList("GYM ACCESSORIES", "College")
        ));

        // Categories
        // Moved to DB load logic above

        // Modifiers
        // Moved to DB load logic above

        // Ingredients
        // Moved to DB load logic above
    }
}
