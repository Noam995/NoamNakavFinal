package com.example.noamnakavfinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etPassword, etFName, etLName, etPhone;
    private Button btnRegister;
    private TextView tvLogin;
    private DatabaseService databaseService;

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedPreferences;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // התיקון לשגיאת ה-R.id.main באדום - מוודא שה-ID קיים ב-XML
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        databaseService = DatabaseService.getInstance();

        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        etFName = findViewById(R.id.etFname);
        etLName = findViewById(R.id.etLname);
        etPhone = findViewById(R.id.etphone);
        btnRegister = findViewById(R.id.btnSubmit);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegister.getId()) {
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            String fName = etFName.getText().toString().trim();
            String lName = etLName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (validateInput(fName, lName, phone, email, password)) {
                registerUser(fName, lName, phone, email, password);
            }
        } else if (v.getId() == tvLogin.getId()) {
            finish();
        }
    }

    private boolean validateInput(String fname, String lname, String phone, String email, String password) {
        if (fname.isEmpty()) { etFName.setError("נא להזין שם פרטי"); return false; }
        if (lname.isEmpty()) { etLName.setError("נא להזין שם משפחה"); return false; }
        if (phone.length() < 9) { etPhone.setError("טלפון לא תקין"); return false; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("אימייל לא תקין"); return false; }
        if (password.length() < 6) { etPassword.setError("סיסמה חייבת להיות לפחות 6 תווים"); return false; }
        return true;
    }

    private void registerUser(String fname, String lname, String phone, String email, String password) {
        User user = new User("", fname, lname, phone, email, password, false);
        createUserInDatabase(user);
    }

    private void createUserInDatabase(User user) {
        databaseService.createNewUser(user, new DatabaseService.DatabaseCallback<String>() {
            @Override
            public void onCompleted(String uid) {
                user.setId(uid);

                // שמירה ב-SharedPreferences לחיבור אוטומטי בעתיד
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.apply();

                // מעבר ישיר ל-MainActivity (חיבור מיידי)
                Intent mainIntent = new Intent(Register.this, UserPage.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(Register.this, "שגיאה ברישום: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
}