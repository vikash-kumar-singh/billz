package com.example.billz;

import android.content.Context;
import java.util.concurrent.Executors;

public class BusinessHelper {
    public static void ensureActiveBusiness(Context context, Runnable onComplete) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Business active = db.businessDao().getSelectedBusiness();
            if (active == null) {
                PreferenceManager pm = new PreferenceManager(context);
                String businessName = pm.getBusinessName();
                if (businessName != null && !businessName.isEmpty()) {
                    Business local = new Business(businessName, pm.getBusinessMobile(), pm.getBusinessRole(), true);
                    local.setCategory(pm.getBusinessCategory());
                    local.setEmail(pm.getBusinessEmail());
                    db.businessDao().insert(local);
                }
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public static int getActiveBusinessId(Context context) {
        Business active = AppDatabase.getInstance(context).businessDao().getSelectedBusiness();
        return active != null ? active.getId() : -1;
    }
}
