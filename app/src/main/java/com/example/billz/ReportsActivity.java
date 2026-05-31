package com.example.billz;

import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    private View emptyStateContainer;
    private View reportsContentPlaceholder;
    private View moreContentContainer;
    private View dateSelectorRow;
    private LinearLayout bottomNavigationView;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private View[] bottomTabs;
    private ImageView[] bottomTabIcons;
    private TextView[] bottomTabLabels;
    private TextView textHeaderBusinessName, txtOwnerEmail;
    private TextView textRoleBadge, textStatusBadge, textPlanBadge, txtUpgradeCTA;
    private View cardRoleBadge, cardStatusBadge, cardPlanBadge, viewStatusDot;
    private ImageView imgLogo, imgPlanIcon;

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
        dateSelectorRow = findViewById(R.id.dateSelectorRow);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbarReports);

        textHeaderBusinessName = findViewById(R.id.textHeaderBusinessName);
        txtOwnerEmail = findViewById(R.id.txtOwnerEmail);
        imgLogo = findViewById(R.id.imgLogo);

        textRoleBadge = findViewById(R.id.textRoleBadge);
        textStatusBadge = findViewById(R.id.textStatusBadge);
        textPlanBadge = findViewById(R.id.textPlanBadge);
        cardRoleBadge = findViewById(R.id.cardRoleBadge);
        cardStatusBadge = findViewById(R.id.cardStatusBadge);
        cardPlanBadge = findViewById(R.id.cardPlanBadge);
        viewStatusDot = findViewById(R.id.viewStatusDot);
        imgPlanIcon = findViewById(R.id.imgPlanIcon);
        txtUpgradeCTA = findViewById(R.id.txtUpgradeCTA);

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
            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    systemBars.top + getResources().getDimensionPixelSize(R.dimen.reports_screen_padding_top),
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
            );
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

        findViewById(R.id.nav_edit_profile_item).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.nav_switch_business).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showBusinessSelectorDialog();
        });

        findViewById(R.id.nav_create_business).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, AddBusinessActivity.class));
        });

        findViewById(R.id.nav_subscription).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showSubscriptionUsageDialog();
        });

        findViewById(R.id.nav_billing_history).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Billing history coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_help_chat).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Help Chat coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_feedback).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Feedback coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_refer).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Refer App coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_buy_printer).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Buy Printer coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            finish();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBusinessData();
    }

    private void loadBusinessData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ReceiptSettings settings = AppDatabase.getInstance(this).receiptSettingsDao().getSettings();
            runOnUiThread(() -> {
                if (settings != null) {
                    if (textHeaderBusinessName != null) {
                        textHeaderBusinessName.setText(settings.getBusinessName());
                    }
                    if (txtOwnerEmail != null) {
                        txtOwnerEmail.setText(settings.getEmail() != null ? settings.getEmail() : "nutritioncompany.com@gmail.com");
                    }
                    updateBadges(settings);
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

    private void showSubscriptionUsageDialog() {
        Subscription sub = SubscriptionManager.getInstance(this).getActiveSubscription();
        if (sub == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Subscription & Usage");
        
        UsageManager usage = UsageManager.getInstance(this);
        String msg = "Plan: " + sub.getPlanType() + "\n" +
                     "Status: " + sub.getStatus() + "\n\n" +
                     "Usage:\n" +
                     "Customers: " + usage.getCustomerCount() + " / 100\n" +
                     "Products: " + usage.getProductCount() + " / 100\n" +
                     "Staff: " + usage.getStaffCount() + " / 1";
        
        builder.setMessage(msg);
        builder.setPositiveButton("Upgrade", (dialog, which) -> {
            SubscriptionManager.getInstance(this).upgradeToPremium();
            Toast.makeText(this, "Upgraded to Premium!", Toast.LENGTH_SHORT).show();
            recreate();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void updateBadges(ReceiptSettings settings) {
        if (settings == null) return;

        // Apply fade-in animation to badge container
        View badgeContainer = findViewById(R.id.cardRoleBadge).getParent() instanceof View ? (View) findViewById(R.id.cardRoleBadge).getParent() : null;
        if (badgeContainer != null) {
            badgeContainer.setAlpha(0f);
            badgeContainer.animate().alpha(1f).setDuration(200).start();
        }
        cardPlanBadge.setAlpha(0f);
        cardPlanBadge.animate().alpha(1f).setDuration(200).start();

        // Role Badge
        String role = settings.getRole() != null ? settings.getRole() : "OWNER";
        textRoleBadge.setText(role);
        if ("OWNER".equalsIgnoreCase(role)) {
            cardRoleBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            textRoleBadge.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("MANAGER".equalsIgnoreCase(role)) {
            cardRoleBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
            textRoleBadge.setTextColor(Color.parseColor("#1565C0"));
        } else {
            cardRoleBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
            textRoleBadge.setTextColor(Color.parseColor("#EF6C00"));
        }

        // Status Badge
        String status = settings.getStatus() != null ? settings.getStatus() : "ACTIVE";
        textStatusBadge.setText(status);
        if ("ACTIVE".equalsIgnoreCase(status)) {
            cardStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            textStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            viewStatusDot.setBackgroundResource(R.drawable.bg_active_dot);
        } else if ("EXPIRED".equalsIgnoreCase(status)) {
            cardStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
            textStatusBadge.setTextColor(Color.parseColor("#EF6C00"));
            viewStatusDot.setBackgroundResource(R.drawable.bg_expired_dot);
        } else {
            cardStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            textStatusBadge.setTextColor(Color.parseColor("#C62828"));
            viewStatusDot.setBackgroundResource(R.drawable.bg_suspended_dot);
        }

        // Plan Badge
        String plan = settings.getPlanType() != null ? settings.getPlanType() : "FREE";
        if ("PREMIUM".equalsIgnoreCase(plan)) {
            textPlanBadge.setText(getString(R.string.plan_premium_label));
            cardPlanBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF8E1")));
            textPlanBadge.setTextColor(Color.parseColor("#F9A825"));
            imgPlanIcon.setImageResource(android.R.drawable.btn_star_big_on);
            imgPlanIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F9A825")));
        } else if ("ENTERPRISE".equalsIgnoreCase(plan)) {
            textPlanBadge.setText(getString(R.string.plan_enterprise_label));
            cardPlanBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EDE7F6")));
            textPlanBadge.setTextColor(Color.parseColor("#512DA8"));
            imgPlanIcon.setImageResource(android.R.drawable.ic_menu_help); // Placeholder for diamond
            imgPlanIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#512DA8")));
        } else {
            textPlanBadge.setText(getString(R.string.plan_free_label));
            cardPlanBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            textPlanBadge.setTextColor(Color.parseColor("#616161"));
            imgPlanIcon.setVisibility(View.GONE);
            txtUpgradeCTA.setVisibility(View.VISIBLE);
        }

        if (!"FREE".equalsIgnoreCase(plan)) {
            txtUpgradeCTA.setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports_top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share || itemId == R.id.action_messages) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        emptyStateContainer.setVisibility(View.GONE);
        reportsContentPlaceholder.setVisibility(View.GONE);
        dateSelectorRow.setVisibility(View.GONE);

        if (selectedId == R.id.tabMore) {
            // SHOW Grid
            moreContentContainer.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.reports_tab_more));
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_tab_selected));
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
        } else if (selectedId == R.id.tabItems) {
            // SHOW Grid
            moreContentContainer.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.reports_tab_items));
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_tab_selected));
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
        } else if (selectedId == R.id.tabReports) {
            // Reports section
            dateSelectorRow.setVisibility(View.VISIBLE);
            showEmptyState(true);
            toolbar.setTitle(getString(R.string.reports_title));
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_surface));
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.reports_text_primary));
            toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.reports_text_primary));
        } else if (selectedId == R.id.tabToday) {
            toolbar.setTitle(getString(R.string.reports_tab_today));
            reportsContentPlaceholder.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_surface));
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.reports_text_primary));
            toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.reports_text_primary));
        } else if (selectedId == R.id.tabCounter) {
            toolbar.setTitle(getString(R.string.reports_tab_counter));
            reportsContentPlaceholder.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_surface));
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.reports_text_primary));
            toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.reports_text_primary));
        }
    }
}
