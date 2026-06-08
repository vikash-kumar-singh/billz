package com.example.billz;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {

    public static String getCurrentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static DocumentReference getUserDoc() {
        String uid = getCurrentUid();
        if (uid == null) return null;
        return FirebaseFirestore.getInstance().collection("users").document(uid);
    }

    public static DocumentReference getUserProfileInfo() {
        DocumentReference userDoc = getUserDoc();
        if (userDoc == null) return null;
        return userDoc.collection("profile").document("info");
    }

    public static CollectionReference getCustomersCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("customers") : null;
    }

    public static CollectionReference getProductsCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("products") : null;
    }

    public static CollectionReference getInvoicesCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("invoices") : null;
    }

    public static CollectionReference getExpensesCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("expenses") : null;
    }

    public static CollectionReference getEmployeesCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("employees") : null;
    }

    public static CollectionReference getSettingsCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("settings") : null;
    }

    public static CollectionReference getSubscriptionCollection() {
        DocumentReference userDoc = getUserDoc();
        return userDoc != null ? userDoc.collection("subscription") : null;
    }
}
