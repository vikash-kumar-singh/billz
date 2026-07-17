package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CustomerDetailsActivity extends AppCompatActivity {

    private String customerId;
    private Customer currentCustomer;
    private AppDatabase db;
    private TextView textMemberSince, textLastVisit, textOrdersCount, textBalance;
    private TextView textDetailName, textDetailNumber, textDetailEmail, textDetailGender;
    private View layoutMoreDetailsContent, layoutTransactionHistoryContent;
    private ImageView imageMoreDetailsToggle, imageOrdersToggle, imageTransactionHistoryToggle;
    private RecyclerView recyclerOrders, recyclerTransactionHistory;
    private boolean isMoreDetailsExpanded = false;
    private boolean isOrdersExpanded = false;
    private boolean isTransactionHistoryExpanded = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_details);

        db = AppDatabase.getInstance(this);
        customerId = getIntent().getStringExtra("customer_id");

        initViews();
        loadCustomerData();

        View appBar = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarCustomerDetails);
        toolbar.setNavigationOnClickListener(v -> finish());

        textMemberSince = findViewById(R.id.textMemberSince);
        textLastVisit = findViewById(R.id.textLastVisit);
        textOrdersCount = findViewById(R.id.textOrdersCount);
        textBalance = findViewById(R.id.textCustomerBalance);

        textDetailName = findViewById(R.id.textDetailName);
        textDetailNumber = findViewById(R.id.textDetailNumber);
        textDetailEmail = findViewById(R.id.textDetailEmail);
        textDetailGender = findViewById(R.id.textDetailGender);

        layoutMoreDetailsContent = findViewById(R.id.layoutMoreDetailsContent);
        imageMoreDetailsToggle = findViewById(R.id.imageMoreDetailsToggle);
        
        recyclerOrders = findViewById(R.id.recyclerCustomerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        imageOrdersToggle = findViewById(R.id.imageOrdersToggle);

        layoutTransactionHistoryContent = findViewById(R.id.layoutTransactionHistoryContent);
        imageTransactionHistoryToggle = findViewById(R.id.imageTransactionHistoryToggle);
        recyclerTransactionHistory = findViewById(R.id.recyclerTransactionHistory);
        recyclerTransactionHistory.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.layoutMoreDetailsHeader).setOnClickListener(v -> toggleMoreDetails());
        findViewById(R.id.layoutOrdersHeader).setOnClickListener(v -> toggleOrders());
        findViewById(R.id.layoutTransactionHistoryHeader).setOnClickListener(v -> toggleTransactionHistory());

        findViewById(R.id.btnEditCustomer).setOnClickListener(v -> 
                Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnReceiveAmount).setOnClickListener(v -> showAmountBottomSheet(true));

        findViewById(R.id.btnReturnAmount).setOnClickListener(v -> showAmountBottomSheet(false));

        findViewById(R.id.btnLoadMore).setOnClickListener(v -> 
                Toast.makeText(this, "No more transactions", Toast.LENGTH_SHORT).show()
        );
    }

    private void showAmountBottomSheet(boolean isReceive) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_receive_amount_bottom_sheet, null);
        bottomSheet.setContentView(view);

        TextView textTitle = view.findViewById(R.id.textDialogTitle);
        EditText editAmount = view.findViewById(R.id.editAmount);
        TextView textSelectedPaymentMode = view.findViewById(R.id.textSelectedPaymentMode);
        View layoutPaymentModeSelector = view.findViewById(R.id.layoutPaymentModeSelector);
        View layoutSendSms = view.findViewById(R.id.layoutSendSms);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnSubmitAmount);

        if (isReceive) {
            textTitle.setText("RECEIVE AMOUNT");
            btnSubmit.setText("ADD AMOUNT");
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            layoutSendSms.setVisibility(View.GONE);
        } else {
            textTitle.setText("RETURN AMOUNT"); // Or keep as "RECEIVE AMOUNT" as per image? I'll use "RETURN AMOUNT"
            btnSubmit.setText("RETURN AMOUNT");
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEF4444));
            layoutSendSms.setVisibility(View.VISIBLE);
        }

        // Fetch Payment Modes
        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = BusinessHelper.getActiveBusinessId(this);
            List<PaymentMode> modes = db.paymentModeDao().getAllPaymentModes(bId);
            runOnUiThread(() -> {
                layoutPaymentModeSelector.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(this, layoutPaymentModeSelector);
                    if (modes.isEmpty()) {
                        String[] defaults = {"Cash", "Debit Card", "Credit Card", "UPI", "Store Credit", "Online", "Sample Rate", "Exchange", "Google Pay", "Credit"};
                        for (String s : defaults) popup.getMenu().add(s);
                    } else {
                        for (PaymentMode mode : modes) {
                            popup.getMenu().add(mode.getName());
                        }
                    }
                    popup.setOnMenuItemClickListener(item -> {
                        textSelectedPaymentMode.setText(item.getTitle());
                        return true;
                    });
                    popup.show();
                });
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> bottomSheet.dismiss());
        
        btnSubmit.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                String mode = textSelectedPaymentMode.getText().toString();
                
                if (currentCustomer != null) {
                    double newDue;
                    if (isReceive) {
                        newDue = Math.max(0, currentCustomer.getDueAmount() - amount);
                    } else {
                        newDue = currentCustomer.getDueAmount() + amount;
                    }
                    currentCustomer.setDueAmount(newDue);
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.customerDao().insert(currentCustomer);
                        new CustomerSyncManager(this).syncCustomerToCloud(currentCustomer);
                        
                        runOnUiThread(() -> {
                            String action = isReceive ? "Received" : "Returned";
                            Toast.makeText(this, action + " ₹" + amountStr + " via " + mode, Toast.LENGTH_LONG).show();
                            loadCustomerData(); // Refresh UI
                            bottomSheet.dismiss();
                        });
                    });
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheet.show();
    }

    private void loadCustomerData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentCustomer = db.customerDao().getById(customerId);
            if (currentCustomer != null) {
                List<Receipt> orders = db.receiptDao().getReceiptsByCustomer(customerId);
                
                runOnUiThread(() -> {
                    MaterialToolbar toolbar = findViewById(R.id.toolbarCustomerDetails);
                    toolbar.setTitle(currentCustomer.getName().toUpperCase());

                    textMemberSince.setText(dateFormat.format(new Date(currentCustomer.getCreatedAt())));
                    
                    if (currentCustomer.getLastPurchaseTimestamp() > 0) {
                        textLastVisit.setText(dateFormat.format(new Date(currentCustomer.getLastPurchaseTimestamp())));
                    } else {
                        textLastVisit.setText("-");
                    }

                    textBalance.setText(String.format(Locale.getDefault(), "₹%,.0f", currentCustomer.getDueAmount()));
                    if (currentCustomer.getDueAmount() > 0) {
                        textBalance.setTextColor(0xFFEF4444); // Red
                    } else {
                        textBalance.setTextColor(0xFF1E293B); // Dark Slate
                    }

                    textDetailName.setText(currentCustomer.getName());
                    textDetailNumber.setText(currentCustomer.getMobile());
                    textDetailEmail.setText(currentCustomer.getEmail() == null || currentCustomer.getEmail().isEmpty() ? "-" : currentCustomer.getEmail());
                    textDetailGender.setText(currentCustomer.getGender() == null || currentCustomer.getGender().isEmpty() ? "-" : currentCustomer.getGender());

                    textOrdersCount.setText(String.format(Locale.getDefault(), "Orders ( %d )", orders.size()));
                    
                    if (!orders.isEmpty()) {
                        recyclerOrders.setAdapter(new ReceiptAdapter(orders));
                        // For transaction history, we use the same orders but shown as due items
                        recyclerTransactionHistory.setAdapter(new ReceiptAdapter(orders));
                    }
                });
            }
        });
    }

    private void toggleMoreDetails() {
        isMoreDetailsExpanded = !isMoreDetailsExpanded;
        layoutMoreDetailsContent.setVisibility(isMoreDetailsExpanded ? View.VISIBLE : View.GONE);
        imageMoreDetailsToggle.setImageResource(isMoreDetailsExpanded ? R.drawable.ic_arrow_drop_up : R.drawable.ic_arrow_drop_down);
    }

    private void toggleOrders() {
        isOrdersExpanded = !isOrdersExpanded;
        recyclerOrders.setVisibility(isOrdersExpanded ? View.VISIBLE : View.GONE);
        imageOrdersToggle.setImageResource(isOrdersExpanded ? R.drawable.ic_arrow_drop_up : R.drawable.ic_arrow_drop_down);
    }

    private void toggleTransactionHistory() {
        isTransactionHistoryExpanded = !isTransactionHistoryExpanded;
        layoutTransactionHistoryContent.setVisibility(isTransactionHistoryExpanded ? View.VISIBLE : View.GONE);
        imageTransactionHistoryToggle.setImageResource(isTransactionHistoryExpanded ? R.drawable.ic_arrow_drop_up : R.drawable.ic_arrow_drop_down);
    }
}
