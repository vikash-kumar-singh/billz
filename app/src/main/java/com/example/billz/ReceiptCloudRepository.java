package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiptCloudRepository {

    private static final String TAG = "ReceiptCloudRepo";
    private final Context context;
    private final AppDatabase localDb;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ReceiptCloudRepository(Context context) {
        this.context = context;
        this.localDb = AppDatabase.getInstance(context);
    }

    public void saveReceipt(Receipt receipt, List<ReceiptItem> items) {
        String uid = FirebaseHelper.getCurrentUid();
        Log.d(TAG, "RECEIPT_SAVE_STARTED: " + receipt.getId() + " | CURRENT_UID: " + uid);
        
        if (uid == null) {
            Log.e(TAG, "RECEIPT_SAVE_FAILED: User not authenticated");
            receipt.setSyncPending(true);
            executor.execute(() -> localDb.receiptDao().insert(receipt));
            return;
        }

        receipt.setBusinessId(uid);
        receipt.setSyncPending(false);

        // 1. Save to Room
        executor.execute(() -> {
            localDb.receiptDao().insert(receipt);
            if (items != null) {
                localDb.receiptItemDao().insertAll(items);
            }
        });

        // 2. Save to Firestore
        CollectionReference receiptsRef = FirebaseHelper.getInvoicesCollection();
        if (receiptsRef == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        DocumentReference receiptDoc = receiptsRef.document(receipt.getId());
        batch.set(receiptDoc, receipt);

        if (items != null) {
            CollectionReference itemsRef = receiptDoc.collection("items");
            for (ReceiptItem item : items) {
                batch.set(itemsRef.document(), item);
            }
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "RECEIPT_SAVE_SUCCESS: " + receipt.getId()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "RECEIPT_SAVE_FAILED: " + receipt.getId(), e);
                    receipt.setSyncPending(true);
                    executor.execute(() -> localDb.receiptDao().update(receipt));
                });
    }

    public void syncReceiptsFromCloud(Runnable onComplete) {
        String uid = FirebaseHelper.getCurrentUid();
        Log.d(TAG, "RECEIPT_LOAD_STARTED for UID: " + uid);
        
        CollectionReference receiptsRef = FirebaseHelper.getInvoicesCollection();
        if (receiptsRef == null) return;

        receiptsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Receipt> cloudReceipts = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Receipt r = doc.toObject(Receipt.class);
                r.setId(doc.getId());
                r.setSyncPending(false);
                cloudReceipts.add(r);
            }

            executor.execute(() -> {
                localDb.receiptDao().insertAll(cloudReceipts);
                Log.d(TAG, "RECEIPT_SYNC_SUCCESS: " + cloudReceipts.size() + " receipts cached.");
                if (onComplete != null) onComplete.run();
            });
            
            Log.d(TAG, "RECEIPT_LOAD_SUCCESS: Count " + queryDocumentSnapshots.size());
        }).addOnFailureListener(e -> Log.e(TAG, "RECEIPT_LOAD_FAILED", e));
    }

    public void deleteReceipt(String receiptId) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        executor.execute(() -> localDb.receiptDao().deleteById(receiptId));

        CollectionReference receiptsRef = FirebaseHelper.getInvoicesCollection();
        if (receiptsRef != null) {
            receiptsRef.document(receiptId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "RECEIPT_DELETE_SUCCESS: " + receiptId))
                .addOnFailureListener(e -> Log.e(TAG, "RECEIPT_DELETE_FAILED", e));
        }
    }

    public void updateReceipt(Receipt receipt) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        receipt.setUpdatedAt(System.currentTimeMillis());
        executor.execute(() -> localDb.receiptDao().update(receipt));

        CollectionReference receiptsRef = FirebaseHelper.getInvoicesCollection();
        if (receiptsRef != null) {
            receiptsRef.document(receipt.getId()).set(receipt)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "RECEIPT_UPDATE_SUCCESS: " + receipt.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "RECEIPT_UPDATE_FAILED", e));
        }
    }

    public void retryPendingSync() {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        executor.execute(() -> {
            List<Receipt> pending = localDb.receiptDao().getPendingSyncReceipts();
            if (pending.isEmpty()) return;

            Log.d(TAG, "RETRY_PENDING_SYNC: Found " + pending.size() + " receipts");
            for (Receipt r : pending) {
                // For each receipt, we'd ideally also need its items, 
                // but since the items collection is nested, we'll just push the receipt first.
                // In a full implementation, we'd fetch items from localDb too.
                r.setSyncPending(false);
                r.setBusinessId(uid);
                
                FirebaseHelper.getInvoicesCollection().document(r.getId()).set(r)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "RETRY_SYNC_SUCCESS: " + r.getId());
                        executor.execute(() -> localDb.receiptDao().update(r));
                    })
                    .addOnFailureListener(e -> {
                        r.setSyncPending(true);
                        Log.e(TAG, "RETRY_SYNC_FAILED: " + r.getId());
                    });
            }
        });
    }
}
