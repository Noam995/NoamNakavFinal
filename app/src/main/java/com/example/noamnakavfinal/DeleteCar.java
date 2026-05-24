package com.example.noamnakavfinal;

// ייבוא ספריות נדרשות של אנדרואיד (תצוגה, מעברים, התראות קופצות) ושל הפרויקט
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.service.DatabaseService;
import com.example.noamnakavfinal.util.ImageUtil;

import java.io.Serializable;
import java.util.List;

// מחלקה האחראית על הצגת כל הרכבים למנהל, ומאפשרת לערוך אותם (לחיצה קצרה) או למחוק אותם (לחיצה ארוכה)
public class DeleteCar extends AppCompatActivity {

    // מיכל (Container) אליו נוסיף את "כרטיסי" הרכבים באופן דינמי (כנראה נמצא בתוך ScrollView ב-XML)
    LinearLayout carsContainer;

    // משתנה לגישה לפעולות מול מסד הנתונים
    DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_car);

        // קישור המשתנה ל-LinearLayout שנמצא בקובץ ה-XML
        carsContainer = findViewById(R.id.carsContainer);

        // קבלת מופע יחיד (Singleton) של שירות מסד הנתונים
        databaseService = DatabaseService.getInstance();

        // קריאה לפונקציה שטוענת את הרכבים מהרשת ומציגה אותם על המסך
        loadCars();
    }

    // פונקציה האחראית למשוך את רשימת הרכבים ממסד הנתונים ולצייר אותם על המסך
    private void loadCars() {
        databaseService.getCarList(new DatabaseService.DatabaseCallback<List<Car>>() {

            // מופעל אוטומטית כשרשימת הרכבים חוזרת בהצלחה ממסד הנתונים
            @Override
            public void onCompleted(List<Car> cars) {
                // מנקים את המיכל מכל הפריטים הישנים לפני שמוסיפים חדשים (למנוע כפילויות ברענון)
                carsContainer.removeAllViews();

                // עוברים בלולאה על כל רכב שחזר ממסד הנתונים
                for (Car car : cars) {

                    // "מנפחים" (ממירים) את קובץ העיצוב הבודד של רכב (car_item.xml) לאובייקט View חי ב-Java
                    // הקובץ הזה מייצג כרטיס אחד של רכב
                    View card = getLayoutInflater().inflate(R.layout.car_item, carsContainer, false);

                    // שולפים את רכיבי התצוגה *מתוך* הכרטיס המנופח (ולא מכלל המסך)
                    ImageView img = card.findViewById(R.id.imgCar);
                    TextView title = card.findViewById(R.id.tvTitle);
                    TextView price = card.findViewById(R.id.tvPrice);
                    TextView year = card.findViewById(R.id.tvYear);

                    // מזינים את הנתונים מהאובייקט (Car) אל תוך רכיבי התצוגה שבכרטיס
                    title.setText(car.getBrand() + " " + car.getModel());
                    price.setText("₪ " + car.getPrice());
                    year.setText("שנה: " + car.getYear());

                    // טיפול בתמונה: אם קיימת מחרוזת תמונה ב-Base64, נמיר אותה ונציג
                    if (car.getImage64() != null && !car.getImage64().isEmpty()) {
                        Bitmap bitmap = ImageUtil.convertFrom64base(car.getImage64());
                        img.setImageBitmap(bitmap);
                    }

                    // --- הגדרת לחיצה רגילה (קצרה) על כרטיס הרכב ---
                    card.setOnClickListener(v -> {
                        // מעבר לדף העדכון (UpdateCar)
                        Intent intent = new Intent(DeleteCar.this, UpdateCar.class);
                        // העברת אובייקט הרכב כולו למסך הבא (דורש ש-Car יהיה Serializable)
                        intent.putExtra("car", (Serializable) car);
                        startActivity(intent);
                    });

                    // --- הגדרת לחיצה ארוכה על כרטיס הרכב ---
                    card.setOnLongClickListener(v -> {
                        // קריאה לפונקציה שמציגה הודעת אזהרה לפני מחיקה
                        showDeleteDialog(car);
                        return true; // מחזיר true כדי לאותת שהלחיצה הארוכה "טופלה" ולא תפעיל גם לחיצה קצרה
                    });

                    // בסוף, מוסיפים את הכרטיס המוכן (עם הנתונים והלחיצות) לתוך המיכל שעל המסך
                    carsContainer.addView(card);
                }
            }

            // מופעל במקרה של שגיאה במשיכת הנתונים
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DeleteCar.this, "שגיאה בטעינת רכבים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה היוצרת ומציגה דיאלוג (הודעה קופצת עם כפתורים) ששואל את המנהל אם הוא בטוח שהוא רוצה למחוק
    private void showDeleteDialog(Car car) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת רכב") // כותרת החלון
                .setMessage("האם אתה בטוח שברצונך למחוק את הרכב:\n" +
                        car.getBrand() + " " + car.getModel() + "?") // תוכן ההודעה

                // הגדרת כפתור אישור (חיובי) - בלחיצה עליו תופעל הפונקציה deleteCar
                .setPositiveButton("כן, מחק", (dialog, which) -> deleteCar(car))

                // הגדרת כפתור ביטול (שלילי) - בלחיצה עליו לא קורה כלום (null), והחלון פשוט ייסגר
                .setNegativeButton("ביטול", null)
                .show(); // הצגת הדיאלוג על המסך
    }

    // פונקציה המבצעת את המחיקה בפועל ממסד הנתונים
    private void deleteCar(Car car) {
        // מעבירים למסד הנתונים את מזהה הרכב (ID) למחיקה
        databaseService.deleteCar(car.getId(), new DatabaseService.DatabaseCallback<Void>() {

            // מופעל כשהמחיקה משרתי פיירבייס הסתיימה בהצלחה
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(DeleteCar.this, "הרכב נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                // לאחר המחיקה, טוענים את הרשימה מחדש כדי שהרכב המחוק ייעלם מהמסך
                loadCars();
            }

            // מופעל במקרה של שגיאה במחיקה (למשל חוסר הרשאות או בעיית רשת)
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DeleteCar.this, "שגיאה במחיקת הרכב", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- פונקציה מובנית של מחזור החיים של האפליקציה (Activity Lifecycle) ---
    // פונקציה זו מופעלת בכל פעם שהמסך חוזר להיות פעיל ומוצג למשתמש.
    // אם המנהל לחץ על רכב, עבר לדף UpdateCar, ערך אותו ולחץ "חזור",
    // הפונקציה onResume תופעל ותטען את הרשימה מחדש כדי להציג את הנתונים המעודכנים.
    @Override
    protected void onResume() {
        super.onResume();
        loadCars(); // רענון הרשימה עם החזרה למסך
    }
}