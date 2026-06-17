package com.example.billz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_chat);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        TextView textGreeting = findViewById(R.id.textGreeting);
        
        // Get business name from PreferenceManager
        PreferenceManager pm = new PreferenceManager(this);
        String businessName = pm.getBusinessName();
        if (businessName == null || businessName.isEmpty()) {
            businessName = "User";
        }
        
        String greeting = getString(R.string.help_greeting_format, businessName);
        textGreeting.setText(greeting);

        findViewById(R.id.btnMessages).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Messages...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnStartConversation).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatbotActivity.class);
            startActivity(intent);
        });
    }
}
