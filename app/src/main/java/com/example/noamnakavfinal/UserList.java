package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות של אנדרואיד (מערכת, התראות קופצות)
import android.os.Bundle;
import android.widget.Toast;

// ייבוא מחלקות התצוגה המודרניות של אנדרואיד
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// ייבוא מחלקות מתוך הפרויקט שלך (המתאם, מודל המשתמש ושירות מסד הנתונים)
import com.example.noamnakavfinal.adapter.UserAdapter;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

import java.util.ArrayList;
import java.util.List;

// מחלקה המייצגת את מסך "רשימת המשתמשים"
public class UserList extends AppCompatActivity {

    // --- הגדרת המשתנים ברמת המחלקה ---

    // רכיב הרשימה (RecyclerView) - רכיב יעיל מאוד להצגת רשימות ארוכות נגללות
    RecyclerView rvUsers;

    // המתאם (Adapter) - "המוח" של הרשימה. הוא לוקח את הנתונים ומייצר מהם כרטיסיות תצוגה על המסך
    UserAdapter adapter;

    // רשימה (מערך דינמי) שתחזיק בתוכה את כל אובייקטי המשתמשים שיימשכו ממסד הנתונים
    ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // חיבור קובץ העיצוב (XML) של מסך רשימת המשתמשים למחלקה זו
        setContentView(R.layout.activity_user_list);

        // --- אתחול ה-RecyclerView ---
        // קישור המשתנה בקוד לרכיב הפיזי בקובץ ה-XML
        rvUsers = findViewById(R.id.rvUsers);

        // הגדרת מנהל פריסה (LayoutManager) - אומר לרשימה לסדר את הפריטים בצורה אנכית (אחד מתחת לשני)
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // אתחול הרשימה הריקה בזיכרון
        userList = new ArrayList<>();

        // יצירת מופע חדש של המתאם. מעבירים לו את הרשימה (שכרגע ריקה) ואת המסך הנוכחי (this)
        adapter = new UserAdapter(userList, this);

        // חיבור המתאם ל-RecyclerView כדי שידע מאיפה לשאוב את הנתונים ואיך להציג אותם
        rvUsers.setAdapter(adapter);

        // --- טעינת הנתונים ממסד הנתונים ---
        // קריאה לפונקציה בשירות שלנו שהולכת לפיירבייס ומביאה את כל המשתמשים.
        // זו פעולה שלוקחת זמן (אסינכרונית), ולכן אנחנו מעבירים לה Callback (פונקציות שיקראו כשהתשובה תחזור).
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {

            // פונקציה זו תופעל אוטומטית ברגע שרשימת המשתמשים חזרה בהצלחה מהשרת
            @Override
            public void onCompleted(List<User> object) {
                // מנקים את הרשימה הקיימת כדי שלא יהיו כפילויות אם הפונקציה נקראת פעמיים
                userList.clear();

                // מוסיפים את כל המשתמשים שהגיעו מהשרת (object) לתוך הרשימה המקומית שלנו
                userList.addAll(object);

                // מודיעים למתאם שהנתונים השתנו!
                // פעולה זו גורמת ל-RecyclerView לצייר מחדש את המסך ולהציג את המשתמשים החדשים
                adapter.notifyDataSetChanged();

                // הקפצת הודעה קטנה (Toast) למסך המציגה למנהל כמה משתמשים נטענו בסך הכל
                Toast.makeText(UserList.this, "נטענו " + userList.size() + " משתמשים", Toast.LENGTH_SHORT).show();
            }

            // פונקציה זו תופעל אם התרחשה שגיאה (למשל אין אינטרנט או שגיאת הרשאות)
            @Override
            public void onFailed(Exception e) {
                // מציגים הודעת שגיאה על המסך הכוללת את סיבת הכישלון (e.getMessage())
                Toast.makeText(UserList.this, "שגיאה בטעינה: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}