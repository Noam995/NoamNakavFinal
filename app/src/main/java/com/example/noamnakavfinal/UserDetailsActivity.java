package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות של אנדרואיד (תצוגה, התראות קופצות) ושל הפרויקט
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

// מחלקה המנהלת את מסך "פרטי משתמש" - צפייה, עריכה ומחיקה של משתמש
public class UserDetailsActivity extends AppCompatActivity {

    // --- הגדרת המשתנים עבור רכיבי התצוגה ---
    EditText etFname, etLname, etEmail, etPhone; // שדות טקסט הניתנים לעריכה לפרטי המשתמש
    Button btnUpdate, btnDelete; // כפתורים לעדכון או מחיקה

    // משתנה שישמור את המזהה (ID) של המשתמש שאנחנו עובדים עליו עכשיו
    String userId;

    // מתעלם מאזהרה של אנדרואיד סטודיו לגבי זיהוי חסר של ID מקובץ ה-XML (למקרה שיש באג זמני בסביבת הפיתוח)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // חיבור קובץ ה-XML של העיצוב למסך הנוכחי
        setContentView(R.layout.activity_user_details);

        // --- קבלת הנתונים מהמסך הקודם ---
        // המסך הקודם (כנראה רשימת המשתמשים) העביר לנו את ה-ID של המשתמש עליו לחצו.
        userId = getIntent().getStringExtra("USER_ID");

        // בדיקת בטיחות: אם משום מה לא הועבר ID, אי אפשר להמשיך. נסגור את המסך מיד.
        if (userId == null) {
            finish();
            return; // עוצר את המשך הפעלת הפונקציה
        }

        // --- קישור המשתנים בקוד אל רכיבי התצוגה הפיזיים בקובץ ה-XML ---
        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // קריאה לפונקציה שטוענת את הנתונים של המשתמש ממסד הנתונים ומציגה אותם על המסך
        loadUser();

        // --- הגדרת מאזינים ללחיצות על הכפתורים ---

        // בלחיצה על כפתור עדכון - תופעל הפונקציה לעדכון משתמש
        btnUpdate.setOnClickListener(v -> updateUser());

        // בלחיצה על כפתור מחיקה - יקפוץ קודם חלון ששואל "האם אתה בטוח?" (confirmDelete)
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    // פונקציה המושכת את נתוני המשתמש ממסד הנתונים בענן לפי ה-ID שלו
    private void loadUser() {
        // פנייה לשירות מסד הנתונים (תבנית Singleton) והפעלת פונקציית שליפת משתמש
        DatabaseService.getInstance().getUser(userId, new DatabaseService.DatabaseCallback<User>() {

            // פונקציה שמופעלת אוטומטית ברגע שהנתונים חזרו מהשרת בהצלחה
            @Override
            public void onCompleted(User user) {
                // מציבים את הנתונים שחזרו (שם, אימייל, טלפון) לתוך שדות הטקסט שעל המסך
                etFname.setText(user.getFname());
                etLname.setText(user.getLname());
                etEmail.setText(user.getEmail());
                etPhone.setText(user.getPhone());
            }

            // פונקציה שמופעלת אם התרחשה שגיאה (למשל המשתמש לא קיים או שאין אינטרנט)
            @Override
            public void onFailed(Exception e) {
                // הצגת הודעת שגיאה קצרה
                Toast.makeText(UserDetailsActivity.this, "שגיאה בטעינת משתמש", Toast.LENGTH_SHORT).show();
                // סגירת המסך מכיוון שאין נתונים להציג
                finish();
            }
        });
    }

    // פונקציה שאוספת את הנתונים החדשים שהוקלדו במסך ושומרת אותם במסד הנתונים
    private void updateUser() {
        // יצירת אובייקט משתמש (User) חדש וריק
        User user = new User();

        // הגדרת ה-ID המקורי (קריטי! כדי שפיירבייס ידע איזה משתמש לעדכן ולא ייצור אחד חדש)
        user.setId(userId);

        // שאיבת הטקסטים (המעודכנים) שהמנהל הקליד בשדות התצוגה והזנתם לאובייקט
        user.setFname(etFname.getText().toString());
        user.setLname(etLname.getText().toString());
        user.setEmail(etEmail.getText().toString());
        user.setPhone(etPhone.getText().toString());

        // שליחת האובייקט המעודכן לשירות מסד הנתונים לשמירה בענן
        DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {

            // מופעל לאחר שהעדכון נשמר בהצלחה
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UserDetailsActivity.this, "המשתמש עודכן", Toast.LENGTH_SHORT).show();
                // סוגר את המסך וחוזר למסך הקודם
                finish();
            }

            // מופעל אם העדכון נכשל
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserDetailsActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה היוצרת דיאלוג (חלון קופץ) המוודא שהמנהל באמת רוצה למחוק את המשתמש
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת משתמש") // כותרת הדיאלוג
                .setMessage("אתה בטוח שברצונך למחוק את המשתמש?") // תוכן השאלה
                // הגדרת הכפתור החיובי ("מחק"). אם ילחצו עליו, תופעל הפונקציה deleteUser()
                .setPositiveButton("מחק", (d, w) -> deleteUser())
                // הגדרת הכפתור השלילי ("ביטול"). null אומר שלא תתבצע שום פעולה נוספת מלבד סגירת החלון.
                .setNegativeButton("ביטול", null)
                .show(); // הצגת החלון הקופץ על המסך
    }

    // פונקציה המבצעת את פעולת המחיקה בפועל מול מסד הנתונים בענן
    private void deleteUser() {
        // פנייה לשירות ובקשה למחוק את המשתמש לפי ה-ID שלו
        DatabaseService.getInstance().deleteUser(userId, new DatabaseService.DatabaseCallback<Void>() {

            // מופעל לאחר שהמחיקה בוצעה בהצלחה
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UserDetailsActivity.this, "המשתמש נמחק", Toast.LENGTH_SHORT).show();
                // סגירת מסך פרטי המשתמש וחזרה לרשימה (כיוון שהמשתמש כבר לא קיים)
                finish();
            }

            // מופעל אם הייתה שגיאה במחיקה
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserDetailsActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}