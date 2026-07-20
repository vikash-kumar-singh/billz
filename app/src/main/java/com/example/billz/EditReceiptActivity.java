package com.example.billz;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    @SuppressWarnings("unused")
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
        findViewById(R.id.btnAddItem).setOnClickListener(v -> showAddItemDialog());
        
        findViewById(R.id.cardPaymentMode).setOnClickListener(v -> showPaymentSelector());

        findViewById(R.id.btnAddTax).setOnClickListener(v -> Toast.makeText(this, "Tax editing coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnAddDiscount).setOnClickListener(v -> Toast.makeText(this, "Discount editing coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnAddOtherCharges).setOnClickListener(v -> Toast.makeText(this, "Other charges editing coming soon", Toast.LENGTH_SHORT).show());
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
            
            Customer customer = null;
            if (currentReceipt != null && currentReceipt.getCustomerId() != null) {
                customer = db.customerDao().getById(currentReceipt.getCustomerId());
            }
            
            final Customer finalCustomer = customer;
            if (currentReceipt != null) {
                runOnUiThread(() -> {
                    editName.setText(currentReceipt.getCustomerName());
                    if (finalCustomer != null) {
                        editMobile.setText(finalCustomer.getMobile());
                    }
                    textPaymentMode.setText(currentReceipt.getPaymentMode());
                    updateTotals();
                    setupAdapter();
                });
            }
        });
    }

    private void setupAdapter() {
        List<CartItem> cartItems = new ArrayList<>();
        for (int i = 0; i < receiptItems.size(); i++) {
            ReceiptItem ri = receiptItems.get(i);
            Item dummyItem = new Item(ri.getItemName(), "", ri.getPrice(), 0, 100, ri.getVariantName(), "Unit", false);
            // Use index as dummy ID to link back
            dummyItem.setId(String.valueOf(i)); 
            Variant dummyVariant = null;
            if (ri.getVariantName() != null && !ri.getVariantName().isEmpty() && !ri.getVariantName().equalsIgnoreCase("Default")) {
                dummyVariant = new Variant("0", ri.getVariantName(), ri.getPrice(), 0, 100);
            }
            cartItems.add(new CartItem(dummyItem, dummyVariant, ri.getQuantity()));
        }
        
        CounterAdapter adapter = new CounterAdapter(cartItems, this::showEditQuantityDialog, item -> {
            Toast.makeText(this, "Price editing is not available in Edit Mode", Toast.LENGTH_SHORT).show();
        });
        recyclerItems.setAdapter(adapter);
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
            int q = Integer.parseInt(editQuantity.getText().toString());
            editQuantity.setText(String.valueOf(q + 1));
        });

        dialog.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            int q = Integer.parseInt(editQuantity.getText().toString());
            if (q > 0) editQuantity.setText(String.valueOf(q - 1));
        });

        dialog.findViewById(R.id.btnUpdateQuantity).setOnClickListener(v -> {
            int newQty = Integer.parseInt(editQuantity.getText().toString());
            int index = Integer.parseInt(cartItem.getItem().getId());
            
            if (newQty <= 0) {
                receiptItems.remove(index);
            } else {
                receiptItems.get(index).setQuantity(newQty);
            }
            
            updateTotals();
            setupAdapter(); // Refresh entire list to keep indices in sync
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
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_checkout_bottom_sheet, null);
        dialog.setContentView(view);

        // Hide customer fields in this selector as we only need payment mode
        View cardCustomer = view.findViewById(R.id.cardCustomerDetails);
        if (cardCustomer != null) cardCustomer.setVisibility(View.GONE);
        
        TextView title = view.findViewById(R.id.textPaymentTitle);
        if (title != null) {
            title.setVisibility(View.VISIBLE);
            title.setText("SELECT NEW PAYMENT MODE");
        }

        RecyclerView rv = view.findViewById(R.id.recyclerPaymentModes);
        if (rv != null) {
            rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

            Executors.newSingleThreadExecutor().execute(() -> {
                int bId = BusinessHelper.getActiveBusinessId(this);
                List<PaymentMode> modes = db.paymentModeDao().getAllPaymentModes(bId);
                runOnUiThread(() -> {
                    rv.setAdapter(new CheckoutPaymentAdapter(modes, new CheckoutPaymentAdapter.OnPaymentModeClickListener() {
                        @Override
                        public void onPaymentClick(PaymentMode mode) {
                            textPaymentMode.setText(mode.getName());
                            dialog.dismiss();
                        }

                        @Override
                        public void onAddNewClick() {
                            Toast.makeText(EditReceiptActivity.this, "Add payment mode in Settings", Toast.LENGTH_SHORT).show();
                        }
                    }));
                });
            });
        }

        dialog.show();
    }

    private void showAddItemDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        // Use business selector layout as it has a title and a recycler view
        dialog.setContentView(R.layout.dialog_business_selector);
        
        TextView title = dialog.findViewById(R.id.textTitle);
        if (title != null) title.setText("SELECT ITEM TO ADD");
        
        RecyclerView rv = dialog.findViewById(R.id.recyclerBusinesses);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));

            Executors.newSingleThreadExecutor().execute(() -> {
                int bId = BusinessHelper.getActiveBusinessId(this);
                List<Item> items = db.itemDao().getAllItems(bId);
                runOnUiThread(() -> {
                    ProductListAdapter adapter = new ProductListAdapter(items);
                    adapter.setListener(new ProductListAdapter.OnProductActionListener() {
                        @Override
                        public void onPlusClick(Item item, int position) {
                            ReceiptItem ri = new ReceiptItem(receiptId, item.getName(), item.getVariantName(), item.getSellingPrice(), 1);
                            ri.setItemId(item.getId());
                            receiptItems.add(ri);
                            updateTotals();
                            setupAdapter();
                            dialog.dismiss();
                        }

                        @Override
                        public void onMinusClick(Item item, int position) {}
                    });
                    rv.setAdapter(adapter);
                });
            });
        }

        dialog.show();
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
        currentReceipt.setUpdatedAt(System.currentTimeMillis());
        
        Executors.newSingleThreadExecutor().execute(() -> {
            db.receiptDao().update(currentReceipt);
            
            // Sync Items: Delete all and re-insert to handle additions/deletions/updates simply
            db.receiptItemDao().deleteByReceiptId(receiptId);
            db.receiptItemDao().insertAll(receiptItems);
            
            // Also update cloud
            new ReceiptCloudRepository(this).updateReceipt(currentReceipt);

            runOnUiThread(() -> {
                Toast.makeText(this, "Receipt Updated Successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
