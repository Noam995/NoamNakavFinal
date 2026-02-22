package com.example.noamnakavfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.Meeting;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ArrangeMeeting extends AppCompatActivity {

    private EditText etDate, etTime;
    private Button btnSaveMeeting;
    private DatabaseService databaseService;
    private String userEmail = "אורח"; // ערך ברירת מחדל

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

        // שליפת כתובת המייל של המשתמש המחובר דרך FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail = currentUser.getEmail();
        }

        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnSaveMeeting = findViewById(R.id.btnSaveMeeting);

        btnSaveMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = etDate.getText().toString().trim();
                String time = etTime.getText().toString().trim();

                if (date.isEmpty() || time.isEmpty()) {
                    Toast.makeText(ArrangeMeeting.this, "אנא מלא תאריך ושעה", Toast.LENGTH_SHORT).show();
                    return;
                }

                // יצירת מזהה ייחודי לפגישה מהדאטה-בייס
                String meetingId = databaseService.generateMeetingId();

                // יצירת אובייקט הפגישה
                Meeting meeting = new Meeting(meetingId, userEmail, date, time);

                // שמירה בעזרת הפונקציה המותאמת שיצרנו ב-DatabaseService
                databaseService.createNewMeeting(meeting, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish(); // חזרה למסך הקודם
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(ArrangeMeeting.this, "שגיאה בקביעת הפגישה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}