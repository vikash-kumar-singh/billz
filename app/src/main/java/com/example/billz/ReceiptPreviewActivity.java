package com.example.billz;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReceiptPreviewActivity extends AppCompatActivity {

    private TextView textBusinessName, textBusinessAddress, textBusinessTaxNo, textBusinessWebsite, textBusinessPhone;
    private TextView textReceiptNo, textDate, textPMode, textICount, textUCount, textTotalTable;
    private TextView textSubtotal, textGrandTotal, textReceived, textChange, textThankYou, textCustomerName, textReturnedBy;
    private ImageView imgBusinessLogo;
    private TableLayout tableItems;
    private AppDatabase db;
    private String receiptId;
    private Receipt currentReceipt;
    private List<ReceiptItem> currentItems;
    private Customer currentCustomer;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy - h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt_preview);

        db = AppDatabase.getInstance(this);
        receiptId = getIntent().getStringExtra("receipt_id");

        MaterialToolbar toolbar = findViewById(R.id.toolbarPreview);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        loadReceiptData();
        
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnDownload).setOnClickListener(v -> downloadInvoice());

        findViewById(R.id.btnReturn).setOnClickListener(v -> showReturnDialog());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteDialog());

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditReceiptActivity.class);
            intent.putExtra("receipt_id", receiptId);
            startActivity(intent);
        });

        findViewById(R.id.btnNewSale).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.imgActionShare).setOnClickListener(v -> showShareBottomSheet());
        findViewById(R.id.imgActionMessage).setOnClickListener(v -> showShareBottomSheet());
    }

    private void downloadInvoice() {
        // Logic to save invoice view as image
        View invoiceView = findViewById(R.id.invoiceCard); // Need to add ID to CardView
        if (invoiceView == null) invoiceView = findViewById(android.R.id.content);

        try {
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(invoiceView.getWidth(), invoiceView.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            invoiceView.draw(canvas);

            String fileName = "Invoice_" + System.currentTimeMillis() + ".png";
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Billz");

                Uri uri = getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
                        Toast.makeText(this, "Invoice downloaded to Pictures/Billz", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                String path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES).toString() + "/" + fileName;
                java.io.File file = new java.io.File(path);
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
                    Toast.makeText(this, "Invoice downloaded: " + path, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("ReceiptPreview", "Download failed", e);
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showReturnDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_return);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnYesReturn).setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                Receipt receipt = db.receiptDao().getById(receiptId);
                if (receipt != null && !receipt.isReturned()) {
                    receipt.setReturned(true);
                    receipt.setUpdatedAt(System.currentTimeMillis());
                    
                    // Update locally and in cloud
                    db.receiptDao().update(receipt);
                    new ReceiptCloudRepository(this).updateReceipt(receipt);
                    
                    int bId = BusinessHelper.getActiveBusinessId(this);
                    ItemCloudRepository itemCloudRepo = new ItemCloudRepository(this);

                    // Restock Logic
                    List<ReceiptItem> items = db.receiptItemDao().getItemsForReceipt(receiptId);
                    for (ReceiptItem ri : items) {
                        // 1. Find and update base item stock
                        Item item = null;
                        if (ri.getItemId() != null) {
                            item = db.itemDao().getById(ri.getItemId());
                        } else {
                            item = db.itemDao().getByName(ri.getItemName(), bId);
                        }

                        if (item != null) {
                            int newItemQty = item.getStockQuantity() + ri.getQuantity();
                            item.setStockQuantity(newItemQty);
                            db.itemDao().update(item);
                            itemCloudRepo.updateStock(item.getId(), newItemQty);

                            // 2. Find and update variant stock if applicable
                            if (ri.getVariantId() != null) {
                                Variant variant = db.variantDao().getById(ri.getVariantId());
                                if (variant != null) {
                                    int newVariantQty = variant.getStockQuantity() + ri.getQuantity();
                                    variant.setStockQuantity(newVariantQty);
                                    db.variantDao().update(variant);
                                    itemCloudRepo.updateVariantStock(item.getId(), variant.getId(), newVariantQty);
                                }
                            } else if (ri.getVariantName() != null && !ri.getVariantName().isEmpty() && !ri.getVariantName().equalsIgnoreCase("Default")) {
                                Variant variant = db.variantDao().getByName(item.getId(), ri.getVariantName());
                                if (variant != null) {
                                    int newVariantQty = variant.getStockQuantity() + ri.getQuantity();
                                    variant.setStockQuantity(newVariantQty);
                                    db.variantDao().update(variant);
                                    itemCloudRepo.updateVariantStock(item.getId(), variant.getId(), newVariantQty);
                                }
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Receipt returned and items restocked", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        });
        dialog.show();
    }

    private void showDeleteDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnYesDelete).setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                db.receiptItemDao().deleteByReceiptId(receiptId);
                db.receiptDao().deleteById(receiptId);
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Receipt Deleted Permanently", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
        dialog.show();
    }

    private void initViews() {
        textBusinessName = findViewById(R.id.textBusinessName);
        textBusinessAddress = findViewById(R.id.textBusinessAddress);
        textBusinessTaxNo = findViewById(R.id.textBusinessTaxNo);
        textBusinessWebsite = findViewById(R.id.textBusinessWebsite);
        textBusinessPhone = findViewById(R.id.textBusinessPhone);
        imgBusinessLogo = findViewById(R.id.imgBusinessLogo);

        textCustomerName = findViewById(R.id.textCustomerName);
        textReceiptNo = findViewById(R.id.textReceiptNo);
        textDate = findViewById(R.id.textDate);
        textPMode = findViewById(R.id.textPMode);
        textICount = findViewById(R.id.textICount);
        textUCount = findViewById(R.id.textUCount);
        textTotalTable = findViewById(R.id.textTotalTable);
        textSubtotal = findViewById(R.id.textSubtotal);
        textGrandTotal = findViewById(R.id.textGrandTotal);
        textThankYou = findViewById(R.id.textThankYou);
        textReturnedBy = findViewById(R.id.textReturnedBy);
        tableItems = findViewById(R.id.tableItems);
    }

    private void loadReceiptData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentReceipt = db.receiptDao().getById(receiptId);
            if (currentReceipt == null) return;
            
            int bId = BusinessHelper.getActiveBusinessId(this);
            
            currentItems = db.receiptItemDao().getItemsForReceipt(receiptId);
            if (currentReceipt.getCustomerId() != null) {
                currentCustomer = db.customerDao().getById(currentReceipt.getCustomerId());
            }
            
            ReceiptSettings settings = db.receiptSettingsDao().getSettingsByBusiness(bId);
            Business business = db.businessDao().getById(bId);

            runOnUiThread(() -> {
                if (settings != null) {
                    textBusinessName.setText(settings.getBusinessName() != null ? settings.getBusinessName().toUpperCase() : 
                                           (business != null ? business.getName().toUpperCase() : "BUSINESS NAME"));
                    
                    if (settings.getBusinessAddress() != null && !settings.getBusinessAddress().isEmpty()) {
                        textBusinessAddress.setText(settings.getBusinessAddress());
                        textBusinessAddress.setVisibility(View.VISIBLE);
                    } else {
                        textBusinessAddress.setVisibility(View.GONE);
                    }

                    if (settings.getTaxNoAndTitle() != null && !settings.getTaxNoAndTitle().isEmpty()) {
                        textBusinessTaxNo.setText(settings.getTaxNoAndTitle());
                        textBusinessTaxNo.setVisibility(View.VISIBLE);
                    } else {
                        textBusinessTaxNo.setVisibility(View.GONE);
                    }

                    if (settings.getWebsite() != null && !settings.getWebsite().isEmpty()) {
                        textBusinessWebsite.setText(settings.getWebsite());
                        textBusinessWebsite.setVisibility(View.VISIBLE);
                    } else {
                        textBusinessWebsite.setVisibility(View.GONE);
                    }

                    if (settings.getPhoneNumber() != null && !settings.getPhoneNumber().isEmpty()) {
                        textBusinessPhone.setText(settings.getPhoneNumber());
                        textBusinessPhone.setVisibility(View.VISIBLE);
                    } else {
                        textBusinessPhone.setVisibility(View.GONE);
                    }

                    if (settings.getBusinessLogoPath() != null && !settings.getBusinessLogoPath().isEmpty()) {
                        try {
                            imgBusinessLogo.setImageURI(Uri.parse(settings.getBusinessLogoPath()));
                            imgBusinessLogo.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            imgBusinessLogo.setImageResource(R.drawable.ic_nav_reports);
                        }
                    } else if (business != null && business.getLogoPath() != null && !business.getLogoPath().isEmpty()) {
                        try {
                            imgBusinessLogo.setImageURI(Uri.parse(business.getLogoPath()));
                            imgBusinessLogo.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            imgBusinessLogo.setImageResource(R.drawable.ic_nav_reports);
                        }
                    } else {
                        imgBusinessLogo.setImageResource(R.drawable.ic_nav_reports);
                    }

                    textThankYou.setText(settings.getThankYouNote() != null ? settings.getThankYouNote() : "Thank You! Visit again!");
                } else if (business != null) {
                    textBusinessName.setText(business.getName().toUpperCase());
                    textBusinessPhone.setText(business.getPhoneNumber());
                    textBusinessPhone.setVisibility(business.getPhoneNumber() != null && !business.getPhoneNumber().isEmpty() ? View.VISIBLE : View.GONE);
                    textBusinessAddress.setVisibility(View.GONE);
                    textBusinessTaxNo.setVisibility(View.GONE);
                    textBusinessWebsite.setVisibility(View.GONE);
                    
                    if (business.getLogoPath() != null && !business.getLogoPath().isEmpty()) {
                        try {
                            imgBusinessLogo.setImageURI(Uri.parse(business.getLogoPath()));
                            imgBusinessLogo.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            imgBusinessLogo.setImageResource(R.drawable.ic_nav_reports);
                        }
                    } else {
                        imgBusinessLogo.setImageResource(R.drawable.ic_nav_reports);
                    }
                    textThankYou.setText("Thank You! Visit again!");
                }

                textCustomerName.setText(currentReceipt.getCustomerName() != null && !currentReceipt.getCustomerName().isEmpty() ? currentReceipt.getCustomerName() : "Cash Customer");
                textReceiptNo.setText("Receipt# " + currentReceipt.getReceiptNo());
                textDate.setText("Date : " + dateTimeFormat.format(new Date(currentReceipt.getTimestamp())));
                
                textPMode.setText(currentReceipt.getPaymentMode());
                textICount.setText(String.valueOf(currentReceipt.getItemCount()));

                if (currentReceipt.isReturned()) {
                    if (textReturnedBy != null) {
                        textReturnedBy.setVisibility(View.VISIBLE);
                        String bName = (settings != null && settings.getBusinessName() != null) ? settings.getBusinessName() : 
                                      (business != null ? business.getName() : "Business");
                        textReturnedBy.setText("Returned by " + bName);
                    }
                    
                    // Simplify header as per image (Only back and download)
                    findViewById(R.id.imgActionShare).setVisibility(View.GONE);
                    findViewById(R.id.imgActionMessage).setVisibility(View.GONE);
                    findViewById(R.id.imgActionUpload).setVisibility(View.GONE);
                    findViewById(R.id.imgActionPrint).setVisibility(View.GONE);

                    // Remove all action buttons for returned receipts
                    View actionLayout = findViewById(R.id.layoutActionButtons);
                    if (actionLayout != null) actionLayout.setVisibility(View.GONE);
                } else {
                    if (textReturnedBy != null) textReturnedBy.setVisibility(View.GONE);
                    findViewById(R.id.imgActionShare).setVisibility(View.VISIBLE);
                    findViewById(R.id.imgActionMessage).setVisibility(View.VISIBLE);
                    findViewById(R.id.imgActionUpload).setVisibility(View.VISIBLE);
                    findViewById(R.id.imgActionPrint).setVisibility(View.VISIBLE);

                    View actionLayout = findViewById(R.id.layoutActionButtons);
                    if (actionLayout != null) actionLayout.setVisibility(View.VISIBLE);
                    
                    View btnDelete = findViewById(R.id.btnDelete);
                    if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
                }
                
                int totalUnits = 0;
                tableItems.removeAllViews();
                for (ReceiptItem item : currentItems) {
                    totalUnits += item.getQuantity();
                    addTableRow(item);
                }
                textUCount.setText(String.valueOf(totalUnits));
                
                String totalStr = String.format(Locale.getDefault(), "₹%,.2f", currentReceipt.getTotalAmount());
                textTotalTable.setText(totalStr);
                textSubtotal.setText(totalStr);
                textGrandTotal.setText(totalStr);
            });
        });
    }

    private void showShareBottomSheet() {
        if (currentReceipt == null) return;

        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_receipt_share_bottom_sheet, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.optionWhatsApp).setOnClickListener(v -> {
            bottomSheet.dismiss();
            shareToWhatsApp();
        });

        view.findViewById(R.id.optionEmail).setOnClickListener(v -> {
            bottomSheet.dismiss();
            shareToEmail();
        });

        view.findViewById(R.id.optionSMS).setOnClickListener(v -> {
            bottomSheet.dismiss();
            shareToSMS();
        });

        bottomSheet.show();
    }

    private void shareToWhatsApp() {
        String message = generateShareText();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "whatsapp://send?text=" + Uri.encode(message);
            // If we have customer mobile, we can pre-fill it
            String mobile = getCustomerMobile();
            if (mobile != null && !mobile.isEmpty()) {
                String cleanMobile = mobile.replaceAll("[^\\d]", "");
                if (cleanMobile.length() == 10) cleanMobile = "91" + cleanMobile;
                url = "whatsapp://send?phone=" + cleanMobile + "&text=" + Uri.encode(message);
            }
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to general share
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            shareIntent.setPackage("com.whatsapp");
            try {
                startActivity(shareIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareToEmail() {
        String message = generateShareText();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Receipt from " + textBusinessName.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToSMS() {
        String message = generateShareText();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        String mobile = getCustomerMobile();
        if (mobile != null && !mobile.isEmpty()) {
            intent.setData(Uri.parse("smsto:" + mobile));
        } else {
            intent.setData(Uri.parse("smsto:"));
        }
        intent.putExtra("sms_body", message);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open SMS app", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateShareText() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(textBusinessName.getText().toString()).append(" ---\n");
        String address = textBusinessAddress.getText().toString();
        if (!address.equals("Business Address") && !address.isEmpty()) sb.append(address).append("\n");
        sb.append("Receipt#: ").append(currentReceipt.getReceiptNo()).append("\n");
        sb.append("Date: ").append(dateTimeFormat.format(new Date(currentReceipt.getTimestamp()))).append("\n");
        sb.append("Customer: ").append(textCustomerName.getText().toString()).append("\n\n");
        
        sb.append("Items:\n");
        for (ReceiptItem item : currentItems) {
            sb.append("- ").append(item.getItemName());
            if (item.getVariantName() != null && !item.getVariantName().isEmpty() && !item.getVariantName().equalsIgnoreCase("Default")) {
                sb.append(" (").append(item.getVariantName()).append(")");
            }
            sb.append(": ").append(item.getQuantity()).append(" x ").append(String.format(Locale.getDefault(), "₹%,.2f", item.getPrice())).append("\n");
        }
        
        sb.append("\nSubtotal: ").append(String.format(Locale.getDefault(), "₹%,.2f", currentReceipt.getSubtotal())).append("\n");
        if (currentReceipt.getTax() > 0) sb.append("Tax: ").append(String.format(Locale.getDefault(), "₹%,.2f", currentReceipt.getTax())).append("\n");
        if (currentReceipt.getDiscount() > 0) sb.append("Discount: ").append(String.format(Locale.getDefault(), "₹%,.2f", currentReceipt.getDiscount())).append("\n");
        sb.append("Grand Total: ").append(String.format(Locale.getDefault(), "₹%,.2f", currentReceipt.getTotalAmount())).append("\n\n");
        
        sb.append(textThankYou.getText().toString()).append("\n");
        sb.append("Powered by Billz");
        
        return sb.toString();
    }

    private String getCustomerMobile() {
        if (currentCustomer != null) return currentCustomer.getMobile();
        return null;
    }

    private void addTableRow(ReceiptItem item) {
        TableRow row = new TableRow(this);
        row.setPadding(0, (int)(8 * getResources().getDisplayMetrics().density), 0, (int)(8 * getResources().getDisplayMetrics().density));

        TextView name = new TextView(this);
        name.setText(item.getItemName() + (item.getVariantName() == null || item.getVariantName().isEmpty() ? "" : "\n" + item.getVariantName()));
        name.setTextColor(0xFF334155);
        name.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        name.setPadding(4, 0, 4, 0);

        TextView price = new TextView(this);
        price.setText(String.format(Locale.getDefault(), "₹%,.2f", item.getPrice()));
        price.setTextColor(0xFF334155);
        price.setGravity(Gravity.END);
        price.setPadding(32, 0, 16, 0); 

        TextView qty = new TextView(this);
        qty.setText(String.valueOf(item.getQuantity()));
        qty.setTextColor(0xFF334155);
        qty.setGravity(Gravity.CENTER);
        qty.setPadding(24, 0, 24, 0); 

        TextView total = new TextView(this);
        total.setText(String.format(Locale.getDefault(), "₹%,.2f", item.getPrice() * item.getQuantity()));
        total.setTextColor(0xFF334155);
        total.setGravity(Gravity.END);
        total.setPadding(16, 0, 4, 0);

        row.addView(name);
        row.addView(price);
        row.addView(qty);
        row.addView(total);

        tableItems.addView(row);
    }
}
