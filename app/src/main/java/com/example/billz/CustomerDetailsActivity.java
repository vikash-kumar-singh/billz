package com.example.billz;

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

        findViewById(R.id.btnReceiveAmount).setOnClickListener(v -> 
                Toast.makeText(this, "Receive Amount coming soon", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnReturnAmount).setOnClickListener(v -> 
                Toast.makeText(this, "Return Amount coming soon", Toast.LENGTH_SHORT).show()
        );
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
