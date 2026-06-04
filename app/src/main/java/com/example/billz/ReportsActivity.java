package com.example.billz;

import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
    private Tax selectedTax;
    private Discount selectedDiscount;
    private OtherFee selectedOtherCharge;
    private DeliveryFee selectedDeliveryCharge;
    private PackingFee selectedPackingCharge;
    private ServiceFee selectedServiceCharge;
    private TextView btnAddTax, btnAddDiscount, btnAddOtherCharges;

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
        btnAddTax = findViewById(R.id.btnAddTax);
        btnAddDiscount = findViewById(R.id.btnAddDiscount);
        btnAddOtherCharges = findViewById(R.id.btnAddOtherCharges);

        if (btnAddTax != null) {
            btnAddTax.setOnClickListener(v -> showSelectTaxDialog());
        }

        if (btnAddDiscount != null) {
            btnAddDiscount.setOnClickListener(v -> showSelectDiscountDialog());
        }

        if (btnAddOtherCharges != null) {
            btnAddOtherCharges.setOnClickListener(v -> showChargeTypeSelector());
        }

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
                    double taxAmount = 0;
                    if (selectedTax != null) {
                        taxAmount = subtotal * (selectedTax.getValue() / 100.0);
                        if (btnAddTax != null) {
                            btnAddTax.setText(String.format(Locale.getDefault(), "Tax: %s(%.0f%%)", selectedTax.getName(), selectedTax.getValue()));
                        }
                    } else {
                        if (btnAddTax != null) btnAddTax.setText("Add Tax");
                    }

                    double discountAmount = 0;
                    if (selectedDiscount != null) {
                        if (selectedDiscount.isPercentage()) {
                            discountAmount = subtotal * (selectedDiscount.getValue() / 100.0);
                        } else {
                            discountAmount = selectedDiscount.getValue();
                        }
                        if (btnAddDiscount != null) {
                            String symbol = selectedDiscount.isPercentage() ? "%" : "₹";
                            btnAddDiscount.setText(String.format(Locale.getDefault(), "Disc: %s(%s%.0f)", selectedDiscount.getName(), symbol, selectedDiscount.getValue()));
                        }
                    } else {
                        if (btnAddDiscount != null) btnAddDiscount.setText("Add Discount");
                    }

                    double otherAmount = 0;
                    StringBuilder chargesDesc = new StringBuilder();
                    
                    if (selectedDeliveryCharge != null) {
                        double val = selectedDeliveryCharge.isPercentage() ? (subtotal * selectedDeliveryCharge.getValue() / 100.0) : selectedDeliveryCharge.getValue();
                        otherAmount += val;
                        chargesDesc.append("Delivery:").append(String.format(Locale.getDefault(), "%.0f", selectedDeliveryCharge.getValue())).append(selectedDeliveryCharge.isPercentage() ? "% " : "₹ ");
                    }
                    
                    if (selectedPackingCharge != null) {
                        double val = selectedPackingCharge.isPercentage() ? (subtotal * selectedPackingCharge.getValue() / 100.0) : selectedPackingCharge.getValue();
                        otherAmount += val;
                        chargesDesc.append("Packing:").append(String.format(Locale.getDefault(), "%.0f", selectedPackingCharge.getValue())).append(selectedPackingCharge.isPercentage() ? "% " : "₹ ");
                    }
                    
                    if (selectedServiceCharge != null) {
                        double val = selectedServiceCharge.isPercentage() ? (subtotal * selectedServiceCharge.getValue() / 100.0) : selectedServiceCharge.getValue();
                        otherAmount += val;
                        chargesDesc.append("Service:").append(String.format(Locale.getDefault(), "%.0f", selectedServiceCharge.getValue())).append(selectedServiceCharge.isPercentage() ? "% " : "₹ ");
                    }

                    if (selectedOtherCharge != null) {
                        double val = selectedOtherCharge.isPercentage() ? (subtotal * selectedOtherCharge.getValue() / 100.0) : selectedOtherCharge.getValue();
                        otherAmount += val;
                        chargesDesc.append("Other:").append(String.format(Locale.getDefault(), "%.0f", selectedOtherCharge.getValue())).append(selectedOtherCharge.isPercentage() ? "% " : "₹ ");
                    }

                    if (btnAddOtherCharges != null) {
                        if (chargesDesc.length() > 0) {
                            btnAddOtherCharges.setText(chargesDesc.toString().trim());
                        } else {
                            btnAddOtherCharges.setText("Add Other Charges");
                        }
                    }

                    double grandTotal = subtotal + taxAmount - discountAmount + otherAmount;

                    if (textCounterSubtotal != null) textCounterSubtotal.setText(String.valueOf((int)subtotal));
                    if (textCounterGrandTotal != null) textCounterGrandTotal.setText(String.format(Locale.getDefault(), "₹%.0f", grandTotal));
                    if (btnCharge != null) {
                        btnCharge.setVisibility(View.VISIBLE);
                        btnCharge.setText(String.format(Locale.getDefault(), "Charge: ₹%.0f", grandTotal));
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
                int maxStock = (cartItem.getVariant() != null) ? cartItem.getVariant().getStockQuantity() : cartItem.getItem().getStockQuantity();
                if (q + 1 <= maxStock) {
                    editQuantity.setText(String.valueOf(q + 1));
                } else {
                    Toast.makeText(this, "Maximum stock reached", Toast.LENGTH_SHORT).show();
                }
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
                int maxStock = (cartItem.getVariant() != null) ? cartItem.getVariant().getStockQuantity() : cartItem.getItem().getStockQuantity();
                if (q > maxStock) {
                    Toast.makeText(this, "Only " + maxStock + " units available in stock", Toast.LENGTH_SHORT).show();
                    q = maxStock;
                }
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
                        if (item.isAdvanceMode()) {
                            showVariantSelectorDialog(item);
                        } else {
                            if (CartManager.getInstance().addItem(item)) {
                                updateCounterUI();
                            } else {
                                Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show();
                            }
                        }
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
                    if (CartManager.getInstance().addItem(item)) {
                        bottomSheet.dismiss();
                        updateCounterUI();
                    } else {
                        Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                VariantSelectorAdapter adapter = new VariantSelectorAdapter(variants, CartManager.getInstance().getCartItems(), (variant, quantity) -> {
                    if (!CartManager.getInstance().setVariantQuantity(item, variant, quantity)) {
                        Toast.makeText(this, "Only " + variant.getStockQuantity() + " units available in stock", Toast.LENGTH_SHORT).show();
                    }
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

    private void showChargeTypeSelector() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_charge_type_selector, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.optionDelivery).setOnClickListener(v -> {
            bottomSheet.dismiss();
            showSelectDeliveryDialog();
        });

        view.findViewById(R.id.optionPacking).setOnClickListener(v -> {
            bottomSheet.dismiss();
            showSelectPackingDialog();
        });

        view.findViewById(R.id.optionService).setOnClickListener(v -> {
            bottomSheet.dismiss();
            showSelectServiceDialog();
        });

        view.findViewById(R.id.optionOther).setOnClickListener(v -> {
            bottomSheet.dismiss();
            showSelectOtherDialog();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> bottomSheet.dismiss());
        bottomSheet.show();
    }

    private void showSelectDeliveryDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);

        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT DELIVERY CHARGE");
        
        EditText editValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView rv = view.findViewById(R.id.recyclerOthers);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedDeliveryCharge != null) {
            editValue.setText(String.format(Locale.getDefault(), "%.1f", selectedDeliveryCharge.getValue()));
            checkPercentage.setChecked(selectedDeliveryCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<DeliveryFee> list = AppDatabase.getInstance(this).deliveryFeeDao().getAllDeliveryFees();
            runOnUiThread(() -> {
                DeliverySelectionAdapter adapter = new DeliverySelectionAdapter(list, fee -> {
                    selectedDeliveryCharge = fee;
                    editValue.setText(String.format(Locale.getDefault(), "%.1f", fee.getValue()));
                    checkPercentage.setChecked(fee.isPercentage());
                    if (rv.getAdapter() != null) ((DeliverySelectionAdapter) rv.getAdapter()).setSelectedId(fee.getId());
                });
                if (selectedDeliveryCharge != null) adapter.setSelectedId(selectedDeliveryCharge.getId());
                rv.setAdapter(adapter);
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String val = editValue.getText().toString().trim();
            if (!val.isEmpty()) {
                double chargeVal = Double.parseDouble(val);
                if (selectedDeliveryCharge == null || chargeVal != selectedDeliveryCharge.getValue() || checkPercentage.isChecked() != selectedDeliveryCharge.isPercentage()) {
                    selectedDeliveryCharge = new DeliveryFee("Custom Delivery", chargeVal, checkPercentage.isChecked(), false);
                }
                updateCounterUI();
                bottomSheet.dismiss();
            } else {
                selectedDeliveryCharge = null;
                updateCounterUI();
                bottomSheet.dismiss();
            }
        });
        bottomSheet.show();
    }

    private void showSelectPackingDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);
        
        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT PACKING CHARGE");

        EditText editValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView rv = view.findViewById(R.id.recyclerOthers);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedPackingCharge != null) {
            editValue.setText(String.format(Locale.getDefault(), "%.1f", selectedPackingCharge.getValue()));
            checkPercentage.setChecked(selectedPackingCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<PackingFee> list = AppDatabase.getInstance(this).packingFeeDao().getAllPackingFees();
            runOnUiThread(() -> {
                PackingSelectionAdapter adapter = new PackingSelectionAdapter(list, fee -> {
                    selectedPackingCharge = fee;
                    editValue.setText(String.format(Locale.getDefault(), "%.1f", fee.getValue()));
                    checkPercentage.setChecked(fee.isPercentage());
                    if (rv.getAdapter() != null) ((PackingSelectionAdapter) rv.getAdapter()).setSelectedId(fee.getId());
                });
                if (selectedPackingCharge != null) adapter.setSelectedId(selectedPackingCharge.getId());
                rv.setAdapter(adapter);
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String val = editValue.getText().toString().trim();
            if (!val.isEmpty()) {
                double chargeVal = Double.parseDouble(val);
                if (selectedPackingCharge == null || chargeVal != selectedPackingCharge.getValue() || checkPercentage.isChecked() != selectedPackingCharge.isPercentage()) {
                    selectedPackingCharge = new PackingFee("Custom Packing", chargeVal, checkPercentage.isChecked(), false);
                }
                updateCounterUI();
                bottomSheet.dismiss();
            } else {
                selectedPackingCharge = null;
                updateCounterUI();
                bottomSheet.dismiss();
            }
        });
        bottomSheet.show();
    }

    private void showSelectServiceDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);
        
        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT SERVICE CHARGE");

        EditText editValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView rv = view.findViewById(R.id.recyclerOthers);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedServiceCharge != null) {
            editValue.setText(String.format(Locale.getDefault(), "%.1f", selectedServiceCharge.getValue()));
            checkPercentage.setChecked(selectedServiceCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<ServiceFee> list = AppDatabase.getInstance(this).serviceFeeDao().getAllServiceFees();
            runOnUiThread(() -> {
                ServiceSelectionAdapter adapter = new ServiceSelectionAdapter(list, fee -> {
                    selectedServiceCharge = fee;
                    editValue.setText(String.format(Locale.getDefault(), "%.1f", fee.getValue()));
                    checkPercentage.setChecked(fee.isPercentage());
                    if (rv.getAdapter() != null) ((ServiceSelectionAdapter) rv.getAdapter()).setSelectedId(fee.getId());
                });
                if (selectedServiceCharge != null) adapter.setSelectedId(selectedServiceCharge.getId());
                rv.setAdapter(adapter);
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String val = editValue.getText().toString().trim();
            if (!val.isEmpty()) {
                double chargeVal = Double.parseDouble(val);
                if (selectedServiceCharge == null || chargeVal != selectedServiceCharge.getValue() || checkPercentage.isChecked() != selectedServiceCharge.isPercentage()) {
                    selectedServiceCharge = new ServiceFee("Custom Service", chargeVal, checkPercentage.isChecked(), false);
                }
                updateCounterUI();
                bottomSheet.dismiss();
            } else {
                selectedServiceCharge = null;
                updateCounterUI();
                bottomSheet.dismiss();
            }
        });
        bottomSheet.show();
    }

    private void showSelectOtherDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);

        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT OTHER CHARGE");

        EditText editOtherValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerOthers);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedOtherCharge != null) {
            editOtherValue.setText(String.format(Locale.getDefault(), "%.1f", selectedOtherCharge.getValue()));
            checkPercentage.setChecked(selectedOtherCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<OtherFee> others = AppDatabase.getInstance(this).otherFeeDao().getAllOtherFees();
            if (others.isEmpty()) {
                AppDatabase.getInstance(this).otherFeeDao().insert(new OtherFee("PREVIOUS DUES", 100, false, false));
                AppDatabase.getInstance(this).otherFeeDao().insert(new OtherFee("DELIVERY CHARGES", 0, false, false));
                AppDatabase.getInstance(this).otherFeeDao().insert(new OtherFee("BUS CHARGE", 100, false, false));
                AppDatabase.getInstance(this).otherFeeDao().insert(new OtherFee("TRANSPORTATION", 0, false, false));
                others = AppDatabase.getInstance(this).otherFeeDao().getAllOtherFees();
            }

            final List<OtherFee> finalOthers = others;
            runOnUiThread(() -> {
                OtherSelectionAdapter adapter = new OtherSelectionAdapter(finalOthers, other -> {
                    selectedOtherCharge = other;
                    editOtherValue.setText(String.format(Locale.getDefault(), "%.1f", other.getValue()));
                    checkPercentage.setChecked(other.isPercentage());
                    if (recyclerView.getAdapter() != null) {
                        ((OtherSelectionAdapter) recyclerView.getAdapter()).setSelectedId(other.getId());
                    }
                });
                if (selectedOtherCharge != null) adapter.setSelectedId(selectedOtherCharge.getId());
                recyclerView.setAdapter(adapter);
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String val = editOtherValue.getText().toString().trim();
            if (!val.isEmpty()) {
                double chargeVal = Double.parseDouble(val);
                // If the value in the box is different from the selected tile, treat it as a Custom Charge
                if (selectedOtherCharge == null || chargeVal != selectedOtherCharge.getValue() || checkPercentage.isChecked() != selectedOtherCharge.isPercentage()) {
                    selectedOtherCharge = new OtherFee("Custom", chargeVal, checkPercentage.isChecked(), false);
                }
                updateCounterUI();
                bottomSheet.dismiss();
            } else {
                selectedOtherCharge = null;
                updateCounterUI();
                bottomSheet.dismiss();
            }
        });

        view.findViewById(R.id.btnAddNewOther).setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(this, OtherFeeSettingsActivity.class));
        });

        bottomSheet.show();
    }

    private void showSelectDiscountDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_discount_dialog, null);
        bottomSheet.setContentView(view);

        EditText editDiscountValue = view.findViewById(R.id.editDiscountValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerDiscounts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedDiscount != null) {
            editDiscountValue.setText(String.format(Locale.getDefault(), "%.1f", selectedDiscount.getValue()));
            checkPercentage.setChecked(selectedDiscount.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Discount> discounts = AppDatabase.getInstance(this).discountDao().getAllDiscounts();
            if (discounts.isEmpty()) {
                AppDatabase.getInstance(this).discountDao().insert(new Discount("DUES", 40, false, false));
                AppDatabase.getInstance(this).discountDao().insert(new Discount("PREVIOUS", 250, false, false));
                AppDatabase.getInstance(this).discountDao().insert(new Discount("ONLINE", 780, false, false));
                AppDatabase.getInstance(this).discountDao().insert(new Discount("RETURN GRIPPER", 250, false, false));
                discounts = AppDatabase.getInstance(this).discountDao().getAllDiscounts();
            }

            final List<Discount> finalDiscounts = discounts;
            runOnUiThread(() -> {
                DiscountSelectionAdapter adapter = new DiscountSelectionAdapter(finalDiscounts, discount -> {
                    selectedDiscount = discount;
                    editDiscountValue.setText(String.format(Locale.getDefault(), "%.1f", discount.getValue()));
                    checkPercentage.setChecked(discount.isPercentage());
                    if (recyclerView.getAdapter() != null) {
                        ((DiscountSelectionAdapter) recyclerView.getAdapter()).setSelectedId(discount.getId());
                    }
                });
                if (selectedDiscount != null) adapter.setSelectedId(selectedDiscount.getId());
                recyclerView.setAdapter(adapter);
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String val = editDiscountValue.getText().toString().trim();
            if (!val.isEmpty()) {
                double discVal = Double.parseDouble(val);
                if (selectedDiscount == null) {
                    selectedDiscount = new Discount("Custom Discount", discVal, checkPercentage.isChecked(), false);
                } else {
                    selectedDiscount.setValue(discVal);
                    selectedDiscount.setPercentage(checkPercentage.isChecked());
                }
                updateCounterUI();
                bottomSheet.dismiss();
            } else {
                selectedDiscount = null;
                updateCounterUI();
                bottomSheet.dismiss();
            }
        });

        view.findViewById(R.id.btnAddNewDiscount).setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(this, DiscountSettingsActivity.class));
        });

        bottomSheet.show();
    }

    private void showSelectTaxDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_tax_dialog, null);
        bottomSheet.setContentView(view);

        EditText editTaxValue = view.findViewById(R.id.editTaxValue);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerTaxes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedTax != null) {
            editTaxValue.setText(String.format(Locale.getDefault(), "%.1f", selectedTax.getValue()));
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Tax> taxes = AppDatabase.getInstance(this).taxDao().getAllTaxes();
            if (taxes.isEmpty()) {
                AppDatabase.getInstance(this).taxDao().insert(new Tax("CGST on sales", 9.0, false));
                AppDatabase.getInstance(this).taxDao().insert(new Tax("SGST+CGST", 18.0, false));
                AppDatabase.getInstance(this).taxDao().insert(new Tax("SGST on sales", 9.0, false));
                AppDatabase.getInstance(this).taxDao().insert(new Tax("TRANSPORTATION", 50.0, false));
                taxes = AppDatabase.getInstance(this).taxDao().getAllTaxes();
            }

            final List<Tax> finalTaxes = taxes;
            runOnUiThread(() -> {
                TaxSelectionAdapter adapter = new TaxSelectionAdapter(finalTaxes, tax -> {
                    selectedTax = tax;
                    editTaxValue.setText(String.format(Locale.getDefault(), "%.1f", tax.getValue()));
                    if (recyclerView.getAdapter() != null) {
                        ((TaxSelectionAdapter) recyclerView.getAdapter()).setSelectedId(tax.getId());
                    }
                });
                if (selectedTax != null) adapter.setSelectedId(selectedTax.getId());
                recyclerView.setAdapter(adapter);
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String val = editTaxValue.getText().toString().trim();
            if (!val.isEmpty()) {
                double taxVal = Double.parseDouble(val);
                if (selectedTax == null) {
                    selectedTax = new Tax("Custom Tax", taxVal, false);
                } else {
                    selectedTax.setValue(taxVal);
                }
                updateCounterUI();
                bottomSheet.dismiss();
            } else {
                selectedTax = null;
                updateCounterUI();
                bottomSheet.dismiss();
            }
        });

        view.findViewById(R.id.btnAddNewTax).setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(this, TaxSettingsActivity.class));
        });

        bottomSheet.show();
    }

    private void showItemViewSelector() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.layout_item_view_selector, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }

        View layoutTextTiles = view.findViewById(R.id.layoutTextTiles);
        View layoutTextList = view.findViewById(R.id.layoutTextList);
        View layoutTextCategory = view.findViewById(R.id.layoutTextCategory);
        TextView textTiles = view.findViewById(R.id.textTiles);
        TextView textList = view.findViewById(R.id.textList);
        TextView textCategory = view.findViewById(R.id.textCategory);

        // Reset Styles
        layoutTextTiles.setBackgroundColor(Color.WHITE);
        textTiles.setTextColor(0xFF3F51B5);
        layoutTextList.setBackgroundColor(Color.WHITE);
        textList.setTextColor(0xFF3F51B5);
        layoutTextCategory.setBackgroundColor(Color.WHITE);
        textCategory.setTextColor(0xFF3F51B5);

        // Apply selected style
        if (itemViewMode == 2) {
            if (itemTileStyle == 0) {
                layoutTextTiles.setBackgroundColor(0xFF3F51B5);
                textTiles.setTextColor(Color.WHITE);
            } else {
                layoutTextList.setBackgroundColor(0xFF3F51B5);
                textList.setTextColor(Color.WHITE);
            }
        } else if (itemViewMode == 0) {
            layoutTextCategory.setBackgroundColor(0xFF3F51B5);
            textCategory.setTextColor(Color.WHITE);
        }

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

        // Add smooth transition for cards
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up);
        view.findViewById(R.id.gridOptions).startAnimation(slideUp);

        dialog.show();
    }
}
