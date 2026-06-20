package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NetworkPrinterSetupActivity extends AppCompatActivity {

    private EditText editPrinterIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_printer_setup);

        View root = findViewById(R.id.networkSetupRoot);
        View header = findViewById(R.id.headerNetworkSetup);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), systemBars.top + header.getPaddingBottom(), 
                             header.getPaddingRight(), header.getPaddingBottom());
            return insets;
        });

        editPrinterIp = findViewById(R.id.editPrinterIp);

        findViewById(R.id.btnDoneNetwork).setOnClickListener(v -> {
            String ip = editPrinterIp.getText().toString().trim();
            if (ip.isEmpty()) {
                Toast.makeText(this, "Please enter printer IP address", Toast.LENGTH_SHORT).show();
            } else {
                saveNetworkPrinter(ip);
            }
        });
    }

    private void saveNetworkPrinter(String ip) {
        AppDatabase db = AppDatabase.getInstance(this);
        Business active = db.businessDao().getSelectedBusiness();
        if (active == null) return;

        Printer existing = db.printerDao().getByAddress(ip, active.getId());
        if (existing == null) {
            Printer printer = new Printer("Network Printer", "ethernet", "Network", "80mm", "Text", true, R.drawable.ic_network);
            printer.setBusinessId(active.getId());
            printer.setAddress(ip);
            db.printerDao().insert(printer);
            Toast.makeText(this, "Printer added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Printer already added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
