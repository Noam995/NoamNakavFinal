package com.example.noamnakavfinal;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.Meeting;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List; // אל תשכח את הייבוא הזה

public class UserPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_page);

        // הגדרת ה-Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("אזור אישי");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_user_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UpdateProfileActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_cars) {
            startActivity(new Intent(this, SearchAllCars.class));
            return true;
        } else if (id == R.id.nav_home) {
            Toast.makeText(this, "אתה כבר באזור האישי שלך \uD83D\uDE0A", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_about) {
            startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // הפונקציה שיוצרת ומקפיצה התראה למכשיר
    private void sendMeetingNotification(String meetingDate, String meetingTime) {
        String channelId = "meeting_alerts";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "התראות פגישה",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("התראות עבור פגישות שנקבעו ב-Noam Motors");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("פגישה נקבעה בהצלחה!")
                .setContentText("הפגישה ב-Noam Motors נקבעה ל-" + meetingDate + " בשעה " + meetingTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                Toast.makeText(this, "אנא אשר התראות כדי לקבל אישורים על פגישות", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    // בחירת תאריך ושעה
    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {

                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int currentMinute = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {

                                // עיצוב התאריך והשעה
                                String meetingTime = String.format("%02d:%02d", hourOfDay, minute);
                                String meetingDate = dayOfMonth + "/" + (month + 1) + "/" + year;

                                // בדיקת זמינות מול שרת הפיירבייס לפני יצירת הפגישה
                                checkIfTimeIsAvailableAndSave(meetingDate, meetingTime);

                            }, currentHour, currentMinute, true);

                    timePickerDialog.show();

                }, currentYear, currentMonth, currentDay);

        // מניעת בחירת תאריך שעבר (לפני היום)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    // פונקציה חדשה: בדיקה במסד הנתונים האם קיימת כבר פגישה באותו המועד
    private void checkIfTimeIsAvailableAndSave(String chosenDate, String chosenTime) {
        DatabaseService dbService = DatabaseService.getInstance();

        // הערה: יש לוודא שקיימת פונקציה כזו במחלקה DatabaseService שמחזירה את כל הפגישות
        dbService.getMeetingList(new DatabaseService.DatabaseCallback<List<Meeting>>() {
            @Override
            public void onCompleted(List<Meeting> allMeetings) {
                boolean isTimeTaken = false;

                if (allMeetings != null) {
                    for (Meeting meeting : allMeetings) {
                        // אם התאריך והשעה קיימים כבר במסד הנתונים
                        if (chosenDate.equals(meeting.getDate()) && chosenTime.equals(meeting.getTime())) {
                            isTimeTaken = true;
                            break; // מצאנו התאמה, אפשר לצאת מהלולאה
                        }
                    }
                }

                if (isTimeTaken) {
                    // המועד תפוס! נודיע למשתמש
                    Toast.makeText(UserPage.this, "המועד שבחרת כבר תפוס \uD83D\uDE15\nאנא נסה לקבוע תאריך או שעה אחרים.", Toast.LENGTH_LONG).show();
                } else {
                    // המועד פנוי! נמשיך בשמירת הפגישה
                    saveMeeting(chosenDate, chosenTime, dbService);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserPage.this, "שגיאה בבדיקת זמינות: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה לשמירת הפגישה לאחר שווידאנו שהמועד פנוי
    private void saveMeeting(String meetingDate, String meetingTime, DatabaseService dbService) {
        String userEmail = "unknown";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        String meetingId = dbService.generateMeetingId();
        Meeting meeting = new Meeting(meetingId, userEmail, meetingDate, meetingTime);

        dbService.createNewMeeting(meeting, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UserPage.this, "הפגישה נשמרה בשרת!", Toast.LENGTH_SHORT).show();
                // שליחת התראה למכשיר
                sendMeetingNotification(meetingDate, meetingTime);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserPage.this, "שגיאה ביצירת הפגישה: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void arrangeMeeting(View view) {
        showDateTimePicker();
    }

    public void goToPurchases(View view) {
        startActivity(new Intent(this, MyPurchasesActivity.class));
    }

    public void logootuser(View view) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void searchcars(View view) {
        startActivity(new Intent(this, SearchAllCars.class));
    }

    public void goToMyMeetings(View view) {
        startActivity(new Intent(this, MyMeetingsActivity.class));
    }
}