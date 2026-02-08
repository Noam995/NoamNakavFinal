package com.example.noamnakavfinal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

public class PurchaseActivity extends AppCompatActivity {

    TextView tvTitle, tvPrice, tvYear;
    EditText etEmail, etIdNumber, etCardNumber, etCardExpiry, etCvv;
    Button btnConfirmPurchase, btnCreateMeeting;

    DatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // UI
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvYear = findViewById(R.id.tvYear);

        etEmail = findViewById(R.id.etEmail);
        etIdNumber = findViewById(R.id.etIdNumber);
        etCardNumber = findViewById(R.id.etCardNumber);
        etCardExpiry = findViewById(R.id.etCardExpiry);
        etCvv = findViewById(R.id.etCvv);

        btnConfirmPurchase = findViewById(R.id.btnConfirmPurchase);
        btnCreateMeeting = findViewById(R.id.btnCreateMeeting);

        db = DatabaseService.getInstance();

        // הצגת הרכב
        Car car = (Car) getIntent().getSerializableExtra("car");
        if (car != null) {
            tvTitle.setText(car.getBrand() + " " + car.getModel());
            tvPrice.setText("₪ " + car.getPrice());
            tvYear.setText("שנה: " + car.getYear());
        }

        // משיכת אימייל המשתמש המחובר
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) {
                        runOnUiThread(() -> etEmail.setText(user.getEmail()));
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(PurchaseActivity.this,
                                    "שגיאה בטעינת פרטי המשתמש", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }

        // אישור רכישה
        btnConfirmPurchase.setOnClickListener(v -> {
            if (etIdNumber.getText().toString().isEmpty() ||
                    etCardNumber.getText().toString().isEmpty() ||
                    etCardExpiry.getText().toString().isEmpty() ||
                    etCvv.getText().toString().isEmpty()) {

                Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "הרכישה בוצעה בהצלחה!", Toast.LENGTH_LONG).show();
        });

        // יצירת פגישה
        btnCreateMeeting.setOnClickListener(v ->
                Toast.makeText(this, "פגישה נוצרה בהצלחה ✔", Toast.LENGTH_SHORT).show()
        );
    }
}