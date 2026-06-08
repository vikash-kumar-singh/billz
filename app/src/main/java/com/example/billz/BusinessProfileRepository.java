package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

public class BusinessProfileRepository {

    private static final String TAG = "BusinessProfileRepo";
    private final Context context;
    private final PreferenceManager preferenceManager;

    public interface ProfileCallback {
        void onProfileLoaded(BusinessProfile profile);
        void onError(String message);
    }

    public BusinessProfileRepository(Context context) {
        this.context = context;
        this.preferenceManager = new PreferenceManager(context);
    }

    public void loadBusinessProfile(ProfileCallback callback) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) {
            Log.e(TAG, "PROFILE_LOAD_FAILED: User not logged in");
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        Log.d(TAG, "PROFILE_LOAD_STARTED for UID: " + uid);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("profile")
                .document("info")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BusinessProfile profile = documentSnapshot.toObject(BusinessProfile.class);
                        if (profile != null) {
                            cacheProfile(profile);
                            Log.d(TAG, "PROFILE_LOAD_SUCCESS");
                            Log.d(TAG, "BUSINESS_PROFILE_LOADED");
                            Log.d(TAG, "BUSINESS_NAME = " + profile.getBusinessName());
                            if (callback != null) callback.onProfileLoaded(profile);
                        }
                    } else {
                        Log.w(TAG, "PROFILE_LOAD_FAILED: Profile document does not exist");
                        if (callback != null) callback.onError("Profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "PROFILE_LOAD_FAILED", e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void saveBusinessProfile(Map<String, Object> profileData, ProfileCallback callback) {
        String uid = FirebaseHelper.getCurrentUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("profile")
                .document("info")
                .set(profileData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "PROFILE_SAVE_SUCCESS");
                    // Reload to update cache and notify UI
                    loadBusinessProfile(callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "PROFILE_SAVE_FAILED", e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void refreshBusinessProfile(ProfileCallback callback) {
        loadBusinessProfile(callback);
    }

    private void cacheProfile(BusinessProfile profile) {
        preferenceManager.setBusinessName(profile.getBusinessName());
        preferenceManager.setBusinessAddress(profile.getAddress());
        preferenceManager.setBusinessCategory(profile.getCategory());
        preferenceManager.setBusinessEmail(profile.getEmail());
        preferenceManager.setBusinessMobile(profile.getMobile());
        preferenceManager.setBusinessPlan(profile.getPlan());
    }

    public BusinessProfile getCachedProfile() {
        BusinessProfile profile = new BusinessProfile();
        profile.setBusinessName(preferenceManager.getBusinessName());
        profile.setAddress(preferenceManager.getBusinessAddress());
        profile.setCategory(preferenceManager.getBusinessCategory());
        profile.setEmail(preferenceManager.getBusinessEmail());
        profile.setMobile(preferenceManager.getBusinessMobile());
        profile.setPlan(preferenceManager.getBusinessPlan());
        return profile;
    }
}
