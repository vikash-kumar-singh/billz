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
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
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
        if (item.getType() == 2) { // Modifier
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
                        // Refresh items list if needed
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
        populateDummyData();

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
            runOnUiThread(() -> {
                categoriesList.clear();
                for (Category cat : dbCategories) {
                    InventoryItem item = new InventoryItem(cat.getName(), "", "0 Items", false, 0, new ArrayList<>());
                    if (cat.getImageUri() != null) item.setImageUri(cat.getImageUri());
                    item.setBackgroundColor(cat.getBackgroundColor());
                    item.setType(1); // Category
                    categoriesList.add(item);
                }
                
                // Add remaining dummy categories
                InventoryItem cat1 = new InventoryItem(getString(R.string.cat_supplements), "", getString(R.string.items_count, 12), false, 0, Arrays.asList("Health", "Wellness"));
                cat1.setType(1);
                categoriesList.add(cat1);
                
                InventoryItem cat2 = new InventoryItem(getString(R.string.cat_oats_grains), "", getString(R.string.items_count, 5), false, 0, Arrays.asList("Food", "Breakfast"));
                cat2.setType(1);
                categoriesList.add(cat2);

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
        checkEmptyState();
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_inventory_filter_bottom_sheet, null);
        dialog.setContentView(view);

        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chipGroupCategories);
        
        view.findViewById(R.id.btnCloseFilter).setOnClickListener(v -> dialog.dismiss());
        
        view.findViewById(R.id.btnResetFilter).setOnClickListener(v -> {
            chipGroup.clearCheck();
            selectedFilterTags.clear();
            applyAllFilters();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            List<Integer> checkedIds = chipGroup.getCheckedChipIds();
            selectedFilterTags.clear();
            for (Integer id : checkedIds) {
                com.google.android.material.chip.Chip chip = view.findViewById(id);
                selectedFilterTags.add(chip.getText().toString());
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
