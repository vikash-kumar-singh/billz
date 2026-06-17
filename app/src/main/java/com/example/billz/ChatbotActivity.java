package com.example.billz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText editMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        View root = findViewById(R.id.chatbotRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        View btnSend = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(messages, this::openWhatsapp);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initial greeting with a small delay
        new Handler().postDelayed(() -> {
            addBotMessage("Hi! I'm your Billz assistant. How can I help you today?");
        }, 500);

        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                editMessage.setText("");
                processUserQuery(text);
            }
        });
    }

    private void addUserMessage(String message) {
        messages.add(new ChatMessage(message, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String message) {
        messages.add(new ChatMessage(message, ChatMessage.TYPE_BOT));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private void addWhatsappLink() {
        messages.add(new ChatMessage("", ChatMessage.TYPE_WHATSAPP_LINK));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private void processUserQuery(String query) {
        String input = query.toLowerCase();

        new Handler().postDelayed(() -> {
            if (input.contains("hello") || input.contains("hi")) {
                addBotMessage("Hello! How can I assist you with your business today?");
            } else if (input.contains("printer")) {
                addBotMessage("To setup your printer, go to Sidebar -> Printer Setup. We support thermal printers via Bluetooth.");
            } else if (input.contains("inventory") || input.contains("item") || input.contains("stock") || input.contains("category")) {
                addBotMessage("You can manage your products, categories, modifiers, and ingredients in the 'Inventory Management' section of the sidebar.");
            } else if (input.contains("report") || input.contains("sale") || input.contains("transaction")) {
                addBotMessage("The 'Reports' tab shows your daily sales and item analytics. You can filter by date using the calendar.");
            } else if (input.contains("how many customers")) {
                try {
                    AppDatabase db = AppDatabase.getInstance(this);
                    Business active = db.businessDao().getSelectedBusiness();
                    int customerCount = 0;
                    if (active != null) {
                        List<Customer> customers = db.customerDao().getAllCustomers(active.getId());
                        if (customers != null) {
                            customerCount = customers.size();
                        }
                    }
                    addBotMessage("You currently have " + customerCount + " customers registered in your system.");
                } catch (Exception e) {
                    Log.e("Chatbot", "Error counting customers", e);
                    addBotMessage("I encountered an error while checking your customer count.");
                }
            } else if (input.contains("how many staff") || input.contains("how many employees")) {
                try {
                    AppDatabase db = AppDatabase.getInstance(this);
                    Business active = db.businessDao().getSelectedBusiness();
                    int staffCount = 0;
                    if (active != null) {
                        List<Staff> staffList = db.staffDao().getAllStaff(active.getId());
                        if (staffList != null) {
                            staffCount = staffList.size();
                        }
                    }
                    addBotMessage("You have " + staffCount + " staff members in your active business.");
                } catch (Exception e) {
                    Log.e("Chatbot", "Error counting staff", e);
                    addBotMessage("I encountered an error while checking your staff members.");
                }
            } else if (input.contains("customer")) {
                addBotMessage("Manage your customers in 'Customer Management'. You can add new customers during checkout or from the sidebar.");
            } else if (input.contains("staff") || input.contains("role") || input.contains("attendance") || input.contains("salary")) {
                addBotMessage("In 'Staff Management', you can add staff members, assign roles (Manager, Helper, etc.), and track their attendance and salaries.");
            } else if (input.contains("expense") || input.contains("income") || input.contains("cash") || input.contains("flow")) {
                addBotMessage("Use the 'Add Expense' (Cash Flow) section to record your business spending and other income sources.");
            } else if (input.contains("receipt") || input.contains("bill") || input.contains("prefix") || input.contains("logo")) {
                addBotMessage("Go to 'Receipt Settings' to upload your business logo, set a bill prefix, or customize what shows on your receipts.");
            } else if (input.contains("business") || input.contains("tax") || input.contains("discount") || input.contains("payment")) {
                addBotMessage("In 'Business Settings', you can configure your Tax types, Discount values, and enable/disable various Payment Modes like UPI.");
            } else if (input.contains("return")) {
                addBotMessage("If a customer returns an item, you can find and handle that in the 'Returned Receipt' section of the sidebar.");
            } else if (input.contains("counter") || input.contains("cart") || input.contains("checkout")) {
                addBotMessage("Use the 'Counter' tab to add items to the cart and process a sale. You can add taxes and discounts before checking out.");
            } else if (input.contains("language")) {
                addBotMessage("You can change the app language anytime from Sidebar -> Language.");
            } else if (input.contains("device") || input.contains("version")) {
                addBotMessage("Check your phone's specific details and app version in Sidebar -> Device Details.");
            } else if (input.contains("switch") || input.contains("create")) {
                addBotMessage("You can manage multiple businesses! Use 'Switch Business' or 'Create Business' at the top of the sidebar.");
            } else {
                addBotMessage("I'm sorry, I'm still learning about that. Would you like to talk to our support team on WhatsApp for immediate help?");
                addWhatsappLink();
            }
        }, 1000);
    }

    private void openWhatsapp() {
        String phoneNumber = "918825347516";
        String url = "https://wa.me/" + phoneNumber;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
