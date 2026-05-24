package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות נדרשות (תצוגה, ניווט, פיירבייס ועוד)
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

// מחלקת מסך האודות, יורשת מ-AppCompatActivity כדי לתמוך בתכונות תצוגה
public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // מאפשר לתצוגת האפליקציה להימתח על פני כל המסך (כולל מתחת לשורות המערכת)
        EdgeToEdge.enable(this);

        // טעינת קובץ העיצוב של המסך
        setContentView(R.layout.activity_about);

        // --- הגדרת ה-Toolbar (הפס העליון) ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // הגדרת ה-Toolbar כ-Action Bar של המסך

        // הוספת כפתור "חזור" (חץ) ב-Toolbar במידה והוא אכן קיים
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // --- הגדרת ריפוד דינמי (Padding) ---
        // מונע חפיפה של תוכן האפליקציה עם שורת הסטטוס (למעלה) ושורת הניווט (למטה)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_about), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- לוגיקת הסתרת כפתורים למשתמש שלא מחובר (אורח) ---
        Button btnNavigate = findViewById(R.id.btnNavigate); // כפתור ניווט לסוכנות
        Button btnMainPage = findViewById(R.id.btnMainPage); // כפתור חזרה לעמוד הראשי

        // בדיקה מול פיירבייס: האם אין משתמש שמחובר כרגע?
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // אם המשתמש אורח, נסתיר את הכפתורים האלו מהמסך לחלוטין
            if (btnNavigate != null) btnNavigate.setVisibility(View.GONE);
            if (btnMainPage != null) btnMainPage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // טעינת תפריט האפשרויות העליון מקובץ ה-XML של התפריט
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // בדיקה: אם המשתמש לא מחובר (אורח), נסתיר את כל אפשרויות התפריט
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // איתור הפריטים בתפריט לפי ה-ID שלהם
            MenuItem home = menu.findItem(R.id.nav_home);
            MenuItem cars = menu.findItem(R.id.nav_cars);
            MenuItem profile = menu.findItem(R.id.nav_profile);

            // הסתרת הפריטים כדי שלמשתמש אורח לא תהיה גישה אליהם
            if (home != null) home.setVisible(false);
            if (cars != null) cars.setVisible(false);
            if (profile != null) profile.setVisible(false);
        }
        return true; // מחזיר true כדי שהתפריט יוצג
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // קבלת ה-ID של הפריט בתפריט שעליו המשתמש לחץ
        int id = item.getItemId();

        // בדיקה על איזה כפתור לחצו:

        // 1. כפתור ה"חזור" (החץ ב-Toolbar המובנה של אנדרואיד)
        if (id == android.R.id.home) {
            finish(); // סוגר את המסך הנוכחי וחוזר אחורה
            return true;
        }
        // 2. לחצן הבית בתפריט
        else if (id == R.id.nav_home) {
            startActivity(new Intent(this, UserPage.class)); // מעבר לדף אזור אישי
            finish(); // סוגר את דף האודות
            return true;
        }
        // 3. לחצן חיפוש רכבים בתפריט
        else if (id == R.id.nav_cars) {
            startActivity(new Intent(this, SearchAllCars.class));
            return true;
        }
        // 4. לחצן הפרופיל בתפריט
        else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UpdateProfileActivity.class));
            return true;
        }

        // במידה ולא נלחץ אף אחד מהכפתורים שמוגדרים מעלה
        return super.onOptionsItemSelected(item);
    }

    // --- פונקציה המופעלת בלחיצה על כפתור "נווט אלינו" (דרך מאפיין onClick ב-XML) ---
    public void navigateToDealership(View view) {
        String address = "ראשון לציון"; // כתובת סוכנות הרכב

        // הכנת הכתובת לפורמט שמערכת ההפעלה יודעת להעביר למפות
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address));

        // יצירת בקשה לפתיחת הכתובת באפליקציית ניווט (כמו Waze או Google Maps)
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);

        // בדיקה בטיחותית האם יש במכשיר אפליקציה שיכולה לבצע ניווט
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent); // פתיחת האפליקציה (למשל ווייז)
        } else {
            // אם אין אפליקציית ניווט, תוקפץ הודעה קצרה למשתמש
            Toast.makeText(this, "לא נמצאה אפליקציית ניווט", Toast.LENGTH_SHORT).show();
        }
    }

    // --- פונקציה המופעלת בלחיצה על כפתור מעבר לעמוד הראשי (דרך מאפיין onClick ב-XML) ---
    public void main(View view) {
        Intent intent = new Intent(this, MainActivity.class);

        // הגדרת דגל שמנקה את היסטוריית המסכים מעל מסך הבית.
        // זה מונע מצב שפותחים את MainActivity שוב ושוב והאפליקציה מתמלאת בכפילויות.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent); // ביצוע המעבר
    }
}