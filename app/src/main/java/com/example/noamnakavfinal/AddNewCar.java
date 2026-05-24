package com.example.noamnakavfinal;

// ייבוא של כל המחלקות הנדרשות מאנדרואיד ומפרויקט שלך
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

public class AddNewCar extends AppCompatActivity  {

    // --- הגדרת המשתנים של רכיבי התצוגה ---

    // שדות טקסט להזנת נתונים
    EditText License, Model, Price, Km, DatetilTest, Enginevolume, Engine;

    // תפריטים נגללים (Spinners) לבחירת אפשרויות
    Spinner spBrand, spColor, spYear, spHand, spOwnership, spGas;

    // כפתורי רדיו לבחירת סוג גיר (ידני/אוטומט)
    RadioGroup radioGrouper;
    RadioButton manual, autmatic;

    // כפתורי פעולה ותצוגת תמונה
    Button btnAdd, btnGallery, btnTakePic;
    ImageView imageView;

    // משתנה לגישה למסד הנתונים (שירות הדאטה-בייס שלך)
    private DatabaseService databaseService;

    // משתנה לטיפול בחזרה ממצלמת המכשיר (קבלת התמונה שצולמה)
    private ActivityResultLauncher<Intent> captureImageLauncher;

    // קבוע מספרי המשמש לזיהוי חזרה מבחירת תמונה מהגלריה
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_car);

        // קריאה לפונקציה שמקשרת בין המשתנים בקוד ל-ID שלהם בקובץ ה-XML
        InitViews();

        // בקשת הרשאות מהמשתמש לגישה למצלמה ולאחסון (כדי להעלות תמונה)
        ImageUtil.requestPermission(this);

        // קבלת מופע (Instance) של מחלקת שירות מסד הנתונים
        databaseService = DatabaseService.getInstance();

        // הגדרת המאזין שיטפל בתוצאה שחוזרת מהמצלמה
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // אם הצילום עבר בהצלחה ויש מידע
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // חילוץ התמונה (Bitmap) מהנתונים שחזרו והצגתה ב-ImageView
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });

        // --- הגדרת מאזינים ללחיצות על כפתורים ---

        // לחיצה על כפתור הגלריה: פותחת את בחירת התמונות מהמכשיר
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        // לחיצה על כפתור המצלמה: פותחת את אפליקציית המצלמה
        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageFromCamera();
            }
        });

        // לחיצה על כפתור ההוספה: איסוף הנתונים, ולידציה ושמירה במסד הנתונים
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // משיכת הטקסט שהוזן בשדות הטקסט
                String license = License.getText().toString();
                String model = Model.getText().toString();
                String stprice = Price.getText().toString();
                String stkm = Km.getText().toString();
                String dateTilTest = DatetilTest.getText().toString();
                String stengineVolume = Enginevolume.getText().toString();
                String engine = Engine.getText().toString();

                // משיכת הפריטים שנבחרו בתפריטים הנגללים (Spinners)
                String brand = spBrand.getSelectedItem().toString();
                String color = spColor.getSelectedItem().toString();
                String year = spYear.getSelectedItem().toString();
                String sthand = spHand.getSelectedItem().toString();
                String ownership = spOwnership.getSelectedItem().toString();
                String gas = spGas.getSelectedItem().toString();

                // המרת נתוני טקסט למספרים (כדי לשמור אותם כסוג הנתון הנכון ב-Car)
                double price = Double.parseDouble(stprice);
                double km = Double.parseDouble(stkm);
                double engineVolume = Double.parseDouble(stengineVolume);
                int hand = Integer.parseInt(sthand);

                // בדיקה איזה כפתור רדיו נבחר עבור סוג הגיר
                int selectedId = radioGrouper.getCheckedRadioButtonId();
                String gearType = "";

                if (selectedId == manual.getId()) {
                    gearType = "Manual";
                } else if (selectedId == autmatic.getId()) {
                    gearType = "Automatic";
                }

                // המרת התמונה שנמצאת ב-ImageView למחרוזת Base64 כדי שיהיה אפשר לשמור אותה במסד הנתונים
                String imageBase64 = ImageUtil.convertTo64Base(imageView);


                if (license.isEmpty() || model.isEmpty() || price >= 0 ||
                        km >= 0 || dateTilTest.isEmpty() || engineVolume > 0
                        || engine.isEmpty() || brand.isEmpty()
                        || color.isEmpty() || year.isEmpty()
                        || hand >= 0 || ownership.isEmpty() || gas.isEmpty()
                        || (selectedId != -1)) {
                    Toast.makeText(AddNewCar.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddNewCar.this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                }

                // ייצור מזהה (ID) חדש וייחודי לרכב דרך שירות מסד הנתונים
                String id = databaseService.generateCarId();

                // יצירת אובייקט Car חדש עם כל הנתונים שנאספו מהמשתמש
                Car newItem = new Car(id, license, brand, model, color, year, price, km, hand, gearType, ownership, gas, dateTilTest, engineVolume, engine, true, imageBase64);

                // קריאה לפונקציה השומרת את הרכב במסד הנתונים (Firebase כנראה)
                databaseService.createNewCar(newItem, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        // אם השמירה הצליחה: מדפיסים ללוג, מציגים הודעה, ומעבירים את המשתמש לדף מנהל
                        Log.d("TAG", "Item added successfully");
                        Toast.makeText(AddNewCar.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Clearing input fields");

                        Intent intent = new Intent(AddNewCar.this, AdminPage.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        // אם השמירה נכשלה: מדפיסים את השגיאה ללוג ומציגים הודעה למשתמש
                        Log.e("TAG", "Failed to add item", e);
                        Toast.makeText(AddNewCar.this, "Failed to add car", Toast.LENGTH_SHORT).show(); // שים לב: תיקנתי food ל-car בהערה
                    }
                });
            }
        });
    }

    // פונקציה שאחראית לקשר בין משתני ה-Java לבין רכיבי התצוגה (Views) מקובץ ה-XML
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

        btnAdd = findViewById(R.id.btnAdd);
        btnGallery = findViewById(R.id.btnimagebrowse);
        btnTakePic = findViewById(R.id.btncamara);
    }

    // פונקציה לבחירת תמונה מהגלריה שמפעילה את imageChooser()
    private void selectImageFromGallery() {
        imageChooser();
    }

    // פונקציה לפתיחת אפליקציית המצלמה המובנית של הטלפון וצילום תמונה
    private void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(takePictureIntent);
    }

    // יצירת בקשה למערכת ההפעלה לבחור קובץ מסוג תמונה מהאחסון
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*"); // מגדיר שאנחנו מחפשים רק תמונות
        i.setAction(Intent.ACTION_GET_CONTENT); // פעולת בחירת תוכן

        // פותח את חלון הבחירה וממתין לתוצאה עם הקוד SELECT_PICTURE
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // פונקציה שמופעלת אוטומטית כשהמשתמש חוזר מבחירת תמונה (או מפעולות אחרות שמחזירות תוצאה)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // בודק אם הפעולה הסתיימה בהצלחה
        if (resultCode == RESULT_OK) {

            // מוודא שהתוצאה שחזרה שייכת לבקשת פתיחת הגלריה (SELECT_PICTURE)
            if (requestCode == SELECT_PICTURE) {
                // מקבל את הקישור (Uri) של התמונה שנבחרה
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // מעדכן את התמונה בתצוגה שעל המסך
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }
}