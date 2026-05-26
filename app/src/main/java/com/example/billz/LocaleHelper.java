package com.example.billz;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class LocaleHelper {
    private static final String PREF_NAME = "LanguagePref";
    private static final String KEY_LANGUAGE = "selected_language";

    public static void setLocale(Context context, String languageCode) {
        persist(context, languageCode);
        applyLocale(languageCode);
    }

    private static void persist(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static String getPersistedLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, "en");
    }

    public static void applyLocale(String languageCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }
}
