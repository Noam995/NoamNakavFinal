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

public class UpdateCar extends AppCompatActivity {

    EditText License, Model, Price, Km, DatetilTest, Enginevolume, Engine;
    Spinner spBrand, spColor, spYear, spHand, spOwnership, spGas;
    RadioGroup radioGrouper;
    RadioButton manual, autmatic;
    Button btnUpdate, btnGallery, btnTakePic;
    ImageView imageView;

    private DatabaseService databaseService;
    private ActivityResultLauncher<Intent> captureImageLauncher;
    int SELECT_PICTURE = 200;

    // נשמור את הרכב הנוכחי שאנחנו עורכים
    private Car currentCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_car);

        InitViews();
        databaseService = DatabaseService.getInstance();
        ImageUtil.requestPermission(this);

        // קבלת הרכב שנלחץ מהדף הקודם
        currentCar = (Car) getIntent().getSerializableExtra("car");

        if (currentCar != null) {
            populateFields(); // פונקציה שממלאת את כל הנתונים במסך
        }

        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });

        btnGallery.setOnClickListener(v -> imageChooser());
        btnTakePic.setOnClickListener(v -> captureImageLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)));

        btnUpdate.setOnClickListener(v -> saveUpdatedCar());
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

        btnUpdate = findViewById(R.id.btnUpdate);
        btnGallery = findViewById(R.id.btnimagebrowse);
        btnTakePic = findViewById(R.id.btncamara);
    }

    // פונקציה למילוי הנתונים הקיימים לתוך השדות
    private void populateFields() {
        License.setText(currentCar.getLicenseCar());
        Model.setText(currentCar.getModel());
        Price.setText(String.valueOf(currentCar.getPrice()));
        Km.setText(String.valueOf(currentCar.getKm()));
        DatetilTest.setText(currentCar.getTimeUntilTest());
        Enginevolume.setText(String.valueOf(currentCar.getEngineVolume()));
        Engine.setText(currentCar.getEngine());

        setSpinnerToValue(spBrand, currentCar.getBrand());
        setSpinnerToValue(spColor, currentCar.getColor());
        setSpinnerToValue(spYear, currentCar.getYear());
        setSpinnerToValue(spHand, String.valueOf(currentCar.getHand()));
        setSpinnerToValue(spOwnership, currentCar.getOwnership());
        setSpinnerToValue(spGas, currentCar.getGas());

        if ("Manual".equalsIgnoreCase(currentCar.getGearbox()) || "ידני".equals(currentCar.getGearbox())) {
            manual.setChecked(true);
        } else {
            autmatic.setChecked(true);
        }

        if (currentCar.getImage64() != null && !currentCar.getImage64().isEmpty()) {
            Bitmap bitmap = ImageUtil.convertFrom64base(currentCar.getImage64());
            imageView.setImageBitmap(bitmap);
        }
    }

    // פונקציית עזר למציאת המיקום הנכון ב-Spinner
    private void setSpinnerToValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveUpdatedCar() {
        try {
            String license = License.getText().toString();
            String brand = spBrand.getSelectedItem().toString();
            String model = Model.getText().toString();
            String color = spColor.getSelectedItem().toString();
            String year = spYear.getSelectedItem().toString();
            double price = Double.parseDouble(Price.getText().toString());
            double km = Double.parseDouble(Km.getText().toString());
            int hand = Integer.parseInt(spHand.getSelectedItem().toString());

            String gearType = manual.isChecked() ? "Manual" : "Automatic";

            String ownership = spOwnership.getSelectedItem().toString();
            String gas = spGas.getSelectedItem().toString();
            String dateTilTest = DatetilTest.getText().toString();
            double engineVolume = Double.parseDouble(Enginevolume.getText().toString());
            String engine = Engine.getText().toString();
            String imageBase64 = ImageUtil.convertTo64Base(imageView);

            // יוצרים אובייקט רכב מעודכן עם אותו ID בדיוק כמו המקורי!
            Car updatedCar = new Car(currentCar.getId(), license, brand, model, color, year, price, km, hand, gearType, ownership, gas, dateTilTest, engineVolume, engine, currentCar.isAvailable(), imageBase64);

            // קריאה לשירות לעדכון ב-Firebase
            databaseService.updateCar(updatedCar, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(UpdateCar.this, "הרכב עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // חזרה לדף הקודם לאחר העדכון
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UpdateCar.this, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "אנא וודא שכל השדות המספריים תקינים", Toast.LENGTH_SHORT).show();
        }
    }

    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                imageView.setImageURI(selectedImageUri);
            }
        }
    }
}