package com.example.billz;

import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.Intent;
import android.widget.Toast;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ReportsActivity extends AppCompatActivity {

    private static final int TAB_REPORTS = 0;
    private static final int TAB_TODAY = 1;
    private static final int TAB_COUNTER = 2;
    private static final int TAB_ITEMS = 3;
    private static final int TAB_MORE = 4;

    private View emptyStateContainer;
    private View reportsContentPlaceholder;
    private View moreContentContainer;
    private View itemsContentContainer;
    private View counterContentContainer;
    private View counterEmptyStateContainer;
    private View dateSelectorRow;
    private LinearLayout bottomNavigationView;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private View topBarReports;
    private View[] bottomTabs;
    private ImageView[] bottomTabIcons;
    private TextView[] bottomTabLabels;
    private TextView textHeaderBusinessName, textHeaderPhoneNumber, txtOwnerName, txtOwnerEmail;
    private ImageView imgLogo;
    
    private RecyclerView recyclerItemCategories;
    private RecyclerView recyclerItemList;
    private RecyclerView recyclerItemGrid;
    private RecyclerView recyclerCounterItems;
    private View btnGoToCounter;
    private Button btnCharge;
    private TextView labelCounterBadge, textCounterSubtotal, textCounterGrandTotal, textItemCountSummary;
    private EditText editSearchItems;
    private int currentTab = TAB_REPORTS;
    private int itemViewMode = 2; // 0: Category, 1: List, 2: Tiles
    private int itemTileStyle = 0; // 0: Tap to add (No banner), 1: Without category (Banner)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply persisted language before super.onCreate
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);

        View root = findViewById(R.id.reportsRoot);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        reportsContentPlaceholder = findViewById(R.id.reportsContentPlaceholder);
        moreContentContainer = findViewById(R.id.moreContentContainer);
        itemsContentContainer = findViewById(R.id.itemsContentContainer);
        counterContentContainer = findViewById(R.id.counterContentContainer);
        counterEmptyStateContainer = findViewById(R.id.counterEmptyStateContainer);
        dateSelectorRow = findViewById(R.id.dateSelectorRow);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        topBarReports = findViewById(R.id.topBarReports);
        toolbar = findViewById(R.id.toolbarReports);

        recyclerItemCategories = findViewById(R.id.recyclerItemCategories);
        if (recyclerItemCategories != null) {
            recyclerItemCategories.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        }

        recyclerItemList = findViewById(R.id.recyclerItemList);
        if (recyclerItemList != null) {
            recyclerItemList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        }

        recyclerItemGrid = findViewById(R.id.recyclerItemGrid);
        if (recyclerItemGrid != null) {
            recyclerItemGrid.setLayoutManager(new GridLayoutManager(this, 3));
        }

        recyclerCounterItems = findViewById(R.id.recyclerCounterItems);
        if (recyclerCounterItems != null) {
            recyclerCounterItems.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        }

        btnGoToCounter = findViewById(R.id.btnGoToCounter);
        btnCharge = findViewById(R.id.btnCharge);
        labelCounterBadge = findViewById(R.id.labelCounterBadge); // Need to add ID in XML
        textCounterSubtotal = findViewById(R.id.textCounterSubtotal);
        textCounterGrandTotal = findViewById(R.id.textCounterGrandTotal);
        textItemCountSummary = findViewById(R.id.textItemCountSummary);

        if (btnGoToCounter != null) {
            btnGoToCounter.setOnClickListener(v -> highlightBottomTab(TAB_COUNTER));
        }

        View btnAddMoreItems = findViewById(R.id.btnAddMoreItems);
        if (btnAddMoreItems != null) {
            btnAddMoreItems.setOnClickListener(v -> highlightBottomTab(TAB_ITEMS));
        }

        if (counterEmptyStateContainer != null) {
            counterEmptyStateContainer.findViewById(R.id.btnNewSale).setOnClickListener(v -> highlightBottomTab(TAB_ITEMS));
            counterEmptyStateContainer.findViewById(R.id.editSearchCounterEmpty).setOnClickListener(v -> highlightBottomTab(TAB_ITEMS));
            counterEmptyStateContainer.findViewById(R.id.editSearchCounterEmpty).setFocusable(false); // Make it behave like a button
        }

        findViewById(R.id.btnClearCart).setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
        });
        editSearchItems = findViewById(R.id.editSearchItems);

        if (editSearchItems != null) {
            editSearchItems.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterItems(s.toString());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        textHeaderBusinessName = findViewById(R.id.textHeaderBusinessName);
        textHeaderPhoneNumber = findViewById(R.id.textHeaderPhoneNumber);
        txtOwnerName = findViewById(R.id.txtOwnerName);
        txtOwnerEmail = findViewById(R.id.txtOwnerEmail);
        imgLogo = findViewById(R.id.imgLogo);

        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
            }
        });
        bottomTabs = new View[]{
                findViewById(R.id.tabReports),
                findViewById(R.id.tabToday),
                findViewById(R.id.tabCounter),
                findViewById(R.id.tabItems),
                findViewById(R.id.tabMore)
        };
        bottomTabIcons = new ImageView[]{
                findViewById(R.id.iconReports),
                findViewById(R.id.iconToday),
                findViewById(R.id.iconCounter),
                findViewById(R.id.iconItems),
                findViewById(R.id.iconMore)
        };
        bottomTabLabels = new TextView[]{
                findViewById(R.id.labelReports),
                findViewById(R.id.labelToday),
                findViewById(R.id.labelCounter),
                findViewById(R.id.labelItems),
                findViewById(R.id.labelMore)
        };

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (topBarReports != null) {
                topBarReports.setPadding(
                        topBarReports.getPaddingLeft(),
                        systemBars.top,
                        topBarReports.getPaddingRight(),
                        topBarReports.getPaddingBottom()
                );
            }
            bottomNavigationView.setPadding(
                    bottomNavigationView.getPaddingLeft(),
                    getResources().getDimensionPixelSize(R.dimen.reports_bottom_nav_padding_top),
                    bottomNavigationView.getPaddingRight(),
                    getResources().getDimensionPixelSize(R.dimen.reports_bottom_nav_padding_bottom)
            );

            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) bottomNavigationView.getLayoutParams();
            if (layoutParams.bottomMargin != systemBars.bottom) {
                layoutParams.bottomMargin = systemBars.bottom;
                bottomNavigationView.setLayoutParams(layoutParams);
            }
            return windowInsets;
        });

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        setupBottomTabs();
        highlightBottomTab(TAB_REPORTS);

        findViewById(R.id.nav_inventory).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, InventoryManagementActivity.class));
        });

        findViewById(R.id.nav_customers).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, CustomerManagementActivity.class));
        });

        findViewById(R.id.nav_staff).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, StaffManagementActivity.class));
        });

        findViewById(R.id.nav_language).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showLanguageDialog();
        });

        findViewById(R.id.nav_add_expense).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, CashFlowActivity.class));
        });

        findViewById(R.id.nav_receipt_settings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, ReceiptSettingsActivity.class));
        });

        findViewById(R.id.nav_business_settings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, BusinessSettingsActivity.class));
        });

        findViewById(R.id.btnEditBusiness).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showBusinessSelectorDialog();
        });

        findViewById(R.id.btnSwitchBusiness).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showBusinessSelectorDialog();
        });

        findViewById(R.id.btnCreateBusiness).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, AddBusinessActivity.class));
        });

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, EditProfileActivity.class));
        });

        // Add this if not already there, but let's check nav_header buttons
        // The buttons in nav_header_reports are usually set up in onCreate if they have IDs.
        // Let's check nav_header_reports.xml IDs.

        findViewById(R.id.cardExpenseIncome).setOnClickListener(v -> {
            startActivity(new Intent(ReportsActivity.this, CashFlowActivity.class));
        });

        findViewById(R.id.cardAllCustomers).setOnClickListener(v -> {
            android.util.Log.d("ReportsActivity", "All Customers card clicked");
            Intent intent = new Intent(ReportsActivity.this, CustomerManagementActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardDueCustomers).setOnClickListener(v -> {
            android.util.Log.d("ReportsActivity", "Due Customers card clicked");
            Intent intent = new Intent(ReportsActivity.this, CustomerManagementActivity.class);
            startActivity(intent);
        });

        showEmptyState(true);
        updateSidebarLanguageText();
        setupCartManager();
    }

    private void setupCartManager() {
        CartManager.getInstance().setListener(this::updateCounterUI);
        updateCounterUI();
    }

    private void updateCounterUI() {
        runOnUiThread(() -> {
            int count = CartManager.getInstance().getItemCount();
            if (labelCounterBadge != null) {
                labelCounterBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                labelCounterBadge.setText(String.valueOf(count));
            }
            
            // Refresh items view to update quantity overlays (x1, x2 etc)
            if (itemViewMode == 1 && recyclerItemList.getAdapter() != null) {
                recyclerItemList.getAdapter().notifyDataSetChanged();
            } else if (itemViewMode == 2 && recyclerItemGrid.getAdapter() != null) {
                recyclerItemGrid.getAdapter().notifyDataSetChanged();
            }

            if (currentTab == TAB_COUNTER) {
                if (count > 0) {
                    counterContentContainer.setVisibility(View.VISIBLE);
                    counterEmptyStateContainer.setVisibility(View.GONE);
                    loadCounterItems();
                    double subtotal = CartManager.getInstance().getSubtotal();
                    if (textCounterSubtotal != null) textCounterSubtotal.setText(String.valueOf((int)subtotal));
                    if (textCounterGrandTotal != null) textCounterGrandTotal.setText("₹" + (int)subtotal);
                    if (btnCharge != null) {
                        btnCharge.setVisibility(View.VISIBLE);
                        btnCharge.setText("Charge: ₹" + (int)subtotal);
                    }
                    if (textItemCountSummary != null) {
                        int units = CartManager.getInstance().getTotalUnits();
                        String itemsText = count == 1 ? " Item" : " Items";
                        String unitsText = units == 1 ? " Unit" : " Units";
                        textItemCountSummary.setText(count + itemsText + " | " + units + unitsText);
                    }
                } else {
                    counterContentContainer.setVisibility(View.GONE);
                    counterEmptyStateContainer.setVisibility(View.VISIBLE);
                    if (btnCharge != null) btnCharge.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadCounterItems() {
        if (recyclerCounterItems != null) {
            recyclerCounterItems.setAdapter(new CounterAdapter(CartManager.getInstance().getCartItems(), this::showEditQuantityDialog));
        }
    }

    private void showEditQuantityDialog(CartItem cartItem) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_quantity);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editQuantity = dialog.findViewById(R.id.editQuantity);
        editQuantity.setText(String.valueOf(cartItem.getQuantity()));

        dialog.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            try {
                String val = editQuantity.getText().toString();
                int q = val.isEmpty() ? 0 : Integer.parseInt(val);
                editQuantity.setText(String.valueOf(q + 1));
            } catch (NumberFormatException e) {
                editQuantity.setText("1");
            }
        });

        dialog.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            try {
                String val = editQuantity.getText().toString();
                int q = val.isEmpty() ? 0 : Integer.parseInt(val);
                if (q > 0) editQuantity.setText(String.valueOf(q - 1));
            } catch (NumberFormatException e) {
                editQuantity.setText("0");
            }
        });

        dialog.findViewById(R.id.btnUpdateQuantity).setOnClickListener(v -> {
            try {
                int q = Integer.parseInt(editQuantity.getText().toString());
                CartManager.getInstance().updateQuantity(cartItem.getItem().getId(), q);
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.findViewById(R.id.btnDialogClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBusinessData();
        if (currentTab == TAB_ITEMS) {
            refreshItemsView();
        }
    }

    private void loadBusinessData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ReceiptSettings settings = AppDatabase.getInstance(this).receiptSettingsDao().getSettings();
            runOnUiThread(() -> {
                if (settings != null) {
                    if (textHeaderBusinessName != null) {
                        textHeaderBusinessName.setText(settings.getBusinessName());
                    }
                    if (textHeaderPhoneNumber != null) {
                        textHeaderPhoneNumber.setText(settings.getPhoneNumber());
                    }
                    if (txtOwnerName != null) {
                        txtOwnerName.setText(settings.getBusinessName() + " " + getString(R.string.header_owner_suffix));
                    }
                    if (txtOwnerEmail != null) {
                        txtOwnerEmail.setText(settings.getEmail() != null ? settings.getEmail() : "nutritioncompany.com@gmail.com");
                    }
                    if (imgLogo != null && settings.getBusinessLogoPath() != null) {
                        try {
                            imgLogo.setImageURI(android.net.Uri.parse(settings.getBusinessLogoPath()));
                            // Remove tint if we're showing a real logo
                            imgLogo.setImageTintList(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
    }

    private void updateSidebarLanguageText() {
        TextView textLanguage = findViewById(R.id.textLanguageName);
        if (textLanguage != null) {
            String code = LocaleHelper.getPersistedLanguage(this);
            String languageName;
            
            switch (code) {
                case "hi": languageName = "Hindi"; break;
                case "ar": languageName = "Arabic"; break;
                case "fr": languageName = "French"; break;
                case "es": languageName = "Spanish"; break;
                case "bn": languageName = "Bengali"; break;
                case "fil": languageName = "Filipino"; break;
                case "ms": languageName = "Malay"; break;
                case "pt": languageName = "Portuguese"; break;
                case "ru": languageName = "Russian"; break;
                case "kn": languageName = "Kannada"; break;
                case "te": languageName = "Telugu"; break;
                case "ta": languageName = "Tamil"; break;
                case "mr": languageName = "Marathi"; break;
                case "ja": languageName = "Japanese"; break;
                case "zh": languageName = "Chinese"; break;
                case "de": languageName = "German"; break;
                case "in": languageName = "Indonesian"; break;
                case "iw": languageName = "Hebrew"; break;
                case "sw": languageName = "Swahili"; break;
                case "tr": languageName = "Turkish"; break;
                default: languageName = "English"; break;
            }
            
            textLanguage.setText(getString(R.string.language_display, languageName));
        }
    }

    public void showEmptyState(boolean isEmpty) {
        emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        reportsContentPlaceholder.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showLanguageDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_select_language);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerLanguages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<Language> languages = new ArrayList<>();
        languages.add(new Language("ENGLISH", "en"));
        languages.add(new Language("عربي", "ar"));
        languages.add(new Language("FRANÇAISE", "fr"));
        languages.add(new Language("ESPAÑOLA", "es"));
        languages.add(new Language("हिंदी", "hi"));
        languages.add(new Language("বাঙালি", "bn"));
        languages.add(new Language("FILIPINO", "fil"));
        languages.add(new Language("MELAYU", "ms"));
        languages.add(new Language("PORTUGUESA", "pt"));
        languages.add(new Language("РУССКИЙ", "ru"));
        languages.add(new Language("ಕನ್ನಡ", "kn"));
        languages.add(new Language("తెలుగు", "te"));
        languages.add(new Language("தமிழ்", "ta"));
        languages.add(new Language("मराठी", "mr"));
        languages.add(new Language("日本語", "ja"));
        languages.add(new Language("中文", "zh"));
        languages.add(new Language("DEUTSCHE", "de"));
        languages.add(new Language("BAHASA INDONESIA", "in"));
        languages.add(new Language("עברית", "iw"));
        languages.add(new Language("KISWAHILI", "sw"));
        languages.add(new Language("TÜRKÇE", "tr"));

        LanguageAdapter adapter = new LanguageAdapter(languages, language -> {
            LocaleHelper.setLocale(this, language.getCode());
            dialog.dismiss();
            recreate(); // Recreate to apply changes immediately
        });
        recyclerView.setAdapter(adapter);
        
        dialog.findViewById(R.id.imageClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showBusinessSelectorDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_business_selector);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerBusinesses);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Business> businesses = AppDatabase.getInstance(this).businessDao().getAllBusinesses();
            runOnUiThread(() -> {
                if (businesses.isEmpty()) {
                    // Seed initial data if empty
                    businesses.add(new Business("Nutrition Co", "+918825347516", "OWNER", true));
                    businesses.add(new Business("PROTEIN HUB -DEOGHAR", "+917903598844", "OWNER", false));
                    businesses.add(new Business("The City Gym (Unisex)", "+910000000000", "OWNER", false));
                }

                BusinessAdapter adapter = new BusinessAdapter(businesses, business -> {
                    dialog.dismiss();
                    Intent intent = new Intent(this, AddBusinessActivity.class);
                    intent.putExtra("business_id", business.getId());
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });

        dialog.findViewById(R.id.btnAddBusiness).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, AddBusinessActivity.class));
        });

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupBottomTabs() {
        for (int i = 0; i < bottomTabs.length; i++) {
            final int tabIndex = i;
            bottomTabs[i].setOnClickListener(v -> highlightBottomTab(tabIndex));
        }
    }

    private void highlightBottomTab(int selectedTab) {
        this.currentTab = selectedTab;
        invalidateOptionsMenu();

        int unselectedColor = ContextCompat.getColor(this, R.color.reports_tab_unselected);
        int selectedColor = ContextCompat.getColor(this, R.color.white);

        for (int i = 0; i < bottomTabs.length; i++) {
            boolean isSelected = i == selectedTab;
            bottomTabs[i].setBackgroundResource(isSelected ? R.drawable.bg_reports_nav_item : 0);
            bottomTabIcons[i].setColorFilter(isSelected ? selectedColor : unselectedColor);
            bottomTabLabels[i].setTextColor(isSelected ? selectedColor : unselectedColor);
            bottomTabLabels[i].setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        }

        // Handle content visibility based on tab
        int selectedId = bottomTabs[selectedTab].getId();

        // Reset all views
        moreContentContainer.setVisibility(View.GONE);
        itemsContentContainer.setVisibility(View.GONE);
        counterContentContainer.setVisibility(View.GONE);
        counterEmptyStateContainer.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
        reportsContentPlaceholder.setVisibility(View.GONE);
        dateSelectorRow.setVisibility(View.GONE);
        if (btnGoToCounter != null) btnGoToCounter.setVisibility(View.GONE);
        if (btnCharge != null) btnCharge.setVisibility(View.GONE);

        if (selectedId == R.id.tabMore) {
            // ... (rest of method)
            // SHOW Grid
            moreContentContainer.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.reports_tab_more));
            int bgColor = ContextCompat.getColor(this, R.color.reports_tab_selected);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
        } else if (selectedId == R.id.tabItems) {
            // SHOW Items screen
            itemsContentContainer.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.reports_tab_items));
            int bgColor = ContextCompat.getColor(this, R.color.reports_tab_selected);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
            refreshItemsView();
        } else if (selectedId == R.id.tabReports) {
            // Reports section
            dateSelectorRow.setVisibility(View.VISIBLE);
            showEmptyState(true);
            toolbar.setTitle(getString(R.string.reports_title));
            int bgColor = ContextCompat.getColor(this, R.color.reports_surface);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.reports_text_primary));
            toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.reports_text_primary));
        } else if (selectedId == R.id.tabToday) {
            toolbar.setTitle(getString(R.string.reports_tab_today));
            reportsContentPlaceholder.setVisibility(View.VISIBLE);
            int bgColor = ContextCompat.getColor(this, R.color.reports_surface);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.reports_text_primary));
            toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.reports_text_primary));
        } else if (selectedId == R.id.tabCounter) {
            toolbar.setTitle(getString(R.string.reports_tab_counter));
            int bgColor = ContextCompat.getColor(this, R.color.reports_tab_selected);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
            updateCounterUI();
        }
    }

    private void refreshItemsView() {
        recyclerItemCategories.setVisibility(View.GONE);
        recyclerItemList.setVisibility(View.GONE);
        recyclerItemGrid.setVisibility(View.GONE);

        switch (itemViewMode) {
            case 0: // With Category
                recyclerItemCategories.setVisibility(View.VISIBLE);
                if (btnGoToCounter != null) btnGoToCounter.setVisibility(View.GONE);
                loadItemCategories();
                break;
            case 1: // Without Category (List)
                recyclerItemList.setVisibility(View.VISIBLE);
                if (btnGoToCounter != null) btnGoToCounter.setVisibility(View.VISIBLE);
                loadItemList();
                break;
            case 2: // Tiles (Grid)
                recyclerItemGrid.setVisibility(View.VISIBLE);
                if (btnGoToCounter != null) btnGoToCounter.setVisibility(View.VISIBLE);
                loadItemGrid();
                break;
        }
    }

    private void filterItems(String query) {
        if (itemViewMode == 0) {
            if (recyclerItemCategories.getAdapter() instanceof ItemCategoryAdapter) {
                ((ItemCategoryAdapter) recyclerItemCategories.getAdapter()).filter(query);
            }
        } else if (itemViewMode == 1) {
            if (recyclerItemList.getAdapter() instanceof ProductListAdapter) {
                ((ProductListAdapter) recyclerItemList.getAdapter()).filter(query);
            }
        } else if (itemViewMode == 2) {
            if (recyclerItemGrid.getAdapter() instanceof ItemGridAdapter) {
                ((ItemGridAdapter) recyclerItemGrid.getAdapter()).filter(query);
            }
        }
    }

    private void loadItemList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Item> dbItems;
            if (itemViewMode == 1) {
                dbItems = AppDatabase.getInstance(this).itemDao().getUncategorizedItems();
            } else {
                dbItems = AppDatabase.getInstance(this).itemDao().getAllItems();
            }
            
            // Add dummy data if empty to match the provided image
            if (dbItems.isEmpty()) {
                if (itemViewMode == 1) {
                    dbItems.add(createDummyItem(-10, "No Category Item 1", "", 100, 80, 5, "Unit"));
                    dbItems.add(createDummyItem(-11, "No Category Item 2", null, 200, 150, 2, "Unit"));
                } else {
                    dbItems.add(createDummyItem(-1, "AA", "AA", 2899, 2500, 10, "PARANORMIC FAT"));
                    dbItems.add(createDummyItem(-2, "Absolute Nutrition", "Whey", 2109, 1800, 10, "WHEY PROTEIN 1KG"));
                    dbItems.add(createDummyItem(-3, "Alpino", "Oats", 499, 400, 10, "OATS 1KG"));
                    dbItems.add(createDummyItem(-4, "Androbol Xterem", "Stack", 2999, 2500, 10, "< ₹2,999 >"));
                    dbItems.add(createDummyItem(-5, "Ashwagandha Af 43", "Tablets", 500, 400, 10, "60 TAB"));
                    dbItems.add(createDummyItem(-6, "Asitis Creatine", "Creatine", 549, 450, 10, "UNFLAVRED 250 GM"));
                    dbItems.add(createDummyItem(-7, "Atom", "Isolated", 2349, 2000, 10, "ISOLATED 1KG"));
                    dbItems.add(createDummyItem(-8, "Avvatar Iso Rich", "Whey", 3499, 3000, 10, "1KG"));
                    dbItems.add(createDummyItem(-9, "Avvatar Whey", "Whey", 2099, 1800, 10, "UNFLAVORED 1KG"));
                }
            }

            final List<Item> finalItems = dbItems;
            runOnUiThread(() -> {
                if (recyclerItemList != null) {
                    recyclerItemList.setAdapter(new ProductListAdapter(finalItems));
                    if (editSearchItems != null && !editSearchItems.getText().toString().isEmpty()) {
                        filterItems(editSearchItems.getText().toString());
                    }
                }
            });
        });
    }

    private void loadItemGrid() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Item> dbItems;
            if (itemTileStyle == 1) {
                dbItems = AppDatabase.getInstance(this).itemDao().getUncategorizedItems();
            } else {
                dbItems = AppDatabase.getInstance(this).itemDao().getAllItems();
            }

            // Add dummy data if empty to match the provided image
            if (dbItems.isEmpty()) {
                if (itemTileStyle == 1) {
                    dbItems.add(createDummyItem(-10, "No Category Item 1", "", 100, 80, 5, "Unit"));
                    dbItems.add(createDummyItem(-11, "No Category Item 2", null, 200, 150, 2, "Unit"));
                } else {
                    dbItems.add(createDummyItem(-1, "AA", "AA", 2899, 2500, 10, "PARANORMIC FAT"));
                    dbItems.add(createDummyItem(-2, "Absolute Nutrition", "Whey", 2109, 1800, 10, "WHEY PROTEIN 1KG"));
                    dbItems.add(createDummyItem(-3, "Alpino", "Oats", 499, 400, 10, "OATS 1KG"));
                    dbItems.add(createDummyItem(-4, "Androbol Xterem", "Stack", 2999, 2500, 10, "< ₹2,999 >"));
                    dbItems.add(createDummyItem(-5, "Ashwagandha Af 43", "Tablets", 500, 400, 10, "60 TAB"));
                    dbItems.add(createDummyItem(-6, "Asitis Creatine", "Creatine", 549, 450, 10, "UNFLAVRED 250 GM"));
                    dbItems.add(createDummyItem(-7, "Atom", "Isolated", 2349, 2000, 10, "ISOLATED 1KG"));
                    dbItems.add(createDummyItem(-8, "Avvatar Iso Rich", "Whey", 3499, 3000, 10, "1KG"));
                    dbItems.add(createDummyItem(-9, "Avvatar Whey", "Whey", 2099, 1800, 10, "UNFLAVORED 1KG"));
                }
            }

            final List<Item> finalItems = dbItems;
            runOnUiThread(() -> {
                if (recyclerItemGrid != null) {
                    recyclerItemGrid.setAdapter(new ItemGridAdapter(finalItems, itemTileStyle));
                    if (editSearchItems != null && !editSearchItems.getText().toString().isEmpty()) {
                        filterItems(editSearchItems.getText().toString());
                    }
                }
            });
        });
    }

    private Item createDummyItem(int id, String name, String category, double selling, double cost, int stock, String variant) {
        Item item = new Item(name, category, selling, cost, stock, variant, "Unit", false);
        item.setId(id);
        return item;
    }

    private void loadItemCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> dbCategories = AppDatabase.getInstance(this).categoryDao().getAllCategories();
            ItemDao itemDao = AppDatabase.getInstance(this).itemDao();
            
            List<ItemCategoryAdapter.CategoryWithCount> list = new ArrayList<>();
            for (Category cat : dbCategories) {
                int count = itemDao.getItemCountByCategory(cat.getName());
                list.add(new ItemCategoryAdapter.CategoryWithCount(cat.getName(), count));
            }

            // Check for uncategorized items
            int uncategorizedCount = itemDao.getUncategorizedItemCount();
            
            if (uncategorizedCount > 0) {
                list.add(new ItemCategoryAdapter.CategoryWithCount("Uncategorized", uncategorizedCount));
            }
            
            // Add dummy if none to match image initially
            if (list.isEmpty()) {
                list.add(new ItemCategoryAdapter.CategoryWithCount("Brand All Item", 5));
                list.add(new ItemCategoryAdapter.CategoryWithCount("Compression Tshirt", 1));
                list.add(new ItemCategoryAdapter.CategoryWithCount("MASS GAINER", 10));
                list.add(new ItemCategoryAdapter.CategoryWithCount("Creatine", 3));
                list.add(new ItemCategoryAdapter.CategoryWithCount("College", 1));
                list.add(new ItemCategoryAdapter.CategoryWithCount("DAILY SUPPORT", 18));
            }

            runOnUiThread(() -> {
                if (recyclerItemCategories != null) {
                    recyclerItemCategories.setAdapter(new ItemCategoryAdapter(list));
                    if (editSearchItems != null && !editSearchItems.getText().toString().isEmpty()) {
                        filterItems(editSearchItems.getText().toString());
                    }
                }
            });
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (currentTab == TAB_ITEMS) {
            getMenuInflater().inflate(R.menu.menu_items_tab, menu);
        } else {
            getMenuInflater().inflate(R.menu.reports_top_app_bar_menu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_toggle_view) {
            showItemViewSelector();
            return true;
        }
        if (itemId == R.id.action_share || itemId == R.id.action_messages) {
            return true;
        }
        if (itemId == R.id.action_add_customer) {
            startActivity(new Intent(this, AddCustomerActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showItemViewSelector() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.layout_item_view_selector, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnCloseSelector).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.optionTiles).setOnClickListener(v -> {
            itemViewMode = 2; // Tiles
            itemTileStyle = 0; // Tap to add style
            refreshItemsView();
            dialog.dismiss();
        });

        view.findViewById(R.id.optionList).setOnClickListener(v -> {
            itemViewMode = 2; // Tiles
            itemTileStyle = 1; // Without category style (Banner)
            refreshItemsView();
            dialog.dismiss();
        });

        view.findViewById(R.id.optionCategory).setOnClickListener(v -> {
            itemViewMode = 0; // With Category
            refreshItemsView();
            dialog.dismiss();
        });

        dialog.show();
    }
}
