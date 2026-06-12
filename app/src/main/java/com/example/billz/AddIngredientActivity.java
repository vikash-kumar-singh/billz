package com.example.billz;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText editIngredientName, editStockValue;
    private TextView textCurrentStock, textUpdatedStock;
    private MaterialButton btnAddStock, btnRemoveStock;
    private ImageView imgStockAction;
    private View btnDeleteIngredient, layoutStockHistory, textHistoryLabel;
    private boolean isAddingStock = true;
    private double currentStock = 0;
    private int ingredientId = -1;
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ingredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbarIngredient);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        editIngredientName = findViewById(R.id.editIngredientName);
        editStockValue = findViewById(R.id.editStockValue);
        textCurrentStock = findViewById(R.id.textCurrentStock);
        textUpdatedStock = findViewById(R.id.textUpdatedStock);
        btnAddStock = findViewById(R.id.btnAddStock);
        btnRemoveStock = findViewById(R.id.btnRemoveStock);
        imgStockAction = findViewById(R.id.imgStockAction);
        btnDeleteIngredient = findViewById(R.id.btnDeleteIngredient);
        layoutStockHistory = findViewById(R.id.layoutStockHistory);
        textHistoryLabel = findViewById(R.id.textHistoryLabel);

        setupListeners();

        if (getIntent().hasExtra("ingredient_id")) {
            isUpdate = true;
            ingredientId = getIntent().getIntExtra("ingredient_id", -1);
            toolbar.setTitle("INGREDIENT");
            ((MaterialButton) findViewById(R.id.btnSaveIngredient)).setText("Save");
            btnDeleteIngredient.setVisibility(View.VISIBLE);
            layoutStockHistory.setVisibility(View.VISIBLE);
            textHistoryLabel.setVisibility(View.VISIBLE);
            loadIngredientData();
        }
    }

    private void loadIngredientData() {
        if (ingredientId == -1) return;
        BusinessHelper.ensureActiveBusiness(this, () -> {
            int bId = BusinessHelper.getActiveBusinessId(this);
            Ingredient ingredient = AppDatabase.getInstance(this).ingredientDao().getAllIngredients(bId).stream()
                    .filter(i -> i.getId() == ingredientId).findFirst().orElse(null);
            
            if (ingredient != null) {
                runOnUiThread(() -> {
                    editIngredientName.setText(ingredient.getName());
                    currentStock = ingredient.getStock();
                    textCurrentStock.setText(String.format(Locale.getDefault(), "%.0f", currentStock));
                });
            }
            
            // Load History
            List<StockHistory> history = AppDatabase.getInstance(this).stockHistoryDao().getHistoryForIngredient(ingredientId);
            runOnUiThread(() -> renderHistory(history));
        });
    }

    private void renderHistory(List<StockHistory> historyList) {
        LinearLayout container = (LinearLayout) layoutStockHistory;
        container.removeAllViews();
        
        if (historyList == null || historyList.isEmpty()) {
            layoutStockHistory.setVisibility(View.GONE);
            textHistoryLabel.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < historyList.size(); i++) {
            StockHistory history = historyList.get(i);
            View view = inflater.inflate(R.layout.item_stock_history, container, false);
            
            View line = view.findViewById(R.id.historyLine);
            MaterialCardView card = view.findViewById(R.id.cardHistoryContent);
            TextView textMain = view.findViewById(R.id.textHistoryMain);
            TextView textChange = view.findViewById(R.id.textHistoryChange);
            TextView textDetail = view.findViewById(R.id.textHistoryDetail);

            if (i == historyList.size() - 1) {
                line.setVisibility(View.GONE);
            }

            String amountText = String.format(Locale.getDefault(), "%.0f", history.getAmount());
            if (history.isAddition()) {
                textMain.setText(String.format(Locale.getDefault(), "%s Added", amountText));
                card.setCardBackgroundColor(ColorStateList.valueOf(0xFF66BB6A));
                textChange.setText(String.format(Locale.getDefault(), "=%s", amountText));
            } else {
                textMain.setText(String.format(Locale.getDefault(), "%s Removed", amountText));
                card.setCardBackgroundColor(ColorStateList.valueOf(0xFFEF5350));
                textChange.setText(String.format(Locale.getDefault(), "=-%s", amountText));
            }

            textDetail.setText(String.format(Locale.getDefault(), "%s\njust now", history.getSource())); 

            container.addView(view);
        }
    }

    private void setupListeners() {
        btnAddStock.setOnClickListener(v -> setStockAction(true));
        btnRemoveStock.setOnClickListener(v -> setStockAction(false));

        findViewById(R.id.btnSaveIngredient).setOnClickListener(v -> saveIngredient());
        btnDeleteIngredient.setOnClickListener(v -> deleteIngredient());

        editStockValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateUpdatedStock(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void deleteIngredient() {
        if (ingredientId == -1) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).ingredientDao().deleteById(ingredientId);
            AppDatabase.getInstance(this).stockHistoryDao().deleteHistoryForIngredient(ingredientId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void setStockAction(boolean add) {
        isAddingStock = add;
        int green = 0xFF4CAF50;
        int blue = 0xFF3F51B5;
        int density = (int) getResources().getDisplayMetrics().density;

        if (add) {
            // Selected: Add Stock (Solid Green)
            btnAddStock.setBackgroundTintList(ColorStateList.valueOf(green));
            btnAddStock.setTextColor(Color.WHITE);
            btnAddStock.setStrokeColor(ColorStateList.valueOf(green));
            btnAddStock.setStrokeWidth(0);

            // Unselected: Remove Stock (Solid White with Blue Border)
            btnRemoveStock.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnRemoveStock.setTextColor(blue);
            btnRemoveStock.setStrokeColor(ColorStateList.valueOf(blue));
            btnRemoveStock.setStrokeWidth(density);

            imgStockAction.setImageResource(android.R.drawable.ic_input_add);
            imgStockAction.setColorFilter(green);
        } else {
            // Selected: Remove Stock (Solid Blue)
            btnRemoveStock.setBackgroundTintList(ColorStateList.valueOf(blue));
            btnRemoveStock.setTextColor(Color.WHITE);
            btnRemoveStock.setStrokeColor(ColorStateList.valueOf(blue));
            btnRemoveStock.setStrokeWidth(0);

            // Unselected: Add Stock (Solid White with Green Border)
            btnAddStock.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnAddStock.setTextColor(green);
            btnAddStock.setStrokeColor(ColorStateList.valueOf(green));
            btnAddStock.setStrokeWidth(density);

            imgStockAction.setImageResource(android.R.drawable.ic_delete);
            imgStockAction.setColorFilter(0xFFF44336);
        }
        calculateUpdatedStock(editStockValue.getText().toString());
    }

    private void calculateUpdatedStock(String value) {
        if (value.isEmpty()) {
            textUpdatedStock.setText("-");
            return;
        }

        try {
            double input = Double.parseDouble(value);
            double updated = isAddingStock ? (currentStock + input) : (currentStock - input);
            textUpdatedStock.setText(String.format(Locale.getDefault(), "%.0f", updated));
        } catch (NumberFormatException e) {
            textUpdatedStock.setText("-");
        }
    }

    private void saveIngredient() {
        String name = editIngredientName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter ingredient name", Toast.LENGTH_SHORT).show();
            return;
        }

        String stockValStr = editStockValue.getText().toString().trim();
        double changeAmount = 0;
        if (!stockValStr.isEmpty()) {
            try {
                changeAmount = Double.parseDouble(stockValStr);
            } catch (Exception ignored) {}
        }

        double finalStock = isAddingStock ? (currentStock + changeAmount) : (currentStock - changeAmount);
        
        Ingredient ingredient = new Ingredient(name, finalStock);
        if (isUpdate && ingredientId != -1) {
            ingredient.setId(ingredientId);
        }

        double finalChangeAmount = changeAmount;
        boolean finalIsAddingStock = isAddingStock;

        BusinessHelper.ensureActiveBusiness(this, () -> {
            AppDatabase db = AppDatabase.getInstance(this);
            ingredient.setBusinessId(BusinessHelper.getActiveBusinessId(this));

            if (isUpdate && ingredientId != -1) {
                db.ingredientDao().insert(ingredient);
                
                if (finalChangeAmount > 0) {
                    StockHistory history = new StockHistory(ingredientId, finalChangeAmount, finalIsAddingStock, System.currentTimeMillis(), "Source manual");
                    db.stockHistoryDao().insert(history);
                }
            } else {
                db.ingredientDao().insert(ingredient);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, isUpdate ? "Ingredient updated" : "Ingredient saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
