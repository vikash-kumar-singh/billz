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
import com.google.android.material.tabs.TabLayout;

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
    private EditText editSearch;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory_management);

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

        View root = findViewById(R.id.toolbarInventory).getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(InventoryManagementActivity.this, AddCustomerActivity.class));
        });

        itemsList = new ArrayList<>();
        categoriesList = new ArrayList<>();
        modifiersList = new ArrayList<>();
        ingredientsList = new ArrayList<>();
        
        populateDummyData();

        currentDisplayList = new ArrayList<>(itemsList);
        adapter = new InventoryAdapter(currentDisplayList, this);
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
                applyFilter("Low Stock");
            } else if (v.getId() == R.id.chipExpired) {
                applyFilter("Expired");
            } else {
                updateDisplayList(itemsList);
            }
        };

        chipAll.setOnClickListener(chipListener);
        chipLowStock.setOnClickListener(chipListener);
        chipExpired.setOnClickListener(chipListener);
    }

    private void applyFilter(String type) {
        List<InventoryItem> filtered = new ArrayList<>();
        for (InventoryItem item : itemsList) {
            if (type.equals("Low Stock")) {
                // Show ONLY if quantity > 0 and < 5 (Not out of stock)
                if (!item.isOutOfStock() && item.getStockQuantity() > 0 && item.getStockQuantity() < 5) {
                    filtered.add(item);
                }
            } else if (type.equals("Expired")) {
                // Show if status contains "expired" OR item is out of stock
                if (item.getStockStatus().toLowerCase().contains("expired") || item.isOutOfStock()) {
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

    private void updateDisplayList(List<InventoryItem> newList) {
        currentDisplayList.clear();
        currentDisplayList.addAll(newList);
        adapter = new InventoryAdapter(currentDisplayList, this);
        recyclerView.setAdapter(adapter);
        checkEmptyState();
    }

    private void populateDummyData() {
        // Items
        itemsList.add(new InventoryItem(
                getString(R.string.item_fat_burner),
                "₹2,899",
                getString(R.string.out_of_stock),
                true,
                0,
                Arrays.asList("Paranormic Fat", "Carbamide Forte", "Carbamide Forte", "Carbamide", "Black Spider")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_whey_protein),
                "₹2,109",
                getString(R.string.out_of_stock),
                true,
                0,
                Arrays.asList("Whey Protein", "Jungli Pre", "Creatine 250gm")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_oats),
                "₹499",
                getString(R.string.left_in_stock, 3),
                false,
                3,
                Arrays.asList("Oats 1kg", "Oats 2.5kg", "Peanut Butter", "Protein Bar", "High Protein", "Muesli 1kg")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_androbol),
                "₹2,999",
                getString(R.string.left_in_stock, 6),
                false,
                6,
                Arrays.asList("< 2,999 >")
        ));
        itemsList.add(new InventoryItem(
                getString(R.string.item_biozyme),
                "₹3,499",
                getString(R.string.expired_on, "12/2023"),
                false,
                10,
                Arrays.asList("Whey", "Premium")
        ));

        // Categories
        categoriesList.add(new InventoryItem(getString(R.string.cat_supplements), "", getString(R.string.items_count, 12), false, 0, Arrays.asList("Health", "Wellness")));
        categoriesList.add(new InventoryItem(getString(R.string.cat_oats_grains), "", getString(R.string.items_count, 5), false, 0, Arrays.asList("Food", "Breakfast")));

        // Modifiers
        modifiersList.add(new InventoryItem("Sugar Free", "₹0", getString(R.string.available), false, 0, Arrays.asList("Health Option")));
        modifiersList.add(new InventoryItem("Extra Scoop", "₹50", getString(R.string.available), false, 0, Arrays.asList("Add-on")));

        // Ingredients
        ingredientsList.add(new InventoryItem("Whey Isolate", "", getString(R.string.unit_kg_left, 10), false, 0, Arrays.asList("Raw Material")));
        ingredientsList.add(new InventoryItem("Cocoa Powder", "", getString(R.string.unit_kg_left, 2), false, 0, Arrays.asList("Flavoring")));
    }
}
