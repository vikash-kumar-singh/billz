package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReceiptDetailsActivity extends AppCompatActivity {

    private TextView textTotalAmount, textReceiptId, textItemCount;
    private AppDatabase db;
    private int receiptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt_details);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        receiptId = getIntent().getIntExtra("receipt_id", -1);

        textTotalAmount = findViewById(R.id.textTotalAmount);
        textReceiptId = findViewById(R.id.textReceiptId);
        textItemCount = findViewById(R.id.textItemCount);

        findViewById(R.id.btnNewSale).setOnClickListener(v -> finish());
        findViewById(R.id.btnGetReceipt).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReceiptPreviewActivity.class);
            intent.putExtra("receipt_id", receiptId);
            startActivity(intent);
        });

        loadReceiptData();
    }

    private void loadReceiptData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Receipt receipt = db.receiptDao().getById(receiptId);
            if (receipt != null) {
                runOnUiThread(() -> {
                    textTotalAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", receipt.getTotalAmount()));
                    textReceiptId.setText(receipt.getReceiptNo());
                    textItemCount.setText(String.valueOf(receipt.getItemCount()));
                });
            }
        });
    }
}
