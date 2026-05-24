package com.example.noamnakavfinal;

// ייבוא מחלקות נדרשות של אנדרואיד, תצוגה, פיירבייס ומודלים
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // ייבוא מחלקת שדה טקסט רגיל
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

// מחלקה זו אחראית על מסך "עריכת פרופיל" של המשתמש.
// היא טוענת את הפרטים הנוכחיים מהשרת, מציגה אותם, ומאפשרת למשתמש לעדכן אותם.
public class UpdateProfileActivity extends AppCompatActivity {

    // --- הגדרת המשתנים של רכיבי התצוגה ---
    // שדות הטקסט (הקלט) שיוצגו למשתמש
    private EditText etName, etPhone, etEmail;
    // כפתור השמירה
    private Button btnSave;
    // עיגול טעינה (ספינר) שיוצג בזמן שהאפליקציה מתקשרת עם השרת
    private ProgressBar progressBar;

    // אובייקט השירות האחראי לתקשורת מול מסד הנתונים (Firebase)
    private DatabaseService databaseService;

    // משתנה שישמור את נתוני המשתמש כפי שנמשכו מהשרת
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // 1. אתחול רכיבי התצוגה וקישורם ל-XML
        initViews();

        // 2. קבלת המופע (Singleton) של שירות מסד הנתונים
        databaseService = DatabaseService.getInstance();

        // 3. שליפת המזהה הייחודי (UID) של המשתמש המחובר כרגע מתוך פיירבייס
        String uid = FirebaseAuth.getInstance().getUid();

        // בדיקה שאכן יש משתמש מחובר (שה-UID לא ריק)
        if (uid != null) {
            // קריאה לפונקציה שטוענת את הנתונים שלו ממסד הנתונים
            loadUserData(uid);
        }

        // 4. הגדרת מאזין ללחיצה על כפתור השמירה
        btnSave.setOnClickListener(v -> updateProfile());
    }

    // פונקציה המקשרת בין משתני ה-Java לרכיבים בקובץ העיצוב (XML)
    private void initViews() {
        // --- הגדרת ה-Toolbar (הפס העליון) ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // הצגת חץ "חזור" בפס העליון
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // הגדרת פעולת הלחיצה על חץ החזור (סוגר את המסך הנוכחי וחוזר לקודם)
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- קישור שאר הרכיבים ---
        etName = findViewById(R.id.etUpdateName);
        etPhone = findViewById(R.id.etUpdatePhone);
        etEmail = findViewById(R.id.etUpdateEmail);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.pbUpdate);
    }

    // פונקציה המושכת את נתוני המשתמש הנוכחי ממסד הנתונים (על סמך ה-ID שלו)
    private void loadUserData(String uid) {
        // מציג את מעגל הטעינה בזמן שאנחנו ממתינים לתשובה מהשרת
        progressBar.setVisibility(View.VISIBLE);

        // פנייה לשירות מסד הנתונים למשיכת פרטי המשתמש
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {

            // מופעל אוטומטית כשהנתונים חוזרים בהצלחה
            @Override
            public void onCompleted(User user) {
                // מעלים את מעגל הטעינה
                progressBar.setVisibility(View.GONE);

                // אם חזר אובייקט משתמש תקין
                if (user != null) {
                    // שומרים את האובייקט המקורי במשתנה הגלובלי
                    currentUser = user;

                    // מציבים את הנתונים בתוך שדות הטקסט על המסך כדי שהמשתמש יוכל לראות ולערוך
                    etName.setText(user.getFname());
                    etPhone.setText(user.getPhone());

                    // שים לב: בדרך כלל קשה יותר לשנות אימייל בפיירבייס (דורש אימות מחדש),
                    // לכן לרוב מציגים אותו אך חוסמים אותו לעריכה בקובץ ה-XML (למשל על ידי android:enabled="false").
                    etEmail.setText(user.getEmail());
                }
            }

            // מופעל אם התרחשה שגיאה במשיכת הנתונים (למשל בעיית רשת)
            @Override
            public void onFailed(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UpdateProfileActivity.this, "שגיאה בטעינת נתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה שנקראת בעת לחיצה על "שמור" ומעדכנת את הנתונים החדשים בענן
    private void updateProfile() {
        // שאיבת הטקסטים החדשים מהשדות (ללא רווחים מיותרים)
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        // ולידציה: נוודא שהמשתמש לא מחק את השם או הטלפון והשאיר אותם ריקים
        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return; // עוצר את התהליך ולא ממשיך לשמירה
        }

        // אם יש לנו את אובייקט המשתמש המקורי (שנטען בהתחלה)
        if (currentUser != null) {
            // מציג מעגל טעינה וחוסם את כפתור השמירה כדי למנוע לחיצות כפולות
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            // עדכון המידע *המקומי* על גבי האובייקט
            currentUser.setFname(newName);
            currentUser.setPhone(newPhone);
            // *הערה: הושמט בכוונה העדכון של currentUser.setEmail() בגלל מגבלות האבטחה של פיירבייס.
            // אם רוצים לשנות אימייל, צריך לבצע קריאה מיוחדת ל- Auth של פיירבייס ולא רק לדאטה-בייס.

            // שליחת האובייקט המעודכן לשמירה ב-Firebase
            databaseService.updateUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {

                // מופעל כאשר השמירה בענן הצליחה
                @Override
                public void onCompleted(Void object) {
                    progressBar.setVisibility(View.GONE); // העלמת הטעינה
                    btnSave.setEnabled(true); // שחרור הכפתור

                    Toast.makeText(UpdateProfileActivity.this, "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת מסך העריכה וחזרה למסך הבית (UserPage)
                }

                // מופעל כאשר השמירה בענן נכשלה
                @Override
                public void onFailed(Exception e) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true); // שחרור הכפתור כדי שהמשתמש יוכל לנסות שוב
                    Toast.makeText(UpdateProfileActivity.this, "עדכון נכשל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}