package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class CustomerManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomerAdapter adapter;
    private TextView textNoCustomers;
    private TabLayout tabLayout;
    private List<Customer> allCustomersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCustomer);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerCustomers);
        textNoCustomers = findViewById(R.id.textNoCustomers);
        tabLayout = findViewById(R.id.tabLayoutCustomers);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fabAddCustomer).setOnClickListener(v -> {
            startActivity(new Intent(this, AddCustomerActivity.class));
        });

        findViewById(R.id.imageSearchCustomers).setOnClickListener(v -> {
            Toast.makeText(this, R.string.search_coming_soon, Toast.LENGTH_SHORT).show();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterCustomers(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        View appBar = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers();
    }

    private void loadCustomers() {
        allCustomersList = AppDatabase.getInstance(this).customerDao().getAllCustomers();
        filterCustomers(tabLayout.getSelectedTabPosition());
    }

    private void filterCustomers(int position) {
        if (allCustomersList == null) return;
        
        List<Customer> displayList;
        if (position == 1) { // DUE CUSTOMERS
            displayList = new ArrayList<>();
        } else {
            displayList = allCustomersList;
        }

        if (displayList.isEmpty()) {
            textNoCustomers.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textNoCustomers.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new CustomerAdapter(displayList);
            recyclerView.setAdapter(adapter);
        }
    }
}
