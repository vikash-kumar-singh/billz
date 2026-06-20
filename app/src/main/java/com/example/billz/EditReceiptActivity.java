package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EditReceiptActivity extends AppCompatActivity {

    private String receiptId;
    private AppDatabase db;
    private Receipt currentReceipt;
    private List<ReceiptItem> receiptItems = new ArrayList<>();
    
    private RecyclerView recyclerItems;
    private TextView textSubtotal, textGrandTotal, textItemCountSummary, textPaymentMode;
    private EditText editMobile, editName;
    private ImageView imgPaymentIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_receipt);

        db = AppDatabase.getInstance(this);
        receiptId = getIntent().getStringExtra("receipt_id");

        View headerEdit = findViewById(R.id.headerEdit);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(headerEdit, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        loadReceiptData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveReceipt());
        findViewById(R.id.btnAddItem).setOnClickListener(v -> {
            Toast.makeText(this, "Add Item coming soon", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.cardPaymentMode).setOnClickListener(v -> showPaymentSelector());
    }

    private void initViews() {
        recyclerItems = findViewById(R.id.recyclerEditItems);
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        
        textSubtotal = findViewById(R.id.textSubtotal);
        textGrandTotal = findViewById(R.id.textGrandTotal);
        textItemCountSummary = findViewById(R.id.textItemCountSummary);
        textPaymentMode = findViewById(R.id.textPaymentMode);
        imgPaymentIcon = findViewById(R.id.imgPaymentIcon);
        
        editMobile = findViewById(R.id.editMobile);
        editName = findViewById(R.id.editName);
    }

    private void loadReceiptData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentReceipt = db.receiptDao().getById(receiptId);
            receiptItems = db.receiptItemDao().getItemsForReceipt(receiptId);
            
            if (currentReceipt != null) {
                runOnUiThread(() -> {
                    editName.setText(currentReceipt.getCustomerName());
                    textPaymentMode.setText(currentReceipt.getPaymentMode());
                    updateTotals();
                    setupAdapter();
                });
            }
        });
    }

    private void setupAdapter() {
        List<CartItem> cartItems = new ArrayList<>();
        for (ReceiptItem ri : receiptItems) {
            Item dummyItem = new Item(ri.getItemName(), "", ri.getPrice(), 0, 100, ri.getVariantName(), "Unit", false);
            dummyItem.setId(String.valueOf(ri.getId())); 
            Variant dummyVariant = null;
            if (ri.getVariantName() != null && !ri.getVariantName().isEmpty()) {
                dummyVariant = new Variant("0", ri.getVariantName(), ri.getPrice(), 0, 100);
            }
            cartItems.add(new CartItem(dummyItem, dummyVariant, ri.getQuantity()));
        }
        
        CounterAdapter adapter = new CounterAdapter(cartItems, item -> {
            showEditQuantityDialog(item);
        });
        recyclerItems.setAdapter(adapter);
    }

    private void showEditQuantityDialog(CartItem cartItem) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_quantity);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editQuantity = dialog.findViewById(R.id.editQuantity);
        editQuantity.setText(String.valueOf(cartItem.getQuantity()));

        dialog.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            int q = Integer.parseInt(editQuantity.getText().toString());
            editQuantity.setText(String.valueOf(q + 1));
        });

        dialog.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            int q = Integer.parseInt(editQuantity.getText().toString());
            if (q > 0) editQuantity.setText(String.valueOf(q - 1));
        });

        dialog.findViewById(R.id.btnUpdateQuantity).setOnClickListener(v -> {
            int newQty = Integer.parseInt(editQuantity.getText().toString());
            cartItem.setQuantity(newQty);
            
            // Sync back to receiptItems
            for (ReceiptItem ri : receiptItems) {
                if (String.valueOf(ri.getId()).equals(cartItem.getItem().getId())) {
                    ri.setQuantity(newQty);
                    break;
                }
            }
            
            updateTotals();
            if (recyclerItems.getAdapter() != null) {
                recyclerItems.getAdapter().notifyDataSetChanged();
            }
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnDialogClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateTotals() {
        double subtotal = 0;
        int units = 0;
        for (ReceiptItem ri : receiptItems) {
            subtotal += ri.getPrice() * ri.getQuantity();
            units += ri.getQuantity();
        }
        
        textSubtotal.setText(String.format(Locale.getDefault(), "%,.0f", subtotal));
        textGrandTotal.setText(String.format(Locale.getDefault(), "₹%,.0f", subtotal)); 
        
        String itemsStr = receiptItems.size() + (receiptItems.size() == 1 ? " ITEM" : " ITEMS");
        String unitsStr = units + (units == 1 ? " UNIT" : " UNITS");
        textItemCountSummary.setText(String.format(Locale.getDefault(), "%s | %s", itemsStr, unitsStr));
    }

    private void showPaymentSelector() {
        Toast.makeText(this, "Change Payment Mode coming soon", Toast.LENGTH_SHORT).show();
    }

    private void saveReceipt() {
        if (currentReceipt == null) return;
        
        double finalTotal = 0;
        int totalItems = receiptItems.size();
        for (ReceiptItem ri : receiptItems) {
            finalTotal += ri.getPrice() * ri.getQuantity();
        }

        currentReceipt.setCustomerName(editName.getText().toString().trim());
        currentReceipt.setPaymentMode(textPaymentMode.getText().toString());
        currentReceipt.setTotalAmount(finalTotal);
        currentReceipt.setItemCount(totalItems);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            db.receiptDao().update(currentReceipt);
            // Update individual receipt items
            for (ReceiptItem ri : receiptItems) {
                db.receiptItemDao().update(ri);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Receipt Updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
