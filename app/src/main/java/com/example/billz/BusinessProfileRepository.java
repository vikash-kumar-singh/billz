package com.example.billz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.List;
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
                            syncToRoom(profile);
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
        preferenceManager.setBusinessRole(profile.getRole());
        preferenceManager.setBusinessStatus(profile.getStatus());
        preferenceManager.setCountry(profile.getCountry());
        preferenceManager.setTimezone(profile.getTimezone());
        preferenceManager.setBusinessType(profile.getBusinessType());
        preferenceManager.setCurrency(profile.getCurrency());
        preferenceManager.setNumberSystem(profile.getNumberSystem());
        preferenceManager.setDecimalPlaces(profile.getDecimalPlaces());
    }

    private void syncToRoom(BusinessProfile profile) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            // Sync to local Room Business entity
            Business b = db.businessDao().getSelectedBusiness();
            if (b == null) {
                // If nothing selected, create or find by name
                List<Business> all = db.businessDao().getAllBusinesses();
                if (all.isEmpty()) {
                    b = new Business(profile.getBusinessName(), profile.getMobile(), profile.getRole(), true);
                } else {
                    b = all.get(0);
                }
            }
            
            b.setName(profile.getBusinessName());
            b.setPhoneNumber(profile.getMobile());
            b.setEmail(profile.getEmail());
            b.setCategory(profile.getCategory());
            b.setRole(profile.getRole());
            b.setPlan(profile.getPlan());
            b.setStatus(profile.getStatus());
            
            db.businessDao().insert(b);

            // Sync to ReceiptSettings
            ReceiptSettings rs = db.receiptSettingsDao().getSettingsByBusiness(b.getId());
            if (rs == null) {
                rs = new ReceiptSettings();
                rs.setId(b.getId());
            }
            rs.setBusinessName(profile.getBusinessName());
            rs.setBusinessAddress(profile.getAddress());
            rs.setEmail(profile.getEmail());
            rs.setPhoneNumber(profile.getMobile());
            db.receiptSettingsDao().insert(rs);

            Log.d(TAG, "SWITCH_BUSINESS_DATA_LOADED");
        });
    }

    public BusinessProfile getCachedProfile() {
        BusinessProfile profile = new BusinessProfile();
        profile.setBusinessName(preferenceManager.getBusinessName());
        profile.setAddress(preferenceManager.getBusinessAddress());
        profile.setCategory(preferenceManager.getBusinessCategory());
        profile.setEmail(preferenceManager.getBusinessEmail());
        profile.setMobile(preferenceManager.getBusinessMobile());
        profile.setPlan(preferenceManager.getBusinessPlan());
        profile.setRole(preferenceManager.getBusinessRole());
        profile.setStatus(preferenceManager.getBusinessStatus());
        profile.setCountry(preferenceManager.getCountry());
        profile.setTimezone(preferenceManager.getTimezone());
        profile.setBusinessType(preferenceManager.getBusinessType());
        profile.setCurrency(preferenceManager.getCurrency());
        profile.setNumberSystem(preferenceManager.getNumberSystem());
        profile.setDecimalPlaces(preferenceManager.getDecimalPlaces());
        return profile;
    }
}
