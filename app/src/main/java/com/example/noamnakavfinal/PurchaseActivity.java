package com.example.noamnakavfinal;

// ייבוא מחלקות נדרשות של אנדרואיד, פיירבייס, מודלים של הפרויקט ועזרים כמו תאריך ושעה
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
import com.example.noamnakavfinal.model.Meeting; // מודל הפגישה
import com.example.noamnakavfinal.model.Sale;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// מחלקה האחראית על מסך הרכישה - מאפשרת למשתמש לקנות רכב (כולל פריסת תשלומים) או לקבוע פגישה לנסיעת מבחן
public class PurchaseActivity extends AppCompatActivity {

    // קודים (מספרים ייחודיים) לזיהוי בקשות ההרשאה לשליחת SMS מול מערכת ההפעלה
    private static final int SMS_PERMISSION_CODE_PURCHASE = 100; // הרשאה במסגרת רכישה
    private static final int SMS_PERMISSION_CODE_MEETING = 101;  // הרשאה במסגרת קביעת פגישה

    // --- רכיבי תצוגה ---
    TextView tvTitle, tvPrice, tvYear; // טקסטים להצגת פרטי הרכב
    EditText etEmail, etIdNumber, etCardNumber, etCardExpiry, etCvv; // שדות קלט לפרטי הלקוח ואשראי
    Button btnConfirmPurchase, btnCreateMeeting; // כפתורי פעולה

    DatabaseService db; // שירות הגישה למסד הנתונים
    Car currentCar; // משתנה שיחזיק את הרכב הספציפי שעליו מתבצעת הפעולה

    // --- משתנים לשמירת נתונים זמניים של התשלום ---
    private int selectedInstallments = 1; // מספר התשלומים (ברירת מחדל: תשלום 1)
    private double monthlyPayment = 0; // גובה התשלום החודשי

    // --- משתנים לשמירת נתוני הפגישה המתוכננת ---
    private String pendingMeetingTime = ""; // המחרוזת המלאה להודעת ה-SMS
    private String pendingDate = ""; // התאריך שנבחר
    private String pendingTimeStr = ""; // השעה שנבחרה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // 1. אתחול רכיבי התצוגה וחיבור ל-XML
        initViews();

        // קבלת מופע של שירות מסד הנתונים
        db = DatabaseService.getInstance();

        // 2. קבלת נתוני הרכב מהמסך הקודם שהפעיל את המסך הזה
        currentCar = (Car) getIntent().getSerializableExtra("car");
        if (currentCar != null) {
            // אם הרכב עבר בהצלחה, נציג את פרטיו בראש המסך
            tvTitle.setText(currentCar.getBrand() + " " + currentCar.getModel());
            tvPrice.setText("₪ " + currentCar.getPrice());
            tvYear.setText("שנה: " + currentCar.getYear());
        }

        // 3. מילוי אוטומטי של שדה האימייל של המשתמש כדי לחסוך לו הקלדה
        autoFillUserEmail();

        // 4. הגדרת לחיצה על כפתור רכישה
        btnConfirmPurchase.setOnClickListener(v -> {
            // מוודאים שכל שדות כרטיס האשראי והת.ז מלאים
            if (validateInputs()) {
                // אם הכל תקין, מציגים את חלון בחירת התשלומים
                showInstallmentOptionsDialog();
            }
        });

        // 5. הגדרת לחיצה על כפתור תיאום פגישה
        btnCreateMeeting.setOnClickListener(v -> openDateTimePicker());
    }

    // פונקציה שמקשרת בין משתני הקוד לבין רכיבי ה-UI במסך
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

    // פותח חלונית (Dialog) של מערכת ההפעלה לבחירת תאריך
    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance(); // מביא את התאריך של היום
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // יצירת חלון בחירת תאריך
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    // ברגע שהמשתמש בחר תאריך ואישר, נפתח לו ישר את חלון בחירת השעה
                    openTimePicker(year1, month1, dayOfMonth);
                }, year, month, day);

        // מונע מהמשתמש לבחור תאריכים בעבר (המינימום הוא הזמן הנוכחי)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // פותח חלונית לבחירת שעה (מופעל אוטומטית אחרי בחירת התאריך)
    private void openTimePicker(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    // פיצול התאריך והשעה כדי שנוכל לשמור אותם מסודר במודל הפגישה (שמים 0 מוביל אם צריך)
                    pendingDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                    pendingTimeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);

                    // משתנה שמשלב את שניהם בשביל הודעת ה-SMS
                    pendingMeetingTime = pendingDate + " בשעה " + pendingTimeStr;

                    // לאחר שהמשתמש סיים לבחור תאריך ושעה, נבדוק הרשאות להודעות ונמשיך
                    checkMeetingPermissionAndProceed();
                }, hour, minute, true); // true = שעון 24 שעות במקום AM/PM
        timePickerDialog.show();
    }

    // בודק אם לאפליקציה יש הרשאה לשלוח SMS. אם כן - ממשיך. אם לא - מבקש הרשאה.
    private void checkMeetingPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            scheduleMeetingAndSendSMS(); // יש הרשאה, אפשר לקבוע פגישה ולשלוח
        } else {
            // אין הרשאה - מבקש מהמשתמש לאשר קופצת של אנדרואיד
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE_MEETING);
        }
    }

    // שומר את הפגישה בדאטה-בייס ושולח הודעת סמס ללקוח
    private void scheduleMeetingAndSendSMS() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return; // הגנה: אם אין משתמש, עוצרים

        String uid = mAuth.getCurrentUser().getUid(); // שליפת ה-ID של המשתמש
        // משיכת פרטי המשתמש המלאים מהמסד כדי לקבל את מספר הטלפון והשם שלו
        db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                // 1. יצירת מזהה ואובייקט פגישה
                String meetingId = db.generateMeetingId();
                Meeting meeting = new Meeting(meetingId, user.getEmail(), pendingDate, pendingTimeStr);

                // 2. שמירת הפגישה בדאטה בייס (כדי שהמנהל יראה אותה)
                db.createNewMeeting(meeting, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        // 3. רק לאחר שהפגישה נשמרה בהצלחה בשרת, נכין ונשלח את ה-SMS
                        String msg = "היי " + user.getFname() + ", נקבעה לך פגישה לתאריך " + pendingMeetingTime +
                                " בקשר לרכב מסוג " + currentCar.getBrand() + " " + currentCar.getModel() + ". נתראה!";

                        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                            sendSmsToUser(user.getPhone(), msg);
                        }

                        Toast.makeText(PurchaseActivity.this, "הפגישה נשמרה בהצלחה ו-SMS נשלח!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(PurchaseActivity.this, "שגיאה בשמירת הפגישה במסד הנתונים", Toast.LENGTH_SHORT).show();
                    }
                });
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

    // מציג דיאלוג לבחירת כמות התשלומים
    private void showInstallmentOptionsDialog() {
        // המערכים המסדרים את האפשרויות לתצוגה ואת הערך המספרי שמאחוריהן
        final String[] options = {"תשלום אחד (ללא ריבית)", "2 תשלומים", "4 תשלומים", "6 תשלומים", "8 תשלומים", "10 תשלומים", "12 תשלומים", "24 תשלומים"};
        final int[] installmentValues = {1, 2, 4, 6, 8, 10, 12, 24};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר פריסת תשלומים");

        // מה יקרה כשמשתמש בוחר פריט מתוך הרשימה (which מייצג את המיקום שנבחר)
        builder.setItems(options, (dialog, which) -> {
            selectedInstallments = installmentValues[which]; // קבלת מספר התשלומים (למשל 4)
            monthlyPayment = currentCar.getPrice() / selectedInstallments; // חישוב חודשי

            // הצגת סיכום ותיקוף סופי של העסקה
            showFinalConfirmationDialog();
        });
        builder.show();
    }

    // מציג דיאלוג סופי שבו הלקוח רואה את סיכום התשלום ומאשר רכישה
    private void showFinalConfirmationDialog() {
        String message;
        if (selectedInstallments == 1) {
            message = "האם לחייב את כרטיסך בסך ₪" + currentCar.getPrice() + "?";
        } else {
            // מעצב את המספר שיראה כמו כסף (למשל 2300.50 במקום 2300.5)
            String formattedMonthly = String.format(Locale.getDefault(), "%.2f", monthlyPayment);
            message = "בחרת ב-" + selectedInstallments + " תשלומים.\n" +
                    "סכום כל תשלום: ₪" + formattedMonthly + "\n" +
                    "האם לאשר את העסקה?";
        }

        new AlertDialog.Builder(this)
                .setTitle("אישור עסקה")
                .setMessage(message)
                // במקרה של אישור, יבדוק הרשאות SMS ויתקדם לרכישה
                .setPositiveButton("אשר רכישה", (dialog, which) -> checkPurchasePermissionAndProceed())
                // במקרה של ביטול לא יעשה כלום
                .setNegativeButton("ביטול", null)
                .show();
    }

    // בדיקת הרשאות SMS במסגרת תהליך הרכישה
    private void checkPurchasePermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            performPurchase(true); // מבצע רכישה ושולח SMS
        } else {
            // מבקש הרשאה אם טרם ניתנה
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE_PURCHASE);
        }
    }

    // הפונקציה המרכזית שמטפלת במכירה: שומרת תיעוד במסד, מוחקת את הרכב מהמלאי, ושולחת SMS
    private void performPurchase(boolean sendSms) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null || currentCar == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        // קודם מביאים את המשתמש הנוכחי כדי שנוכל לחבר אותו לעסקה
        db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                // קבלת התאריך והשעה הנוכחיים כדי לתעד את העסקה
                String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

                // יצירת אובייקט מכירה (Sale) שכולל את הרכב, הקונה, התאריך והסכום
                Sale newSale = new Sale(null, currentCar, user, currentDate, currentCar.getPrice());

                // 1. שמירת עסקת הרכישה בהיסטוריית העסקאות (כדי שהמנהל יוכל לראות)
                db.createNewSale(newSale, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) {

                        // 2. מחיקת הרכב לחלוטין מהמלאי (כדי שמשתמשים אחרים לא יוכלו לקנות אותו)
                        db.deleteCar(currentCar.getId(), new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                // 3. הכנת הודעת SMS עם סיכום העסקה
                                String smsMsg = "מזל טוב " + user.getFname() + "! תתחדש על ה-" + currentCar.getBrand() + ". ";
                                if (selectedInstallments > 1) {
                                    String formattedMonthly = String.format(Locale.getDefault(), "%.2f", monthlyPayment);
                                    smsMsg += "החיוב חולק ל-" + selectedInstallments + " תשלומים בסך " + formattedMonthly + " ש\"ח.";
                                } else {
                                    smsMsg += "החיוב בסך " + currentCar.getPrice() + " בוצע בהצלחה.";
                                }

                                // בדיקה שיש אישור לשלוח SMS ושמספר הטלפון לא ריק
                                if (sendSms && user.getPhone() != null && !user.getPhone().isEmpty()) {
                                    sendSmsToUser(user.getPhone(), smsMsg);
                                }

                                Toast.makeText(PurchaseActivity.this, "הרכישה הושלמה והרכב הוסר מהמאגר!", Toast.LENGTH_LONG).show();

                                // 4. מעבר חזרה למסך כל הרכבים, וניקוי המסכים מעליו בהיסטוריה (Clear Top)
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

    // פונקציה מובנית שמופעלת מיד לאחר שהמשתמש מגיב לחלון בקשת ההרשאה (אפשר או חסם)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // בדיקה האם ההרשאה אושרה
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        // ניתוב התשובה בהתאם לסיבה שבגללה ביקשנו (רכישה או פגישה)
        if (requestCode == SMS_PERMISSION_CODE_PURCHASE) {
            if (granted) performPurchase(true); // יבצע קנייה וישלח הודעה
            else {
                // המשתמש סירב להרשאת הודעות, אז נבצע קנייה אך בלי לשלוח לו חיווי ב-SMS
                Toast.makeText(this, "אין הרשאת SMS - הרכישה תבוצע ללא הודעה", Toast.LENGTH_LONG).show();
                performPurchase(false);
            }
        } else if (requestCode == SMS_PERMISSION_CODE_MEETING) {
            if (granted) scheduleMeetingAndSendSMS(); // הלקוח אישר - קובעים ושולחים SMS
            else Toast.makeText(this, "חובה הרשאת SMS כדי לשלוח זימון לפגישה", Toast.LENGTH_LONG).show(); // אי אפשר לקבוע פגישה ללא SMS (כך הוגדר כאן)
        }
    }

    // פונקציית העזר ששולחת פיזית את הודעת ה-SMS דרך רכיב ה-SmsManager של המכשיר
    private void sendSmsToUser(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace(); // במקרה של שגיאה בשליחה נדפיס ללוג
        }
    }

    // פונקציה שמושכת את האימייל של המשתמש המחובר מהרשת ושמה אותו ישירות בתיבת הטקסט
    private void autoFillUserEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    // runOnUiThread - מוודא שהעדכון של התצוגה מתבצע בתהליך הראשי, אחרת האפליקציה עלולה לקרוס
                    if (user != null) runOnUiThread(() -> etEmail.setText(user.getEmail()));
                }
                @Override
                public void onFailed(Exception e) {}
            });
        }
    }

    // ולידציה לשדות התשלום - מוודא שהמשתמש הזין נתונים בכל שדות האשראי
    private boolean validateInputs() {
        if (etIdNumber.getText().toString().isEmpty() ||
                etCardNumber.getText().toString().isEmpty() ||
                etCardExpiry.getText().toString().isEmpty() ||
                etCvv.getText().toString().isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל פרטי התשלום", Toast.LENGTH_SHORT).show();
            return false; // אם משהו חסר יחזיר שקר והרכישה לא תתקדם
        }
        return true;
    }
}