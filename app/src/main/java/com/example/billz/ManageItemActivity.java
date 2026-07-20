package com.example.billz;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

public class ManageItemActivity extends AppCompatActivity {

    private EditText editItemName, editSellingPriceSimple, editStockSimple;
    private TextView textCategory, textSellBy, btnSimple, btnAdvance;
    private ImageView imgCategory;
    private View layoutCategorySelector, layoutSimpleMode, layoutAdvanceMode, layoutSellBySelector;
    private LinearLayout containerVariants;
    private android.widget.ProgressBar progressSave;
    private View topBarManage;
    private String itemId;
    private AppDatabase db;
    private Item currentItem;
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
        setContentView(R.layout.activity_manage_item);

        setupImageLaunchers();
        setupCategoryLauncher();

        View root = findViewById(R.id.manageItemRoot);
        topBarManage = findViewById(R.id.topBarManage);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            if (topBarManage != null) {
                topBarManage.setPadding(0, systemBars.top, 0, 0);
            }
            return insets;
        });

        db = AppDatabase.getInstance(this);
        itemId = getIntent().getStringExtra("item_id");

        editItemName = findViewById(R.id.editItemName);
        editSellingPriceSimple = findViewById(R.id.editSellingPriceSimple);
        editStockSimple = findViewById(R.id.editStockSimple);
        textCategory = findViewById(R.id.textCategory);
        textSellBy = findViewById(R.id.textSellBy);
        imgCategory = findViewById(R.id.imgCategory);
        layoutCategorySelector = findViewById(R.id.layoutCategorySelector);
        layoutSellBySelector = findViewById(R.id.layoutSellBySelector);
        layoutSimpleMode = findViewById(R.id.layoutSimpleMode);
        layoutAdvanceMode = findViewById(R.id.layoutAdvanceMode);
        containerVariants = findViewById(R.id.containerVariants);
        
        // Enable smooth layout transitions
        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        transition.setDuration(300);
        containerVariants.setLayoutTransition(transition);

        btnSimple = findViewById(R.id.btnSimple);
        btnAdvance = findViewById(R.id.btnAdvance);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteItem());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());
        
        layoutCategorySelector.setOnClickListener(v -> showCategorySelectionDialog());
        layoutSellBySelector.setOnClickListener(v -> showSellByBottomSheet());
        findViewById(R.id.cardSimpleImage).setOnClickListener(this::showImageSourceDialog);

        btnSimple.setOnClickListener(v -> setMode(true));
        btnAdvance.setOnClickListener(v -> setMode(false));

        findViewById(R.id.btnAddVariant).setOnClickListener(v -> addNewVariantView(null));
        progressSave = findViewById(R.id.progressSave);

        loadItemData();
    }

    private void setupCategoryLauncher() {
        addCategoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Refresh categories if needed
            }
        });
    }

    private void setupImageLaunchers() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                android.net.Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception e) {
                        android.util.Log.e("ManageItem", "Failed to take persistable permission", e);
                    }
                    updateVariantImage(uri);
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            
            // If switched to Advance and no variants, add one default row
            if (variantViews.isEmpty()) {
                addNewVariantView(null);
            }
        }
    }

    private void addNewVariantView(Variant data) {
        View variantView = LayoutInflater.from(this).inflate(R.layout.item_add_variant, containerVariants, false);
        
        EditText editName = variantView.findViewById(R.id.editVariantName);
        EditText editSelling = variantView.findViewById(R.id.editSellingPriceAdvance);
        EditText editCost = variantView.findViewById(R.id.editCostPriceAdvance);
        EditText editStock = variantView.findViewById(R.id.editStockAdvance);
        ImageView imgVariant = variantView.findViewById(R.id.imgVariant);
        View cardImage = variantView.findViewById(R.id.cardVariantImage);
        View btnRemove = variantView.findViewById(R.id.btnRemoveVariant);
        View btnUp = variantView.findViewById(R.id.btnMoveUp);
        View btnDown = variantView.findViewById(R.id.btnMoveDown);

        if (data != null) {
            editName.setText(data.getName());
            editSelling.setText(String.valueOf((int)data.getSellingPrice()));
            editCost.setText(data.getCostPrice() > 0 ? String.valueOf((int)data.getCostPrice()) : "");
            editStock.setText(String.valueOf(data.getStockQuantity()));
            variantView.setTag(data.getId()); // Store ID for updates
            
            if (data.getImageUri() != null && !data.getImageUri().isEmpty()) {
                Glide.with(this)
                        .load(data.getImageUri())
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(imgVariant);
                imgVariant.setImageTintList(null);
                imgVariant.setPadding(0, 0, 0, 0);
                variantView.setTag(R.id.imgVariant, data.getImageUri());
            }
        }

        cardImage.setOnClickListener(v -> showImageSourceDialog(variantView));

        btnRemove.setOnClickListener(v -> {
            if (variantViews.size() > 1) {
                variantViews.remove(variantView);
                containerVariants.removeView(variantView);
            } else {
                Toast.makeText(this, "At least one variant is required", Toast.LENGTH_SHORT).show();
            }
        });

        btnUp.setOnClickListener(v -> moveVariantView(variantView, -1));
        btnDown.setOnClickListener(v -> moveVariantView(variantView, 1));

        variantViews.add(variantView);
        containerVariants.addView(variantView);
    }

    private void moveVariantView(View view, int direction) {
        int currentIndex = variantViews.indexOf(view);
        int newIndex = currentIndex + direction;

        if (newIndex >= 0 && newIndex < variantViews.size()) {
            variantViews.remove(currentIndex);
            variantViews.add(newIndex, view);

            containerVariants.removeViewAt(currentIndex);
            containerVariants.addView(view, newIndex);
            
            // Scroll to the moved view
            view.getParent().requestChildFocus(view, view);
        }
    }

    private void showSellByBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_sell_by_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.optionSellByUnit).setOnClickListener(v -> {
            selectedSellBy = "Unit";
            textSellBy.setText("Sell by Unit");
            dialog.dismiss();
        });

        view.findViewById(R.id.optionSellByFraction).setOnClickListener(v -> {
            selectedSellBy = "Fraction";
            textSellBy.setText("Sell by Fraction");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveItem() {
        if (currentItem == null) {
            Toast.makeText(this, "Error: Item data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

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

        currentItem.setName(name);
        currentItem.setCategory(category);
        currentItem.setSellBy(selectedSellBy);
        currentItem.setAdvanceMode(!isSimpleMode);

        final List<Variant> variantsToSave = new ArrayList<>();
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
            
            currentItem.setSellingPrice(sellingPrice);
            currentItem.setStockQuantity(stock);

            String simpleImageUri = (String) findViewById(R.id.cardSimpleImage).getTag(R.id.imgVariant);
            
            // For simple mode, we use the first variant's ID if it exists, otherwise create new
            // In ItemCloudRepository, we delete and re-insert anyway, but keeping ID is cleaner
            Variant v = new Variant(itemId, "Default", sellingPrice, 0, stock);
            v.setId(java.util.UUID.randomUUID().toString());
            v.setImageUri(simpleImageUri);
            variantsToSave.add(v);
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

                Variant variant = new Variant(itemId, vName, selling, cost, stock);
                variant.setSortOrder(i);
                
                String vImageUri = (String) v.getTag(R.id.imgVariant);
                if (vImageUri != null) variant.setImageUri(vImageUri);

                if (v.getTag() != null && v.getTag() instanceof String) {
                    variant.setId((String) v.getTag());
                } else {
                    variant.setId(java.util.UUID.randomUUID().toString());
                }
                variantsToSave.add(variant);
            }
        }

        uploadImagesAndSave(variantsToSave);
    }

    private void uploadImagesAndSave(List<Variant> variantsToSave) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Toast.makeText(this, "User not logged in. Cannot save.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressSave.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSave).setVisibility(View.GONE);
        findViewById(R.id.btnDelete).setVisibility(View.GONE);

        AtomicInteger pendingUploads = new AtomicInteger(0);
        for (Variant v : variantsToSave) {
            if (v.getImageUri() != null && Objects.equals(Uri.parse(v.getImageUri()).getScheme(), "content")) {
                pendingUploads.incrementAndGet();
            }
        }

        if (pendingUploads.get() == 0) {
            finalizeSave(variantsToSave);
            return;
        }

        for (Variant v : variantsToSave) {
            if (v.getImageUri() != null && Objects.equals(Uri.parse(v.getImageUri()).getScheme(), "content")) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("users").child(uid).child("products")
                        .child(System.currentTimeMillis() + "_" + v.getName() + ".jpg");

                storageRef.putFile(Uri.parse(v.getImageUri()))
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            v.setImageUri(downloadUri.toString());
                            if (pendingUploads.decrementAndGet() == 0) {
                                finalizeSave(variantsToSave);
                            }
                        }))
                        .addOnFailureListener(e -> {
                            Log.e("ManageItem", "Image upload failed for variant " + v.getName(), e);
                            if (pendingUploads.decrementAndGet() == 0) {
                                finalizeSave(variantsToSave);
                            }
                        });
            }
        }
    }

    private void finalizeSave(List<Variant> variantsToSave) {
        if (currentItem == null) return;

        if (!isSimpleMode && !variantsToSave.isEmpty()) {
            double totalStock = 0;
            for (Variant v : variantsToSave) totalStock += v.getStockQuantity();
            
            Variant first = variantsToSave.get(0);
            currentItem.setSellingPrice(first.getSellingPrice());
            currentItem.setCostPrice(first.getCostPrice());
            currentItem.setVariantName(first.getName());
            currentItem.setStockQuantity((int)totalStock);
            currentItem.setImageUri(first.getImageUri());
        } else if (isSimpleMode && !variantsToSave.isEmpty()) {
            currentItem.setImageUri(variantsToSave.get(0).getImageUri());
        }

        new ItemCloudRepository(this).saveItem(currentItem, variantsToSave, new ItemCloudRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        progressSave.setVisibility(View.GONE);
                        Toast.makeText(ManageItemActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
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
                        findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
                        Toast.makeText(ManageItemActivity.this, "Update failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void loadItemData() {
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Invalid Item ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Business active = db.businessDao().getSelectedBusiness();
                int bId = (active != null) ? active.getId() : -1;
                
                currentItem = db.itemDao().getById(itemId);
                List<Variant> variants = db.variantDao().getVariantsForItem(itemId);

                if (currentItem == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Item not found. Please wait for sync.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                String categoryName = currentItem.getCategory();
                Category category = db.categoryDao().getByName(categoryName, bId);

                runOnUiThread(() -> {
                    try {
                        editItemName.setText(currentItem.getName());
                        textCategory.setText(categoryName != null && !categoryName.isEmpty() ? categoryName : "Select Category");
                        
                        selectedSellBy = currentItem.getSellBy() != null ? currentItem.getSellBy() : "Unit";
                        textSellBy.setText("Sell by " + selectedSellBy);
                        
                        setMode(!currentItem.isAdvanceMode());
                        
                        if (isSimpleMode) {
                            editSellingPriceSimple.setText(String.valueOf((int)currentItem.getSellingPrice()));
                            editStockSimple.setText(String.valueOf(currentItem.getStockQuantity()));
                            
                            // Load image into simple mode card
                            if (currentItem.getImageUri() != null && !currentItem.getImageUri().isEmpty()) {
                                ImageView imgSimple = findViewById(R.id.imgSimple);
                                Glide.with(this)
                                        .load(currentItem.getImageUri())
                                        .centerCrop()
                                        .into(imgSimple);
                                imgSimple.setImageTintList(null);
                                imgSimple.setPadding(0, 0, 0, 0);
                                findViewById(R.id.cardSimpleImage).setTag(R.id.imgVariant, currentItem.getImageUri());
                            }
                        }

                        // Populate variants in container
                        containerVariants.removeAllViews();
                        variantViews.clear();
                        if (currentItem.isAdvanceMode()) {
                            for (Variant v : variants) {
                                addNewVariantView(v);
                            }
                        }

                        if (category != null && category.getImageUri() != null && !category.getImageUri().isEmpty()) {
                            Glide.with(this)
                                    .load(category.getImageUri())
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_nav_reports)
                                    .into(imgCategory);
                            imgCategory.setImageTintList(null);
                            imgCategory.setPadding(0, 0, 0, 0);
                        } else {
                            imgCategory.setImageResource(R.drawable.ic_nav_reports);
                            int color = ContextCompat.getColor(this, R.color.reports_tab_selected);
                            imgCategory.setImageTintList(android.content.res.ColorStateList.valueOf(color));
                        }
                    } catch (Exception e) {
                        Log.e("ManageItem", "UI update failed", e);
                    }
                });
            } catch (Exception e) {
                Log.e("ManageItem", "Data loading failed", e);
            }
        });
    }

    private void showCategorySelectionDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_select_category_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        view.findViewById(R.id.btnAddNewCategory).setOnClickListener(v -> {
            dialog.dismiss();
            addCategoryLauncher.launch(new Intent(this, AddCategoryActivity.class));
        });

        androidx.recyclerview.widget.RecyclerView recyclerView = view.findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

        // Fetch categories from DB
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
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
                    updateCategory(category.getName());
                    dialog.dismiss();
                });
                recyclerView.setAdapter(adapter);
            });
        });

        dialog.show();
    }

    private void updateCategory(String newCategory) {
        if (currentItem != null) {
            currentItem.setCategory(newCategory);
            runOnUiThread(() -> {
                textCategory.setText(newCategory != null && !newCategory.isEmpty() ? newCategory : "Select Category");
                
                // Fetch category icon update
                Executors.newSingleThreadExecutor().execute(() -> {
                    Business active = db.businessDao().getSelectedBusiness();
                    int bId = (active != null) ? active.getId() : -1;
                    Category category = db.categoryDao().getByName(newCategory, bId);
                    runOnUiThread(() -> {
                        if (category != null && category.getImageUri() != null && !category.getImageUri().isEmpty()) {
                            Glide.with(this)
                                    .load(category.getImageUri())
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_nav_reports)
                                    .into(imgCategory);
                            imgCategory.setImageTintList(null);
                            imgCategory.setPadding(0, 0, 0, 0);
                        } else {
                            imgCategory.setImageResource(R.drawable.ic_nav_reports);
                            int color = ContextCompat.getColor(this, R.color.reports_tab_selected);
                            imgCategory.setImageTintList(android.content.res.ColorStateList.valueOf(color));
                        }
                    });
                });
            });
        }
    }

    private void deleteItem() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (currentItem != null) {
                new ItemCloudRepository(this).deleteItem(currentItem);
            }
            db.itemDao().deleteById(itemId);
            db.variantDao().deleteVariantsForItem(itemId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
