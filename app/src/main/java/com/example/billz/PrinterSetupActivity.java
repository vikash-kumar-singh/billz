package com.example.billz;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PrinterSetupActivity extends AppCompatActivity {

    private RecyclerView recyclerPrinters;
    private PrinterAdapter adapter;
    private List<Printer> printers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setup);

        View root = findViewById(R.id.printerRoot);
        View header = findViewById(R.id.toolbarPrinter);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarPrinter);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        recyclerPrinters = findViewById(R.id.recyclerPrinters);
        recyclerPrinters.setLayoutManager(new LinearLayoutManager(this));

        loadSamplePrinters();
        
        adapter = new PrinterAdapter(printers);
        recyclerPrinters.setAdapter(adapter);

        findViewById(R.id.btnAddNewPrinter).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AddPrinterActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrintersFromDb();
    }

    private void loadPrintersFromDb() {
        AppDatabase db = AppDatabase.getInstance(this);
        Business active = db.businessDao().getSelectedBusiness();
        if (active != null) {
            printers.clear();
            List<Printer> dbPrinters = db.printerDao().getAllPrinters(active.getId());
            if (dbPrinters.isEmpty()) {
                loadSamplePrinters(); // Optional: show samples if empty
            } else {
                printers.addAll(dbPrinters);
            }
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void loadSamplePrinters() {
        // Matches the image provided
        printers.add(new Printer("Any A4/A3/A5 Etc.", "via android default", null, null, null, false, R.drawable.ic_printer));
        printers.add(new Printer("Thermal Printer", "bluetooth", "Thermal Printer", "58mm", "Text", true, R.drawable.ic_bluetooth));
        printers.add(new Printer("MPT-II", "bluetooth", "MPT-II", "58mm", "Text", true, R.drawable.ic_bluetooth));
    }
}
