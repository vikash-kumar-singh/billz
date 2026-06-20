package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomerSyncManager {

    private static final String TAG = "CustomerSyncManager";
    private final Context context;
    private final CustomerDao customerDao;

    public interface SyncCallback {
        void onSyncComplete();
        void onSyncFailed(String error);
    }

    public CustomerSyncManager(Context context) {
        this.context = context;
        this.customerDao = AppDatabase.getInstance(context).customerDao();
    }

    public void syncCustomersFromCloud(SyncCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "SYNC_STARTED: FAILED (User not logged in)");
            if (callback != null) callback.onSyncFailed("User not logged in");
            return;
        }

        String uid = user.getUid();
        Log.d(TAG, "SYNC_STARTED for user: " + uid);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("customers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "FIRESTORE_RECORD_COUNT: " + queryDocumentSnapshots.size());
                    saveToRoom(queryDocumentSnapshots, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "SYNC_FAILED: " + e.getMessage());
                    if (callback != null) callback.onSyncFailed(e.getMessage());
                });
    }

    private void saveToRoom(QuerySnapshot snapshots, SyncCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                int activeBusinessId = BusinessHelper.getActiveBusinessId(context);
                
                List<Customer> cloudCustomers = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Customer customer = doc.toObject(Customer.class);
                    if (customer != null) {
                        customer.setId(doc.getId()); // Map Firestore ID to Room Primary Key
                        customer.setBusinessId(activeBusinessId);
                        cloudCustomers.add(customer);
                        Log.d(TAG, "UPSERT_CUSTOMER: " + customer.getName() + " ID: " + customer.getId());
                    }
                }

                if (!cloudCustomers.isEmpty()) {
                    customerDao.insertAll(cloudCustomers);
                    Log.d(TAG, "ROOM_RECORD_COUNT: " + cloudCustomers.size() + " customers upserted");
                }

                Log.d(TAG, "SYNC_SUCCESS");
                if (callback != null) {
                    callback.onSyncComplete();
                }
            } catch (Exception e) {
                Log.e(TAG, "SYNC_FAILED (ROOM_UPDATE): " + e.getMessage());
                if (callback != null) callback.onSyncFailed(e.getMessage());
            }
        });
    }

    public void syncCustomerToCloud(Customer customer) {
        // Reuse existing cloud repo or implement here
        new CustomerCloudRepository(context).saveCustomerToCloud(customer);
    }
}
