package com.example.noamnakavfinal;

// ייבוא ספריות נדרשות של אנדרואיד, פיירבייס, מודלים ומתאמים מהפרויקט
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.adapter.SalesAdapter;
import com.example.noamnakavfinal.model.Sale;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

// מחלקה המציגה למשתמש הספציפי את היסטוריית הרכישות האישית שלו
public class MyPurchasesActivity extends AppCompatActivity {

    // רכיבי התצוגה
    private RecyclerView rvMyPurchases;
    private ProgressBar progressBar;

    // שירות מסד הנתונים
    private DatabaseService db;
    // שמירת ה-ID של המשתמש המחובר כרגע
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_purchases); // חיבור לקובץ העיצוב שניצור

        // --- הגדרת הפס העליון (Toolbar) ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // הצגת חץ חזור
            getSupportActionBar().setTitle("היסטוריית הרכישות שלי");
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // סגירת המסך בלחיצה על החץ

        // --- אתחול רכיבים ---
        rvMyPurchases = findViewById(R.id.rvMyPurchases);
        progressBar = findViewById(R.id.progressBar);

        // הגדרת תצוגה אנכית לרשימה
        rvMyPurchases.setLayoutManager(new LinearLayoutManager(this));

        // קבלת מופע מסד הנתונים ושליפת ה-ID של המשתמש המחובר
        db = DatabaseService.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        // בדיקה שאכן יש משתמש מחובר
        if (currentUid != null) {
            loadMyPurchases();
        } else {
            Toast.makeText(this, "עליך להתחבר כדי לראות את היסטוריית הרכישות", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // פונקציה הטוענת את העסקאות ומסננת רק את אלו של המשתמש
    private void loadMyPurchases() {
        progressBar.setVisibility(View.VISIBLE); // הצגת עיגול טעינה

        // נמשוך את כל העסקאות מהדאטה-בייס (בדיוק כמו במסך המנהל)
        db.getAllSales(new DatabaseService.DatabaseCallback<List<Sale>>() {
            @Override
            public void onCompleted(List<Sale> allSales) {
                // חייבים לעדכן את התצוגה על ה-Main Thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // העלמת הטעינה

                    // רשימה חדשה שתכיל רק את העסקאות של המשתמש שלנו
                    List<Sale> mySales = new ArrayList<>();

                    if (allSales != null) {
                        // מעבר על כל העסקאות במערכת
                        for (Sale sale : allSales) {
                            // אם לעסקה יש משתמש, וה-ID שלו שווה ל-ID של המשתמש המחובר באפליקציה:
                            if (sale.getUser() != null && sale.getUser().getId().equals(currentUid)) {
                                mySales.add(sale); // נוסיף את העסקה לרשימה האישית שלו
                            }
                        }
                    }

                    // אם נמצאו רכישות למשתמש הזה
                    if (!mySales.isEmpty()) {
                        // נשתמש ב-SalesAdapter הקיים שלנו כדי להציג את הרשימה!
                        SalesAdapter adapter = new SalesAdapter(mySales);
                        rvMyPurchases.setAdapter(adapter);
                    } else {
                        // אם הרשימה ריקה
                        Toast.makeText(MyPurchasesActivity.this, "עדיין לא ביצעת רכישות", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyPurchasesActivity.this, "שגיאה בטעינת נתונים: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}