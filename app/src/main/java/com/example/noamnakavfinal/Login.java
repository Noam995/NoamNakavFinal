package com.example.noamnakavfinal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private String email2;
    private String pass2;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;
    public static final String MyPREFERENCES="MyPrefs";
    SharedPreferences sharedPreferences;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences=getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.emailInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginBtn);
        tvRegister = findViewById(R.id.registerText);
        email2=sharedPreferences.getString("email","");
        pass2=sharedPreferences.getString("password","");

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

        etEmail.setText(email2);
        etPassword.setText(pass2);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == btnLogin.getId()) {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("email", email);
            editor.putString("password", password);

            editor.commit();


            if (!checkInput(email, password)) {
                return;
            }

            loginUser(email, password);

        } else if (id == tvRegister.getId()) {
            Intent registerIntent = new Intent(Login.this, Register.class);
            startActivity(registerIntent);
        }

    }

    private boolean checkInput(String email, String password) {
        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("נא להכניס אימייל תקין");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("סיסמה חייבת להיות לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            etPassword.setError("שגיאה בכניסה, נסה שוב");
                            etPassword.requestFocus();
                            return;
                        }
                        String uid = firebaseUser.getUid();

                        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) {
                                Log.d(TAG, "Login success, user: " + user.getId());

                                Intent homepageIntent = new Intent(Login.this, MainActivity.class);
                                homepageIntent.putExtra("USER_ID", user.getId());
                                homepageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(homepageIntent);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Log.e(TAG, "Failed to get user data", e);
                                etPassword.setError("שגיאה בטעינת נתוני המשתמש");
                                etPassword.requestFocus();
                            }
                        });

                    } else {
                        etPassword.setError("אימייל או סיסמה שגויים");
                        etPassword.requestFocus();
                        Log.e(TAG, "Login failed", task.getException());
                    }
                });
    }
}

