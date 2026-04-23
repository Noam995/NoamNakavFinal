package com.example.noamnakavfinal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.model.Meeting;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ArrangeMeeting extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;

    private EditText etDate, etTime;
    private Button btnSaveMeeting;
    private DatabaseService databaseService;
    private String userEmail = "אורח";

    private Car currentCar; // משתנה לשמירת פרטי הרכב

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_arrange_meeting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();

        // שליפת פרטי הרכב אם הועברו מהמסך הקודם (כדי לשלב בהודעה)
        if (getIntent().hasExtra("car")) {
            currentCar = (Car) getIntent().getSerializableExtra("car");
        }

        // שליפת כתובת המייל של המשתמש המחובר דרך FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail = currentUser.getEmail();
        }

        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnSaveMeeting = findViewById(R.id.btnSaveMeeting);

        btnSaveMeeting.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(ArrangeMeeting.this, "אנא מלא תאריך ושעה", Toast.LENGTH_SHORT).show();
                return;
            }

            // במקום לשמור ישר, קודם נבדוק הרשאת SMS
            checkPermissionAndProceed();
        });
    }

    // =====================================
    // ניהול הרשאות ושמירה
    // =====================================

    private void checkPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            saveMeeting(true); // שמירה + שליחת SMS
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveMeeting(true); // המשתמש אישר, נשמור ונשלח
            } else {
                Toast.makeText(this, "אין הרשאת SMS - הפגישה תישמר ללא התראה לנייד", Toast.LENGTH_LONG).show();
                saveMeeting(false); // המשתמש סירב להרשאה, נשמור בלי לשלוח SMS
            }
        }
    }

    // =====================================
    // לוגיקת השמירה והשליחה
    // =====================================

    private void saveMeeting(boolean shouldSendSms) {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String meetingId = databaseService.generateMeetingId();
        Meeting meeting = new Meeting(meetingId, userEmail, date, time);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. שמירת הפגישה בדאטה-בייס קודם כל
        databaseService.createNewMeeting(meeting, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {

                // 2. אם צריך לשלוח SMS, נמשוך את פרטי המשתמש כדי לקבל את הטלפון שלו
                if (shouldSendSms) {
                    fetchUserAndSendSms(currentUser.getUid(), date, time);
                } else {
                    Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ArrangeMeeting.this, "שגיאה בקביעת הפגישה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserAndSendSms(String uid, String date, String time) {
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {

                    // בניית ההודעה
                    String msg = "היי " + user.getFname() + ", נקבעה לך פגישה לתאריך " + date + " בשעה " + time;
                    if (currentCar != null) {
                        msg += " בקשר לרכב מסוג " + currentCar.getBrand() + " " + currentCar.getModel();
                    }
                    msg += ". נתראה!";

                    // שליחה בפועל
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(user.getPhone(), null, msg, null, null);
                        Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה ו-SMS נשלח בהצלחה!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה אך שליחת ה-SMS נכשלה", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה (חסר מספר טלפון לשליחת SMS)", Toast.LENGTH_LONG).show();
                }
                finish(); // חזרה למסך הקודם
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה (שגיאה במשיכת נתוני המשתמש ל-SMS)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}