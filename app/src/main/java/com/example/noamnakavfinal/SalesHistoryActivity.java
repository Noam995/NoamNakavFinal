package com.example.noamnakavfinal;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noamnakavfinal.adapter.SalesAdapter;
import com.example.noamnakavfinal.model.Sale; // מודל שמייצג עסקת מכירה
import com.example.noamnakavfinal.service.DatabaseService;
import java.util.List;

// מחלקה המציגה את היסטוריית המכירות (עבור מנהל או משתמש)
public class SalesHistoryActivity extends AppCompatActivity {

    // רכיב להצגת רשימת העסקאות בצורה יעילה
    private RecyclerView rvSales;
    // שירות לגישה למסד הנתונים
    private DatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        // 1. אתחול ה-RecyclerView לפי ה-ID בקובץ ה-XML
        rvSales = findViewById(R.id.rvSalesHistory);

        // הגדרת מנהל תצוגה (LayoutManager) שמסדר את הפריטים בצורה אנכית
        if (rvSales != null) {
            rvSales.setLayoutManager(new LinearLayoutManager(this));
        }

        // קבלת מופע השירות לגישה לנתוני המכירות
        db = DatabaseService.getInstance();

        // 2. קריאה לפונקציה שטוענת את היסטוריית המכירות ממסד הנתונים
        loadHistory();
    }

    private void loadHistory() {
        // משיכת כל המכירות מהדאטה-בייס (פעולה אסינכרונית)
        db.getAllSales(new DatabaseService.DatabaseCallback<List<Sale>>() {

            // מופעל כשהנתונים חוזרים בהצלחה
            @Override
            public void onCompleted(List<Sale> sales) {
                // חייבים לבצע עדכוני ממשק משתמש (UI) על גבי ה-Main Thread
                runOnUiThread(() -> {
                    // בדיקה האם הרשימה תקינה ואינה ריקה
                    if (sales != null && !sales.isEmpty()) {
                        // יצירת מתאם (Adapter) חדש עם נתוני המכירות וחיבורו ל-RecyclerView
                        SalesAdapter adapter = new SalesAdapter(sales);
                        rvSales.setAdapter(adapter);
                    } else {
                        // אם אין עסקאות, מודיעים למשתמש
                        Toast.makeText(SalesHistoryActivity.this, "אין היסטוריית רכישות", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // מופעל אם הייתה שגיאה במשיכת הנתונים
            @Override
            public void onFailed(Exception e) {
                // הצגת הודעת שגיאה למשתמש על המסך
                runOnUiThread(() ->
                        Toast.makeText(SalesHistoryActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}