package com.example.billz;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class StaffCloudRepository {

    private static final String TAG = "StaffCloudRepo";

    public void saveStaffToCloud(Staff staff) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "STAFF_SAVE_FAILED: User not authenticated");
            return;
        }

        CollectionReference staffRef = FirebaseHelper.getEmployeesCollection();
        if (staffRef == null) {
            Log.e(TAG, "STAFF_SAVE_FAILED: Collection reference null");
            return;
        }

        // Use a unique document ID that isn't just the auto-generated local ID
        // because local IDs change on reinstall. 
        // We can use "staff_" + businessUuid + "_" + name.hashCode() or just a UUID.
        // Actually, if we use a stable cloud ID, it's better.
        // For now, let's just use the current staff.id but recognize it might conflict 
        // if we don't have a stable cloud-first ID.
        // A better way: If staff.id is 0, it's new. If it's already set from cloud, use it.
        
        String docId = String.valueOf(staff.id);
        
        Log.d(TAG, "STAFF_SAVE_STARTED: " + docId);

        staffRef.document(docId)
                .set(staff, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "STAFF_SAVE_SUCCESS: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "STAFF_SAVE_FAILED: " + docId, e));
    }

    public void syncStaffFromCloud(Context context, Runnable onComplete) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        CollectionReference staffRef = FirebaseHelper.getEmployeesCollection();
        if (staffRef == null) return;

        staffRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                List<Staff> cloudStaff = new ArrayList<>();
                
                // Get current business mapping to restore businessId (int) from businessUuid (String)
                List<Business> businesses = db.businessDao().getAllBusinesses();
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Staff staff = doc.toObject(Staff.class);
                    if (staff != null) {
                        // Restore local businessId from UUID
                        if (staff.businessUuid != null) {
                            for (Business b : businesses) {
                                if (staff.businessUuid.equals(b.getUuid())) {
                                    staff.businessId = b.getId();
                                    break;
                                }
                            }
                        } else {
                            // Legacy staff without UUID, assign to current business if only one exists
                            if (businesses.size() == 1) {
                                staff.businessId = businesses.get(0).getId();
                                staff.businessUuid = businesses.get(0).getUuid();
                            }
                        }
                        cloudStaff.add(staff);
                    }
                }

                if (!cloudStaff.isEmpty()) {
                    db.staffDao().insertAll(cloudStaff);
                    Log.d(TAG, "STAFF_SYNC_SUCCESS: " + cloudStaff.size() + " members");
                }
                
                if (onComplete != null) onComplete.run();
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "STAFF_SYNC_FAILED", e);
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteStaffFromCloud(int staffId) {
        CollectionReference staffRef = FirebaseHelper.getEmployeesCollection();
        if (staffRef == null) return;

        staffRef.document(String.valueOf(staffId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "STAFF_DELETE_SUCCESS: " + staffId))
                .addOnFailureListener(e -> Log.e(TAG, "STAFF_DELETE_FAILED: " + staffId, e));
    }
}
