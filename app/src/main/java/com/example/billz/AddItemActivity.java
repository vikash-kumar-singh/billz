package com.example.billz;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {

    private EditText editItemName;
    private TextView btnSimple, btnAdvance, textCategory;
    private View layoutSimpleMode, layoutAdvanceMode;
    private boolean isSimpleMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        MaterialToolbar toolbar = findViewById(R.id.toolbarItem);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarItemLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        toolbar.setNavigationOnClickListener(v -> finish());
        
        editItemName = findViewById(R.id.editItemName);
        textCategory = findViewById(R.id.textCategory);
        btnSimple = findViewById(R.id.btnSimple);
        btnAdvance = findViewById(R.id.btnAdvance);
        layoutSimpleMode = findViewById(R.id.layoutSimpleMode);
        layoutAdvanceMode = findViewById(R.id.layoutAdvanceMode);

        btnSimple.setOnClickListener(v -> setMode(true));
        btnAdvance.setOnClickListener(v -> setMode(false));

        findViewById(R.id.cardCategory).setOnClickListener(v -> showSelectCategoryBottomSheet());
        findViewById(R.id.cardSellByUnit).setOnClickListener(v -> showSellByBottomSheet());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = editItemName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
                return;
            }
            // For now, just show a success message and finish
            Toast.makeText(this, "Item Saved Successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setMode(boolean simple) {
        isSimpleMode = simple;
        if (simple) {
            btnSimple.setBackgroundResource(R.drawable.bg_toggle_left_selected);
            btnSimple.setTextColor(0xFFFFFFFF);
            btnAdvance.setBackgroundResource(R.drawable.bg_toggle_unselected);
            btnAdvance.setTextColor(0xFF3F51B5);
            layoutSimpleMode.setVisibility(View.VISIBLE);
            layoutAdvanceMode.setVisibility(View.GONE);
        } else {
            btnAdvance.setBackgroundResource(R.drawable.bg_toggle_right_selected);
            btnAdvance.setTextColor(0xFFFFFFFF);
            btnSimple.setBackgroundResource(R.drawable.bg_toggle_unselected);
            btnSimple.setTextColor(0xFF3F51B5);
            layoutSimpleMode.setVisibility(View.GONE);
            layoutAdvanceMode.setVisibility(View.VISIBLE);
        }
    }

    private void showSellByBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_sell_by_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.optionSellByUnit).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Sell By Unit selected", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.optionSellByFraction).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Sell By Fraction selected", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showSelectCategoryBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_category_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        view.findViewById(R.id.btnAddNewCategory).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, AddCategoryActivity.class));
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Fetch categories from DB
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = AppDatabase.getInstance(this).categoryDao().getAllCategories();
            
            // Add dummy data if empty to match image
            if (categories.isEmpty()) {
                categories.add(new Category("Brand All Item", false, true));
                categories.add(new Category("Creatine", false, false));
                categories.add(new Category("MASS GAINER", false, false));
                categories.add(new Category("Sample", false, false));
                categories.add(new Category("DAILY SUPPORT", false, false));
                categories.add(new Category("PRE WORKOUT", false, false));
                categories.add(new Category("Stack", false, false));
                categories.add(new Category("Compression Tshirt", false, false));
            }

            runOnUiThread(() -> {
                CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                    textCategory.setText(category.getName());
                    textCategory.setTextColor(0xFF000000); // Change from hint color to black
                    dialog.dismiss();
                });
                recyclerView.setAdapter(adapter);
            });
        });

        dialog.show();
    }
}
