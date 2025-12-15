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

import com.example.noamnakavfinal.service.DatabaseService;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;


    private DatabaseService databaseService;

    public static final String MyPREFERENCES="MyPrefs";
    SharedPreferences sharedPreferences;
    private String password,email;


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

        sharedPreferences=getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.emailInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginBtn);
        tvRegister = findViewById(R.id.registerText);
        email=sharedPreferences.getString("email","");
        password=sharedPreferences.getString("password","");

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

        etEmail.setText(email);
        etPassword.setText(password);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == btnLogin.getId()) {
             email = etEmail.getText().toString();
             password = etPassword.getText().toString();



            if (!checkInput(email, password)) {
         //       return;
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




        databaseService.loginUser(email, password, new DatabaseService.DatabaseCallback<String>() {
            @Override
            public void onCompleted(String uid) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("email", email);
                editor.putString("password", password);

                editor.commit();
                Log.d(TAG, "Login: User logged successfully");

                Intent go = new Intent(Login.this, UserPage.class);
                startActivity(go);


                if (email.equals("jhhj") && password.equals("hjjhj")) {


                    Intent go1 = new Intent(Login.this, AdminPage.class);
                    startActivity(go1);

                } else

                    {


                    Intent go2 = new Intent(Login.this, UserPage.class);
                     startActivity(go2);

            }

            }

            @Override
            public void onFailed(Exception e) {

                Log.d(TAG, "Login: Error:   "+e.toString());

            }
        });


    }
}

