package com.example.billz;

import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.SetOptions;

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

        String staffId = String.valueOf(staff.id);
        Log.d(TAG, "STAFF_SAVE_STARTED: " + staffId);

        staffRef.document(staffId)
                .set(staff, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "STAFF_SAVE_SUCCESS: " + staffId))
                .addOnFailureListener(e -> Log.e(TAG, "STAFF_SAVE_FAILED: " + staffId, e));
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
