package com.example.noamnakavfinal;

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

import java.util.List;

public class AddNewCar extends AppCompatActivity  {

    // --- הגדרת המשתנים של רכיבי התצוגה ---
    EditText License, Model, Price, Km, DatetilTest, Enginevolume, Engine;
    Spinner spBrand, spColor, spYear, spHand, spOwnership, spGas;
    RadioGroup radioGrouper;
    RadioButton manual, autmatic;
    Button btnAdd, btnGallery, btnTakePic;
    ImageView imageView;

    private DatabaseService databaseService;
    private ActivityResultLauncher<Intent> captureImageLauncher;
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_car);

        InitViews();
        ImageUtil.requestPermission(this);
        databaseService = DatabaseService.getInstance();

        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });

        btnGallery.setOnClickListener(v -> selectImageFromGallery());
        btnTakePic.setOnClickListener(v -> captureImageFromCamera());

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // משיכת הטקסט שהוזן (ושימוש ב-trim לניקוי רווחים מיותרים)
                String license = License.getText().toString().trim();
                String model = Model.getText().toString().trim();
                String stprice = Price.getText().toString().trim();
                String stkm = Km.getText().toString().trim();
                String dateTilTest = DatetilTest.getText().toString().trim();
                String stengineVolume = Enginevolume.getText().toString().trim();
                String engine = Engine.getText().toString().trim();

                String brand = spBrand.getSelectedItem() != null ? spBrand.getSelectedItem().toString() : "";
                String color = spColor.getSelectedItem() != null ? spColor.getSelectedItem().toString() : "";
                String year = spYear.getSelectedItem() != null ? spYear.getSelectedItem().toString() : "";
                String sthand = spHand.getSelectedItem() != null ? spHand.getSelectedItem().toString() : "";
                String ownership = spOwnership.getSelectedItem() != null ? spOwnership.getSelectedItem().toString() : "";
                String gas = spGas.getSelectedItem() != null ? spGas.getSelectedItem().toString() : "";

                int selectedId = radioGrouper.getCheckedRadioButtonId();

                // 1. בדיקת תקינות - מוודאים שאף שדה לא ריק לפני ההמרות למספרים
                if (license.isEmpty() || model.isEmpty() || stprice.isEmpty() ||
                        stkm.isEmpty() || dateTilTest.isEmpty() || stengineVolume.isEmpty() ||
                        engine.isEmpty() || brand.isEmpty() || color.isEmpty() || year.isEmpty() ||
                        sthand.isEmpty() || ownership.isEmpty() || gas.isEmpty() || selectedId == -1) {

                    Toast.makeText(AddNewCar.this, "אנא מלא את כל השדות ובחר סוג גיר", Toast.LENGTH_SHORT).show();
                    return; // עוצרים את הפעולה כאן אם חסרים נתונים
                }

                // 2. המרת נתוני טקסט למספרים (בטוח לעשות זאת כעת כי בדקנו שהם לא ריקים)
                double price = Double.parseDouble(stprice);
                double km = Double.parseDouble(stkm);
                double engineVolume = Double.parseDouble(stengineVolume);
                int hand = Integer.parseInt(sthand);

                String gearType = (selectedId == manual.getId()) ? "Manual" : "Automatic";
                String imageBase64 = ImageUtil.convertTo64Base(imageView);

                // 3. יצירת אובייקט הרכב
                String id = databaseService.generateCarId();
                Car newItem = new Car(id, license, brand, model, color, year, price, km, hand, gearType, ownership, gas, dateTilTest, engineVolume, engine, true, imageBase64);

                // 4. בדיקה מול מסד הנתונים האם הרכב כבר קיים (לפי מספר רישוי) ואז שמירה
                checkIfCarExistsAndSave(newItem);
            }
        });
    }

    // פונקציה חדשה שבודקת כפילויות לפני השמירה
    private void checkIfCarExistsAndSave(Car newCar) {
        // מניח שיש לך פונקציה ב-DatabaseService שמחזירה את כל הרכבים (למשל getCarList או getAllCars)
        databaseService.getCarList(new DatabaseService.DatabaseCallback<List<Car>>() {
            @Override
            public void onCompleted(List<Car> allCars) {
                boolean carExists = false;

                if (allCars != null) {
                    for (Car existingCar : allCars) {
                        // בודקים האם מספר הרישוי של הרכב החדש שווה למספר רישוי של רכב קיים
                        if (existingCar.getLicenseCar() != null && existingCar.getLicenseCar().equals(newCar.getLicenseCar())) {
                            carExists = true;
                            break;
                        }
                    }
                }

                if (carExists) {
                    // הרכב כבר קיים! מציגים הודעת שגיאה
                    Toast.makeText(AddNewCar.this, "שגיאה: רכב עם מספר רישוי זה כבר קיים במערכת!", Toast.LENGTH_LONG).show();
                } else {
                    // הרכב לא קיים - ממשיכים בשמירה
                    saveCarToDatabase(newCar);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddNewCar.this, "שגיאה בבדיקת הרכבים הקיימים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה השומרת את הרכב בפועל ב-Firebase
    private void saveCarToDatabase(Car newItem) {
        databaseService.createNewCar(newItem, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Log.d("TAG", "Car added successfully");
                Toast.makeText(AddNewCar.this, "הרכב נוסף בהצלחה!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(AddNewCar.this, AdminPage.class);
                startActivity(intent);
                finish(); // מומלץ לסגור את המסך הנוכחי כדי שהמשתמש לא יחזור אליו בלחיצה על "חזור"
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "Failed to add car", e);
                Toast.makeText(AddNewCar.this, "שגיאה בהוספת הרכב", Toast.LENGTH_SHORT).show();
            }
        });
    }

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

    private void selectImageFromGallery() {
        imageChooser();
    }

    private void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(takePictureIntent);
    }

    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }
}