package com.example.billz;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "billz_prefs";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_BUSINESS_SETUP_COMPLETED = "business_setup_completed";
    private static final String KEY_SELECTED_LANGUAGE = "selected_language";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setFirstLaunch(boolean isFirst) {
        editor.putBoolean(KEY_IS_FIRST_LAUNCH, isFirst);
        editor.apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
    }

    public void setBusinessSetupCompleted(boolean completed) {
        editor.putBoolean(KEY_BUSINESS_SETUP_COMPLETED, completed);
        editor.apply();
    }

    public boolean isBusinessSetupCompleted() {
        return sharedPreferences.getBoolean(KEY_BUSINESS_SETUP_COMPLETED, false);
    }

    public void setSelectedLanguage(String languageCode) {
        editor.putString(KEY_SELECTED_LANGUAGE, languageCode);
        editor.apply();
    }

    public String getSelectedLanguage() {
        return sharedPreferences.getString(KEY_SELECTED_LANGUAGE, null);
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
