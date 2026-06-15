package com.example.billz;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class DeviceDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        TextView textDeviceName = findViewById(R.id.textDeviceName);
        TextView textDeviceCode = findViewById(R.id.textDeviceCode);
        TextView textAndroidVersion = findViewById(R.id.textAndroidVersion);
        TextView textAppVersion = findViewById(R.id.textAppVersion);
        TextView textLocale = findViewById(R.id.textLocale);
        TextView textSignedInEmail = findViewById(R.id.textSignedInEmail);

        // Fetch details
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String deviceFullName = manufacturer + " " + model + " (" + product + ")";
        textDeviceName.setText(deviceFullName);

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceCode = "#" + (androidId != null && androidId.length() >= 4 ? androidId.substring(androidId.length() - 4) : "0000");
        textDeviceCode.setText(deviceCode);

        textAndroidVersion.setText(Build.VERSION.RELEASE);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            textAppVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            textAppVersion.setText(getString(R.string.unknown_version));
        }

        textLocale.setText(Locale.getDefault().getLanguage());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            textSignedInEmail.setText(user.getEmail());
        } else {
            textSignedInEmail.setText(getString(R.string.not_signed_in));
        }

        findViewById(R.id.btnCopyDetails).setOnClickListener(v -> {
            String details = getString(R.string.device_label) + ": " + deviceFullName + "\n" +
                    getString(R.string.device_code_label) + ": " + deviceCode + "\n" +
                    getString(R.string.android_label) + ": " + Build.VERSION.RELEASE + "\n" +
                    getString(R.string.app_version_label) + ": " + textAppVersion.getText() + "\n" +
                    getString(R.string.locale_label) + ": " + textLocale.getText() + "\n" +
                    getString(R.string.signed_in_as_label) + ": " + textSignedInEmail.getText();

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Device Details", details);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getString(R.string.details_copied), Toast.LENGTH_SHORT).show();
        });
    }
}
