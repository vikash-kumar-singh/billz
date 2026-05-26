package com.example.billz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReceiptSettingsActivity extends AppCompatActivity {

    private EditText etBusinessName, etPhoneNumber, etBusinessAddress, etTaxNo, etWebsite, etReceiptTitle, etThankYouNote, etMessageTemplate;
    private CheckBox cbShowPhoneNumber, cbShowBusinessAddress, cbShowTaxNo, cbShowWebsite, cbShowLogo;
    private SwitchCompat switchListPrice, switchShowRate, switchTotalSaved, switchCashierName, switchCustomerPhone, switchCustomerAddress;
    private SwitchCompat switchAppLang, switchTotalItem, switchChangeReturn, switchPaymentDetails, switchPoweredBy;
    private RadioButton rbOrderName, rbOrderAdded, rbWhatsapp, rbWhatsappBusiness;
    private ImageView ivBusinessLogo, ivAddIcon;
    private AppDatabase db;
    private ReceiptSettings currentSettings;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    currentSettings.setBusinessLogoPath(uri.toString());
                    updateLogoUI(uri);
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt_settings);

        db = AppDatabase.getInstance(this);

        initViews();
        loadSettings();

        findViewById(R.id.imageBack).setOnClickListener(v -> finish());
        findViewById(R.id.layoutSave).setOnClickListener(v -> saveSettings());
        
        findViewById(R.id.layoutAddLogo).setOnClickListener(v -> 
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));

        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void initViews() {
        etBusinessName = findViewById(R.id.etBusinessName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBusinessAddress = findViewById(R.id.etBusinessAddress);
        etTaxNo = findViewById(R.id.etTaxNo);
        etWebsite = findViewById(R.id.etWebsite);
        etReceiptTitle = findViewById(R.id.etReceiptTitle);
        etThankYouNote = findViewById(R.id.etThankYouNote);
        etMessageTemplate = findViewById(R.id.etMessageTemplate);

        cbShowPhoneNumber = findViewById(R.id.cbShowPhoneNumber);
        cbShowBusinessAddress = findViewById(R.id.cbShowBusinessAddress);
        cbShowTaxNo = findViewById(R.id.cbShowTaxNo);
        cbShowWebsite = findViewById(R.id.cbShowWebsite);
        cbShowLogo = findViewById(R.id.cbShowLogo);

        switchListPrice = findViewById(R.id.switchListPrice);
        switchShowRate = findViewById(R.id.switchShowRate);
        switchTotalSaved = findViewById(R.id.switchTotalSaved);
        switchCashierName = findViewById(R.id.switchCashierName);
        switchCustomerPhone = findViewById(R.id.switchCustomerPhone);
        switchCustomerAddress = findViewById(R.id.switchCustomerAddress);
        
        switchAppLang = findViewById(R.id.switchAppLang);
        switchTotalItem = findViewById(R.id.switchTotalItem);
        switchChangeReturn = findViewById(R.id.switchChangeReturn);
        switchPaymentDetails = findViewById(R.id.switchPaymentDetails);
        switchPoweredBy = findViewById(R.id.switchPoweredBy);

        rbOrderName = findViewById(R.id.rbOrderName);
        rbOrderAdded = findViewById(R.id.rbOrderAdded);

        rbWhatsapp = findViewById(R.id.rbWhatsapp);
        rbWhatsappBusiness = findViewById(R.id.rbWhatsappBusiness);

        ivBusinessLogo = findViewById(R.id.ivBusinessLogo);
        ivAddIcon = findViewById(R.id.ivAddIcon);
    }

    private void updateLogoUI(Uri uri) {
        if (uri != null) {
            ivBusinessLogo.setImageURI(uri);
            ivAddIcon.setVisibility(View.GONE);
        } else {
            ivBusinessLogo.setImageDrawable(null);
            ivAddIcon.setVisibility(View.VISIBLE);
        }
    }

    private void loadSettings() {
        new Thread(() -> {
            currentSettings = db.receiptSettingsDao().getSettings();
            if (currentSettings != null) {
                runOnUiThread(() -> {
                    etBusinessName.setText(currentSettings.getBusinessName());
                    etPhoneNumber.setText(currentSettings.getPhoneNumber());
                    cbShowPhoneNumber.setChecked(currentSettings.isShowPhoneNumber());
                    etBusinessAddress.setText(currentSettings.getBusinessAddress());
                    cbShowBusinessAddress.setChecked(currentSettings.isShowBusinessAddress());
                    etTaxNo.setText(currentSettings.getTaxNoAndTitle());
                    cbShowTaxNo.setChecked(currentSettings.isShowTaxNo());
                    etWebsite.setText(currentSettings.getWebsite());
                    cbShowWebsite.setChecked(currentSettings.isShowWebsite());
                    etReceiptTitle.setText(currentSettings.getReceiptTitle());
                    etThankYouNote.setText(currentSettings.getThankYouNote());
                    etMessageTemplate.setText(currentSettings.getMessageTemplate());
                    
                    cbShowLogo.setChecked(currentSettings.isShowLogo());
                    switchListPrice.setChecked(currentSettings.isShowListPrice());
                    switchShowRate.setChecked(currentSettings.isShowRateInReceipt());
                    switchTotalSaved.setChecked(currentSettings.isShowTotalMoneySaved());
                    switchCashierName.setChecked(currentSettings.isShowCashierName());
                    switchCustomerPhone.setChecked(currentSettings.isShowCustomerPhone());
                    switchCustomerAddress.setChecked(currentSettings.isShowCustomerAddress());
                    
                    switchAppLang.setChecked(currentSettings.isUseAppLanguage());
                    switchTotalItem.setChecked(currentSettings.isShowTotalItemCount());
                    switchChangeReturn.setChecked(currentSettings.isShowChangeReturn());
                    switchPaymentDetails.setChecked(currentSettings.isShowPaymentDetails());
                    switchPoweredBy.setChecked(currentSettings.isShowPoweredBy());

                    if ("OrderAdded".equals(currentSettings.getOrderItemsBy())) {
                        rbOrderAdded.setChecked(true);
                    } else {
                        rbOrderName.setChecked(true);
                    }

                    if ("WhatsAppBusiness".equals(currentSettings.getWhatsappShareApp())) {
                        rbWhatsappBusiness.setChecked(true);
                    } else {
                        rbWhatsapp.setChecked(true);
                    }

                    if (currentSettings.getBusinessLogoPath() != null) {
                        updateLogoUI(Uri.parse(currentSettings.getBusinessLogoPath()));
                    }
                });
            } else {
                currentSettings = new ReceiptSettings();
                // Set defaults based on image
                runOnUiThread(() -> {
                    switchShowRate.setChecked(true);
                    switchCustomerPhone.setChecked(true);
                    switchTotalItem.setChecked(true);
                    switchChangeReturn.setChecked(true);
                    switchPaymentDetails.setChecked(true);
                    switchPoweredBy.setChecked(true);
                    rbOrderName.setChecked(true);
                    rbWhatsapp.setChecked(true);
                    etMessageTemplate.setText(getString(R.string.template_default));
                });
            }
        }).start();
    }

    private void saveSettings() {
        String businessName = etBusinessName.getText().toString().trim();
        if (businessName.isEmpty()) {
            Toast.makeText(this, "Business Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSettings.setBusinessName(businessName);
        currentSettings.setPhoneNumber(etPhoneNumber.getText().toString().trim());
        currentSettings.setShowPhoneNumber(cbShowPhoneNumber.isChecked());
        currentSettings.setBusinessAddress(etBusinessAddress.getText().toString().trim());
        currentSettings.setShowBusinessAddress(cbShowBusinessAddress.isChecked());
        currentSettings.setTaxNoAndTitle(etTaxNo.getText().toString().trim());
        currentSettings.setShowTaxNo(cbShowTaxNo.isChecked());
        currentSettings.setWebsite(etWebsite.getText().toString().trim());
        currentSettings.setShowWebsite(cbShowWebsite.isChecked());
        currentSettings.setReceiptTitle(etReceiptTitle.getText().toString().trim());
        currentSettings.setThankYouNote(etThankYouNote.getText().toString().trim());
        currentSettings.setMessageTemplate(etMessageTemplate.getText().toString().trim());
        
        currentSettings.setShowLogo(cbShowLogo.isChecked());
        currentSettings.setShowListPrice(switchListPrice.isChecked());
        currentSettings.setShowRateInReceipt(switchShowRate.isChecked());
        currentSettings.setShowTotalMoneySaved(switchTotalSaved.isChecked());
        currentSettings.setShowCashierName(switchCashierName.isChecked());
        currentSettings.setShowCustomerPhone(switchCustomerPhone.isChecked());
        currentSettings.setShowCustomerAddress(switchCustomerAddress.isChecked());

        currentSettings.setUseAppLanguage(switchAppLang.isChecked());
        currentSettings.setShowTotalItemCount(switchTotalItem.isChecked());
        currentSettings.setShowChangeReturn(switchChangeReturn.isChecked());
        currentSettings.setShowPaymentDetails(switchPaymentDetails.isChecked());
        currentSettings.setShowPoweredBy(switchPoweredBy.isChecked());

        currentSettings.setOrderItemsBy(rbOrderAdded.isChecked() ? "OrderAdded" : "Name");
        currentSettings.setWhatsappShareApp(rbWhatsappBusiness.isChecked() ? "WhatsAppBusiness" : "WhatsApp");

        new Thread(() -> {
            db.receiptSettingsDao().insert(currentSettings);
            runOnUiThread(() -> {
                Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
