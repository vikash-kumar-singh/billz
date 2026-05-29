package com.example.billz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CreateModifierActivity extends AppCompatActivity {

    private LinearLayout layoutModifierRows;
    private EditText editModifierSetName;
    private View btnDeleteModifierSet;
    private int modifierSetId = -1;
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_modifier);

        MaterialToolbar toolbar = findViewById(R.id.toolbarModifier);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        editModifierSetName = findViewById(R.id.editModifierSetName);
        layoutModifierRows = findViewById(R.id.layoutModifierRows);
        btnDeleteModifierSet = findViewById(R.id.btnDeleteModifierSet);

        findViewById(R.id.btnAddRow).setOnClickListener(v -> addModifierRow());
        findViewById(R.id.btnSaveModifier).setOnClickListener(v -> saveModifierSet());
        btnDeleteModifierSet.setOnClickListener(v -> deleteModifierSet());

        // Check if we are in update mode
        if (getIntent().hasExtra("modifier_set_id")) {
            isUpdate = true;
            modifierSetId = getIntent().getIntExtra("modifier_set_id", -1);
            String existingName = getIntent().getStringExtra("modifier_set_name");
            
            editModifierSetName.setText(existingName);
            toolbar.setTitle("UPDATE MODIFIER SET");
            btnDeleteModifierSet.setVisibility(View.VISIBLE);
            
            loadExistingOptions();
        } else {
            addModifierRow();
        }
    }

    private void loadExistingOptions() {
        if (modifierSetId == -1) return;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModifierOption> options = AppDatabase.getInstance(this).modifierDao().getOptionsForSet(modifierSetId);
            runOnUiThread(() -> {
                layoutModifierRows.removeAllViews();
                if (options != null && !options.isEmpty()) {
                    for (ModifierOption opt : options) {
                        addModifierRowWithData(opt.getName(), String.valueOf((int) opt.getPrice()));
                    }
                } else {
                    addModifierRow();
                }
            });
        });
    }

    private void addModifierRow() {
        addModifierRowWithData("", "");
    }

    private void addModifierRowWithData(String name, String price) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_modifier_row, layoutModifierRows, false);
        EditText editName = row.findViewById(R.id.editRowName);
        EditText editPrice = row.findViewById(R.id.editRowPrice);
        
        editName.setText(name);
        editPrice.setText(price);

        row.findViewById(R.id.btnRemoveRow).setOnClickListener(v -> {
            if (layoutModifierRows.getChildCount() > 1) {
                layoutModifierRows.removeView(row);
            } else {
                Toast.makeText(this, "At least one modifier is required", Toast.LENGTH_SHORT).show();
            }
        });
        layoutModifierRows.addView(row);
    }

    private void deleteModifierSet() {
        if (modifierSetId == -1) return;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).modifierDao().deleteFullSet(modifierSetId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Modifier Set deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void saveModifierSet() {
        String setName = editModifierSetName.getText().toString().trim();
        if (setName.isEmpty()) {
            Toast.makeText(this, "Please enter modifier set name", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ModifierOption> options = new ArrayList<>();
        for (int i = 0; i < layoutModifierRows.getChildCount(); i++) {
            View row = layoutModifierRows.getChildAt(i);
            EditText editName = row.findViewById(R.id.editRowName);
            EditText editPrice = row.findViewById(R.id.editRowPrice);
            
            String name = editName.getText().toString().trim();
            if (!name.isEmpty()) {
                double price = 0;
                try { price = Double.parseDouble(editPrice.getText().toString()); } catch (Exception ignored) {}
                options.add(new ModifierOption(0, name, price));
            }
        }

        if (options.isEmpty()) {
            Toast.makeText(this, "Please add at least one modifier with a name", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ModifierDao dao = AppDatabase.getInstance(this).modifierDao();
            if (isUpdate && modifierSetId != -1) {
                dao.deleteOptionsForSet(modifierSetId);
                // Update set name if changed
                ModifierSet existingSet = new ModifierSet(setName);
                existingSet.setId(modifierSetId);
                dao.insertModifierSet(existingSet); // OnConflict REPLACE should work if set up
                
                for (ModifierOption opt : options) {
                    opt.setModifierSetId(modifierSetId);
                }
            } else {
                ModifierSet set = new ModifierSet(setName);
                long setId = dao.insertModifierSet(set);
                for (ModifierOption opt : options) {
                    opt.setModifierSetId((int) setId);
                }
            }
            dao.insertModifierOptions(options);

            runOnUiThread(() -> {
                Toast.makeText(this, "Modifier Set '" + setName + "' saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
