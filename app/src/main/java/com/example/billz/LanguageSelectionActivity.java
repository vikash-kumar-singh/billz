package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionActivity extends AppCompatActivity {

    private LanguageAdapter adapter;
    private List<Language> languageList;
    private boolean isFromSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        isFromSettings = getIntent().getBooleanExtra("isFromSettings", false);

        RecyclerView recyclerView = findViewById(R.id.recyclerLanguages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        setupLanguages();

        adapter = new LanguageAdapter(languageList, language -> {
            // Language selected
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Language selected = adapter.getSelectedLanguage();
            if (selected != null) {
                LocaleHelper.setLocale(this, selected.getCode());
                PreferenceManager preferenceManager = new PreferenceManager(this);
                preferenceManager.setSelectedLanguage(selected.getCode());
                if (isFromSettings) {
                    finish();
                } else {
                    navigateToNextScreen();
                }
            } else {
                Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLanguages() {
        languageList = new ArrayList<>();
        languageList.add(new Language("English", "English", "en"));
        languageList.add(new Language("Hindi", "हिन्दी", "hi"));
        languageList.add(new Language("Bengali", "বাংলা", "bn"));
        languageList.add(new Language("Tamil", "தமிழ்", "ta"));
        languageList.add(new Language("Telugu", "తెలుగు", "te"));
        languageList.add(new Language("Kannada", "ಕನ್ನಡ", "kn"));
        languageList.add(new Language("Marathi", "मराठी", "mr"));
    }

    private void navigateToNextScreen() {
        PreferenceManager preferenceManager = new PreferenceManager(this);
        preferenceManager.setFirstLaunch(false);
        
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
