package com.example.noamnakavfinal;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.model.Sale;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PurchaseActivity extends AppCompatActivity {

    // קודים לזיהוי בקשת הרשאה
    private static final int SMS_PERMISSION_CODE_PURCHASE = 100;
    private static final int SMS_PERMISSION_CODE_MEETING = 101;

    TextView tvTitle, tvPrice, tvYear;
    EditText etEmail, etIdNumber, etCardNumber, etCardExpiry, etCvv;
    Button btnConfirmPurchase, btnCreateMeeting;

    DatabaseService db;
    Car currentCar;

    // משתנים לשמירת נתונים זמניים
    private int selectedInstallments = 1;
    private double monthlyPayment = 0;
    private String pendingMeetingTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // 1. אתחול רכיבים
        initViews();

        db = DatabaseService.getInstance();

        // 2. קבלת נתוני הרכב
        currentCar = (Car) getIntent().getSerializableExtra("car");
        if (currentCar != null) {
            tvTitle.setText(currentCar.getBrand() + " " + currentCar.getModel());
            tvPrice.setText("₪ " + currentCar.getPrice());
            tvYear.setText("שנה: " + currentCar.getYear());
        }

        // 3. מילוי אימייל אוטומטי
        autoFillUserEmail();

        // 4. כפתור רכישה
        btnConfirmPurchase.setOnClickListener(v -> {
            if (validateInputs()) {
                showInstallmentOptionsDialog();
            }
        });

        // 5. כפתור פגישה
        btnCreateMeeting.setOnClickListener(v -> openDateTimePicker());
    }

    private void initViews() {
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
    }

    // ==========================================
    // חלק א': לוגיקת יצירת פגישה
    // ==========================================

    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    openTimePicker(year1, month1, dayOfMonth);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void openTimePicker(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    pendingMeetingTime = String.format(Locale.getDefault(), "%02d/%02d/%d בשעה %02d:%02d",
                            day, month + 1, year, hourOfDay, minute1);
                    checkMeetingPermissionAndProceed();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void checkMeetingPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            scheduleMeetingAndSendSMS();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE_MEETING);
        }
    }

    private void scheduleMeetingAndSendSMS() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                String msg = "היי " + user.getFname() + ", נקבעה לך פגישה לתאריך " + pendingMeetingTime +
                        " בקשר לרכב מסוג " + currentCar.getBrand() + " " + currentCar.getModel() + ". נתראה!";

                if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                    sendSmsToUser(user.getPhone(), msg);
                }

                Toast.makeText(PurchaseActivity.this, "הפגישה נקבעה ו-SMS נשלח!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(PurchaseActivity.this, "שגיאה במשיכת פרטי משתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // חלק ב': לוגיקת רכישה, תשלומים ומחיקת רכב
    // ==========================================

    private void showInstallmentOptionsDialog() {
        final String[] options = {"תשלום אחד (ללא ריבית)", "2 תשלומים", "4 תשלומים", "6 תשלומים", "8 תשלומים", "10 תשלומים", "12 תשלומים", "24 תשלומים"};
        final int[] installmentValues = {1, 2, 4, 6, 8, 10, 12, 24};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר פריסת תשלומים");
        builder.setItems(options, (dialog, which) -> {
            selectedInstallments = installmentValues[which];
            monthlyPayment = currentCar.getPrice() / selectedInstallments;
            showFinalConfirmationDialog();
        });
        builder.show();
    }

    private void showFinalConfirmationDialog() {
        String message;
        if (selectedInstallments == 1) {
            message = "האם לחייב את כרטיסך בסך ₪" + currentCar.getPrice() + "?";
        } else {
            String formattedMonthly = String.format(Locale.getDefault(), "%.2f", monthlyPayment);
            message = "בחרת ב-" + selectedInstallments + " תשלומים.\n" +
                    "סכום כל תשלום: ₪" + formattedMonthly + "\n" +
                    "האם לאשר את העסקה?";
        }

        new AlertDialog.Builder(this)
                .setTitle("אישור עסקה")
                .setMessage(message)
                .setPositiveButton("אשר רכישה", (dialog, which) -> checkPurchasePermissionAndProceed())
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void checkPurchasePermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            performPurchase(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE_PURCHASE);
        }
    }

    private void performPurchase(boolean sendSms) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null || currentCar == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Sale newSale = new Sale(null, currentCar, user, currentDate, currentCar.getPrice());

                // 1. שמירת הרכישה בהיסטוריה
                db.createNewSale(newSale, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) {

                        // 2. מחיקת הרכב מהדאטהבייס (כדי שלא יקנו אותו שוב)
                        db.deleteCar(currentCar.getId(), new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                // 3. שליחת SMS
                                String smsMsg = "מזל טוב " + user.getFname() + "! תתחדש על ה-" + currentCar.getBrand() + ". ";
                                if (selectedInstallments > 1) {
                                    String formattedMonthly = String.format(Locale.getDefault(), "%.2f", monthlyPayment);
                                    smsMsg += "החיוב חולק ל-" + selectedInstallments + " תשלומים בסך " + formattedMonthly + " ש\"ח.";
                                } else {
                                    smsMsg += "החיוב בסך " + currentCar.getPrice() + " בוצע בהצלחה.";
                                }

                                if (sendSms && user.getPhone() != null && !user.getPhone().isEmpty()) {
                                    sendSmsToUser(user.getPhone(), smsMsg);
                                }

                                Toast.makeText(PurchaseActivity.this, "הרכישה הושלמה והרכב הוסר מהמאגר!", Toast.LENGTH_LONG).show();

                                // 4. מעבר למסך כל הרכבים
                                Intent intent = new Intent(PurchaseActivity.this, SearchAllCars.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); // סגירת המסך הנוכחי
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(PurchaseActivity.this, "שגיאה במחיקת הרכב מהמערכת", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(PurchaseActivity.this, "שגיאה בשמירת נתוני הרכישה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailed(Exception e) {}
        });
    }

    // ==========================================
    // חלק ג': כללי (הרשאות ועזרים)
    // ==========================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == SMS_PERMISSION_CODE_PURCHASE) {
            if (granted) performPurchase(true);
            else {
                Toast.makeText(this, "אין הרשאת SMS - הרכישה תבוצע ללא הודעה", Toast.LENGTH_LONG).show();
                performPurchase(false);
            }
        } else if (requestCode == SMS_PERMISSION_CODE_MEETING) {
            if (granted) scheduleMeetingAndSendSMS();
            else Toast.makeText(this, "חובה הרשאת SMS כדי לשלוח זימון לפגישה", Toast.LENGTH_LONG).show();
        }
    }

    private void sendSmsToUser(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoFillUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) runOnUiThread(() -> etEmail.setText(user.getEmail()));
                }
                @Override
                public void onFailed(Exception e) {}
            });
        }
    }

    private boolean validateInputs() {
        if (etIdNumber.getText().toString().isEmpty() ||
                etCardNumber.getText().toString().isEmpty() ||
                etCardExpiry.getText().toString().isEmpty() ||
                etCvv.getText().toString().isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל פרטי התשלום", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}