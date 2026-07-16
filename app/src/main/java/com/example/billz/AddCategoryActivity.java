package com.example.billz;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import java.util.Objects;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText editCategoryName;
    private ImageView imgCategory;
    private ProgressBar progressImage;
    private com.google.android.material.card.MaterialCardView cardCategoryImage;
    private Uri selectedImageUri;
    private int selectedColor = Color.parseColor("#3F51B5");
    private String categoryId;
    private Category existingCategory;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) {}
                    selectedImageUri = uri;
                    imgCategory.setImageURI(uri);
                    imgCategory.setImageTintList(null);
                    imgCategory.setPadding(0, 0, 0, 0);
                    imgCategory.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_category);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCategory);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        editCategoryName = findViewById(R.id.editCategoryName);
        imgCategory = findViewById(R.id.imgCategory);
        progressImage = findViewById(R.id.progressImage);
        cardCategoryImage = findViewById(R.id.cardCategoryImage);

        categoryId = getIntent().getStringExtra("category_id");
        if (categoryId != null) {
            toolbar.setTitle("EDIT CATEGORY");
            loadCategoryData();
        }

        findViewById(R.id.btnSaveCategory).setOnClickListener(v -> saveCategory());
        cardCategoryImage.setOnClickListener(v -> showImagePickerBottomSheet());
    }

    private void loadCategoryData() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            existingCategory = db.categoryDao().getById(categoryId);
            if (existingCategory != null) {
                runOnUiThread(() -> {
                    editCategoryName.setText(existingCategory.getName());
                    selectedColor = existingCategory.getBackgroundColor();
                    
                    if (existingCategory.getImageUri() != null && !existingCategory.getImageUri().isEmpty()) {
                        selectedImageUri = Uri.parse(existingCategory.getImageUri());
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(imgCategory);
                        imgCategory.setImageTintList(null);
                        imgCategory.setPadding(0, 0, 0, 0);
                    } else {
                        cardCategoryImage.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(selectedColor));
                    }
                });
            }
        });
    }

    private void showImagePickerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_image_picker_bottom_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.cardUploadImage).setOnClickListener(v -> {
            dialog.dismiss();
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        view.findViewById(R.id.cardSelectColor).setOnClickListener(v -> {
            dialog.dismiss();
            showColorPickerBottomSheet();
        });

        dialog.show();
    }

    private void showColorPickerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_color_picker_bottom_sheet, null);
        dialog.setContentView(view);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerColors);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#3F51B5")); // Blue (Default)
        colors.add(Color.parseColor("#EF4444")); // Red
        colors.add(Color.parseColor("#10B981")); // Green
        colors.add(Color.parseColor("#F59E0B")); // Orange
        colors.add(Color.parseColor("#EC4899")); // Pink
        colors.add(Color.parseColor("#8B5CF6")); // Purple
        colors.add(Color.parseColor("#06B6D4")); // Cyan
        colors.add(Color.parseColor("#1E293B")); // Dark Slate
        colors.add(Color.parseColor("#64748B")); // Slate
        colors.add(Color.parseColor("#000000")); // Black

        ColorAdapter adapter = new ColorAdapter(colors, color -> {
            selectedColor = color;
            selectedImageUri = null; // Clear image if color is chosen
            cardCategoryImage.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(color));
            imgCategory.setImageResource(android.R.drawable.ic_menu_gallery);
            imgCategory.setPadding(48, 48, 48, 48); // Restore padding for icon
            imgCategory.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnCloseColorPicker).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveCategory() {
        String name = editCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null && Objects.equals(selectedImageUri.getScheme(), "content")) {
            uploadImageToFirebase(selectedImageUri, name);
        } else {
            finalizeSave(name, selectedImageUri != null ? selectedImageUri.toString() : null);
        }
    }

    private void uploadImageToFirebase(Uri uri, String categoryName) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        progressImage.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveCategory).setEnabled(false);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("users")
                .child(uid)
                .child("categories")
                .child(System.currentTimeMillis() + ".jpg");

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        finalizeSave(categoryName, downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    progressImage.setVisibility(View.GONE);
                    findViewById(R.id.btnSaveCategory).setEnabled(true);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Save anyway with local URI as fallback or null
                    finalizeSave(categoryName, uri.toString());
                });
    }

    private void finalizeSave(String name, String imageUriStr) {
        Category category = (existingCategory != null) ? existingCategory : new Category(name, imageUriStr, selectedColor);
        category.setName(name);
        category.setImageUri(imageUriStr);
        category.setBackgroundColor(selectedColor);
        category.setCustom(true);

        BusinessHelper.ensureActiveBusiness(this, () -> {
            AppDatabase db = AppDatabase.getInstance(this);
            category.setBusinessId(BusinessHelper.getActiveBusinessId(this));
            
            // Save locally
            db.categoryDao().insert(category);
            
            // Save to Cloud
            new CategoryCloudRepository(this).saveCategory(category);

            runOnUiThread(() -> {
                progressImage.setVisibility(View.GONE);
                Toast.makeText(this, "Category " + name + " saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
