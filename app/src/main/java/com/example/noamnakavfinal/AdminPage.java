package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות נדרשות של מערכת אנדרואיד ושל פיירבייס
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

// מחלקה המייצגת את דף הבית של מנהל המערכת (Admin), יורשת מ-AppCompatActivity
public class AdminPage extends AppCompatActivity {

    // --- הגדרת משתנים לרכיבי התצוגה (כפתורים) ---
    Button Addnewcar, logout, allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת עיצוב המאפשר לאפליקציה להימתח על פני כל המסך (מאחורי שורות המערכת)
        EdgeToEdge.enable(this);

        // חיבור קובץ ה-XML שמכיל את עיצוב דף המנהל למחלקה זו
        setContentView(R.layout.activity_admin_page);

        // --- מניעת חפיפה עם שורות המערכת (סטטוס וניווט) ---
        // מוסיף ריפוד (Padding) פנימי כדי שהתוכן של המסך לא יוסתר מתחת לאייקונים של הסוללה או כפתורי הניווט של הטלפון
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // קישור המשתנים בקוד אל הכפתורים הפיזיים שנמצאים בקובץ ה-XML לפי ה-ID שלהם
        Addnewcar = findViewById(R.id.btnAddcar);
        logout = findViewById(R.id.btnLogoutAdmin);
        allUsers = findViewById(R.id.btnUsers);
    }

    // --- פונקציות ניווט המופעלות בלחיצה על כפתורים (דרך מאפיין onClick ב-XML) ---

    // מעבר למסך הוספת רכב חדש למלאי
    public void gotoAddnewcar(View view) {
        // יצירת אובייקט Intent שאחראי על המעבר מהמסך הנוכחי (this) למסך AddNewCar
        Intent go = new Intent(this, AddNewCar.class);
        startActivity(go); // הפעלת המעבר
    }

    // פונקציית התנתקות מהמערכת
    public void logout(View view) {
        // 1. התנתקות בפועל משרתי Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // 2. הצגת הודעה קופצת קצרה (Toast) למשתמש שההתנתקות הצליחה
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

        // 3. חזרה למסך ההתחברות (Login)
        Intent intent = new Intent(this, Login.class);

        // הוספת דגלים (Flags) קריטיים: אומרים למערכת ההפעלה למחוק את כל היסטוריית המסכים של האפליקציה.
        // זה מבטיח שאחרי שהמנהל התנתק, הוא לא יוכל פשוט ללחוץ על "חזור" בטלפון ולהיכנס חזרה לדף המנהל.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent); // הפעלת המעבר למסך ההתחברות
    }

    // מעבר למסך הצגת כל המשתמשים הרשומים באפליקציה
    public void gotoallUsers(View view) {
        Intent go = new Intent(this, UserList.class);
        startActivity(go);
    }

    // מעבר למסך מחיקת רכבים מהמלאי
    public void DeleteCar(View view) {
        Intent go = new Intent(this, DeleteCar.class);
        startActivity(go);
    }

    // מעבר למסך היסטוריית המכירות (צפייה בעסקאות שבוצעו)
    public void salehistory(View view) {
        Intent go = new Intent(this, SalesHistoryActivity.class);
        startActivity(go);
    }

    // מעבר למסך הפגישות שנקבעו מול סוכנות הרכב
    public void gotoMeetings(View view) {
        Intent intent = new Intent(this, AdminMeetingsActivity.class);
        startActivity(intent);
    }
}