package com.example.billz;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothPrinterScanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BluetoothDeviceAdapter adapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Scan finished", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_printer_scan);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        View root = findViewById(R.id.bluetoothScanRoot);
        View header = findViewById(R.id.headerBluetoothScan);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), systemBars.top + header.getPaddingBottom(), 
                             header.getPaddingRight(), header.getPaddingBottom());
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerAvailablePrinters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new BluetoothDeviceAdapter(deviceList, device -> {
            String name = "Bluetooth Printer";
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String devName = device.getName();
                if (devName != null) name = devName;
            }
            saveBluetoothPrinter(name, device.getAddress());
        });
        recyclerView.setAdapter(adapter);

        loadPairedDevices();

        findViewById(R.id.btnRefreshScan).setOnClickListener(v -> startDiscovery());

        findViewById(R.id.btnBluetoothSettings).setOnClickListener(v -> {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Could not open Bluetooth settings", Toast.LENGTH_SHORT).show();
            }
        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        
        startDiscovery();
    }

    private void saveBluetoothPrinter(String name, String address) {
        AppDatabase db = AppDatabase.getInstance(this);
        Business active = db.businessDao().getSelectedBusiness();
        if (active == null) return;

        Printer existing = db.printerDao().getByAddress(address, active.getId());
        if (existing == null) {
            Printer printer = new Printer(name, "bluetooth", name, "58mm", "Text", true, R.drawable.ic_bluetooth);
            printer.setBusinessId(active.getId());
            printer.setAddress(address);
            db.printerDao().insert(printer);
            Toast.makeText(this, "Printer added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Printer already added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void loadPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void startDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        deviceList.clear();
        loadPairedDevices();
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, "Scanning for available printers...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null) bluetoothAdapter.cancelDiscovery();
        }
    }
}
