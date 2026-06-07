package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import java.util.concurrent.Executors;

public class ReturnedReceiptsActivity extends AppCompatActivity {

    private RecyclerView recyclerReceipts;
    private AppDatabase db;
    private int currentBusinessId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipts); // Reuse the same layout

        db = AppDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarReceipts);
        toolbar.setTitle("Returned Receipts");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Hide date range selectors for returned list
        ((View)findViewById(R.id.layoutDateFrom).getParent()).setVisibility(View.GONE);
        findViewById(R.id.btnFilterAll).setVisibility(View.GONE);

        recyclerReceipts = findViewById(R.id.recyclerReceipts);
        recyclerReceipts.setLayoutManager(new LinearLayoutManager(this));

        loadCurrentBusinessAndReceipts();
    }

    private void loadCurrentBusinessAndReceipts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Business selected = db.businessDao().getSelectedBusiness();
            if (selected != null) {
                currentBusinessId = selected.getId();
            }
            loadReceipts();
        });
    }

    private void loadReceipts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Receipt> list = db.receiptDao().getReturnedReceipts(currentBusinessId);
            runOnUiThread(() -> {
                recyclerReceipts.setAdapter(new ReceiptAdapter(list));
            });
        });
    }
}
