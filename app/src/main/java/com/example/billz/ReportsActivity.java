package com.example.billz;

import android.os.Bundle;
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
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;

public class ReportsActivity extends AppCompatActivity {

    private static final int TAB_REPORTS = 0;
    private static final int TAB_TODAY = 1;
    private static final int TAB_COUNTER = 2;
    private static final int TAB_ITEMS = 3;
    private static final int TAB_MORE = 4;

    private View emptyStateContainer;
    private View reportsContentPlaceholder;
    private View moreContentContainer;
    private View premiumBanner;
    private View dateSelectorRow;
    private LinearLayout bottomNavigationView;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private View[] bottomTabs;
    private ImageView[] bottomTabIcons;
    private TextView[] bottomTabLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);

        View root = findViewById(R.id.reportsRoot);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        reportsContentPlaceholder = findViewById(R.id.reportsContentPlaceholder);
        moreContentContainer = findViewById(R.id.moreContentContainer);
        premiumBanner = findViewById(R.id.premiumBanner);
        dateSelectorRow = findViewById(R.id.dateSelectorRow);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbarReports);
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

        findViewById(R.id.nav_add_expense).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ReportsActivity.this, CashFlowActivity.class));
        });

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
            bottomTabLabels[i].setTypeface(bottomTabLabels[i].getTypeface(), isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        // Handle content visibility based on tab
        int selectedId = bottomTabs[selectedTab].getId();

        // Reset all views
        moreContentContainer.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
        reportsContentPlaceholder.setVisibility(View.GONE);
        dateSelectorRow.setVisibility(View.GONE);

        if (selectedId == R.id.tabMore) {
            // SHOW Grid + SHOW Premium
            moreContentContainer.setVisibility(View.VISIBLE);
            premiumBanner.setVisibility(View.VISIBLE);
            toolbar.setTitle("More");
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_tab_selected));
            toolbar.setTitleTextColor(selectedColor);
            toolbar.setNavigationIconTint(selectedColor);
        } else if (selectedId == R.id.tabItems) {
            // SHOW Grid + HIDE Premium
            moreContentContainer.setVisibility(View.VISIBLE);
            premiumBanner.setVisibility(View.GONE);
            toolbar.setTitle("Items");
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
        } else {
            // Placeholder for Today and Counter
            reportsContentPlaceholder.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.reports_surface));
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.reports_text_primary));
            toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.reports_text_primary));
        }
    }
}
