package com.example.noamnakavfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // שינוי הייבוא לכאן
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

public class UpdateProfileActivity extends AppCompatActivity {

    // שינוי המשתנים ל-EditText רגיל
    private EditText etName, etPhone, etEmail;
    private Button btnSave;
    private ProgressBar progressBar;
    private DatabaseService databaseService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // אתחול רכיבים
        initViews();

        databaseService = DatabaseService.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            loadUserData(uid);
        }

        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etUpdateName);
        etPhone = findViewById(R.id.etUpdatePhone);
        etEmail = findViewById(R.id.etUpdateEmail);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.pbUpdate);
    }

    private void loadUserData(String uid) {
        progressBar.setVisibility(View.VISIBLE);

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                progressBar.setVisibility(View.GONE);
                if (user != null) {
                    currentUser = user;
                    etName.setText(user.getFname());
                    etPhone.setText(user.getPhone());
                    etEmail.setText(user.getEmail());
                }
            }

            @Override
            public void onFailed(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UpdateProfileActivity.this, "שגיאה בטעינת נתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            // עדכון האובייקט המקומי
            currentUser.setFname(newName);
            currentUser.setPhone(newPhone);

            // שמירה ב-Firebase
            databaseService.updateUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(UpdateProfileActivity.this, "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // חזרה לדף הקודם
                }

                @Override
                public void onFailed(Exception e) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(UpdateProfileActivity.this, "עדכון נכשל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}