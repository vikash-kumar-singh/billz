package com.example.billz;

import android.content.ContentValues;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AddItemActivity extends AppCompatActivity {

    private EditText editItemName, editSellingPriceSimple, editStockSimple;
    private TextView btnSimple, btnAdvance, textCategory;
    private View layoutSimpleMode, layoutAdvanceMode;
    private LinearLayout containerVariants;
    private android.widget.ProgressBar progressSave;
    private boolean isSimpleMode = true;
    private String selectedSellBy = "Unit";
    private List<View> variantViews = new ArrayList<>();

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> addCategoryLauncher;
    private View activeVariantView;
    private android.net.Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        setupImageLaunchers();
        setupCategoryLauncher();

        MaterialToolbar toolbar = findViewById(R.id.toolbarItem);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarItemLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        toolbar.setNavigationOnClickListener(v -> finish());
        
        editItemName = findViewById(R.id.editItemName);
        editSellingPriceSimple = findViewById(R.id.editSellingPriceSimple);
        editStockSimple = findViewById(R.id.editStockSimple);
        
        textCategory = findViewById(R.id.textCategory);
        btnSimple = findViewById(R.id.btnSimple);
        btnAdvance = findViewById(R.id.btnAdvance);
        layoutSimpleMode = findViewById(R.id.layoutSimpleMode);
        layoutAdvanceMode = findViewById(R.id.layoutAdvanceMode);
        containerVariants = findViewById(R.id.containerVariants);

        btnSimple.setOnClickListener(v -> setMode(true));
        btnAdvance.setOnClickListener(v -> setMode(false));

        findViewById(R.id.cardCategory).setOnClickListener(v -> showSelectCategoryBottomSheet());
        findViewById(R.id.cardSellByUnit).setOnClickListener(v -> showSellByBottomSheet());

        findViewById(R.id.cardSimpleImage).setOnClickListener(v -> showImageSourceDialog(v));
        findViewById(R.id.btnAddVariant).setOnClickListener(v -> addNewVariantView());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());
        progressSave = findViewById(R.id.progressSave);

        // Handle pre-selected category
        String preselectedCategory = getIntent().getStringExtra("category");
        if (preselectedCategory != null) {
            textCategory.setText(preselectedCategory);
            textCategory.setTextColor(0xFF000000);
        }

        // Initialize first variant for advance mode
        addNewVariantView();
    }

    private void setupCategoryLauncher() {
        addCategoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Refresh categories if needed, or if we want to auto-select
                // For now, we'll just show the bottom sheet again or trust the user will see it
            }
        });
    }

    private void setupImageLaunchers() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                android.net.Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception e) {
                        Log.e("AddItem", "Failed to take persistable permission", e);
                    }
                    updateVariantImage(uri);
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                updateVariantImage(cameraImageUri);
            }
        });
    }

    private void updateVariantImage(android.net.Uri uri) {
        if (activeVariantView != null && uri != null) {
            ImageView img = activeVariantView.findViewById(R.id.imgVariant);
            if (img == null) img = activeVariantView.findViewById(R.id.imgSimple);
            
            if (img != null) {
                Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(img);
                img.setImageTintList(null);
                img.setPadding(0, 0, 0, 0);
                activeVariantView.setTag(R.id.imgVariant, uri.toString());
            }
        }
    }

    private void showImageSourceDialog(View variantView) {
        activeVariantView = variantView;
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_image_source_selector, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            bottomSheet.dismiss();
            openCamera();
        });

        view.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            bottomSheet.dismiss();
            openGallery();
        });

        bottomSheet.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 101);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (cameraImageUri == null) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(intent);
    }

    private void addNewVariantView() {
        View variantView = LayoutInflater.from(this).inflate(R.layout.item_add_variant, containerVariants, false);
        variantViews.add(variantView);
        containerVariants.addView(variantView);

        variantView.findViewById(R.id.cardVariantImage).setOnClickListener(v -> showImageSourceDialog(variantView));

        variantView.findViewById(R.id.btnRemoveVariant).setOnClickListener(v -> {
            if (variantViews.size() > 1) {
                variantViews.remove(variantView);
                containerVariants.removeView(variantView);
            } else {
                Toast.makeText(this, "At least one variant is required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveItem() {
        String name = editItemName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = textCategory.getText().toString();
        if (category.equals("Ex: Fruits") || category.equals("Select Category") || category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<VariantData> variantsToSave = new ArrayList<>();

        if (isSimpleMode) {
            String priceStr = editSellingPriceSimple.getText().toString().trim();
            if (priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter selling price", Toast.LENGTH_SHORT).show();
                return;
            }

            double sellingPrice = 0;
            int stock = 0;
            try {
                sellingPrice = Double.parseDouble(priceStr);
            } catch (Exception e) {
                Toast.makeText(this, "Invalid selling price", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String stockStr = editStockSimple.getText().toString().trim();
                if (!stockStr.isEmpty()) {
                    stock = Integer.parseInt(stockStr);
                }
            } catch (Exception ignored) {}
            String simpleImageUri = (String) findViewById(R.id.cardSimpleImage).getTag(R.id.imgVariant);
            variantsToSave.add(new VariantData("Default", sellingPrice, 0, stock, simpleImageUri));
        } else {
            for (int i = 0; i < variantViews.size(); i++) {
                View v = variantViews.get(i);
                EditText editName = v.findViewById(R.id.editVariantName);
                EditText editSelling = v.findViewById(R.id.editSellingPriceAdvance);
                EditText editCost = v.findViewById(R.id.editCostPriceAdvance);
                EditText editStock = v.findViewById(R.id.editStockAdvance);

                String vName = editName.getText().toString().trim();
                if (vName.isEmpty()) vName = "Variant " + (i + 1);

                String sellingStr = editSelling.getText().toString().trim();
                if (sellingStr.isEmpty()) {
                    Toast.makeText(this, "Please enter selling price for " + vName, Toast.LENGTH_SHORT).show();
                    return;
                }

                double selling = 0, cost = 0;
                int stock = 0;
                try { selling = Double.parseDouble(sellingStr); } catch (Exception e) {
                    Toast.makeText(this, "Invalid selling price for " + vName, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String costStr = editCost.getText().toString().trim();
                    if (!costStr.isEmpty()) cost = Double.parseDouble(costStr);
                } catch (Exception ignored) {}
                try {
                    String stockStr = editStock.getText().toString().trim();
                    if (!stockStr.isEmpty()) stock = Integer.parseInt(stockStr);
                } catch (Exception ignored) {}

                String vImageUri = (String) v.getTag(R.id.imgVariant);
                variantsToSave.add(new VariantData(vName, selling, cost, stock, vImageUri));
            }
        }

        if (variantsToSave.isEmpty()) {
            Toast.makeText(this, "No variants to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start upload process
        uploadImagesAndSave(name, category, variantsToSave);
    }

    private void uploadImagesAndSave(String name, String category, List<VariantData> variantsToSave) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Toast.makeText(this, "User not logged in. Cannot save.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressSave.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        AtomicInteger pendingUploads = new AtomicInteger(0);
        for (VariantData vd : variantsToSave) {
            if (vd.imageUri != null && Objects.equals(Uri.parse(vd.imageUri).getScheme(), "content")) {
                pendingUploads.incrementAndGet();
            }
        }

        if (pendingUploads.get() == 0) {
            finalizeSave(name, category, variantsToSave);
            return;
        }

        for (VariantData vd : variantsToSave) {
            if (vd.imageUri != null && Objects.equals(Uri.parse(vd.imageUri).getScheme(), "content")) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("users").child(uid).child("products")
                        .child(System.currentTimeMillis() + "_" + vd.name + ".jpg");

                storageRef.putFile(Uri.parse(vd.imageUri))
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            vd.imageUri = downloadUri.toString();
                            if (pendingUploads.decrementAndGet() == 0) {
                                finalizeSave(name, category, variantsToSave);
                            }
                        }))
                        .addOnFailureListener(e -> {
                            Log.e("AddItem", "Image upload failed for variant " + vd.name, e);
                            // Even if upload fails, continue with local URI as fallback or null
                            if (pendingUploads.decrementAndGet() == 0) {
                                finalizeSave(name, category, variantsToSave);
                            }
                        });
            }
        }
    }

    private void finalizeSave(String name, String category, List<VariantData> variantsToSave) {
        VariantData first = variantsToSave.get(0);
        int totalStock = 0;
        if (!isSimpleMode) {
            for (VariantData vd : variantsToSave) totalStock += vd.stockQuantity;
        } else {
            totalStock = first.stockQuantity;
        }
        
        int bId = BusinessHelper.getActiveBusinessId(this);
        if (bId == -1) {
            runOnUiThread(() -> {
                progressSave.setVisibility(View.GONE);
                findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
                Toast.makeText(this, "No active business found. Please setup business first.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        final Item item = new Item(name, category, first.sellingPrice, first.costPrice, totalStock, first.name, selectedSellBy, !isSimpleMode);
        item.setBusinessId(bId);
        item.setId(java.util.UUID.randomUUID().toString());
        item.setImageUri(first.imageUri);

        final List<Variant> savedVariants = new ArrayList<>();
        for (int i = 0; i < variantsToSave.size(); i++) {
            VariantData vd = variantsToSave.get(i);
            Variant variant = new Variant(item.getId(), vd.name, vd.sellingPrice, vd.costPrice, vd.stockQuantity);
            variant.setId(java.util.UUID.randomUUID().toString());
            variant.setSortOrder(i);
            variant.setImageUri(vd.imageUri);
            savedVariants.add(variant);
        }

        new ItemCloudRepository(this).saveItem(item, savedVariants, new ItemCloudRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        progressSave.setVisibility(View.GONE);
                        Toast.makeText(AddItemActivity.this, "Item Saved Successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        progressSave.setVisibility(View.GONE);
                        findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
                        Toast.makeText(AddItemActivity.this, "Failed to save: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private static class VariantData {
        String name;
        double sellingPrice;
        double costPrice;
        int stockQuantity;
        String imageUri;

        VariantData(String name, double sellingPrice, double costPrice, int stockQuantity, String imageUri) {
            this.name = name;
            this.sellingPrice = sellingPrice;
            this.costPrice = costPrice;
            this.stockQuantity = stockQuantity;
            this.imageUri = imageUri;
        }
    }

    private void setMode(boolean simple) {
        isSimpleMode = simple;
        if (simple) {
            btnSimple.setBackgroundResource(R.drawable.bg_toggle_left_selected);
            btnSimple.setTextColor(0xFFFFFFFF);
            btnAdvance.setBackgroundResource(R.drawable.bg_toggle_unselected);
            btnAdvance.setTextColor(0xFF3F51B5);
            layoutSimpleMode.setVisibility(View.VISIBLE);
            layoutAdvanceMode.setVisibility(View.GONE);
        } else {
            btnAdvance.setBackgroundResource(R.drawable.bg_toggle_right_selected);
            btnAdvance.setTextColor(0xFFFFFFFF);
            btnSimple.setBackgroundResource(R.drawable.bg_toggle_unselected);
            btnSimple.setTextColor(0xFF3F51B5);
            layoutSimpleMode.setVisibility(View.GONE);
            layoutAdvanceMode.setVisibility(View.VISIBLE);
        }
    }

    private void showSellByBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_sell_by_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.optionSellByUnit).setOnClickListener(v -> {
            selectedSellBy = "Unit";
            dialog.dismiss();
            Toast.makeText(this, "Sell By Unit selected", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.optionSellByFraction).setOnClickListener(v -> {
            selectedSellBy = "Fraction";
            dialog.dismiss();
            Toast.makeText(this, "Sell By Fraction selected", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showSelectCategoryBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_category_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        view.findViewById(R.id.btnAddNewCategory).setOnClickListener(v -> {
            dialog.dismiss();
            addCategoryLauncher.launch(new Intent(this, AddCategoryActivity.class));
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Fetch categories from DB
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Business active = db.businessDao().getSelectedBusiness();
            int bId = active != null ? active.getId() : -1;
            List<Category> categories = db.categoryDao().getAllCategories(bId);
            
            // Add dummy data if empty to match image
            if (categories.isEmpty()) {
                categories.add(new Category("Brand All Item", false, true));
                categories.add(new Category("Creatine", false, false));
                categories.add(new Category("MASS GAINER", false, false));
                categories.add(new Category("Sample", false, false));
                categories.add(new Category("DAILY SUPPORT", false, false));
                categories.add(new Category("PRE WORKOUT", false, false));
                categories.add(new Category("Stack", false, false));
                categories.add(new Category("Compression Tshirt", false, false));
            }

            runOnUiThread(() -> {
                CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                    textCategory.setText(category.getName());
                    textCategory.setTextColor(0xFF000000); // Change from hint color to black
                    dialog.dismiss();
                });
                recyclerView.setAdapter(adapter);
            });
        });

        dialog.show();
    }
}
