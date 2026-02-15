package com.example.noamnakavfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

public class Register extends AppCompatActivity {

    EditText etFname, etLname, etEmail, etPassword, etPhone;
    DatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = DatabaseService.getInstance();

        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        etPhone = findViewById(R.id.etphone);
    }

    public void submit(View view) {
        String fname = etFname.getText().toString();
        String lname = etLname.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String phone = etPhone.getText().toString();

        // בדיקת תקינות בסיסית
        if(fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(null, fname, lname, phone, email, password, false);

        db.createNewUser(user, new DatabaseService.DatabaseCallback<String>() {
            @Override
            public void onCompleted(String uid) {
                // הודעת הצלחה
                Toast.makeText(Register.this, "נרשמת והתחברת בהצלחה!", Toast.LENGTH_SHORT).show();

                // --- השינוי כאן: מעבר ישיר ל-UserPage ---
                Intent intent = new Intent(Register.this, UserPage.class);

                // ניקוי ההיסטוריה כדי שהמשתמש לא יוכל לחזור למסך ההרשמה בלחיצה על 'חזור'
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(Register.this, "שגיאה בהרשמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void mainactivity(View view) {
        // כפתור חזרה למסך התחברות (במקרה וכבר יש משתמש)
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}