package com.example.billz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class StaffManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StaffAdapter adapter;
    private TextView textNoStaff;
    private ActivityResultLauncher<Intent> addStaffLauncher;
    private List<Staff> allStaffList;
    private TextView chipAll, chipAccess, chipAttendance;
    private View searchBarContainer, noResultsView;
    private EditText editSearch;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(LocaleHelper.getPersistedLanguage(this));
        super.onCreate(savedInstanceState);

        // Security Check: Ensure user is logged in
        if (FirebaseHelper.getCurrentUid() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbarStaff);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerStaff);
        textNoStaff = findViewById(R.id.textNoStaff);
        noResultsView = findViewById(R.id.noResultsView);
        
        chipAll = findViewById(R.id.chipAll);

        searchBarContainer = findViewById(R.id.searchBarContainer);
        editSearch = findViewById(R.id.editSearchStaff);
        ImageView imageSearch = findViewById(R.id.imageSearchStaff);
        ImageView imageClearSearch = findViewById(R.id.imageClearSearchStaff);
        ViewGroup mainContainer = findViewById(android.R.id.content);

        imageSearch.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(mainContainer);
            if (isSearchVisible) {
                searchBarContainer.setVisibility(View.GONE);
                editSearch.setText("");
                hideKeyboard();
            } else {
                searchBarContainer.setVisibility(View.VISIBLE);
                editSearch.requestFocus();
                showKeyboard();
            }
            isSearchVisible = !isSearchVisible;
        });

        imageClearSearch.setOnClickListener(v -> {
            if (editSearch.getText().length() > 0) {
                editSearch.setText("");
            } else {
                searchBarContainer.setVisibility(View.GONE);
                isSearchVisible = false;
                hideKeyboard();
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    checkEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupChips();

        addStaffLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String name = result.getData().getStringExtra("new_staff_name");
                        String email = result.getData().getStringExtra("new_staff_email");
                        showShareStaffBottomSheet(name, email);
                        loadStaff();
                    }
                }
        );

        findViewById(R.id.fabAddStaff).setOnClickListener(v -> {
            addStaffLauncher.launch(new Intent(this, AddStaffActivity.class));
        });

        View appBar = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void setupChips() {
        ViewGroup chipContainer = (ViewGroup) chipAll.getParent();
        chipAccess = (TextView) chipContainer.getChildAt(1);
        chipAttendance = (TextView) chipContainer.getChildAt(2);

        View.OnClickListener listener = v -> {
            updateChipUI((TextView) v);
            filterStaff();
        };

        chipAll.setOnClickListener(listener);
        chipAccess.setOnClickListener(listener);
        chipAttendance.setOnClickListener(listener);
        
        updateChipUI(chipAll);
    }

    private void updateChipUI(TextView selected) {
        int unselectedBg = Color.parseColor("#E2E8F0");
        int unselectedText = Color.parseColor("#64748B");
        int selectedBg = ContextCompat.getColor(this, R.color.colorPrimary);
        int selectedText = Color.WHITE;

        chipAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBg));
        chipAll.setTextColor(unselectedText);
        chipAccess.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBg));
        chipAccess.setTextColor(unselectedText);
        chipAttendance.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBg));
        chipAttendance.setTextColor(unselectedText);

        selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBg));
        selected.setTextColor(selectedText);
    }

    private void checkEmptyState() {
        if (adapter != null) {
            boolean isListEmpty = adapter.getItemCount() == 0;
            boolean isSearching = editSearch != null && !editSearch.getText().toString().isEmpty();

            if (isSearching) {
                noResultsView.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                textNoStaff.setVisibility(View.GONE);
                recyclerView.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            } else {
                noResultsView.setVisibility(View.GONE);
                textNoStaff.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }

    private void filterStaff() {
        if (allStaffList == null) return;

        List<Staff> filteredList = new ArrayList<>();
        if (chipAll.getTextColors().getDefaultColor() == Color.WHITE) {
            filteredList = allStaffList;
        } else if (chipAccess.getTextColors().getDefaultColor() == Color.WHITE) {
            for (Staff s : allStaffList) if (s.allowAppUse) filteredList.add(s);
        } else if (chipAttendance.getTextColors().getDefaultColor() == Color.WHITE) {
            for (Staff s : allStaffList) if (s.trackAttendance) filteredList.add(s);
        }

        updateRecyclerView(filteredList);
    }

    private void updateRecyclerView(List<Staff> list) {
        adapter = new StaffAdapter(list, new StaffAdapter.OnStaffClickListener() {
            @Override
            public void onStaffClick(Staff staff) {
                // When card is clicked, open the Add/Update Staff page
                Intent intent = new Intent(StaffManagementActivity.this, AddStaffActivity.class);
                intent.putExtra("staff_id", staff.id);
                addStaffLauncher.launch(intent);
            }

            @Override
            public void onPermissionsClick(Staff staff) {
                // When the "Permissions" badge is clicked, open the Add/Update Staff page
                Intent intent = new Intent(StaffManagementActivity.this, AddStaffActivity.class);
                intent.putExtra("staff_id", staff.id);
                addStaffLauncher.launch(intent);
            }

            @Override
            public void onAttendanceClick(Staff staff) {
                Intent intent = new Intent(StaffManagementActivity.this, AttendanceManagementActivity.class);
                intent.putExtra("staff_id", staff.id);
                startActivity(intent);
            }

            @Override
            public void onPaySlipClick(Staff staff) {
                Intent intent = new Intent(StaffManagementActivity.this, SalarySlipActivity.class);
                intent.putExtra("staff_id", staff.id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        checkEmptyState();
    }

    private void showShareStaffBottomSheet(String name, String email) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_share_staff_bottom_sheet, null);
        dialog.setContentView(view);

        TextView textName = view.findViewById(R.id.textShareName);
        TextView textEmail = view.findViewById(R.id.textShareEmail);
        MaterialButton btnShare = view.findViewById(R.id.btnShareStaff);

        textName.setText("Name: " + name);
        textEmail.setText("Email: " + email);

        btnShare.setOnClickListener(v -> {
            Toast.makeText(this, "Sharing " + name, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaff();
    }

    private void loadStaff() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Business active = db.businessDao().getSelectedBusiness();
            int bId = active != null ? active.getId() : -1;
            allStaffList = db.staffDao().getAllStaff(bId);
            runOnUiThread(this::filterStaff);
        });
    }
}
