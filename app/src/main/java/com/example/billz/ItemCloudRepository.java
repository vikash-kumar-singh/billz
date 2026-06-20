package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemCloudRepository {
    private static final String TAG = "ItemCloudRepository";
    private final FirebaseFirestore db;
    private final Context context;
    private final String uid;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ItemCloudRepository(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.uid = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                   FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    private CollectionReference getItemsCollection() {
        if (uid == null) return null;
        return db.collection("users").document(uid).collection("products");
    }

    public void saveItem(Item item, List<Variant> variants) {
        if (uid == null) {
            Log.e(TAG, "CURRENT_UID: NULL - Cannot save to Firestore");
            return;
        }
        
        CollectionReference itemsRef = getItemsCollection();
        DocumentReference docRef;
        
        if (item.getId() != null && !item.getId().isEmpty()) {
            docRef = itemsRef.document(item.getId());
        } else {
            docRef = itemsRef.document();
            item.setId(docRef.getId()); // Use Document ID as Primary Key
            // Update local Room database with the new ID
            executor.execute(() -> {
                AppDatabase dbLocal = AppDatabase.getInstance(context);
                dbLocal.itemDao().insert(item);
            });
        }

        Log.d(TAG, "PRODUCT_SAVE_STARTED: " + item.getName() + " ID: " + item.getId());
        Log.d(TAG, "CURRENT_UID: " + uid);

        WriteBatch batch = db.batch();

        // Save Item
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("id", item.getId());
        itemMap.put("name", item.getName());
        itemMap.put("category", item.getCategory());
        itemMap.put("sellingPrice", item.getSellingPrice());
        itemMap.put("costPrice", item.getCostPrice());
        itemMap.put("stockQuantity", item.getStockQuantity());
        itemMap.put("variantName", item.getVariantName());
        itemMap.put("sellBy", item.getSellBy());
        itemMap.put("isAdvanceMode", item.isAdvanceMode());
        itemMap.put("updatedAt", com.google.firebase.Timestamp.now());

        batch.set(docRef, itemMap);

        // Save Variants as sub-collection
        CollectionReference variantsRef = docRef.collection("variants");
        for (Variant variant : variants) {
            DocumentReference vDocRef;
            if (variant.getId() != null && !variant.getId().isEmpty()) {
                vDocRef = variantsRef.document(variant.getId());
            } else {
                vDocRef = variantsRef.document();
                variant.setId(vDocRef.getId());
                variant.setItemId(item.getId());
                // Update local variant ID
                executor.execute(() -> {
                    AppDatabase dbLocal = AppDatabase.getInstance(context);
                    dbLocal.variantDao().insert(variant);
                });
            }

            Map<String, Object> vMap = new HashMap<>();
            vMap.put("id", variant.getId());
            vMap.put("itemId", variant.getItemId());
            vMap.put("name", variant.getName());
            vMap.put("sellingPrice", variant.getSellingPrice());
            vMap.put("costPrice", variant.getCostPrice());
            vMap.put("stockQuantity", variant.getStockQuantity());
            vMap.put("sortOrder", variant.getSortOrder());
            vMap.put("imageUri", variant.getImageUri());
            
            batch.set(vDocRef, vMap);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "PRODUCT_SAVE_SUCCESS: " + item.getName());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "PRODUCT_SAVE_FAILED: " + e.getMessage());
        });
    }

    public void deleteItem(Item item) {
        if (uid == null || item.getId() == null) return;

        getItemsCollection().document(item.getId()).delete()
            .addOnSuccessListener(aVoid -> Log.d(TAG, "PRODUCT_DELETE_SUCCESS: " + item.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "PRODUCT_DELETE_FAILED: " + e.getMessage()));
    }

    public void updateStock(String itemId, int newQuantity) {
        if (uid == null || itemId == null) return;
        getItemsCollection().document(itemId).update("stockQuantity", newQuantity)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "STOCK_UPDATE_SUCCESS: " + itemId + " Qty: " + newQuantity))
            .addOnFailureListener(e -> Log.e(TAG, "STOCK_UPDATE_FAILED: " + itemId, e));
    }

    public void updateVariantStock(String itemId, String variantId, int newQuantity) {
        if (uid == null || itemId == null || variantId == null) return;
        getItemsCollection().document(itemId).collection("variants").document(variantId)
            .update("stockQuantity", newQuantity)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "VARIANT_STOCK_UPDATE_SUCCESS: " + variantId + " Qty: " + newQuantity))
            .addOnFailureListener(e -> Log.e(TAG, "VARIANT_STOCK_UPDATE_FAILED: " + variantId, e));
    }

    public void syncProductsFromCloud(SyncCallback callback) {
        if (uid == null) {
            Log.e(TAG, "SYNC_STARTED: FAILED (UID is null)");
            return;
        }
        
        int activeBusinessId = BusinessHelper.getActiveBusinessId(context);
        if (activeBusinessId == -1) {
            Log.e(TAG, "SYNC_STARTED: FAILED (No active business)");
            return;
        }

        Log.d(TAG, "SYNC_STARTED for Business: " + activeBusinessId);

        getItemsCollection().get().addOnSuccessListener(queryDocumentSnapshots -> {
            int cloudCount = queryDocumentSnapshots.size();
            Log.d(TAG, "FIRESTORE_RECORD_COUNT: " + cloudCount);
            
            executor.execute(() -> {
                AppDatabase localDb = AppDatabase.getInstance(context);
                int localCountBefore = localDb.itemDao().getAllItems(activeBusinessId).size();
                Log.d(TAG, "ROOM_RECORD_COUNT (Before): " + localCountBefore);

                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    String fId = doc.getId();
                    
                    Item cloudItem = new Item(
                        doc.getString("name"),
                        doc.getString("category"),
                        doc.getDouble("sellingPrice") != null ? doc.getDouble("sellingPrice") : 0,
                        doc.getDouble("costPrice") != null ? doc.getDouble("costPrice") : 0,
                        doc.getLong("stockQuantity") != null ? doc.getLong("stockQuantity").intValue() : 0,
                        doc.getString("variantName") != null ? doc.getString("variantName") : "", 
                        doc.getString("sellBy"),
                        doc.getBoolean("isAdvanceMode") != null ? doc.getBoolean("isAdvanceMode") : false
                    );
                    cloudItem.setId(fId); // Primary Key is now Document ID
                    cloudItem.setBusinessId(activeBusinessId);

                    Log.d(TAG, "UPSERT_PRODUCT: " + cloudItem.getName() + " ID: " + fId);
                    localDb.itemDao().insert(cloudItem); // This will REPLACE if conflict occurs

                    // Sync Variants
                    doc.getReference().collection("variants").get().addOnSuccessListener(vSnaps -> {
                        executor.execute(() -> {
                            List<Variant> cloudVariants = new ArrayList<>();
                            for (com.google.firebase.firestore.DocumentSnapshot vDoc : vSnaps) {
                                Variant v = new Variant(
                                    fId,
                                    vDoc.getString("name"),
                                    vDoc.getDouble("sellingPrice") != null ? vDoc.getDouble("sellingPrice") : 0,
                                    vDoc.getDouble("costPrice") != null ? vDoc.getDouble("costPrice") : 0,
                                    vDoc.getLong("stockQuantity") != null ? vDoc.getLong("stockQuantity").intValue() : 0
                                );
                                v.setId(vDoc.getId());
                                v.setSortOrder(vDoc.getLong("sortOrder") != null ? vDoc.getLong("sortOrder").intValue() : 0);
                                v.setImageUri(vDoc.getString("imageUri"));
                                cloudVariants.add(v);
                            }
                            if (!cloudVariants.isEmpty()) {
                                localDb.variantDao().insertAll(cloudVariants);
                            }
                        });
                    });
                }
                
                int localCountAfter = localDb.itemDao().getAllItems(activeBusinessId).size();
                Log.d(TAG, "ROOM_RECORD_COUNT (After): " + localCountAfter);
                if (localCountAfter > cloudCount) {
                    Log.w(TAG, "DUPLICATE_DETECTED: Local count " + localCountAfter + " exceeds Cloud count " + cloudCount);
                }

                if (callback != null) callback.onSyncComplete();
            });
        }).addOnFailureListener(e -> Log.e(TAG, "SYNC_FAILED", e));
    }

    public interface SyncCallback {
        void onSyncComplete();
    }
}
