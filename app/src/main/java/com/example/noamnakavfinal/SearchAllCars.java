package com.example.noamnakavfinal;

// ייבוא מחלקות נדרשות של אנדרואיד (תצוגה, רשימות, תפריטים, אירועי טקסט ועוד)
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// ייבוא מודלים ושירותים מהפרויקט
import com.example.noamnakavfinal.adapter.CarsAdapter;
import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.service.DatabaseService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// מחלקה זו אחראית על מסך הצגת כל הרכבים, כולל אפשרויות חיפוש טקסטואלי חכם ומיון (לפי מחיר/ק"מ)
public class SearchAllCars extends AppCompatActivity {

    // --- הגדרת רכיבי התצוגה ---
    RecyclerView rvCars; // רכיב הרשימה הנגללת המציג את כרטיסי הרכבים
    EditText etSearch; // שורת החיפוש החופשי (טקסט)
    Spinner spSort; // תפריט נגלל (Drop-down) לבחירת סוג המיון

    // --- משתני שירות ועזר ---
    DatabaseService databaseService; // גישה למסד הנתונים
    CarsAdapter adapter; // המתאם שמקשר בין נתוני הרכבים לתצוגה ברשימה

    // --- רשימות הנתונים ---
    List<Car> allCars = new ArrayList<>(); // שומרת את *כל* הרכבים שנטענו מהשרת (גיבוי קבוע)
    List<Car> currentList = new ArrayList<>(); // הרשימה המוצגת בפועל (משתנה בהתאם לחיפוש ולמיון)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all_cars);

        // ======= הגדרת ה-Toolbar (הפס העליון) =======
        // הכרחי כדי שהתפריט (שלוש הנקודות/האייקונים) יופיע למעלה
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- אתחול וקישור רכיבי התצוגה ל-XML ---
        rvCars = findViewById(R.id.rvCars);
        etSearch = findViewById(R.id.etSearch);
        spSort = findViewById(R.id.spSort);

        // --- הגדרת ה-RecyclerView (הרשימה) ---
        // הגדרת תצוגה אנכית (פריט מתחת לפריט)
        rvCars.setLayoutManager(new LinearLayoutManager(this));

        // יצירת המתאם. אנחנו מעבירים לו גם פעולה (Lambda) שתקרה בלחיצה על רכב מסוים:
        adapter = new CarsAdapter(this, new ArrayList<>(), car -> {
            // בלחיצה על רכב מתוך הרשימה - עוברים למסך פרטי הרכב (CarDetailsActivity)
            Intent intent = new Intent(this, CarDetailsActivity.class);
            // מעבירים את אובייקט הרכב שנבחר למסך הבא
            intent.putExtra("car", (Serializable) car);
            startActivity(intent);
        });
        rvCars.setAdapter(adapter); // חיבור המתאם לרשימה במסך

        // --- הגדרת הספינר (תפריט אפשרויות המיון) ---
        // יוצר מתאם טקסטואלי מתוך רשימת מחרוזות (string-array) שמוגדרת בקובץ strings.xml בשם sort_options
        ArrayAdapter<CharSequence> sortAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.sort_options, // רשימת האופציות (למשל: מחיר נמוך לגבוה, ק"מ וכו')
                        android.R.layout.simple_spinner_item);

        // הגדרת עיצוב התפריט כשהוא נפתח
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        // --- טעינת נתונים ממסד הנתונים ---
        databaseService = DatabaseService.getInstance();
        loadCars();

        // --- מאזין לתיבת החיפוש (פועל בכל פעם שהמשתמש מקליד או מוחק אות) ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            // מופעל בזמן אמת כשהטקסט משתנה
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // קורא לפונקציית הסינון ומעביר לה את הטקסט שהוקלד עד כה
                filterCars(s.toString());
            }
        });

        // --- מאזין לבחירה מתוך הספינר (המיון) ---
        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // כשנבחרת אפשרות חדשה, קוראים לפונקציית המיון עם המיקום (האינדקס) שנבחר
                sortCars(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ==========================================
    //           טיפול בתפריט העליון (Menu)
    // ==========================================

    // מנפח (טוען) את קובץ ה-XML של התפריט אל תוך ה-Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // מטפל בלחיצות על כפתורי התפריט העליון
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // איזה כפתור נלחץ?

        if (id == R.id.nav_home) {
            // מעבר למסך הבית האזור האישי
            Intent intent = new Intent(this, UserPage.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.nav_cars) {
            // הודעה למשתמש שהוא כבר נמצא במסך החיפוש
            Toast.makeText(this, "אתה כבר צופה ברכבים", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.nav_profile) {
            // מעבר למסך עריכת פרופיל משתמש
            Intent intent = new Intent(this, UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_about) {
            // מעבר למסך "אודות" העסק
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ==========================================
    //           לוגיקה (טעינה, סינון, מיון)
    // ==========================================

    // טעינת רשימת הרכבים מהרשת
    private void loadCars() {
        databaseService.getCarList(new DatabaseService.DatabaseCallback<List<Car>>() {
            @Override
            public void onCompleted(List<Car> cars) {
                // שומרים את הרשימה המקורית ללא שינויים
                allCars = cars;
                // יוצרים עותק נפרד עבור הרשימה שתוצג בפועל
                currentList = new ArrayList<>(cars);

                // עדכון הרשימה במתאם כדי שהרכבים יופיעו על המסך
                adapter.updateList(currentList);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(SearchAllCars.this, "שגיאה בטעינת רכבים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציית עזר (נרמול טקסט) המיועדת להקל על החיפוש.
    // היא מסירה רווחים ואותיות תהו"י כדי שחיפוש כמו "מאזדה" ימצא גם "מזדה" ולהפך.
    private String normalize(String text) {
        if (text == null) return "";
        return text.replace("י","")
                .replace("ו","")
                .replace("ה","")
                .replace("א","")
                .replace(" ","");
    }

    // סינון הרכבים לפי הטקסט שהוקלד בתיבת החיפוש
    private void filterCars(String query) {
        String nq = normalize(query); // נרמול מילת החיפוש
        currentList.clear(); // מרוקנים את הרשימה המוצגת לקראת מילויה מחדש

        // עוברים על כל הרכבים במלאי (שנטענו בהתחלה)
        for (Car car : allCars) {
            // בודקים האם מילת החיפוש מופיעה ביצרן, בדגם או בשנה
            if (normalize(car.getBrand()).contains(nq) ||
                    normalize(car.getModel()).contains(nq) ||
                    car.getYear().contains(query)) {

                // אם יש התאמה, מוסיפים את הרכב לרשימה שתוצג
                currentList.add(car);
            }
        }

        // לאחר הסינון, ממיינים מחדש את התוצאות בהתאם למה שנבחר בספינר
        sortCars(spSort.getSelectedItemPosition());
    }

    // מיון רשימת הרכבים המוצגת (currentList) לפי קריטריונים
    private void sortCars(int option) {
        // בודק איזה אינדקס (מיקום) נבחר בספינר
        switch (option) {
            case 1: // מחיר: נמוך לגבוה
                // משתמש בפונקציית המיון של Java כדי לסדר את הרשימה מהמחיר הנמוך לגבוה
                Collections.sort(currentList, Comparator.comparingDouble(Car::getPrice));
                break;
            case 2: // מחיר: גבוה לנמוך
                // כאן אנחנו משווים הפוך (b מול a) כדי לקבל סדר יורד (יקר לזול)
                Collections.sort(currentList, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case 3: // ק"מ: נמוך לגבוה
                Collections.sort(currentList, Comparator.comparingDouble(Car::getKm));
                break;
            case 4: // ק"מ: גבוה לנמוך
                Collections.sort(currentList, (a, b) -> Double.compare(b.getKm(), a.getKm()));
                break;
        }
        // מעדכנים את ה-Adapter עם הרשימה החדשה (המסוננת והממוינת) ומרעננים את המסך
        adapter.updateList(currentList);
    }
}