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
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private TextView textHeaderBusinessName, textHeaderPhoneNumber, txtOwnerName;
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
            recyclerItemCategories.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerItemList = findViewById(R.id.recyclerItemList);
        if (recyclerItemList != null) {
            recyclerItemList.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerItemGrid = findViewById(R.id.recyclerItemGrid);
        if (recyclerItemGrid != null) {
            recyclerItemGrid.setLayoutManager(new GridLayoutManager(this, 3));
        }

        recyclerCounterItems = findViewById(R.id.recyclerCounterItems);
        if (recyclerCounterItems != null) {
            recyclerCounterItems.setLayoutManager(new LinearLayoutManager(this));
        }

        btnGoToCounter = findViewById(R.id.btnGoToCounter);
        btnCharge = findViewById(R.id.btnCharge);
        labelCounterBadge = findViewById(R.id.labelCounterBadge);
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
            counterEmptyStateContainer.findViewById(R.id.editSearchCounterEmpty).setFocusable(false);
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
        imgLogo = findViewById(R.id.imgLogo);

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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

        findViewById(R.id.cardExpenseIncome).setOnClickListener(v -> {
            startActivity(new Intent(ReportsActivity.this, CashFlowActivity.class));
        });

        findViewById(R.id.cardAllCustomers).setOnClickListener(v -> {
            Intent intent = new Intent(ReportsActivity.this, CustomerManagementActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardDueCustomers).setOnClickListener(v -> {
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
                    if (textCounterGrandTotal != null) textCounterGrandTotal.setText(String.format(Locale.getDefault(), "₹%.0f", subtotal));
                    if (btnCharge != null) {
                        btnCharge.setVisibility(View.VISIBLE);
                        btnCharge.setText(String.format(Locale.getDefault(), "Charge: ₹%.0f", subtotal));
                    }
                    if (textItemCountSummary != null) {
                        int units = CartManager.getInstance().getTotalUnits();
                        String itemsStr = count == 1 ? " Item" : " Items";
                        String unitsStr = units == 1 ? " Unit" : " Units";
                        textItemCountSummary.setText(String.format(Locale.getDefault(), "%d%s | %d%s", count, itemsStr, units, unitsStr));
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
            } catch (NumberFormatException ignored) {}
        });

        dialog.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            try {
                String val = editQuantity.getText().toString();
                int q = val.isEmpty() ? 0 : Integer.parseInt(val);
                if (q > 0) editQuantity.setText(String.valueOf(q - 1));
            } catch (NumberFormatException ignored) {}
        });

        dialog.findViewById(R.id.btnUpdateQuantity).setOnClickListener(v -> {
            try {
                int q = Integer.parseInt(editQuantity.getText().toString());
                int vId = (cartItem.getVariant() != null) ? cartItem.getVariant().getId() : -1;
                CartManager.getInstance().updateQuantity(cartItem.getItem().getId(), vId, q);
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
                    if (textHeaderBusinessName != null) textHeaderBusinessName.setText(settings.getBusinessName());
                    if (textHeaderPhoneNumber != null) textHeaderPhoneNumber.setText(settings.getPhoneNumber());
                    if (txtOwnerName != null) txtOwnerName.setText(settings.getBusinessName() + " " + getString(R.string.header_owner_suffix));
                    if (imgLogo != null && settings.getBusinessLogoPath() != null) {
                        try {
                            imgLogo.setImageURI(android.net.Uri.parse(settings.getBusinessLogoPath()));
                            imgLogo.setImageTintList(null);
                        } catch (Exception ignored) {}
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
        languages.add(new Language("मরাठी", "mr"));
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
            recreate();
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

        moreContentContainer.setVisibility(View.GONE);
        itemsContentContainer.setVisibility(View.GONE);
        counterContentContainer.setVisibility(View.GONE);
        counterEmptyStateContainer.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
        reportsContentPlaceholder.setVisibility(View.GONE);
        dateSelectorRow.setVisibility(View.GONE);
        if (btnGoToCounter != null) btnGoToCounter.setVisibility(View.GONE);
        if (btnCharge != null) btnCharge.setVisibility(View.GONE);

        int selectedId = bottomTabs[selectedTab].getId();
        if (selectedId == R.id.tabMore) {
            moreContentContainer.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.reports_tab_more));
            int bgColor = ContextCompat.getColor(this, R.color.reports_tab_selected);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
        } else if (selectedId == R.id.tabItems) {
            itemsContentContainer.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.reports_tab_items));
            int bgColor = ContextCompat.getColor(this, R.color.reports_tab_selected);
            topBarReports.setBackgroundColor(bgColor);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
            refreshItemsView();
        } else if (selectedId == R.id.tabReports) {
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
            case 0: recyclerItemCategories.setVisibility(View.VISIBLE); loadItemCategories(); break;
            case 1: recyclerItemList.setVisibility(View.VISIBLE); loadItemList(); break;
            case 2: recyclerItemGrid.setVisibility(View.VISIBLE); loadItemGrid(); break;
        }
    }

    private void filterItems(String query) {
        if (itemViewMode == 0) {
            if (recyclerItemCategories.getAdapter() instanceof ItemCategoryAdapter) ((ItemCategoryAdapter) recyclerItemCategories.getAdapter()).filter(query);
        } else if (itemViewMode == 1) {
            if (recyclerItemList.getAdapter() instanceof ProductListAdapter) ((ProductListAdapter) recyclerItemList.getAdapter()).filter(query);
        } else if (itemViewMode == 2) {
            if (recyclerItemGrid.getAdapter() instanceof ItemGridAdapter) ((ItemGridAdapter) recyclerItemGrid.getAdapter()).filter(query);
        }
    }

    private void loadItemList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Item> dbItems = AppDatabase.getInstance(this).itemDao().getAllItems();
            runOnUiThread(() -> { if (recyclerItemList != null) recyclerItemList.setAdapter(new ProductListAdapter(dbItems)); });
        });
    }

    private void loadItemGrid() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Item> dbItems;
            if (itemTileStyle == 1) dbItems = AppDatabase.getInstance(this).itemDao().getUncategorizedItems();
            else dbItems = AppDatabase.getInstance(this).itemDao().getAllItems();
            final List<Item> finalItems = dbItems;
            runOnUiThread(() -> {
                if (recyclerItemGrid != null) {
                    recyclerItemGrid.setAdapter(new ItemGridAdapter(finalItems, itemTileStyle, (item, pos) -> {
                        if (item.isAdvanceMode()) showVariantSelectorDialog(item);
                        else { CartManager.getInstance().addItem(item); updateCounterUI(); }
                    }));
                }
            });
        });
    }

    private void showVariantSelectorDialog(Item item) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_variant_selector_bottom_sheet, null);
        bottomSheet.setContentView(view);

        TextView textItemNameHeader = view.findViewById(R.id.textItemNameHeader);
        textItemNameHeader.setText(item.getName().toUpperCase());

        view.findViewById(R.id.btnEditItem).setOnClickListener(v -> {
            bottomSheet.dismiss();
            Intent intent = new Intent(this, ManageItemActivity.class);
            intent.putExtra("item_id", item.getId());
            startActivity(intent);
        });

        view.findViewById(R.id.btnSelectDone).setOnClickListener(v -> bottomSheet.dismiss());

        RecyclerView rv = view.findViewById(R.id.recyclerVariantSelector);
        rv.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Variant> variants = AppDatabase.getInstance(this).variantDao().getVariantsForItem(item.getId());
            runOnUiThread(() -> {
                if (variants.isEmpty()) {
                    CartManager.getInstance().addItem(item);
                    bottomSheet.dismiss();
                    updateCounterUI();
                    return;
                }

                VariantSelectorAdapter adapter = new VariantSelectorAdapter(variants, CartManager.getInstance().getCartItems(), (variant, quantity) -> {
                    CartManager.getInstance().setVariantQuantity(item, variant, quantity);
                });
                rv.setAdapter(adapter);
            });
        });

        bottomSheet.show();
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
            int unc = itemDao.getUncategorizedItemCount();
            if (unc > 0) list.add(new ItemCategoryAdapter.CategoryWithCount("Uncategorized", unc));
            runOnUiThread(() -> { if (recyclerItemCategories != null) recyclerItemCategories.setAdapter(new ItemCategoryAdapter(list)); });
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (currentTab == TAB_ITEMS) getMenuInflater().inflate(R.menu.menu_items_tab, menu);
        else getMenuInflater().inflate(R.menu.reports_top_app_bar_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) { return true; }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        if (id == R.id.action_toggle_view) { showItemViewSelector(); return true; }
        if (id == R.id.action_add_customer) { startActivity(new Intent(this, AddCustomerActivity.class)); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showItemViewSelector() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.layout_item_view_selector, null);
        dialog.setContentView(view);
        view.findViewById(R.id.btnCloseSelector).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.optionTiles).setOnClickListener(v -> { itemViewMode = 2; itemTileStyle = 0; refreshItemsView(); dialog.dismiss(); });
        view.findViewById(R.id.optionList).setOnClickListener(v -> { itemViewMode = 2; itemTileStyle = 1; refreshItemsView(); dialog.dismiss(); });
        view.findViewById(R.id.optionCategory).setOnClickListener(v -> { itemViewMode = 0; refreshItemsView(); dialog.dismiss(); });
        dialog.show();
    }
}
