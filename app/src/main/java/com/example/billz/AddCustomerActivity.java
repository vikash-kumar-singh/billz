package com.example.billz;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddCustomerActivity extends AppCompatActivity {

    private EditText editMobile, editFullName, editEmail, editDob, editAnniversary, editGstin, editAddress, editNotes;
    private android.widget.RadioButton radioMale, radioFemale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_customer);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddCustomer);
        toolbar.setNavigationOnClickListener(v -> finish());

        editMobile = findViewById(R.id.editMobile);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editDob = findViewById(R.id.editDob);
        editAnniversary = findViewById(R.id.editAnniversary);
        editGstin = findViewById(R.id.editGstin);
        editAddress = findViewById(R.id.editAddress);
        editNotes = findViewById(R.id.editNotes);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);

        findViewById(R.id.btnSaveCustomer).setOnClickListener(v -> saveCustomer());

        editDob.setOnClickListener(v -> showDatePickerDialog(editDob));
        editAnniversary.setOnClickListener(v -> showDatePickerDialog(editAnniversary));

        View appBar = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void saveCustomer() {
        String mobile = editMobile.getText().toString().trim();
        String name = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String anniversary = editAnniversary.getText().toString().trim();
        String gstin = editGstin.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();
        String gender = radioMale.isChecked() ? "Male" : (radioFemale.isChecked() ? "Female" : "");

        if (mobile.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            int bId = BusinessHelper.getActiveBusinessId(this);
            
            Customer existing = db.customerDao().getCustomerByMobile(mobile, bId);
            Customer customer;
            
            if (existing != null) {
                customer = existing;
                customer.setName(name);
                customer.setEmail(email);
                customer.setDob(dob);
                customer.setAnniversary(anniversary);
                customer.setGstin(gstin);
                customer.setAddress(address);
                customer.setNotes(notes);
                customer.setGender(gender);
            } else {
                customer = new Customer(mobile, name, email, gender, dob, anniversary, gstin, address, notes);
                customer.setBusinessId(bId);
                customer.setOrdersCount(0); // Explicitly 0 for manual add, will increment on purchase
                customer.setCreatedAt(System.currentTimeMillis());
                
                // Use Firestore-style ID generation
                String uid = FirebaseHelper.getCurrentUid();
                if (uid != null) {
                    String customerId = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(uid).collection("customers").document().getId();
                    customer.setId(customerId);
                } else {
                    customer.setId(java.util.UUID.randomUUID().toString());
                }
            }

            db.customerDao().insert(customer);
            
            // Sync to Cloud
            new CustomerSyncManager(this).syncCustomerToCloud(customer);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.success_save, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void showDatePickerDialog(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }
}
