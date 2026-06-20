package com.example.billz;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddPrinterActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_printer);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        setupLaunchers();

        View root = findViewById(R.id.addPrinterRoot);
        View header = findViewById(R.id.headerAddPrinter);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), systemBars.top + header.getPaddingBottom(), 
                             header.getPaddingRight(), header.getPaddingBottom());
            return insets;
        });

        findViewById(R.id.btnBluetoothPrinter).setOnClickListener(v -> checkBluetoothAndPermissions());
        
        findViewById(R.id.btnUsbPrinter).setOnClickListener(v -> 
            startActivity(new Intent(this, UsbPrinterScanActivity.class)));
        
        findViewById(R.id.btnNetworkPrinter).setOnClickListener(v -> 
            startActivity(new Intent(this, NetworkPrinterSetupActivity.class)));
    }

    private void setupLaunchers() {
        bluetoothEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        startActivity(new Intent(this, BluetoothPrinterScanActivity.class));
                    } else {
                        Toast.makeText(this, "Bluetooth is required to scan for printers", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean granted : permissions.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        enableBluetooth();
                    } else {
                        Toast.makeText(this, "Permissions required for Bluetooth scanning", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkBluetoothAndPermissions() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                });
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                return;
            }
        }

        enableBluetooth();
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        } else {
            startActivity(new Intent(this, BluetoothPrinterScanActivity.class));
        }
    }
}
