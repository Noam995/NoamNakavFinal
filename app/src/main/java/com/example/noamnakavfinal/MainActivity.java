package com.example.noamnakavfinal;

// ייבוא המחלקות הנדרשות של מערכת אנדרואיד
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// מחלקת המסך הראשי של האפליקציה. זהו בדרך כלל המסך הראשון שעולה לאחר מסך הפתיחה (Splash).
// מחלקה זו יורשת מ-AppCompatActivity כדי לקבל תמיכה בתכונות תצוגה מודרניות.
public class MainActivity extends AppCompatActivity {

    // הפונקציה שמופעלת ברגע שהמסך נוצר (חלק ממחזור החיים של Activity)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // מאפשר לתצוגה להימתח על פני כל המסך מקצה לקצה (כולל מתחת לשורת הסטטוס העליונה ושורת הניווט התחתונה)
        EdgeToEdge.enable(this);

        // מקשר את קובץ העיצוב (XML) שנקרא activity_main למחלקה הזו כדי שיציג את כפתורי התפריט הראשי
        setContentView(R.layout.activity_main);

        // מגדיר מאזין שמתאים את הריפוד (Padding) של המסך המרכזי כדי שלא יוסתר על ידי שורות המערכת
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // שולף את הגדלים המדויקים של שורת המצב (סוללה/שעון) ושורת הניווט
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // מחיל את הריפוד על ה-View המרכזי כדי למנוע חפיפה
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // --- פונקציות ניווט המופעלות בלחיצה על הכפתורים במסך הראשי (דרך מאפיין onClick ב-XML) ---

    // פונקציה למעבר למסך ההתחברות
    public void login(View view) {
        // יוצר "כוונת מעבר" (Intent) מהמסך הנוכחי (this) למסך Login
        Intent go = new Intent(this, Login.class);
        startActivity(go); // מוציא את המעבר לפועל
    }

    // פונקציה למעבר למסך ההרשמה למשתמשים חדשים
    public void register(View view) {
        Intent go = new Intent(this, Register.class);
        startActivity(go);
    }

    // פונקציה למעבר למסך "אודות" המכיל מידע על הסוכנות
    public void about(View view) {
        Intent go = new Intent(this, About.class);
        startActivity(go);
    }

    // פונקציה (שכנראה מיועדת לבדיקות או מוסתרת) למעבר ישיר לדף המנהל
    public void adminpage(View view) {
        Intent go = new Intent(this, AdminPage.class);
        startActivity(go);
    }

    // פונקציה (למעבר מהיר או לבדיקות) לדף האזור האישי של משתמש רגיל
    public void userpage(View view) {
        Intent go = new Intent(this, UserPage.class);
        startActivity(go);
    }
}