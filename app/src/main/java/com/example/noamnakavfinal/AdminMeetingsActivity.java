package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות נדרשות של אנדרואיד ושל הפרויקט
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.adapter.MeetingAdapter;
import com.example.noamnakavfinal.model.Meeting;
import com.example.noamnakavfinal.service.DatabaseService;

import java.util.ArrayList;
import java.util.List;

// מחלקה המייצגת מסך שבו המנהל (Admin) יכול לראות את כל הפגישות שנקבעו
public class AdminMeetingsActivity extends AppCompatActivity {

    // --- הגדרת משתנים ברמת המחלקה ---

    // רכיב התצוגה שמציג רשימה נגללת ויעילה של פריטים (פגישות)
    private RecyclerView rvMeetings;

    // המתאם (Adapter) שאחראי לחבר בין הנתונים (רשימת הפגישות) לבין התצוגה (RecyclerView)
    private MeetingAdapter adapter;

    // רשימה שתשמור בתוכה את כל אובייקטי הפגישות שיימשכו ממסד הנתונים
    private List<Meeting> meetingList;

    // משתנה להתחברות וביצוע פעולות מול מסד הנתונים (למשל Firebase)
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת עיצוב מודרני בו האפליקציה נמתחת עד קצוות המסך (מתחת לשורות המערכת)
        EdgeToEdge.enable(this);

        // חיבור קובץ העיצוב (XML) למסך הנוכחי
        setContentView(R.layout.activity_all_meetings);

        // מניעת חפיפה בין התוכן של האפליקציה לבין שורות המערכת (שורת המצב למעלה ושורת הניווט למטה)
        // על ידי הוספת ריפוד (Padding) בגודל המדויק של השורות האלו
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- אתחול והגדרת הרשימה (RecyclerView) ---

        // קישור משתנה הרשימה לרכיב ה-XML
        rvMeetings = findViewById(R.id.rvMeetings);

        // הגדרת מנהל פריסה (LayoutManager) שאומר לרשימה להציג את הפריטים בצורה אנכית (אחד מתחת לשני)
        rvMeetings.setLayoutManager(new LinearLayoutManager(this));

        // אתחול הרשימה הריקה שתכיל את נתוני הפגישות
        meetingList = new ArrayList<>();

        // יצירת מופע חדש של המתאם, והעברת ההקשר (this - המסך הנוכחי) והרשימה הריקה כרגע
        adapter = new MeetingAdapter(this, meetingList);

        // חיבור המתאם ל-RecyclerView כדי שיידע מאיפה לקחת את הנתונים ואיך להציג אותם
        rvMeetings.setAdapter(adapter);

        // --- אתחול שירות מסד הנתונים ---
        // קבלת מופע של שירות הדאטה-בייס (תבנית Singleton כדי להשתמש באותו מופע בכל האפליקציה)
        databaseService = DatabaseService.getInstance();

        // קריאה לפונקציה שאחראית למשוך את הנתונים (הפגישות) ממסד הנתונים
        loadMeetings();
    }

    // פונקציה שאחראית לטעון את כל הפגישות ממסד הנתונים אל הרשימה שבמסך
    private void loadMeetings() {

        // פנייה לשירות מסד הנתונים כדי לקבל את רשימת הפגישות.
        // מכיוון שזו פעולה שלוקחת זמן (תקשורת אינטרנט), אנחנו משתמשים ב-Callback שיופעל כשהתשובה תחזור.
        databaseService.getMeetingList(new DatabaseService.DatabaseCallback<List<Meeting>>() {

            // פונקציה זו תופעל באופן אוטומטי אם משיכת הנתונים הצליחה
            @Override
            public void onCompleted(List<Meeting> object) {
                // מנקים את הרשימה הקיימת כדי למנוע כפילויות אם הפונקציה נקראת שוב
                meetingList.clear();

                // בודקים שהרשימה שחזרה ממסד הנתונים היא לא Null
                if (object != null) {
                    // מוסיפים את כל הפגישות שחזרו לתוך הרשימה שלנו
                    meetingList.addAll(object);
                }

                // מודיעים למתאם (Adapter) שהנתונים ברשימה השתנו.
                // פעולה זו תגרום ל-RecyclerView לרענן את המסך ולהציג את הפגישות החדשות.
                adapter.notifyDataSetChanged();
            }

            // פונקציה זו תופעל באופן אוטומטי במקרה של שגיאה (למשל אין אינטרנט או שגיאת הרשאות)
            @Override
            public void onFailed(Exception e) {
                // מקפיצים הודעת שגיאה קצרה (Toast) למשתמש
                Toast.makeText(AdminMeetingsActivity.this, "שגיאה בטעינת פגישות", Toast.LENGTH_SHORT).show();
            }
        });
    }
}