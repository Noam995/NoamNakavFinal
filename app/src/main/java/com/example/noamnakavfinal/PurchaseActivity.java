package com.example.noamnakavfinal;

// ייבוא מחלקות נדרשות של אנדרואיד, פיירבייס, מודלים של הפרויקט ועזרים כמו תאריך ושעה
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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

    // קודים (מספרים ייחודיים) לזיהוי בקשות ההרשאה להתראות מול מערכת ההפעלה
    private static final int NOTIFICATION_PERMISSION_CODE_PURCHASE = 100; // הרשאה במסגרת רכישה
    private static final int NOTIFICATION_PERMISSION_CODE_MEETING = 101;  // הרשאה במסגרת קביעת פגישה

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
    private String pendingMeetingTime = ""; // המחרוזת המלאה להודעת ההתראה
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
                    // פיצול התאריך והשעה כדי שנוכל לשמור אותם מסודר במודל הפגישה
                    pendingDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                    pendingTimeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);

                    // משתנה שמשלב את שניהם בשביל ההתראה
                    pendingMeetingTime = pendingDate + " בשעה " + pendingTimeStr;

                    // לאחר שהמשתמש סיים לבחור תאריך ושעה, נבדוק הרשאות להתראות ונמשיך
                    checkMeetingPermissionAndProceed();
                }, hour, minute, true); // true = שעון 24 שעות
        timePickerDialog.show();
    }

    // בודק אם לאפליקציה יש הרשאה לשלוח התראות. אם כן - ממשיך. אם לא - מבקש הרשאה.
    private void checkMeetingPermissionAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                scheduleMeeting(true); // יש הרשאה, שומר ומקפיץ התראה
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE_MEETING);
            }
        } else {
            // באנדרואיד ישן אין צורך לבקש הרשאה
            scheduleMeeting(true);
        }
    }

    // שומר את הפגישה בדאטה-בייס ומקפיץ התראה (אם יש אישור)
    private void scheduleMeeting(boolean showNotification) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return; // הגנה: אם אין משתמש, עוצרים

        String uid = mAuth.getCurrentUser().getUid(); // שליפת ה-ID של המשתמש
        db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                // 1. יצירת מזהה ואובייקט פגישה
                String meetingId = db.generateMeetingId();
                Meeting meeting = new Meeting(meetingId, user.getEmail(), pendingDate, pendingTimeStr);

                // 2. שמירת הפגישה בדאטה בייס
                db.createNewMeeting(meeting, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        // 3. הקפצת ההתראה (אם אושר)
                        if (showNotification) {
                            String msg = "נקבעה לך פגישה לתאריך " + pendingMeetingTime +
                                    " בקשר לרכב " + currentCar.getBrand() + " " + currentCar.getModel() + ". נתראה!";
                            sendAppNotification("פגישה נקבעה בהצלחה!", msg);
                        }

                        Toast.makeText(PurchaseActivity.this, "הפגישה נשמרה בהצלחה בשרת!", Toast.LENGTH_LONG).show();
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

    // בדיקת הרשאות התראות במסגרת תהליך הרכישה
    private void checkPurchasePermissionAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                performPurchase(true);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE_PURCHASE);
            }
        } else {
            performPurchase(true);
        }
    }

    // הפונקציה המרכזית שמטפלת במכירה: שומרת תיעוד, מוחקת רכב, ומקפיצה התראה
    private void performPurchase(boolean showNotification) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null || currentCar == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Sale newSale = new Sale(null, currentCar, user, currentDate, currentCar.getPrice());

                // 1. שמירת העסקה
                db.createNewSale(newSale, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) {
                        // 2. מחיקת הרכב מהמלאי
                        db.deleteCar(currentCar.getId(), new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                // 3. הקפצת התראה
                                if (showNotification) {
                                    String notifMsg = "תתחדש על ה-" + currentCar.getBrand() + "! ";
                                    if (selectedInstallments > 1) {
                                        String formattedMonthly = String.format(Locale.getDefault(), "%.2f", monthlyPayment);
                                        notifMsg += "החיוב חולק ל-" + selectedInstallments + " תשלומים בסך " + formattedMonthly + " ש\"ח.";
                                    } else {
                                        notifMsg += "החיוב בוצע בהצלחה.";
                                    }
                                    sendAppNotification("עסקה אושרה", notifMsg);
                                }

                                Toast.makeText(PurchaseActivity.this, "הרכישה הושלמה והרכב הוסר מהמאגר!", Toast.LENGTH_LONG).show();

                                // 4. חזרה לכל הרכבים
                                Intent intent = new Intent(PurchaseActivity.this, SearchAllCars.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(PurchaseActivity.this, "שגיאה במחיקת הרכב", Toast.LENGTH_SHORT).show();
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
    // =========================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == NOTIFICATION_PERMISSION_CODE_PURCHASE) {
            if (granted) performPurchase(true);
            else {
                Toast.makeText(this, "העסקה תתבצע, אך לא תוצג התראה ללא הרשאה", Toast.LENGTH_LONG).show();
                performPurchase(false);
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE_MEETING) {
            if (granted) scheduleMeeting(true);
            else {
                Toast.makeText(this, "הפגישה תישמר, אך לא תקבל התראה ללא הרשאה", Toast.LENGTH_LONG).show();
                scheduleMeeting(false);
            }
        }
    }

    // הפונקציה הכללית שיוצרת ומקפיצה את ההתראות (משמשת גם למכירה וגם לפגישה)
    private void sendAppNotification(String title, String message) {
        String channelId = "noam_motors_alerts";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // יצירת ערוץ (חובה באנדרואיד 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "התראות המערכת",
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // שימוש באייקון מובנה ובטוח של אנדרואיד, ושימוש ב-BigTextStyle כדי שהטקסט הארוך לא ייחתך
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
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