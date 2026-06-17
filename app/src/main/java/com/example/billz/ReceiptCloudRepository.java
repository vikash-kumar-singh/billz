package com.example.billz;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class ReceiptCloudRepository {

    private static final String TAG = "ReceiptCloudRepo";

    public void saveReceiptToCloud(Receipt receipt, List<ReceiptItem> items) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "RECEIPT_SAVE_FAILED: User not authenticated");
            return;
        }

        CollectionReference receiptsRef = FirebaseHelper.getInvoicesCollection();
        if (receiptsRef == null) {
            Log.e(TAG, "RECEIPT_SAVE_FAILED: Collection reference null");
            return;
        }

        String receiptId = receipt.getReceiptNo();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        DocumentReference receiptDoc = receiptsRef.document(receiptId);
        batch.set(receiptDoc, receipt);

        if (items != null) {
            CollectionReference itemsRef = receiptDoc.collection("items");
            for (ReceiptItem item : items) {
                batch.set(itemsRef.document(), item);
            }
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "RECEIPT_SAVE_SUCCESS: " + receiptId))
                .addOnFailureListener(e -> Log.e(TAG, "RECEIPT_SAVE_FAILED: " + receiptId, e));
    }

    // Removed individual saveReceiptItemsToCloud as it's now part of the batch save
}
