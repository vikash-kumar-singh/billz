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
import android.widget.RelativeLayout;
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
import android.util.Log;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

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
    private TextView textDashboardCustomerCount;
    private TextView textSidebarCustomerCount, textSidebarInventoryCount;

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
    private boolean isRoundOffEnabled = true;
    private LinearLayout layoutBreakdownContainer;
    private TextView btnAddTax, btnAddDiscount, btnAddOtherCharges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);

        // Security Check: Ensure user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

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
        layoutBreakdownContainer = findViewById(R.id.layoutBreakdownContainer);
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

        if (btnCharge != null) {
            btnCharge.setOnClickListener(v -> showCheckoutDialog());
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
        txtOwnerEmail = findViewById(R.id.txtOwnerEmail);
        imgLogo = findViewById(R.id.imgLogo);
        textDashboardCustomerCount = findViewById(R.id.textDashboardCustomerCount);
        textSidebarCustomerCount = findViewById(R.id.textSidebarCustomerCount);
        textSidebarInventoryCount = findViewById(R.id.textSidebarInventoryCount);

        // Explicitly look in sidebar if needed
        View sidebar = findViewById(R.id.sidebarContent);
        if (sidebar != null) {
            if (textHeaderBusinessName == null) textHeaderBusinessName = sidebar.findViewById(R.id.textHeaderBusinessName);
            if (textHeaderPhoneNumber == null) textHeaderPhoneNumber = sidebar.findViewById(R.id.textHeaderPhoneNumber);
            if (txtOwnerName == null) txtOwnerName = sidebar.findViewById(R.id.txtOwnerName);
            if (txtOwnerEmail == null) txtOwnerEmail = sidebar.findViewById(R.id.txtOwnerEmail);
            if (imgLogo == null) imgLogo = sidebar.findViewById(R.id.imgLogo);
            if (textSidebarCustomerCount == null) textSidebarCustomerCount = sidebar.findViewById(R.id.textSidebarCustomerCount);
            if (textSidebarInventoryCount == null) textSidebarInventoryCount = sidebar.findViewById(R.id.textSidebarInventoryCount);
        }

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
                    systemBars.bottom + getResources().getDimensionPixelSize(R.dimen.reports_bottom_nav_padding_bottom)
            );

            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) bottomNavigationView.getLayoutParams();
            if (layoutParams.bottomMargin != 0) {
                layoutParams.bottomMargin = 0;
                bottomNavigationView.setLayoutParams(layoutParams);
            }
            return windowInsets;
        });

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        
        // Fix: Hide bottom navigation when drawer is sliding out
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (bottomNavigationView != null) {
                    bottomNavigationView.setTranslationY(bottomNavigationView.getHeight() * slideOffset);
                }
            }

            @Override public void onDrawerOpened(@NonNull View drawerView) {}
            @Override public void onDrawerClosed(@NonNull View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}
        });

        toggle.syncState();

        setupCartManager();
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
            Intent intent = new Intent(ReportsActivity.this, LanguageSelectionActivity.class);
            intent.putExtra("isFromSettings", true);
            startActivity(intent);
        });

        findViewById(R.id.nav_add_expense).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, CashFlowActivity.class));
        });

        findViewById(R.id.nav_receipts).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, ReceiptsActivity.class));
        });

        findViewById(R.id.nav_returned_receipt).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, ReturnedReceiptsActivity.class));
        });

        findViewById(R.id.nav_feedback).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Feedback module coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_receipt_settings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, ReceiptSettingsActivity.class));
        });

        findViewById(R.id.nav_business_settings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, BusinessSettingsActivity.class));
        });

        findViewById(R.id.nav_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            logoutUser();
        });

        findViewById(R.id.btnEditBusiness).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, EditBusinessActivity.class));
        });

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, EditProfileActivity.class));
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
        ensureActiveBusiness();
        updateCustomerCount();
        syncCloudData();
    }

    private void ensureActiveBusiness() {
        BusinessHelper.ensureActiveBusiness(this, null);
    }

    private int getActiveBusinessId() {
        return BusinessHelper.getActiveBusinessId(this);
    }

    private void syncCloudData() {
        new CustomerSyncManager(this).syncCustomersFromCloud(new CustomerSyncManager.SyncCallback() {
            @Override
            public void onSyncComplete() {
                runOnUiThread(() -> {
                    updateCustomerCount();
                    // If CustomerManagementActivity is open, it might need to refresh, 
                    // but it refreshes onResume anyway.
                });
            }

            @Override
            public void onSyncFailed(String error) {
                // Silently log or handle if needed
                Log.e("ReportsActivity", "Sync failed: " + error);
                runOnUiThread(() -> updateCustomerCount());
            }
        });
    }

    private void updateCustomerCount() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            int count = AppDatabase.getInstance(this).customerDao().getAllCustomers(bId).size();
            runOnUiThread(() -> {
                if (textDashboardCustomerCount != null) {
                    textDashboardCustomerCount.setText(String.valueOf(count));
                }
                if (textSidebarCustomerCount != null) {
                    textSidebarCustomerCount.setText(String.valueOf(count));
                }
            });
        });
    }

    private void updateInventoryCount() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            int count = AppDatabase.getInstance(this).itemDao().getAllItems(bId).size();
            runOnUiThread(() -> {
                if (textSidebarInventoryCount != null) {
                    textSidebarInventoryCount.setText(String.valueOf(count));
                }
            });
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        
        // Google Sign Out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            PreferenceManager preferenceManager = new PreferenceManager(ReportsActivity.this);
            // We don't clear everything, just enough to ensure session is gone
            // User requested "Clear local session"
            preferenceManager.setBusinessSetupCompleted(false);
            
            Intent intent = new Intent(ReportsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
                    if (textCounterSubtotal != null) textCounterSubtotal.setText(String.format(Locale.getDefault(), "%,.2f", subtotal));

                    if (layoutBreakdownContainer != null) {
                        layoutBreakdownContainer.removeAllViews();
                        layoutBreakdownContainer.setVisibility(View.GONE);
                    }

                    double totalAdditions = 0;

                    // 1. Tax
                    if (selectedTax != null) {
                        double taxAmount = subtotal * (selectedTax.getValue() / 100.0);
                        totalAdditions += taxAmount;
                        String desc = String.format(Locale.getDefault(), "%s - (%.0f%% of ₹%,.0f)", selectedTax.getName(), selectedTax.getValue(), subtotal);
                        addBreakdownRow(desc, taxAmount, false);
                        if (btnAddTax != null) btnAddTax.setText(String.format(Locale.getDefault(), "Tax: %s(%.0f%%)", selectedTax.getName(), selectedTax.getValue()));
                    } else {
                        if (btnAddTax != null) btnAddTax.setText("Add Tax");
                    }

                    // 2. Discount
                    double discountAmount = 0;
                    if (selectedDiscount != null) {
                        discountAmount = selectedDiscount.isPercentage() ? (subtotal * selectedDiscount.getValue() / 100.0) : selectedDiscount.getValue();
                        totalAdditions -= discountAmount;
                        String symbol = selectedDiscount.isPercentage() ? "%" : "₹";
                        String desc = String.format(Locale.getDefault(), "%s - (%s%.0f)", selectedDiscount.getName(), symbol, selectedDiscount.getValue());
                        addBreakdownRow(desc, -discountAmount, false);
                        if (btnAddDiscount != null) btnAddDiscount.setText(String.format(Locale.getDefault(), "Disc: %s(%s%.0f)", selectedDiscount.getName(), symbol, selectedDiscount.getValue()));
                    } else {
                        if (btnAddDiscount != null) btnAddDiscount.setText("Add Discount");
                    }

                    // 3. Extra Charges
                    StringBuilder chargesSummary = new StringBuilder();
                    if (selectedDeliveryCharge != null) {
                        double val = selectedDeliveryCharge.isPercentage() ? (subtotal * selectedDeliveryCharge.getValue() / 100.0) : selectedDeliveryCharge.getValue();
                        totalAdditions += val;
                        addBreakdownRow("Delivery Charge", val, false);
                        chargesSummary.append("Delivery ");
                    }
                    if (selectedPackingCharge != null) {
                        double val = selectedPackingCharge.isPercentage() ? (subtotal * selectedPackingCharge.getValue() / 100.0) : selectedPackingCharge.getValue();
                        totalAdditions += val;
                        addBreakdownRow("Packing Charge", val, false);
                        chargesSummary.append("Packing ");
                    }
                    if (selectedServiceCharge != null) {
                        double val = selectedServiceCharge.isPercentage() ? (subtotal * selectedServiceCharge.getValue() / 100.0) : selectedServiceCharge.getValue();
                        totalAdditions += val;
                        addBreakdownRow("Service Charge", val, false);
                        chargesSummary.append("Service ");
                    }
                    if (selectedOtherCharge != null) {
                        double val = selectedOtherCharge.isPercentage() ? (subtotal * selectedOtherCharge.getValue() / 100.0) : selectedOtherCharge.getValue();
                        totalAdditions += val;
                        addBreakdownRow(selectedOtherCharge.getName(), val, false);
                        chargesSummary.append("Other ");
                    }

                    if (btnAddOtherCharges != null) {
                        btnAddOtherCharges.setText(!chargesSummary.toString().isEmpty() ? chargesSummary.toString().trim() : "Add Other Charges");
                    }

                    double intermediateTotal = subtotal + totalAdditions;
                    double finalGrandTotal = intermediateTotal;

                    // 4. Round Off
                    if (isRoundOffEnabled) {
                        double rounded = Math.round(intermediateTotal);
                        double diff = rounded - intermediateTotal;
                        if (Math.abs(diff) > 0.001) {
                            addBreakdownRow("Round Off (Click here to Remove)", diff, true);
                            finalGrandTotal = rounded;
                        }
                    }

                    if (textCounterGrandTotal != null) textCounterGrandTotal.setText(String.format(Locale.getDefault(), "₹%,.0f", finalGrandTotal));
                    if (btnCharge != null) {
                        btnCharge.setVisibility(View.VISIBLE);
                        btnCharge.setText(String.format(Locale.getDefault(), "Charge: ₹%,.0f", finalGrandTotal));
                        btnCharge.setOnClickListener(v -> showCheckoutDialog());
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
                    if (btnAddTax != null) btnAddTax.setText("Add Tax");
                    if (btnAddDiscount != null) btnAddDiscount.setText("Add Discount");
                    if (btnAddOtherCharges != null) btnAddOtherCharges.setText("Add Other Charges");
                }
            }
        });
    }

    private void addBreakdownRow(String label, double value, boolean isRoundOff) {
        if (layoutBreakdownContainer == null) return;
        layoutBreakdownContainer.setVisibility(View.VISIBLE);

        RelativeLayout row = new RelativeLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        row.setLayoutParams(params);

        TextView textLabel = new TextView(this);
        textLabel.setText(label);
        textLabel.setTextColor(isRoundOff ? 0xFF94A3B8 : 0xFF64748B);
        textLabel.setTextSize(12);
        RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        
        if (isRoundOff) {
            row.setOnClickListener(v -> {
                isRoundOffEnabled = false;
                updateCounterUI();
            });
        }

        TextView textValue = new TextView(this);
        String prefix = value >= 0 ? "" : "-";
        textValue.setText(String.format(Locale.getDefault(), "%s%.2f", prefix, Math.abs(value)));
        textValue.setTextColor(0xFF1E293B);
        textValue.setTextSize(12);
        RelativeLayout.LayoutParams valueParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.addRule(RelativeLayout.ALIGN_PARENT_END);

        row.addView(textLabel, labelParams);
        row.addView(textValue, valueParams);
        layoutBreakdownContainer.addView(row);
    }

    private void loadCounterItems() {
        if (recyclerCounterItems != null) {
            recyclerCounterItems.setAdapter(new CounterAdapter(CartManager.getInstance().getCartItems(), cartItem -> {
                if (cartItem.getItem().isAdvanceMode()) {
                    showVariantSelectorDialog(cartItem.getItem());
                } else {
                    showEditQuantityDialog(cartItem);
                }
            }));
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
        
        Executors.newSingleThreadExecutor().execute(() -> {
            CartManager.getInstance().refreshItems(AppDatabase.getInstance(this));
            runOnUiThread(() -> {
                if (currentTab == TAB_COUNTER) updateCounterUI();
                if (currentTab == TAB_ITEMS) refreshItemsView();
            });
        });
    }

    private void loadBusinessData() {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Ensure PreferenceManager is synced with the selected business in Room DB
        Executors.newSingleThreadExecutor().execute(() -> {
            Business selected = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            if (selected != null) {
                PreferenceManager pm = new PreferenceManager(this);
                pm.setBusinessName(selected.getName());
                pm.setBusinessMobile(selected.getPhoneNumber());
                pm.setBusinessEmail(selected.getEmail());
                pm.setBusinessRole(selected.getRole());
                
                runOnUiThread(this::refreshProfileUI);
            }
        });

        // First, load from local cache for immediate UI update
        refreshProfileUI();
        updateCustomerCount();
        updateInventoryCount();
        syncCloudData();

        // Removed automatic Firestore pull that was reverting local business switches
        /*
        new BusinessProfileRepository(this).loadBusinessProfile(new BusinessProfileRepository.ProfileCallback() {
            @Override
            public void onProfileLoaded(BusinessProfile profile) {
                android.util.Log.d("SETUP", "BUSINESS_PROFILE_LOADED");
                android.util.Log.d("SETUP", "BUSINESS_NAME = " + profile.getBusinessName());
                runOnUiThread(() -> {
                    refreshProfileUI();
                    syncProfileToLocalBusinessTable(profile);
                });
            }

            @Override
            public void onError(String message) {
                Log.e("ReportsActivity", "PROFILE_LOAD_FAILED: " + message);
            }
        });
        */

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Business selected = db.businessDao().getSelectedBusiness();
            if (selected != null) {
                ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(selected.getId());
                
                runOnUiThread(() -> {
                    if (settings != null) {
                        if (textHeaderPhoneNumber != null) textHeaderPhoneNumber.setText(settings.getPhoneNumber());
                        
                        if (imgLogo != null) {
                            if (settings.getBusinessLogoPath() != null && !settings.getBusinessLogoPath().isEmpty()) {
                                try {
                                    imgLogo.setImageURI(android.net.Uri.parse(settings.getBusinessLogoPath()));
                                    imgLogo.setImageTintList(null);
                                } catch (Exception ignored) {
                                    setDefaultLogo();
                                }
                            } else {
                                setDefaultLogo();
                            }
                        }
                    }
                });
            }
        });
    }

    private void refreshProfileUI() {
        PreferenceManager pm = new PreferenceManager(this);
        String name = pm.getBusinessName();
        String email = pm.getBusinessEmail();
        String mobile = pm.getBusinessMobile();
        String role = pm.getBusinessRole();

        if (textHeaderBusinessName != null) textHeaderBusinessName.setText(name != null ? name : "");
        
        if (txtOwnerName != null) {
            String suffix = (role != null && !role.isEmpty()) ? " (" + role.toLowerCase() + ")" : " " + getString(R.string.header_owner_suffix);
            txtOwnerName.setText((name != null ? name : "") + suffix);
        }

        if (toolbar != null && currentTab == TAB_TODAY) {
            toolbar.setTitle(getString(R.string.reports_tab_today));
        }
        if (toolbar != null && currentTab == TAB_REPORTS) {
            toolbar.setTitle(getString(R.string.reports_title));
        }

        if (txtOwnerEmail != null) txtOwnerEmail.setText(email != null ? email : "");
        if (textHeaderPhoneNumber != null) textHeaderPhoneNumber.setText(mobile != null ? mobile : "");

        // Also refresh logo if we have it in settings or pm
        Executors.newSingleThreadExecutor().execute(() -> {
            Business active = AppDatabase.getInstance(this).businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : 1;
            ReceiptSettings settings = AppDatabase.getInstance(this).receiptSettingsDao().getSettingsByBusiness(bId);
            runOnUiThread(() -> {
                if (settings != null && imgLogo != null) {
                    if (settings.getBusinessLogoPath() != null && !settings.getBusinessLogoPath().isEmpty()) {
                        try {
                            imgLogo.setImageURI(android.net.Uri.parse(settings.getBusinessLogoPath()));
                            imgLogo.setImageTintList(null);
                        } catch (Exception ignored) {
                            setDefaultLogo();
                        }
                    } else {
                        setDefaultLogo();
                    }
                }
            });
        });
    }

    private void setDefaultLogo() {
        if (imgLogo != null) {
            imgLogo.setImageResource(R.drawable.ic_nav_reports);
            imgLogo.setImageTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.reports_tab_selected)));
        }
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
        languages.add(new Language("English", "ENGLISH", "en"));
        languages.add(new Language("Arabic", "عربي", "ar"));
        languages.add(new Language("French", "FRANÇAISE", "fr"));
        languages.add(new Language("Spanish", "ESPAÑOLA", "es"));
        languages.add(new Language("Hindi", "हिंदी", "hi"));
        languages.add(new Language("Bengali", "বাঙালি", "bn"));
        languages.add(new Language("Filipino", "FILIPINO", "fil"));
        languages.add(new Language("Malay", "MELAYU", "ms"));
        languages.add(new Language("Portuguese", "PORTUGUESA", "pt"));
        languages.add(new Language("Russian", "РУССКИЙ", "ru"));
        languages.add(new Language("Kannada", "ಕನ್ನಡ", "kn"));
        languages.add(new Language("Telugu", "తెలుగు", "te"));
        languages.add(new Language("Tamil", "தமிழ்", "ta"));
        languages.add(new Language("Marathi", "मরাठी", "mr"));
        languages.add(new Language("Japanese", "日本語", "ja"));
        languages.add(new Language("Chinese", "中文", "zh"));
        languages.add(new Language("German", "DEUTSCHE", "de"));
        languages.add(new Language("Indonesian", "BAHASA INDONESIA", "in"));
        languages.add(new Language("Hebrew", "עברית", "iw"));
        languages.add(new Language("Swahili", "KISWAHILI", "sw"));
        languages.add(new Language("Turkish", "TÜRKÇE", "tr"));
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
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        }
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerBusinesses);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Add slide-up animation for the grid items
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up);
        recyclerView.startAnimation(slideUp);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Business> businesses = AppDatabase.getInstance(this).businessDao().getAllBusinesses();
            runOnUiThread(() -> {
                BusinessAdapter adapter = new BusinessAdapter(businesses, business -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        // Perform the switch
                        AppDatabase db = AppDatabase.getInstance(this);
                        db.businessDao().deselectAll();
                        db.businessDao().selectBusiness(business.getId());
                        
                        // Sync with ReceiptSettings for THIS specific business
                        ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(business.getId());
                        if (settings == null) {
                            settings = new ReceiptSettings();
                            settings.setId(business.getId());
                        }
                        settings.setBusinessName(business.getName());
                        settings.setPhoneNumber(business.getPhoneNumber());
                        settings.setEmail(business.getEmail());
                        settings.setBusinessLogoPath(business.getLogoPath());
                        db.receiptSettingsDao().insert(settings);
                        
                        // Sync with PreferenceManager for immediate UI update in refreshProfileUI()
                        PreferenceManager pm = new PreferenceManager(ReportsActivity.this);
                        pm.setBusinessName(business.getName());
                        pm.setBusinessEmail(business.getEmail());
                        pm.setBusinessMobile(business.getPhoneNumber());
                        pm.setBusinessRole(business.getRole());

                        runOnUiThread(() -> {
                            dialog.dismiss();
                            loadBusinessData();
                            Toast.makeText(this, "Switched to " + business.getName(), Toast.LENGTH_SHORT).show();
                        });
                    });
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
        int activeBlue = Color.parseColor("#1E40AF");

        for (int i = 0; i < bottomTabs.length; i++) {
            boolean isSelected = i == selectedTab;
            if (isSelected) {
                bottomTabs[i].setBackgroundResource(R.drawable.bg_reports_nav_item);
                bottomTabs[i].getBackground().setTint(activeBlue);
                bottomTabIcons[i].setColorFilter(selectedColor);
                bottomTabLabels[i].setTextColor(selectedColor);
                bottomTabLabels[i].setTypeface(null, Typeface.BOLD);
            } else {
                bottomTabs[i].setBackgroundResource(0);
                bottomTabIcons[i].setColorFilter(unselectedColor);
                bottomTabLabels[i].setTextColor(unselectedColor);
                bottomTabLabels[i].setTypeface(null, Typeface.NORMAL);
            }
        }

        // Add a smooth transition when switching tabs
        android.transition.TransitionManager.beginDelayedTransition(findViewById(R.id.reportsRoot));

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
            int bId = getActiveBusinessId();
            List<Item> dbItems = AppDatabase.getInstance(this).itemDao().getAllItems(bId);
            runOnUiThread(() -> {
                if (recyclerItemList != null) {
                    ProductListAdapter adapter = new ProductListAdapter(dbItems);
                    adapter.setListener(new ProductListAdapter.OnProductActionListener() {
                        @Override
                        public void onPlusClick(Item item, int position) {
                            if (item.isAdvanceMode()) {
                                showVariantSelectorDialog(item);
                            } else {
                                // Simple mode handled in adapter, but we can centralize
                                if (CartManager.getInstance().addItem(item)) {
                                    adapter.notifyItemChanged(position);
                                    updateCounterUI();
                                }
                            }
                        }

                        @Override
                        public void onMinusClick(Item item, int position) {
                            if (item.isAdvanceMode()) {
                                showVariantSelectorDialog(item);
                            }
                        }
                    });
                    recyclerItemList.setAdapter(adapter);
                }
            });
        });
    }

    private void loadItemGrid() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<Item> dbItems;
            if (itemTileStyle == 1) dbItems = AppDatabase.getInstance(this).itemDao().getUncategorizedItems(bId);
            else dbItems = AppDatabase.getInstance(this).itemDao().getAllItems(bId);
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

        view.findViewById(R.id.btnCloseSheet).setOnClickListener(v -> bottomSheet.dismiss());

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
                    // Update the "blue line" variant name on the item tile immediately
                    item.setVariantName(variant.getName());
                    item.setSellingPrice(variant.getSellingPrice());
                    
                    if (!CartManager.getInstance().setVariantQuantity(item, variant, quantity)) {
                        Toast.makeText(this, "Maximum stock reached: " + variant.getStockQuantity(), Toast.LENGTH_SHORT).show();
                    }
                    
                    // Refresh the grid to show updated "blue line" and quantity overlay
                    if (recyclerItemGrid != null && recyclerItemGrid.getAdapter() != null) {
                        recyclerItemGrid.getAdapter().notifyDataSetChanged();
                    }
                });
                rv.setAdapter(adapter);
            });
        });

        bottomSheet.show();
    }

    private void loadItemCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<Category> dbCategories = AppDatabase.getInstance(this).categoryDao().getAllCategories(bId);
            ItemDao itemDao = AppDatabase.getInstance(this).itemDao();
            List<ItemCategoryAdapter.CategoryWithCount> list = new ArrayList<>();
            for (Category cat : dbCategories) {
                if (cat.getName() != null && (cat.getName().equalsIgnoreCase("Uncategorized") || cat.getName().equalsIgnoreCase("No Category") || cat.getName().isEmpty())) {
                    continue;
                }
                int count = itemDao.getItemCountByCategory(cat.getName(), bId);
                list.add(new ItemCategoryAdapter.CategoryWithCount(cat.getName(), count));
            }
            runOnUiThread(() -> {
                if (recyclerItemCategories != null) {
                    recyclerItemCategories.setAdapter(new ItemCategoryAdapter(list, bId, item -> {
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

    private void showCheckoutDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_checkout_bottom_sheet, null);
        bottomSheet.setContentView(view);

        EditText editMobile = view.findViewById(R.id.editCustomerMobile);
        EditText editName = view.findViewById(R.id.editCustomerName);
        EditText editEmail = view.findViewById(R.id.editCustomerEmail);
        EditText editAddress = view.findViewById(R.id.editCustomerAddress);
        View btnExpand = view.findViewById(R.id.btnExpandCustomer);
        ViewGroup layoutFields = view.findViewById(R.id.layoutCustomerFields);
        
        // Initial state
        editEmail.setVisibility(View.GONE);
        editAddress.setVisibility(View.GONE);
        
        btnExpand.setOnClickListener(v -> {
            android.transition.TransitionManager.beginDelayedTransition(layoutFields);
            boolean isVisible = editEmail.getVisibility() == View.VISIBLE;
            editEmail.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            editAddress.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            ((ImageView)btnExpand).setImageResource(isVisible ? android.R.drawable.arrow_down_float : android.R.drawable.arrow_up_float);
        });

        view.findViewById(R.id.btnSearchCustomer).setOnClickListener(v -> {
            String mobile = editMobile.getText().toString().trim();
            if (mobile.isEmpty()) return;
            
            Executors.newSingleThreadExecutor().execute(() -> {
                int bId = getActiveBusinessId();
                Customer customer = AppDatabase.getInstance(this).customerDao().getCustomerByMobile(mobile, bId);
                runOnUiThread(() -> {
                    if (customer != null) {
                        editName.setText(customer.getName());
                        editEmail.setText(customer.getEmail());
                        editAddress.setText(customer.getAddress());
                        // Automatically expand if hidden
                        if (editEmail.getVisibility() == View.GONE) {
                            editEmail.setVisibility(View.VISIBLE);
                            editAddress.setVisibility(View.VISIBLE);
                            ((ImageView)btnExpand).setImageResource(android.R.drawable.arrow_up_float);
                        }
                    } else {
                        Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        RecyclerView rv = view.findViewById(R.id.recyclerPaymentModes);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<PaymentMode> modes = AppDatabase.getInstance(this).paymentModeDao().getAllPaymentModes(bId);
            if (modes.isEmpty()) {
                PaymentMode p1 = new PaymentMode("Cash", true, false); p1.setBusinessId(bId);
                PaymentMode p2 = new PaymentMode("Debit Card", true, false); p2.setBusinessId(bId);
                PaymentMode p3 = new PaymentMode("Credit Card", true, false); p3.setBusinessId(bId);
                PaymentMode p4 = new PaymentMode("Credit", true, false); p4.setBusinessId(bId);
                PaymentMode p5 = new PaymentMode("UPI", true, true); p5.setBusinessId(bId);
                PaymentMode p6 = new PaymentMode("Store Credit", true, false); p6.setBusinessId(bId);
                PaymentMode p7 = new PaymentMode("Online", true, false); p7.setBusinessId(bId);
                PaymentMode p8 = new PaymentMode("Sample Rate", true, false); p8.setBusinessId(bId);
                PaymentMode p9 = new PaymentMode("Exchange", true, false); p9.setBusinessId(bId);
                PaymentMode p10 = new PaymentMode("Google Pay", true, false); p10.setBusinessId(bId);
                
                AppDatabase.getInstance(this).paymentModeDao().insert(p1);
                AppDatabase.getInstance(this).paymentModeDao().insert(p2);
                AppDatabase.getInstance(this).paymentModeDao().insert(p3);
                AppDatabase.getInstance(this).paymentModeDao().insert(p4);
                AppDatabase.getInstance(this).paymentModeDao().insert(p5);
                AppDatabase.getInstance(this).paymentModeDao().insert(p6);
                AppDatabase.getInstance(this).paymentModeDao().insert(p7);
                AppDatabase.getInstance(this).paymentModeDao().insert(p8);
                AppDatabase.getInstance(this).paymentModeDao().insert(p9);
                AppDatabase.getInstance(this).paymentModeDao().insert(p10);
                modes = AppDatabase.getInstance(this).paymentModeDao().getAllPaymentModes(bId);
            }

            final List<PaymentMode> finalModes = modes;
            runOnUiThread(() -> {
                CheckoutPaymentAdapter adapter = new CheckoutPaymentAdapter(finalModes, new CheckoutPaymentAdapter.OnPaymentModeClickListener() {
                    @Override
                    public void onPaymentClick(PaymentMode mode) {
                        String custName = editName.getText().toString().trim();
                        double subtotal = CartManager.getInstance().getSubtotal();
                        double tax = selectedTax != null ? subtotal * (selectedTax.getValue() / 100.0) : 0;
                        double disc = 0;
                        if (selectedDiscount != null) {
                            disc = selectedDiscount.isPercentage() ? (subtotal * selectedDiscount.getValue() / 100.0) : selectedDiscount.getValue();
                        }
                        double extra = 0;
                        if (selectedDeliveryCharge != null) extra += selectedDeliveryCharge.isPercentage() ? (subtotal * selectedDeliveryCharge.getValue() / 100.0) : selectedDeliveryCharge.getValue();
                        if (selectedPackingCharge != null) extra += selectedPackingCharge.isPercentage() ? (subtotal * selectedPackingCharge.getValue() / 100.0) : selectedPackingCharge.getValue();
                        if (selectedServiceCharge != null) extra += selectedServiceCharge.isPercentage() ? (subtotal * selectedServiceCharge.getValue() / 100.0) : selectedServiceCharge.getValue();
                        if (selectedOtherCharge != null) extra += selectedOtherCharge.isPercentage() ? (subtotal * selectedOtherCharge.getValue() / 100.0) : selectedOtherCharge.getValue();
                        
                        double finalTotal = subtotal + tax - disc + extra;
                        if (isRoundOffEnabled) finalTotal = Math.round(finalTotal);

                        final double totalToSave = finalTotal;
                        final int itemsToSave = CartManager.getInstance().getItemCount();

                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase db = AppDatabase.getInstance(ReportsActivity.this);
                            Business activeBus = db.businessDao().getSelectedBusiness();
                            int bId = activeBus != null ? activeBus.getId() : 0;

                            ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(bId);
                            if (settings == null) {
                                settings = new ReceiptSettings();
                                settings.setId(bId);
                                if (activeBus != null) {
                                    settings.setBusinessName(activeBus.getName());
                                    settings.setPhoneNumber(activeBus.getPhoneNumber());
                                }
                                db.receiptSettingsDao().insert(settings);
                            }
                            
                            int billNo = settings.getCurrentBillNo() + 1;
                            settings.setCurrentBillNo(billNo);
                            db.receiptSettingsDao().update(settings);

                            String rNo = (settings.getReceiptIdPrefix() != null ? settings.getReceiptIdPrefix() : "NC") + "-" + billNo;
                            Receipt receipt = new Receipt(rNo, custName, mode.getName(), totalToSave, itemsToSave, System.currentTimeMillis(), bId);
                            long insertedId = db.receiptDao().insert(receipt);

                            // Save individual items
                            List<CartItem> cartItems = CartManager.getInstance().getCartItems();
                            List<ReceiptItem> receiptItems = new ArrayList<>();
                            for (CartItem ci : cartItems) {
                                String vName = ci.getVariant() != null ? ci.getVariant().getName() : "";
                                receiptItems.add(new ReceiptItem((int)insertedId, ci.getItem().getName(), vName, 
                                        ci.getVariant() != null ? ci.getVariant().getSellingPrice() : ci.getItem().getSellingPrice(), 
                                        ci.getQuantity()));
                            }
                            db.receiptItemDao().insertAll(receiptItems);

                            runOnUiThread(() -> {
                                Toast.makeText(ReportsActivity.this, "Payment: " + mode.getName() + " - Saved as " + rNo, Toast.LENGTH_SHORT).show();
                                bottomSheet.dismiss();
                                CartManager.getInstance().clearCart();
                                highlightBottomTab(TAB_REPORTS);
                                
                                // Open Details
                                Intent intent = new Intent(ReportsActivity.this, ReceiptDetailsActivity.class);
                                intent.putExtra("receipt_id", (int) insertedId);
                                startActivity(intent);
                            });
                        });
                    }

                    @Override
                    public void onAddNewClick() {
                        bottomSheet.dismiss();
                        startActivity(new Intent(ReportsActivity.this, PaymentSettingsActivity.class));
                    }
                });
                rv.setAdapter(adapter);
            });
        });

        bottomSheet.show();
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

        TextView btnAddNew = view.findViewById(R.id.btnAddNewOther);
        btnAddNew.setText("ADD NEW DELIVERY FEE");
        
        EditText editValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView rv = view.findViewById(R.id.recyclerOthers);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedDeliveryCharge != null) {
            editValue.setText(String.format(Locale.getDefault(), "%.1f", selectedDeliveryCharge.getValue()));
            checkPercentage.setChecked(selectedDeliveryCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<DeliveryFee> list = AppDatabase.getInstance(this).deliveryFeeDao().getAllDeliveryFees(bId);
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

        view.findViewById(R.id.btnAddNewOther).setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(this, DeliveryFeeSettingsActivity.class));
        });

        bottomSheet.show();
    }

    private void showSelectPackingDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);
        
        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT PACKING CHARGE");

        TextView btnAddNew = view.findViewById(R.id.btnAddNewOther);
        btnAddNew.setText("ADD NEW PACKING FEE");

        EditText editValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView rv = view.findViewById(R.id.recyclerOthers);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedPackingCharge != null) {
            editValue.setText(String.format(Locale.getDefault(), "%.1f", selectedPackingCharge.getValue()));
            checkPercentage.setChecked(selectedPackingCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<PackingFee> list = AppDatabase.getInstance(this).packingFeeDao().getAllPackingFees(bId);
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

        view.findViewById(R.id.btnAddNewOther).setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(this, PackingFeeSettingsActivity.class));
        });

        bottomSheet.show();
    }

    private void showSelectServiceDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);
        
        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT SERVICE CHARGE");

        TextView btnAddNew = view.findViewById(R.id.btnAddNewOther);
        btnAddNew.setText("ADD NEW SERVICE FEE");

        EditText editValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView rv = view.findViewById(R.id.recyclerOthers);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedServiceCharge != null) {
            editValue.setText(String.format(Locale.getDefault(), "%.1f", selectedServiceCharge.getValue()));
            checkPercentage.setChecked(selectedServiceCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<ServiceFee> list = AppDatabase.getInstance(this).serviceFeeDao().getAllServiceFees(bId);
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

        view.findViewById(R.id.btnAddNewOther).setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(this, ServiceFeeSettingsActivity.class));
        });

        bottomSheet.show();
    }

    private void showSelectOtherDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_other_charge_dialog, null);
        bottomSheet.setContentView(view);

        TextView title = view.findViewById(R.id.textChargeTitle);
        title.setText("SELECT OTHER CHARGE");

        TextView btnAddNew = view.findViewById(R.id.btnAddNewOther);
        btnAddNew.setText("ADD NEW OTHER FEE");

        EditText editOtherValue = view.findViewById(R.id.editOtherValue);
        CheckBox checkPercentage = view.findViewById(R.id.checkPercentage);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerOthers);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (selectedOtherCharge != null) {
            editOtherValue.setText(String.format(Locale.getDefault(), "%.1f", selectedOtherCharge.getValue()));
            checkPercentage.setChecked(selectedOtherCharge.isPercentage());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = getActiveBusinessId();
            List<OtherFee> others = AppDatabase.getInstance(this).otherFeeDao().getAllOtherFees(bId);
            if (others.isEmpty()) {
                OtherFee f1 = new OtherFee("PREVIOUS DUES", 100, false, false); f1.setBusinessId(bId);
                OtherFee f2 = new OtherFee("DELIVERY CHARGES", 0, false, false); f2.setBusinessId(bId);
                OtherFee f3 = new OtherFee("BUS CHARGE", 100, false, false); f3.setBusinessId(bId);
                OtherFee f4 = new OtherFee("TRANSPORTATION", 0, false, false); f4.setBusinessId(bId);
                
                AppDatabase.getInstance(this).otherFeeDao().insert(f1);
                AppDatabase.getInstance(this).otherFeeDao().insert(f2);
                AppDatabase.getInstance(this).otherFeeDao().insert(f3);
                AppDatabase.getInstance(this).otherFeeDao().insert(f4);
                others = AppDatabase.getInstance(this).otherFeeDao().getAllOtherFees(bId);
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
            int bId = getActiveBusinessId();
            List<Discount> discounts = AppDatabase.getInstance(this).discountDao().getAllDiscounts(bId);
            if (discounts.isEmpty()) {
                Discount d1 = new Discount("DUES", 40, false, false); d1.setBusinessId(bId);
                Discount d2 = new Discount("PREVIOUS", 250, false, false); d2.setBusinessId(bId);
                Discount d3 = new Discount("ONLINE", 780, false, false); d3.setBusinessId(bId);
                Discount d4 = new Discount("RETURN GRIPPER", 250, false, false); d4.setBusinessId(bId);
                
                AppDatabase.getInstance(this).discountDao().insert(d1);
                AppDatabase.getInstance(this).discountDao().insert(d2);
                AppDatabase.getInstance(this).discountDao().insert(d3);
                AppDatabase.getInstance(this).discountDao().insert(d4);
                discounts = AppDatabase.getInstance(this).discountDao().getAllDiscounts(bId);
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
            int bId = getActiveBusinessId();
            List<Tax> taxes = AppDatabase.getInstance(this).taxDao().getAllTaxes(bId);
            if (taxes.isEmpty()) {
                Tax t1 = new Tax("CGST on sales", 9.0, false); t1.setBusinessId(bId);
                Tax t2 = new Tax("SGST+CGST", 18.0, false); t2.setBusinessId(bId);
                Tax t3 = new Tax("SGST on sales", 9.0, false); t3.setBusinessId(bId);
                Tax t4 = new Tax("TRANSPORTATION", 50.0, false); t4.setBusinessId(bId);
                
                AppDatabase.getInstance(this).taxDao().insert(t1);
                AppDatabase.getInstance(this).taxDao().insert(t2);
                AppDatabase.getInstance(this).taxDao().insert(t3);
                AppDatabase.getInstance(this).taxDao().insert(t4);
                taxes = AppDatabase.getInstance(this).taxDao().getAllTaxes(bId);
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
