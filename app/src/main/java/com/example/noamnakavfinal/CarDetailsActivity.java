package com.example.noamnakavfinal;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.util.ImageUtil;

public class CarDetailsActivity extends AppCompatActivity {

    ImageView imgCar;
    TextView tvTitle, tvPrice, tvYear, tvKm, tvEngine, tvGear, tvGas, tvOwnership, tvDateTest;
    Button btnPurchase;
    Car car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

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

        car = (Car) getIntent().getSerializableExtra("car");

        if (car != null) {
            tvTitle.setText(car.getBrand() + " " + car.getModel());
            tvPrice.setText("₪ " + car.getPrice());
            tvYear.setText("שנה: " + car.getYear());
            tvKm.setText("קילומטראז': " + car.getKm());
            tvEngine.setText("נפח מנוע: " + car.getEngineVolume());
            tvGear.setText("סוג הילוכים: " + car.getGearbox());
            tvGas.setText("דלק: " + car.getGas());
            tvOwnership.setText("בעלות: " + car.getOwnership());
            tvDateTest.setText("תאריך טסט: " + car.getTimeUntilTest());

            if (car.getImage64() != null) {
                Bitmap bitmap = ImageUtil.convertFrom64base(car.getImage64());
                imgCar.setImageBitmap(bitmap);
            }
        }

        btnPurchase.setOnClickListener(v -> {
            Intent intent = new Intent(CarDetailsActivity.this, PurchaseActivity.class);
            intent.putExtra("car", car);
            startActivity(intent);
        });
    }
}