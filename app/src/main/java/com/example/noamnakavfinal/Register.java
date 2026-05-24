package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות הנדרשות לפעולת המסך (שמירת נתונים, תצוגה, מעברי מסך וכו')
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

// מחלקת ההרשמה. יורשת מ-AppCompatActivity ומממשת את ממשק View.OnClickListener
// כדי לטפל בלחיצות על כפתורים באלגנטיות בפונקציה אחת (onClick).
public class Register extends AppCompatActivity implements View.OnClickListener {

    // תגית לשימוש בהדפסות למסוף (Logcat) לצורכי מעקב ותיקון שגיאות
    private static final String TAG = "RegisterActivity";

    // --- הגדרת המשתנים של רכיבי התצוגה ושירותים ---
    private EditText etEmail, etPassword, etFName, etLName, etPhone; // שדות טקסט להזנת פרטי המשתמש
    private Button btnRegister; // כפתור "הרשם"
    private TextView tvLogin; // טקסט לחיץ למעבר חזרה למסך ההתחברות (למשל: "כבר יש לך חשבון? התחבר")
    private DatabaseService databaseService; // שירות מסד הנתונים שמטפל ברישום מול פיירבייס

    // --- משתנים עבור SharedPreferences (זיכרון מקומי) ---
    public static final String MyPREFERENCES = "MyPrefs"; // שם קובץ ההעדפות המקומי
    SharedPreferences sharedPreferences;
    private String email, password; // משתנים לשמירת האימייל והסיסמה להרשמה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת תצוגה מודרנית מקצה לקצה
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // התיקון לשגיאת ה-R.id.main באדום - מוודא שה-ID באמת קיים בקובץ ה-XML לפני שמפעילים עליו פעולות.
        // זה מוסיף ריפוד פנימי למסך כדי שהשדות לא יוסתרו על ידי שורת הסטטוס או כפתורי הניווט.
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // קבלת המופע (Singleton) של שירות הגישה למסד הנתונים
        databaseService = DatabaseService.getInstance();

        // --- קישור המשתנים אל רכיבי התצוגה (Views) בקובץ ה-XML ---
        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        etFName = findViewById(R.id.etFname);
        etLName = findViewById(R.id.etLname);
        etPhone = findViewById(R.id.etphone);
        btnRegister = findViewById(R.id.btnSubmit);
        tvLogin = findViewById(R.id.tvLogin);

        // הגדרת המחלקה הנוכחית כמטפלת באירועי הלחיצה של הכפתור והטקסט
        btnRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        // אתחול מערכת ה-SharedPreferences (קובץ מקומי) במצב פרטי לאפליקציה
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    // פונקציה זו מופעלת אוטומטית ברגע שהמשתמש לוחץ על רכיב שהוגדר לו OnClickListener
    @Override
    public void onClick(View v) {
        // אם נלחץ כפתור ההרשמה
        if (v.getId() == btnRegister.getId()) {

            // איסוף הטקסט מכל השדות תוך שימוש ב-trim() שמנקה רווחים מיותרים בהתחלה ובסוף
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            String fName = etFName.getText().toString().trim();
            String lName = etLName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // קריאה לפונקציית הוולידציה לבדיקה שהקלט תקין.
            // אם היא מחזירה true, ממשיכים לתהליך הרישום בפועל.
            if (validateInput(fName, lName, phone, email, password)) {
                registerUser(fName, lName, phone, email, password);
            }
        }
        // אם נלחץ הטקסט "התחבר"
        else if (v.getId() == tvLogin.getId()) {
            // סגירת מסך ההרשמה הנוכחי וחזרה למסך הקודם (מסך ההתחברות)
            finish();
        }
    }

    // פונקציה שמבצעת בדיקות תקינות על כל הנתונים שהמשתמש הקליד
    private boolean validateInput(String fname, String lname, String phone, String email, String password) {
        // בודק אם השם הפרטי ריק
        if (fname.isEmpty()) {
            etFName.setError("נא להזין שם פרטי");
            return false;
        }
        // בודק אם שם המשפחה ריק
        if (lname.isEmpty()) {
            etLName.setError("נא להזין שם משפחה");
            return false;
        }
        // בודק שהטלפון מכיל לפחות 9 ספרות
        if (phone.length() < 9) {
            etPhone.setError("טלפון לא תקין");
            return false;
        }
        // משתמש במחלקה המובנית של אנדרואיד (Patterns) כדי לבדוק שכתובת המייל בפורמט חוקי (מכילה @, נקודה וכו')
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("אימייל לא תקין");
            return false;
        }
        // פיירבייס דורש סיסמה של לפחות 6 תווים, נוודא זאת מראש
        if (password.length() < 6) {
            etPassword.setError("סיסמה חייבת להיות לפחות 6 תווים");
            return false;
        }

        // אם כל הבדיקות עברו בהצלחה
        return true;
    }

    // פונקציה מתווכת שמרכזת את הנתונים לתוך אובייקט מסוג User
    private void registerUser(String fname, String lname, String phone, String email, String password) {
        // יוצר אובייקט משתמש חדש. ה-ID כרגע ריק (נקבל אותו מפיירבייס בהמשך). isAdmin מוגדר כ-false.
        User user = new User("", fname, lname, phone, email, password, false);
        // העברת האובייקט לפונקציה השומרת אותו במסד הנתונים
        createUserInDatabase(user);
    }

    // פונקציה ששולחת את בקשת הרישום ל-Firebase דרך מחלקת השירות שלנו
    private void createUserInDatabase(User user) {
        // קריאה לשירות ה-DB, כולל Callback שיופעל כשהתהליך יסתיים
        databaseService.createNewUser(user, new DatabaseService.DatabaseCallback<String>() {

            // אם הרישום הצליח - מקבלים בחזרה את מזהה המשתמש שפיירבייס ייצר (uid)
            @Override
            public void onCompleted(String uid) {
                // מעדכנים את אובייקט המשתמש עם המזהה האמיתי שלו
                user.setId(uid);

                // --- שמירה ב-SharedPreferences לחיבור אוטומטי בעתיד ---
                SharedPreferences.Editor editor = sharedPreferences.edit(); // פתיחת עורך להעדפות
                editor.putString("email", email);
                editor.putString("password", password);
                editor.apply(); // apply שומר את הנתונים ברקע בצורה אסינכרונית (יעיל יותר מ-commit)

                // --- מעבר ישיר למסך האזור האישי (UserPage) ---
                Intent mainIntent = new Intent(Register.this, UserPage.class);

                // הוספת דגלים למחיקת היסטוריית המסכים.
                // זה מונע מצב שמשתמש שנרשם ילחץ "חזור" ויחזור בטעות למסך ההרשמה.
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(mainIntent); // ביצוע המעבר
                finish(); // סגירת מסך ההרשמה כדי לפנות זיכרון
            }

            // במקרה והרישום נכשל (למשל המייל כבר תפוס במערכת)
            @Override
            public void onFailed(Exception e) {
                // מציג למשתמש הודעת שגיאה על המסך הכוללת את סיבת הכישלון (מגיעה מה-Exception)
                Toast.makeText(Register.this, "שגיאה ברישום: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
}