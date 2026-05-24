package com.example.noamnakavfinal;

// ייבוא מחלקות וספריות הנדרשות מאנדרואיד ומפרויקט האפליקציה שלך
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.service.DatabaseService;
import com.example.noamnakavfinal.util.ImageUtil;

// מחלקה המייצגת את מסך "עריכת רכב".
// מאפשרת למנהל לראות את פרטי הרכב הקיימים ולשמור שינויים בחזרה למסד הנתונים.
public class UpdateCar extends AppCompatActivity {

    // --- הגדרת המשתנים עבור כל רכיבי התצוגה הפיזיים שעל המסך ---
    EditText License, Model, Price, Km, DatetilTest, Enginevolume, Engine; // שדות טקסט
    Spinner spBrand, spColor, spYear, spHand, spOwnership, spGas; // רשימות נפתחות (תפריטים)
    RadioGroup radioGrouper; // קבוצת כפתורי הרדיו (מאפשרת לבחור רק אפשרות אחת בתוכה)
    RadioButton manual, autmatic; // כפתורי הבחירה לגיר ידני או אוטומטי
    Button btnUpdate, btnGallery, btnTakePic; // כפתורי שמירה ובחירת תמונות
    ImageView imageView; // תצוגת תמונת הרכב

    // שירות מסד הנתונים לבקשות עדכון מול פיירבייס
    private DatabaseService databaseService;

    // מפעיל מיוחד שאחראי לפתוח את המצלמה ולקבל חזרה את התמונה שצולמה
    private ActivityResultLauncher<Intent> captureImageLauncher;

    // קוד מזהה פנימי כדי לדעת שהמשתמש חוזר מהגלריה
    int SELECT_PICTURE = 200;

    // אובייקט שישמור את הרכב הספציפי שאותו המנהל בחר לערוך מתוך המסך הקודם
    private Car currentCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_car); // חיבור לעיצוב של מסך עריכת הרכב

        // 1. אתחול כל רכיבי התצוגה וחיבורם למשתנים בקוד
        InitViews();

        // 2. קבלת המופע המרכזי (Singleton) של שירות מסד הנתונים
        databaseService = DatabaseService.getInstance();

        // בקשת הרשאות שימוש במצלמה ובאחסון מהמשתמש
        ImageUtil.requestPermission(this);

        // --- שאיבת נתוני הרכב שהועברו מהמסך הקודם ---
        // כאשר המנהל לחץ על רכב במסך ניהול הרכבים, הרכב "נארז" והועבר לכאן דרך ה-Intent
        currentCar = (Car) getIntent().getSerializableExtra("car");

        // אם המעבר הצליח ויש לנו אובייקט רכב לעבוד איתו
        if (currentCar != null) {
            // נקרא לפונקציה ששופכת את כל הנתונים של הרכב לתוך השדות שעל המסך
            populateFields();
        }

        // --- הגדרת מאזין לחזרה מאפליקציית המצלמה ---
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // אם הצילום עבר בהצלחה (RESULT_OK) ויש מידע
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // חילוץ תמונת ה-Bitmap (תמונה חיה בזיכרון) מהמידע שחזר, והצבתה ב-ImageView
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });

        // --- הגדרת פעולות (לחיצות) לכפתורים ---

        // לחיצה על "בחר מהגלריה" תפעיל פונקציה לפתיחת אלבומי התמונות במכשיר
        btnGallery.setOnClickListener(v -> imageChooser());

        // לחיצה על "צלם תמונה" תפתח את אפליקציית המצלמה המובנית דרך המפעיל שהגדרנו קודם
        btnTakePic.setOnClickListener(v -> captureImageLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)));

        // לחיצה על "עדכן רכב" תתחיל את תהליך השמירה של השינויים
        btnUpdate.setOnClickListener(v -> saveUpdatedCar());
    }

    // פונקציה שמקשרת את כל המשתנים בקוד ל-ID המדויק שלהם בקובץ ה-XML
    private void InitViews() {
        License = findViewById(R.id.etLicense);
        Model = findViewById(R.id.etModel);
        Price = findViewById(R.id.etPrice);
        Km = findViewById(R.id.etKm);
        DatetilTest = findViewById(R.id.etDatetilTest);
        Enginevolume = findViewById(R.id.etEnginevolume);
        Engine = findViewById(R.id.etEngine);

        spBrand = findViewById(R.id.spBrand);
        spColor = findViewById(R.id.spColor);
        spYear = findViewById(R.id.spYear);
        spHand = findViewById(R.id.spHand);
        spOwnership = findViewById(R.id.spOwnership);
        spGas = findViewById(R.id.spGas);

        radioGrouper = findViewById(R.id.radiogroupgear);
        manual = findViewById(R.id.rBmanual);
        autmatic = findViewById(R.id.rBautmatic);
        imageView = findViewById(R.id.imageView);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnGallery = findViewById(R.id.btnimagebrowse);
        btnTakePic = findViewById(R.id.btncamara);
    }

    // פונקציה ששואבת את המידע מתוך אובייקט ה-Car ומציבה אותו על גבי המסך
    // כדי שהמנהל יראה מה הוא עורך ולא יתחיל מדף ריק.
    private void populateFields() {
        // מילוי שדות הטקסט הרגילים
        License.setText(currentCar.getLicenseCar());
        Model.setText(currentCar.getModel());

        // כדי לשים ערך מספרי (כמו מחיר או ק"מ) בתיבת טקסט, חייבים להמיר אותו למחרוזת (String)
        Price.setText(String.valueOf(currentCar.getPrice()));
        Km.setText(String.valueOf(currentCar.getKm()));
        DatetilTest.setText(currentCar.getTimeUntilTest());
        Enginevolume.setText(String.valueOf(currentCar.getEngineVolume()));
        Engine.setText(currentCar.getEngine());

        // שימוש בפונקציית עזר שתמצא את האינדקס הנכון ברשימה (Spinner) לפי הערך השמור ברכב
        setSpinnerToValue(spBrand, currentCar.getBrand());
        setSpinnerToValue(spColor, currentCar.getColor());
        setSpinnerToValue(spYear, currentCar.getYear());
        setSpinnerToValue(spHand, String.valueOf(currentCar.getHand()));
        setSpinnerToValue(spOwnership, currentCar.getOwnership());
        setSpinnerToValue(spGas, currentCar.getGas());

        // טיפול בכפתורי הרדיו (סוג גיר): בודק מה כתוב ברכב ומסמן את העיגול המתאים
        if ("Manual".equalsIgnoreCase(currentCar.getGearbox()) || "ידני".equals(currentCar.getGearbox())) {
            manual.setChecked(true); // מסמן "ידני"
        } else {
            autmatic.setChecked(true); // מסמן "אוטומט"
        }

        // טיפול בתמונה: אם קיימת מחרוזת תמונה בפורמט Base64, נמיר ונציג אותה
        if (currentCar.getImage64() != null && !currentCar.getImage64().isEmpty()) {
            Bitmap bitmap = ImageUtil.convertFrom64base(currentCar.getImage64());
            imageView.setImageBitmap(bitmap);
        }
    }

    // פונקציית עזר שנועדה לבחור את השורה הנכונה מתוך תפריט נגלל (Spinner)
    private void setSpinnerToValue(Spinner spinner, String value) {
        if (value == null) return;

        // עובר על כל הפריטים (השורות) שנמצאים כרגע בתפריט הנגלל
        for (int i = 0; i < spinner.getCount(); i++) {
            // אם הטקסט בשורה שווה לערך שאנחנו מחפשים (מתעלם מאותיות גדולות/קטנות באנגלית)
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i); // מסמן את השורה הזו כבחורה
                break; // עוצר את הלולאה כי מצאנו את מה שרצינו
            }
        }
    }

    // פונקציה שנקראת בלחיצה על "עדכן רכב". אוספת את כל המידע מהמסך ושומרת במסד הנתונים.
    private void saveUpdatedCar() {
        // שימוש ב-try-catch חשוב כאן, כי המרות (Parsing) של טקסט למספר יכולות לקרוס אם השדה נשאר ריק.
        try {
            // אוספים חזרה את כל הנתונים, אחרי שהמנהל אולי שינה אותם
            String license = License.getText().toString();
            String brand = spBrand.getSelectedItem().toString();
            String model = Model.getText().toString();
            String color = spColor.getSelectedItem().toString();
            String year = spYear.getSelectedItem().toString();

            // המרת שדות הטקסט למספרים מדויקים (double או int)
            double price = Double.parseDouble(Price.getText().toString());
            double km = Double.parseDouble(Km.getText().toString());
            int hand = Integer.parseInt(spHand.getSelectedItem().toString());

            // בחירת סוג הגיר בעזרת תנאי מקוצר: אם כפתור "ידני" לחוץ אז "Manual", אחרת "Automatic"
            String gearType = manual.isChecked() ? "Manual" : "Automatic";

            String ownership = spOwnership.getSelectedItem().toString();
            String gas = spGas.getSelectedItem().toString();
            String dateTilTest = DatetilTest.getText().toString();
            double engineVolume = Double.parseDouble(Enginevolume.getText().toString());
            String engine = Engine.getText().toString();

            // המרת התמונה שמוצגת כרגע בחזרה למחרוזת Base64
            String imageBase64 = ImageUtil.convertTo64Base(imageView);

            // --- הנקודה החשובה ביותר בעדכון ---
            // אנחנו יוצרים אובייקט Car *חדש*, אבל מעבירים אליו את ה-ID *הישן* של הרכב המקורי (currentCar.getId()).
            // כשפיירבייס רואה ששומרים אובייקט עם ID שכבר קיים - הוא מעדכן (דורס) את הישן במקום ליצור חדש.
            Car updatedCar = new Car(currentCar.getId(), license, brand, model, color, year, price, km, hand, gearType, ownership, gas, dateTilTest, engineVolume, engine, currentCar.isAvailable(), imageBase64);

            // שליחת בקשת העדכון למסד הנתונים בענן
            databaseService.updateCar(updatedCar, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(UpdateCar.this, "הרכב עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // סוגר את מסך העריכה וחוזר אוטומטית למסך הניהול הקודם
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UpdateCar.this, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            // אם המנהל השאיר שדה מספרי ריק (כמו מחיר) וניסינו להמיר אותו ל-double, התוכנה לא תקרוס אלא תקפיץ הודעה
            Toast.makeText(this, "אנא וודא שכל השדות המספריים תקינים", Toast.LENGTH_SHORT).show();
        }
    }

    // פונקציה לפתיחת אלבום התמונות של המכשיר
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*"); // רק קבצי תמונות
        i.setAction(Intent.ACTION_GET_CONTENT);
        // מפעיל את בחירת התמונות וממתין לתשובה עם הקוד SELECT_PICTURE
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // פונקציה מובנית שקולטת את התשובה מבחירת תמונה מהגלריה
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // בדיקה שהתשובה הגיעה מהגלריה ושהמשתמש באמת בחר תמונה
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            Uri selectedImageUri = data.getData(); // מקבל את הנתיב הפיזי של התמונה
            if (null != selectedImageUri) {
                // מציג את התמונה החדשה בחלונית על המסך
                imageView.setImageURI(selectedImageUri);
            }
        }
    }
}