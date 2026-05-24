package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות נדרשות (תצוגה, זיכרון מקומי, כפתורים, שירותי פיירבייס ועוד)
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noamnakavfinal.service.DatabaseService;

// מחלקת מסך ההתחברות. היא מיישמת (implements) את View.OnClickListener
// כדי לטפל בלחיצות על כפתורים דרך הפונקציה onClick שמוגדרת למטה.
public class Login extends AppCompatActivity implements View.OnClickListener {

    // תגית (TAG) המשמשת להדפסות בלוג (Logcat) לצורכי מעקב ודיבוג (Debugging)
    private static final String TAG = "LoginActivity";

    // --- הגדרת המשתנים של רכיבי התצוגה ---
    private EditText etEmail, etPassword; // שדות טקסט להזנת אימייל וסיסמה
    private Button btnLogin; // כפתור התחברות
    private TextView tvRegister; // טקסט לחיץ למעבר למסך הרשמה

    // משתנה לגישה לפעולות מסד הנתונים (התחברות משתמש דרך Firebase)
    private DatabaseService databaseService;

    // --- משתנים עבור SharedPreferences (זיכרון מקומי בטלפון) ---
    // בעזרת מנגנון זה אפשר לשמור נתונים על המכשיר כדי שלא יימחקו כשהאפליקציה נסגרת
    public static final String MyPREFERENCES="MyPrefs"; // שם הקובץ שבו יישמרו הנתונים
    SharedPreferences sharedPreferences;

    // משתנים לשמירת האימייל והסיסמה שנשלפו מהזיכרון
    private String password, email;

    @SuppressLint("MissingInflatedId") // מתעלם מאזהרות על ID חסר בקובץ ה-XML (למקרה שיש בעיית זיהוי זמנית של אנדרואיד סטודיו)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת עיצוב המסך מקצה לקצה
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // מניעת חפיפת התוכן עם שורות המערכת (ריפוד פנימי)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // קבלת מופע לשירות מסד הנתונים
        databaseService = DatabaseService.getInstance();

        // אתחול מערכת ה-SharedPreferences על מצב פרטי (רק האפליקציה הזו יכולה לגשת לנתונים)
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        // קישור המשתנים בקוד לרכיבי התצוגה בקובץ ה-XML
        etEmail = findViewById(R.id.emailInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginBtn);
        tvRegister = findViewById(R.id.registerText);

        // שליפת האימייל והסיסמה מהזיכרון המקומי (אם המשתמש התחבר בעבר).
        // הפרמטר השני ("") הוא ערך ברירת המחדל שיוחזר אם לא נמצא שום דבר בזיכרון.
        email = sharedPreferences.getString("email","");
        password = sharedPreferences.getString("password","");

        // הגדרת המחלקה הזו כמטפלת באירועי לחיצה עבור כפתור ההתחברות והטקסט של ההרשמה
        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

        // הכנסה אוטומטית של המייל והסיסמה שנשלפו מהזיכרון לתוך שדות הטקסט (כדי לחסוך למשתמש הקלדה מחדש)
        etEmail.setText(email);
        etPassword.setText(password);
    }

    // פונקציה זו מופעלת בכל פעם שהמשתמש לוחץ על רכיב שהוגדר לו setOnClickListener(this)
    @Override
    public void onClick(View v) {
        // שמירת ה-ID של הרכיב שנלחץ
        int id = v.getId();

        // אם נלחץ כפתור ה"התחבר"
        if (id == btnLogin.getId()) {
            // שאיבת הטקסט שמוזן כרגע בשדות
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();

            // פניה לפונקציית ולידציה שבודקת האם הקלט תקין.
            // *הערה חשובה לגבי הקוד שלך: שורת ה- return; שמופיעה בהערה גורמת לכך שהבדיקה מתבצעת אבל גם אם היא נכשלת (מחזירה false), התהליך ממשיך לפונקציית loginUser.
            // כדי שהבדיקה תעצור את ההתחברות, צריך להוריד את סימני ההערה // לפני ה-return;
            if (!checkInput(email, password)) {
                //       return;
            }

            // קריאה לפונקציית ההתחברות מול מסד הנתונים
            loginUser(email, password);

        }
        // אם נלחץ הטקסט של "הרשם עכשיו"
        else if (id == tvRegister.getId()) {
            // מעבר למסך ההרשמה (Register)
            Intent registerIntent = new Intent(Login.this, Register.class);
            startActivity(registerIntent);
        }
    }

    // פונקציה לבדיקת תקינות הקלט (אימייל וסיסמה)
    private boolean checkInput(String email, String password) {
        // בדיקה שהאימייל לא ריק ושהוא מכיל את התו '@'
        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("נא להכניס אימייל תקין"); // מקפיץ שגיאה אדומה על השדה
            etEmail.requestFocus(); // מקפיץ את הסמן חזרה לשדה הזה
            return false;
        }

        // בדיקה שהסיסמה לא ריקה ושהיא מכילה לפחות 6 תווים (דרישת מינימום של Firebase)
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("סיסמה חייבת להיות לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }

        return true; // אם הכל תקין
    }

    // מעבר למסך הראשי (MainActivity). כנראה מקושר למאפיין onClick ב-XML (למשל עבור "המשך כאורח")
    public void main(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // פונקציה שמבצעת את ההתחברות בפועל מול שירותי הרשת (Firebase)
    private void loginUser(String email, String password) {

        // קריאה לפונקציית ההתחברות דרך DatabaseService
        databaseService.loginUser(email, password, new DatabaseService.DatabaseCallback<String>() {

            // הפונקציה שתופעל אם ההתחברות עברה בהצלחה מול שרתי Firebase
            @Override
            public void onCompleted(String uid) {

                // --- שמירת פרטי ההתחברות בזיכרון המקומי ---
                // פותחים עורך (Editor) ל-SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // שומרים את המייל והסיסמה כדי שבפעם הבאה שהמשתמש יפתח את האפליקציה הם יופיעו אוטומטית
                editor.putString("email", email);
                editor.putString("password", password);

                editor.commit(); // ביצוע והחלת השמירה בפועל

                Log.d(TAG, "Login: User logged successfully"); // הדפסה ללוג שהכל עבד

                // --- בדיקת הרשאות (מנהל מול משתמש רגיל) ---
                // בדיקה האם האימייל והסיסמה שהוזנו שייכים למנהל האפליקציה (Admin)
                if (email.equals("noam123@gmail.com") && password.equals("1234567")) {

                    // מעבר לדף המנהל (AdminPage)
                    Intent go1 = new Intent(Login.this, AdminPage.class);
                    startActivity(go1);

                } else {
                    // אם זה משתמש רגיל, מעבר לאזור האישי שלו (UserPage)
                    Intent go2 = new Intent(Login.this, UserPage.class);
                    startActivity(go2);
                }
            }

            // הפונקציה שתופעל אם ההתחברות נכשלה (סיסמה שגויה, משתמש לא קיים וכו')
            @Override
            public void onFailed(Exception e) {
                // הדפסת השגיאה ללוג כדי שהמפתח יוכל להבין מה קרה
                Log.d(TAG, "Login: Error:   "+e.toString());
            }
        });
    }
}