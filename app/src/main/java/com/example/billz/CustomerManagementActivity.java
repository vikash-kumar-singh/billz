package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
    private View noResultsView;
    private TabLayout tabLayout;
    private List<Customer> allCustomersList;
    private EditText editSearch;
    private View searchBarContainer;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);

        // Security Check: Ensure user is logged in
        if (FirebaseHelper.getCurrentUid() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCustomer);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerCustomers);
        textNoCustomers = findViewById(R.id.textNoCustomers);
        noResultsView = findViewById(R.id.noResultsView);
        tabLayout = findViewById(R.id.tabLayoutCustomers);
        searchBarContainer = findViewById(R.id.searchBarContainer);
        editSearch = findViewById(R.id.editSearchCustomers);
        ImageView imageClearSearch = findViewById(R.id.imageClearSearchCustomers);
        ViewGroup mainContainer = findViewById(android.R.id.content);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fabAddCustomer).setOnClickListener(v -> {
            startActivity(new Intent(this, AddCustomerActivity.class));
        });

        findViewById(R.id.imageSearchCustomers).setOnClickListener(v -> {
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
                searchBarContainer.setVisibility(View.GONE);
                isSearchVisible = false;
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
        
        // Also trigger a background sync to ensure data is fresh
        new CustomerSyncManager(this).syncCustomersFromCloud(new CustomerSyncManager.SyncCallback() {
            @Override
            public void onSyncComplete() {
                runOnUiThread(() -> loadCustomers());
            }

            @Override
            public void onSyncFailed(String error) {
                // Silently fail or log
            }
        });
    }

    private void loadCustomers() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Business active = db.businessDao().getSelectedBusiness();
            int bId = (active != null) ? active.getId() : -1;
            allCustomersList = db.customerDao().getAllCustomers(bId);
            runOnUiThread(() -> filterCustomers(tabLayout.getSelectedTabPosition()));
        });
    }

    private void checkEmptyState() {
        if (adapter != null) {
            boolean isListEmpty = adapter.getItemCount() == 0;
            boolean isSearching = editSearch != null && !editSearch.getText().toString().isEmpty();

            if (isSearching) {
                noResultsView.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                textNoCustomers.setVisibility(View.GONE);
                recyclerView.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            } else {
                noResultsView.setVisibility(View.GONE);
                textNoCustomers.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            }
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

    private void filterCustomers(int position) {
        if (allCustomersList == null) return;
        
        List<Customer> displayList;
        if (position == 1) { // DUE CUSTOMERS
            displayList = new ArrayList<>();
            for (Customer c : allCustomersList) {
                if (c.getDueAmount() > 0) {
                    displayList.add(c);
                }
            }
        } else {
            displayList = allCustomersList;
        }

        adapter = new CustomerAdapter(displayList);
        recyclerView.setAdapter(adapter);
        
        // Re-apply existing search filter if active
        if (isSearchVisible && editSearch != null && !editSearch.getText().toString().isEmpty()) {
            adapter.filter(editSearch.getText().toString());
        }
        
        checkEmptyState();
    }
}
