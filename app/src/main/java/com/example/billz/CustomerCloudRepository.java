package com.example.billz;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class CustomerCloudRepository {

    private static final String TAG = "CustomerCloudRepo";

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

        String customerId = String.valueOf(customer.getId());
        Log.d(TAG, "CUSTOMER_SAVE_STARTED: " + customerId);

        customersRef.document(customerId)
                .set(customer)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "CUSTOMER_SAVE_SUCCESS: " + customerId))
                .addOnFailureListener(e -> Log.e(TAG, "CUSTOMER_SAVE_FAILED: " + customerId, e));
    }

    public void updateCustomerInCloud(Customer customer) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "User not authenticated. Cannot update customer in cloud.");
            return;
        }

        CollectionReference customersRef = FirebaseHelper.getCustomersCollection();
        if (customersRef == null) return;

        String customerId = String.valueOf(customer.getId());

        customersRef.document(customerId)
                .set(customer, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Customer successfully updated in Firestore: " + customerId))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating customer in Firestore: " + customerId, e));
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
