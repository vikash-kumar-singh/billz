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
    private TextView textMemberSince, textLastVisit, textOrdersCount;
    private TextView textDetailName, textDetailNumber, textDetailEmail, textDetailGender;
    private View layoutMoreDetailsContent;
    private ImageView imageMoreDetailsToggle, imageOrdersToggle;
    private RecyclerView recyclerOrders;
    private boolean isMoreDetailsExpanded = false;
    private boolean isOrdersExpanded = false;
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

        textDetailName = findViewById(R.id.textDetailName);
        textDetailNumber = findViewById(R.id.textDetailNumber);
        textDetailEmail = findViewById(R.id.textDetailEmail);
        textDetailGender = findViewById(R.id.textDetailGender);

        layoutMoreDetailsContent = findViewById(R.id.layoutMoreDetailsContent);
        imageMoreDetailsToggle = findViewById(R.id.imageMoreDetailsToggle);
        
        recyclerOrders = findViewById(R.id.recyclerCustomerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        imageOrdersToggle = findViewById(R.id.imageOrdersToggle);

        findViewById(R.id.layoutMoreDetailsHeader).setOnClickListener(v -> toggleMoreDetails());
        findViewById(R.id.layoutOrdersHeader).setOnClickListener(v -> toggleOrders());

        findViewById(R.id.btnEditCustomer).setOnClickListener(v -> 
                Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnReceiveAmount).setOnClickListener(v -> showReceiveAmountBottomSheet());

        findViewById(R.id.btnReturnAmount).setOnClickListener(v -> 
                Toast.makeText(this, "Return Amount coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void showReceiveAmountBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_receive_amount_bottom_sheet, null);
        bottomSheet.setContentView(view);

        EditText editAmount = view.findViewById(R.id.editAmount);
        TextView textSelectedPaymentMode = view.findViewById(R.id.textSelectedPaymentMode);
        View layoutPaymentModeSelector = view.findViewById(R.id.layoutPaymentModeSelector);

        // Fetch Payment Modes
        Executors.newSingleThreadExecutor().execute(() -> {
            int bId = BusinessHelper.getActiveBusinessId(this);
            List<PaymentMode> modes = db.paymentModeDao().getAllPaymentModes(bId);
            runOnUiThread(() -> {
                layoutPaymentModeSelector.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(this, layoutPaymentModeSelector);
                    if (modes.isEmpty()) {
                        popup.getMenu().add("Cash");
                        popup.getMenu().add("Debit Card");
                        popup.getMenu().add("Credit Card");
                        popup.getMenu().add("UPI");
                        popup.getMenu().add("Store Credit");
                        popup.getMenu().add("Online");
                        popup.getMenu().add("Sample Rate");
                        popup.getMenu().add("Exchange");
                        popup.getMenu().add("Google Pay");
                        popup.getMenu().add("Credit");
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
        
        view.findViewById(R.id.btnAddAmount).setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String mode = textSelectedPaymentMode.getText().toString();
            Toast.makeText(this, "Received ₹" + amountStr + " via " + mode, Toast.LENGTH_LONG).show();
            bottomSheet.dismiss();
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

                    textDetailName.setText(currentCustomer.getName());
                    textDetailNumber.setText(currentCustomer.getMobile());
                    textDetailEmail.setText(currentCustomer.getEmail() == null || currentCustomer.getEmail().isEmpty() ? "-" : currentCustomer.getEmail());
                    textDetailGender.setText(currentCustomer.getGender() == null || currentCustomer.getGender().isEmpty() ? "-" : currentCustomer.getGender());

                    textOrdersCount.setText(String.format(Locale.getDefault(), "Orders ( %d )", orders.size()));
                    
                    if (!orders.isEmpty()) {
                        recyclerOrders.setAdapter(new ReceiptAdapter(orders));
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
}
