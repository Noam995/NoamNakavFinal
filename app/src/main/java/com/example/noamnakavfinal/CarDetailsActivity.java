package com.example.noamnakavfinal;

// ייבוא מחלקות נדרשות של אנדרואיד ושל הפרויקט (תצוגה, מעבר מסכים, מודל הרכב ועזרים לתמונות
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.util.ImageUtil;

// מחלקה המייצגת את המסך המציג את הפרטים המלאים של רכב ספציפי שנבחר
public class CarDetailsActivity extends AppCompatActivity {

    // --- הגדרת משתנים עבור רכיבי התצוגה שעל המסך ---
    ImageView imgCar; // רכיב להצגת תמונת הרכב
    TextView tvTitle, tvPrice, tvYear, tvKm, tvEngine, tvGear, tvGas, tvOwnership, tvDateTest; // תיבות טקסט להצגת המידע
    Button btnPurchase; // כפתור "רכוש עכשיו" או בקשת רכישה

    // משתנה שיחזיק את כל הנתונים של הרכב הספציפי שמוצג כעת
    Car car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // חיבור קובץ העיצוב (XML) של מסך פרטי הרכב אל המחלקה
        setContentView(R.layout.activity_car_details);

        // --- קישור המשתנים בקוד לרכיבי התצוגה הפיזיים דרך ה-ID שלהם ---
        imgCar = findViewById(R.id.imgCar);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvYear = findViewById(R.id.tvYear);
        tvKm = findViewById(R.id.tvKm);
        tvEngine = findViewById(R.id.tvEngine);
        tvGear = findViewById(R.id.tvGear);
        tvGas = findViewById(R.id.tvGas);
        tvOwnership = findViewById(R.id.tvOwnership);
        tvDateTest = findViewById(R.id.tvDateTest);
        btnPurchase = findViewById(R.id.btnPurchase);

        // --- קבלת הנתונים מהמסך הקודם ---
        // כאשר עברנו למסך הזה מהרשימה, "ארזנו" את אובייקט הרכב (Car) לתוך ה-Intent.
        // כאן אנחנו שולפים אותו החוצה (חובה שהמחלקה Car תממש את הממשק Serializable כדי שזה יעבוד).
        car = (Car) getIntent().getSerializableExtra("car");

        // בדיקה שאכן עבר רכב תקין (כדי למנוע קריסת האפליקציה במידה וקרתה שגיאה במעבר)
        if (car != null) {
            // הזרקת המידע מתוך אובייקט הרכב אל תיבות הטקסט שעל המסך
            tvTitle.setText(car.getBrand() + " " + car.getModel()); // חיבור שם המותג והדגם לכותרת אחת
            tvPrice.setText("₪ " + car.getPrice()); // הצגת המחיר עם סמל השקל
            tvYear.setText("שנה: " + car.getYear());
            tvKm.setText("קילומטראז': " + car.getKm());
            tvEngine.setText("נפח מנוע: " + car.getEngineVolume());
            tvGear.setText("סוג הילוכים: " + car.getGearbox());
            tvGas.setText("דלק: " + car.getGas());
            tvOwnership.setText("בעלות: " + car.getOwnership());
            tvDateTest.setText("תאריך טסט: " + car.getTimeUntilTest());

            // --- טיפול בתמונה של הרכב ---
            // בודק אם שמורה תמונה עבור הרכב הזה במסד הנתונים
            if (car.getImage64() != null) {
                // התמונה נשמרה בפורמט של מחרוזת טקסט ארוכה (Base64).
                // כאן אנחנו ממירים אותה חזרה לפורמט של תמונה אמיתית (Bitmap) דרך מחלקת העזר שלנו.
                Bitmap bitmap = ImageUtil.convertFrom64base(car.getImage64());

                // מציבים את התמונה המוכנה בתוך ה-ImageView שעל המסך
                imgCar.setImageBitmap(bitmap);
            }
        }

        // --- הגדרת פעולה ללחיצה על כפתור הרכישה ---
        btnPurchase.setOnClickListener(v -> {
            // יצירת Intent כדי לעבור למסך ביצוע הרכישה (PurchaseActivity)
            Intent intent = new Intent(CarDetailsActivity.this, PurchaseActivity.class);

            // אנחנו מעבירים גם למסך הרכישה את אובייקט הרכב הנוכחי, כדי ששם ידעו בדיוק על איזה רכב מדובר
            intent.putExtra("car", car);

            // תחילת המעבר למסך החדש
            startActivity(intent);
        });
    }
}