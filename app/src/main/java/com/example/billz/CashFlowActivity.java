package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CashFlowActivity extends AppCompatActivity {

    private TextView textDateRange, textEmptyDateRange;

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
        setContentView(R.layout.activity_cash_flow);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCashFlow);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        textDateRange = findViewById(R.id.textDateRange);
        textEmptyDateRange = findViewById(R.id.textEmptyDateRange);

        setupDateRange();

        View.OnClickListener incomeListener = v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("START_TAB", 1); // 1 for INCOME
            startActivity(intent);
        };
        
        View.OnClickListener expenseListener = v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("START_TAB", 0); // 0 for EXPENSE
            startActivity(intent);
        };

        findViewById(R.id.btnIncome).setOnClickListener(incomeListener);
        findViewById(R.id.boxIncome).setOnClickListener(incomeListener);
        
        findViewById(R.id.btnExpense).setOnClickListener(expenseListener);
        findViewById(R.id.boxExpense).setOnClickListener(expenseListener);

        View appBar = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void setupDateRange() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        
        String endDate = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        String startDate = sdf.format(calendar.getTime());
        
        String range = startDate + " - " + endDate;
        textDateRange.setText(range);
        textEmptyDateRange.setText(range);
    }
}
