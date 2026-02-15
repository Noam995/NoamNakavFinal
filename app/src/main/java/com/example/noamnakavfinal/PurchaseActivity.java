package com.example.noamnakavfinal;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.model.Sale;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class PurchaseActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    TextView tvTitle, tvPrice, tvYear;
    EditText etEmail, etIdNumber, etCardNumber, etCardExpiry, etCvv;
    Button btnConfirmPurchase, btnCreateMeeting;

    DatabaseService db;

    // משתנים לשמירת נתונים זמניים לתהליך הפגישה והמחיקה
    String userPhone = "";
    Car currentCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // אתחול רכיבי הממשק
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

        // קבלת נתוני הרכב מהמסך הקודם
        currentCar = (Car) getIntent().getSerializableExtra("car");
        if (currentCar != null) {
            tvTitle.setText(currentCar.getBrand() + " " + currentCar.getModel());
            tvPrice.setText("₪ " + currentCar.getPrice());
            tvYear.setText("שנה: " + currentCar.getYear());
        }

        // טעינת פרטי המשתמש המחובר (אימייל וטלפון)
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) {
                        runOnUiThread(() -> {
                            etEmail.setText(user.getEmail());
                            // שמירת הטלפון למקרה שתיקבע פגישה
                            if (user.getPhone() != null) {
                                userPhone = user.getPhone();
                            }
                        });
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

        // --- לוגיקת כפתור רכישה (כולל מחיקת הרכב) ---
        btnConfirmPurchase.setOnClickListener(v -> {
            // 1. בדיקת תקינות שדות
            String idNum = etIdNumber.getText().toString().trim();
            String cardNum = etCardNumber.getText().toString().trim();
            String expiry = etCardExpiry.getText().toString().trim();
            String cvv = etCvv.getText().toString().trim();

            if (idNum.isEmpty() || cardNum.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "אנא מלא את כל פרטי התשלום", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. קבלת המשתמש המחובר והרכב שנבחר

            Car car = (Car) getIntent().getSerializableExtra("car");

            if (mAuth.getCurrentUser() != null && car != null) {
                String uid = mAuth.getCurrentUser().getUid();

                // 3. שליפת פרטי המשתמש המלאים מהדאטהבייס כדי ליצור אובייקט Sale מלא
                db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        // יצירת תאריך נוכחי
                        String currentDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                                java.util.Locale.getDefault()).format(new java.util.Date());

                        // יצירת אובייקט המכירה (saleId ייווצר בתוך DatabaseService)
                        Sale newSale = new Sale(null, car, user, currentDate, car.getPrice());

                        // 4. שמירה להיסטוריית הרכישות (sales)
                        db.createNewSale(newSale, new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                Toast.makeText(PurchaseActivity.this,
                                        "הרכישה בוצעה בהצלחה ונשמרה בהיסטוריה!", Toast.LENGTH_LONG).show();
                                finish(); // סגירת המסך וחזרה אחורה
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(PurchaseActivity.this,
                                        "שגיאה בשמירת הרכישה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(PurchaseActivity.this, "שגיאה בזיהוי המשתמש", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "משתמש לא מחובר או רכב לא נמצא", Toast.LENGTH_SHORT).show();
            }
        });
        // --- לוגיקת כפתור יצירת פגישה (יומן + SMS) ---
        btnCreateMeeting.setOnClickListener(v -> showDateTimePicker());
    }

    // פונקציה להצגת דיאלוג בחירת תאריך
    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // לאחר בחירת תאריך, פתח את בחירת השעה
                    showTimePicker(selectedYear, selectedMonth, selectedDay);
                }, year, month, day);

        // חסימת תאריכים מהעבר
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    // פונקציה להצגת דיאלוג בחירת שעה
    private void showTimePicker(int year, int month, int day) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // יצירת מחרוזת של מועד הפגישה הסופי
                    String meetingTime = String.format("%02d/%02d/%d בשעה %02d:%02d",
                            day, month + 1, year, selectedHour, selectedMinute);

                    // בדיקת הרשאות ושליחת SMS
                    checkPermissionAndSendSMS(meetingTime);

                }, hour, minute, true); // פורמט 24 שעות
        timePickerDialog.show();
    }

    // בדיקת הרשאת SMS ושליחה
    private void checkPermissionAndSendSMS(String meetingTime) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // אם אין הרשאה - בקש אותה
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            // יש הרשאה - שלח
            sendSMS(meetingTime);
        }
    }

    // שליחת ה-SMS בפועל
    private void sendSMS(String meetingTime) {
        if (userPhone == null || userPhone.isEmpty()) {
            Toast.makeText(this, "לא ניתן לשלוח SMS: מספר הטלפון חסר", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "שלום, נקבעה פגישה בסוכנות AutoDeal עבור רכב " +
                    currentCar.getBrand() + " " + currentCar.getModel() +
                    "\nמועד: " + meetingTime;

            smsManager.sendTextMessage(userPhone, null, message, null, null);
            Toast.makeText(this, "הפגישה שוריינה והודעה נשלחה לנייד! ✔", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בשליחת SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // טיפול בתוצאת בקשת ההרשאה
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ההרשאה אושרה. אנא נסה לקבוע פגישה שוב.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "נדרשת הרשאת SMS כדי לשלוח אישור", Toast.LENGTH_SHORT).show();
            }
        }
    }
}