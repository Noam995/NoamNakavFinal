package com.example.noamnakavfinal;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

public class UserDetailsActivity extends AppCompatActivity {

    EditText etFname, etLname, etEmail, etPhone;
    Button btnUpdate, btnDelete;

    String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            finish();
            return;
        }

        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        loadUser();

        btnUpdate.setOnClickListener(v -> updateUser());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadUser() {
        DatabaseService.getInstance().getUser(userId, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                etFname.setText(user.getFname());
                etLname.setText(user.getLname());
                etEmail.setText(user.getEmail());
                etPhone.setText(user.getPhone());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserDetailsActivity.this, "שגיאה בטעינת משתמש", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUser() {
        User user = new User();
        user.setId(userId);
        user.setFname(etFname.getText().toString());
        user.setLname(etLname.getText().toString());
        user.setEmail(etEmail.getText().toString());
        user.setPhone(etPhone.getText().toString());

        DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UserDetailsActivity.this, "המשתמש עודכן", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserDetailsActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת משתמש")
                .setMessage("אתה בטוח שברצונך למחוק את המשתמש?")
                .setPositiveButton("מחק", (d, w) -> deleteUser())
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void deleteUser() {
        DatabaseService.getInstance().deleteUser(userId, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UserDetailsActivity.this, "המשתמש נמחק", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserDetailsActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}