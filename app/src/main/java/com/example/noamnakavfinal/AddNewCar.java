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

public class AddNewCar extends AppCompatActivity  {

    EditText License, Model, Price, Km, DatetilTest, Enginevolume, Engine;

    // Spinners
    Spinner spBrand, spColor, spYear, spHand, spOwnership, spGas;

    // RadioButtons + RadioGroup
    RadioGroup radioGrouper;
    RadioButton manual, autmatic;

    // Button
    Button btnAdd,btnGallery,btnTakePic;
    ImageView imageView;


    private DatabaseService databaseService;


    /// Activity result launcher for capturing image from camera
    private ActivityResultLauncher<Intent> captureImageLauncher;



    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_car);



        InitViews();

        /// request permission for the camera and storage
        ImageUtil.requestPermission(this);

        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();





        /// register the activity result launcher for capturing image from camera
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });







        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();


            }
        });

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageFromCamera();

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String license = License.getText().toString();
                String model = Model.getText().toString();
                String stprice = Price.getText().toString();
                String stkm = Km.getText().toString();
                String dateTilTest = DatetilTest.getText().toString();
                String stengineVolume = Enginevolume.getText().toString();
                String engine = Engine.getText().toString();

                // Spinners
                String brand = spBrand.getSelectedItem().toString();
                String color = spColor.getSelectedItem().toString();
                String year = spYear.getSelectedItem().toString();
                String sthand = spHand.getSelectedItem().toString();
                String ownership = spOwnership.getSelectedItem().toString();
                String gas = spGas.getSelectedItem().toString();


                double price = Double.parseDouble(stprice);
                double km = Double.parseDouble(stkm);
                double engineVolume = Double.parseDouble(stengineVolume);
                int hand = Integer.parseInt(sthand);


                // RadioGroup – Gear type
                int selectedId = radioGrouper.getCheckedRadioButtonId();
                String gearType = "";

                if (selectedId == manual.getId()) {
                    gearType = "Manual";
                } else if (selectedId == autmatic.getId()) {
                    gearType = "Automatic";
                }

                // עכשיו כל הנתונים מאורגנים וניתנים לשימוש

                String imageBase64 = ImageUtil.convertTo64Base(imageView);


                if (license.isEmpty() || model.isEmpty() || price > 0 ||
                        km >= 0 || dateTilTest.isEmpty() || engineVolume > 0
                        || engine.isEmpty() || brand.isEmpty()
                        || color.isEmpty() || year.isEmpty()
                        || hand >= 0 || ownership.isEmpty() || gas.isEmpty()
                        || (selectedId != -1)) {
                    Toast.makeText(AddNewCar.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddNewCar.this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                }

                /// generate a new id for the item
                String id = databaseService.generateCarId();


                Car newItem = new Car(id, license, brand, model, color, year, price, km, hand, gearType, ownership, gas, dateTilTest, engineVolume, engine, true,imageBase64);


                /// save the item to the database and get the result in the callback
                databaseService.createNewCar(newItem, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Log.d("TAG", "Item added successfully");
                        Toast.makeText(AddNewCar.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        /// clear the input fields after adding the item for the next item
                        Log.d("TAG", "Clearing input fields");

                        Intent intent = new Intent(AddNewCar.this, AdminPage.class);
                        startActivity(intent);


                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e("TAG", "Failed to add item", e);
                        Toast.makeText(AddNewCar.this, "Failed to add food", Toast.LENGTH_SHORT).show();
                    }
                });
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
        imageView=findViewById(R.id.imageView);

        btnAdd=findViewById(R.id.btnAdd);
        btnGallery=findViewById(R.id.btnimagebrowse);
        btnTakePic=findViewById(R.id.btncamara);
    }


    /// select image from gallery
    private void selectImageFromGallery() {
        //   Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //  selectImageLauncher.launch(intent);

        imageChooser();
    }

    /// capture image from camera
    private void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(takePictureIntent);
    }





    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }
}


