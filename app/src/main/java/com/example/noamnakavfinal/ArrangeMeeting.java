package com.example.noamnakavfinal;

// ייבוא מחלקות נדרשות של אנדרואיד, פיירבייס והמודלים של הפרויקט
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

// מחלקה האחראית על מסך "קביעת פגישה" שבו הלקוח יכול לתאם תאריך ושעה מול הסוכנות
public class ArrangeMeeting extends AppCompatActivity {

    // --- הגדרת המשתנים של רכיבי התצוגה והשירותים ---
    private EditText etDate, etTime; // שדות טקסט להזנת תאריך ושעה
    private Button btnSaveMeeting; // כפתור לשמירת הפגישה
    private DatabaseService databaseService; // שירות הגישה למסד הנתונים
    private String userEmail = "אורח"; // כתובת המייל של המשתמש. ברירת המחדל היא "אורח" למקרה שלא מחובר איש

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת עיצוב המסך מקצה לקצה (מותח את התצוגה מאחורי שורות המערכת)
        EdgeToEdge.enable(this);

        // חיבור קובץ ה-XML שמכיל את עיצוב מסך קביעת הפגישה למחלקה זו
        setContentView(R.layout.activity_arrange_meeting);

        // הוספת ריפוד (Padding) פנימי כדי שהתוכן המרכזי לא יוסתר תחת שורת הסוללה למעלה או כפתורי הניווט למטה
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // קבלת מופע (Instance) של שירות מסד הנתונים כדי שנוכל לשמור את הפגישה מאוחר יותר
        databaseService = DatabaseService.getInstance();

        // --- שליפת פרטי המשתמש המחובר ---
        // בדיקה מול פיירבייס כדי להביא את המשתמש הנוכחי
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // אם אכן יש משתמש מחובר ויש לו כתובת מייל מתועדת
        if (currentUser != null && currentUser.getEmail() != null) {
            // נשמור את המייל שלו לתוך המשתנה במקום המילה "אורח"
            userEmail = currentUser.getEmail();
        }

        // --- קישור המשתנים אל רכיבי התצוגה בקובץ ה-XML ---
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnSaveMeeting = findViewById(R.id.btnSaveMeeting);

        // --- לוגיקת לחיצה על כפתור קביעת הפגישה ---
        btnSaveMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // משיכת הטקסט שהמשתמש הזין (תוך ניקוי רווחים מיותרים בהתחלה ובסוף עם trim)
                String date = etDate.getText().toString().trim();
                String time = etTime.getText().toString().trim();

                // ולידציה (בדיקת תקינות): חובה להזין גם תאריך וגם שעה
                if (date.isEmpty() || time.isEmpty()) {
                    // אם אחד השדות ריק, מקפיצים הודעה ועוצרים את המשך הפעולה (return)
                    Toast.makeText(ArrangeMeeting.this, "אנא מלא תאריך ושעה", Toast.LENGTH_SHORT).show();
                    return;
                }

                // יצירת מזהה (ID) חדש וייחודי לפגישה דרך שירות הדאטה-בייס
                String meetingId = databaseService.generateMeetingId();

                // יצירת אובייקט פגישה (Meeting) חדש עם כל הנתונים שנאספו (מזהה, מייל המזמין, תאריך ושעה)
                Meeting meeting = new Meeting(meetingId, userEmail, date, time);

                // קריאה לפונקציה השומרת את הפגישה החדשה במסד הנתונים בענן
                // אנו מעבירים Callback שיגיד לנו מה לעשות כשהשמירה תסתיים (או תיכשל)
                databaseService.createNewMeeting(meeting, new DatabaseService.DatabaseCallback<Void>() {

                    // הפונקציה שתופעל אם השמירה בענן עברה בהצלחה
                    @Override
                    public void onCompleted(Void object) {
                        // הודעה למשתמש שהפגישה נקבעה
                        Toast.makeText(ArrangeMeeting.this, "הפגישה נקבעה בהצלחה!", Toast.LENGTH_SHORT).show();
                        // סגירת המסך הנוכחי (כך שהמשתמש יחזור אוטומטית למסך ממנו הוא הגיע)
                        finish();
                    }

                    // הפונקציה שתופעל אם השמירה נכשלה (למשל בעיית אינטרנט)
                    @Override
                    public void onFailed(Exception e) {
                        // הודעת שגיאה למשתמש
                        Toast.makeText(ArrangeMeeting.this, "שגיאה בקביעת הפגישה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}