package com.example.billz;

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
            String phoneNumber = "+918825347516"; // Support/Business Owner Number
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
