package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryCloudRepository {
    private static final String TAG = "CategoryCloudRepo";
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryCloudRepository(Context context) {
        this.context = context;
    }

    private CollectionReference getCategoriesCollection() {
        return FirebaseHelper.getCategoriesCollection();
    }

    public void saveCategory(Category category) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "UID is null, cannot save category to cloud");
            return;
        }

        CollectionReference categoriesRef = getCategoriesCollection();
        if (categoriesRef == null) return;

        DocumentReference docRef;
        if (category.getId() != null && !category.getId().isEmpty() && !isUUID(category.getId())) {
            docRef = categoriesRef.document(category.getId());
        } else {
            String oldId = category.getId();
            docRef = categoriesRef.document();
            category.setId(docRef.getId());
            
            // Update local Room database with the new ID
            executor.execute(() -> {
                AppDatabase dbLocal = AppDatabase.getInstance(context);
                if (oldId != null) {
                    dbLocal.categoryDao().deleteById(oldId);
                }
                dbLocal.categoryDao().insert(category);
                Log.d(TAG, "Local category ID updated from " + oldId + " to " + category.getId());
            });
        }

        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("id", category.getId());
        categoryMap.put("businessId", category.getBusinessId());
        categoryMap.put("name", category.getName());
        categoryMap.put("imageUri", category.getImageUri());
        categoryMap.put("backgroundColor", category.getBackgroundColor());
        categoryMap.put("isExpense", category.isExpense());
        categoryMap.put("isCustom", category.isCustom());
        categoryMap.put("updatedAt", com.google.firebase.Timestamp.now());

        docRef.set(categoryMap)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Category saved to cloud: " + category.getName()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save category to cloud", e));
    }

    public static boolean isUUID(String id) {
        if (id == null) return false;
        try {
            java.util.UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void syncCategoriesFromCloud(SyncCallback callback) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "SYNC_FAILED: UID is null");
            if (callback != null) callback.onSyncComplete();
            return;
        }

        int activeBusinessId = BusinessHelper.getActiveBusinessId(context);
        if (activeBusinessId == -1) {
            Log.e(TAG, "SYNC_FAILED: No active business");
            if (callback != null) callback.onSyncComplete();
            return;
        }

        CollectionReference categoriesRef = getCategoriesCollection();
        if (categoriesRef == null) {
            if (callback != null) callback.onSyncComplete();
            return;
        }

        Log.d(TAG, "SYNC_STARTED for Business: " + activeBusinessId);

        categoriesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            int cloudCount = queryDocumentSnapshots.size();
            Log.d(TAG, "FIRESTORE_CATEGORY_COUNT: " + cloudCount);
            
            executor.execute(() -> {
                AppDatabase localDb = AppDatabase.getInstance(context);
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    if (name == null) continue;

                    // Clean up existing local duplicates with same name but UUID
                    Category existing = localDb.categoryDao().getByName(name, activeBusinessId);
                    if (existing != null && isUUID(existing.getId())) {
                        localDb.categoryDao().deleteById(existing.getId());
                    }

                    Category cloudCategory = new Category();
                    cloudCategory.setId(doc.getId());
                    cloudCategory.setName(name);
                    cloudCategory.setImageUri(doc.getString("imageUri"));
                    cloudCategory.setBackgroundColor(doc.getLong("backgroundColor") != null ? 
                                                    doc.getLong("backgroundColor").intValue() : 0);
                    cloudCategory.setExpense(doc.getBoolean("isExpense") != null ? 
                                            doc.getBoolean("isExpense") : false);
                    cloudCategory.setCustom(doc.getBoolean("isCustom") != null ? 
                                           doc.getBoolean("isCustom") : false);
                    cloudCategory.setBusinessId(activeBusinessId);

                    localDb.categoryDao().insert(cloudCategory);
                    Log.d(TAG, "Synced category: " + name);
                }
                
                Log.d(TAG, "SYNC_COMPLETE");
                if (callback != null) callback.onSyncComplete();
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "SYNC_FAILED", e);
            if (callback != null) callback.onSyncComplete();
        });
    }

    public interface SyncCallback {
        void onSyncComplete();
    }
}
