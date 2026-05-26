package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Category> expenseCategories;
    private List<Category> incomeCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);

        recyclerView = findViewById(R.id.recyclerCategories);
        TabLayout tabLayout = findViewById(R.id.tabLayoutTransaction);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        initCategories();

        int startTab = getIntent().getIntExtra("START_TAB", 0);
        TabLayout.Tab tab = tabLayout.getTabAt(startTab);
        if (tab != null) tab.select();
        
        updateGrid(startTab == 0);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateGrid(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        findViewById(R.id.imageBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.imageSearch).setOnClickListener(v -> 
            Toast.makeText(this, "Search coming soon!", Toast.LENGTH_SHORT).show());

        View appBar = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void initCategories() {
        expenseCategories = new ArrayList<>();
        expenseCategories.add(new Category(getString(R.string.cat_tax), true));
        expenseCategories.add(new Category(getString(R.string.cat_fuel), true));
        expenseCategories.add(new Category(getString(R.string.cat_food), true));
        expenseCategories.add(new Category(getString(R.string.cat_bill), true));
        expenseCategories.add(new Category(getString(R.string.cat_transport), true));
        expenseCategories.add(new Category(getString(R.string.cat_insurance), true));
        expenseCategories.add(new Category(getString(R.string.cat_salary), true));
        expenseCategories.add(new Category(getString(R.string.cat_rent), true));
        expenseCategories.add(new Category(getString(R.string.cat_repairs), true));
        expenseCategories.add(new Category(getString(R.string.cat_commission), true));
        expenseCategories.add(new Category(getString(R.string.cat_advertising), true));
        expenseCategories.add(new Category(getString(R.string.cat_fee), true));
        expenseCategories.add(new Category(getString(R.string.cat_interest), true));
        expenseCategories.add(new Category(getString(R.string.cat_loan), true));
        expenseCategories.add(new Category(getString(R.string.cat_supplies), true));
        expenseCategories.add(new Category(getString(R.string.cat_transfer), true));
        expenseCategories.add(new Category(getString(R.string.cat_contract), true));
        expenseCategories.add(new Category(getString(R.string.cat_miscellaneous), true));
        expenseCategories.add(new Category(getString(R.string.cat_custom), true, true));

        incomeCategories = new ArrayList<>();
        incomeCategories.add(new Category(getString(R.string.cat_profit), false));
        incomeCategories.add(new Category(getString(R.string.cat_salary), false));
        incomeCategories.add(new Category(getString(R.string.cat_awards), false));
        incomeCategories.add(new Category(getString(R.string.cat_rental), false));
        incomeCategories.add(new Category(getString(R.string.cat_sale), false));
        incomeCategories.add(new Category(getString(R.string.cat_refund), false));
        incomeCategories.add(new Category(getString(R.string.cat_lottery), false));
        incomeCategories.add(new Category(getString(R.string.cat_dividend), false));
        incomeCategories.add(new Category(getString(R.string.cat_investment), false));
        incomeCategories.add(new Category(getString(R.string.cat_interest), false));
        incomeCategories.add(new Category(getString(R.string.cat_commission), false));
        incomeCategories.add(new Category(getString(R.string.cat_fee), false));
        incomeCategories.add(new Category(getString(R.string.cat_loan), false));
        incomeCategories.add(new Category(getString(R.string.cat_miscellaneous), false));
        incomeCategories.add(new Category(getString(R.string.cat_custom), false, true));
    }

    private void updateGrid(boolean isExpense) {
        List<Category> list = isExpense ? expenseCategories : incomeCategories;
        CategoryAdapter adapter = new CategoryAdapter(list, category -> {
            if (category.isCustom()) {
                Toast.makeText(this, "Add Custom Category coming soon!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selected: " + category.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }
}
