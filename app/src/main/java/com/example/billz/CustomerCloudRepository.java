package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class CustomerCloudRepository {

    private static final String TAG = "CustomerCloudRepo";
    private final Context context;

    public CustomerCloudRepository(Context context) {
        this.context = context;
    }

    public void saveCustomerToCloud(Customer customer) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "CUSTOMER_SAVE_FAILED: User not authenticated");
            return;
        }

        CollectionReference customersRef = FirebaseHelper.getCustomersCollection();
        if (customersRef == null) {
            Log.e(TAG, "CUSTOMER_SAVE_FAILED: Collection reference null");
            return;
        }

        DocumentReference docRef;
        if (customer.getId() != null && !customer.getId().isEmpty()) {
            docRef = customersRef.document(customer.getId());
        } else {
            docRef = customersRef.document();
            customer.setId(docRef.getId()); // Use Document ID
            // Update local ID
            new Thread(() -> AppDatabase.getInstance(context).customerDao().insert(customer)).start();
        }

        Log.d(TAG, "CUSTOMER_SAVE_STARTED: " + customer.getId());

        docRef.set(customer)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "CUSTOMER_SAVE_SUCCESS: " + customer.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "CUSTOMER_SAVE_FAILED: " + customer.getId(), e));
    }

    public void updateCustomerInCloud(Customer customer) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null || customer.getId() == null) {
            Log.e(TAG, "Update failed: No UID or customer ID");
            return;
        }

        CollectionReference customersRef = FirebaseHelper.getCustomersCollection();
        if (customersRef == null) return;

        customersRef.document(customer.getId())
                .set(customer, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Customer successfully updated in Firestore: " + customer.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating customer in Firestore: " + customer.getId(), e));
    }

    public void deleteCustomerFromCloud(String customerId) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        CollectionReference customersRef = FirebaseHelper.getCustomersCollection();
        if (customersRef == null) return;

        customersRef.document(customerId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Customer successfully deleted from Firestore: " + customerId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting customer from Firestore: " + customerId, e));
    }
}
