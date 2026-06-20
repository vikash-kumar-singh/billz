package com.example.billz;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsbPrinterScanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsbDeviceAdapter adapter;
    private List<UsbDevice> deviceList = new ArrayList<>();
    private UsbManager usbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_printer_scan);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        View root = findViewById(R.id.usbScanRoot);
        View header = findViewById(R.id.headerUsbScan);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), systemBars.top + header.getPaddingBottom(), 
                             header.getPaddingRight(), header.getPaddingBottom());
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerUsbPrinters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new UsbDeviceAdapter(deviceList, device -> {
            String name = "USB Printer";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String devName = device.getProductName();
                if (devName != null) name = devName;
            }
            saveUsbPrinter(name, String.valueOf(device.getDeviceId()));
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnRefreshUsbScan).setOnClickListener(v -> scanUsbDevices());
        
        scanUsbDevices();
    }

    private void saveUsbPrinter(String name, String address) {
        AppDatabase db = AppDatabase.getInstance(this);
        Business active = db.businessDao().getSelectedBusiness();
        if (active == null) return;

        Printer existing = db.printerDao().getByAddress(address, active.getId());
        if (existing == null) {
            Printer printer = new Printer(name, "usb", name, "58mm", "Text", true, R.drawable.ic_usb);
            printer.setBusinessId(active.getId());
            printer.setAddress(address);
            db.printerDao().insert(printer);
            Toast.makeText(this, "Printer added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Printer already added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void scanUsbDevices() {
        deviceList.clear();
        HashMap<String, UsbDevice> currentDevices = usbManager.getDeviceList();
        if (currentDevices != null) {
            for (UsbDevice device : currentDevices.values()) {
                // Check if device is a printer by inspecting its interfaces
                if (isUsbPrinter(device)) {
                    deviceList.add(device);
                }
            }
        }
        adapter.notifyDataSetChanged();
        
        if (deviceList.isEmpty()) {
            Toast.makeText(this, "No USB printers found. Connect via OTG.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Scanning USB printers...", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUsbPrinter(UsbDevice device) {
        // Standard USB Printer Class is 7
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            if (device.getInterface(i).getInterfaceClass() == 7) {
                return true;
            }
        }
        // Some thermal printers might report class 0 (base) and have a printer interface
        if (device.getDeviceClass() == 7) return true;
        
        // As a fallback for many common thermal printers, we also check for vendor-specific or standard HID
        // but typically class 7 is the reliable way to identify a printer.
        return false;
    }
}
