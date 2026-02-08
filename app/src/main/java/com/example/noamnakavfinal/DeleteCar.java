package com.example.noamnakavfinal;

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

public class DeleteCar extends AppCompatActivity {

    LinearLayout carsContainer;
    DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_car);

        // קישור ל-LinearLayout
        carsContainer = findViewById(R.id.carsContainer);
        databaseService = DatabaseService.getInstance();

        loadCars();
    }

    private void loadCars() {
        databaseService.getCarList(new DatabaseService.DatabaseCallback<List<Car>>() {
            @Override
            public void onCompleted(List<Car> cars) {
                carsContainer.removeAllViews();

                for (Car car : cars) {
                    View card = getLayoutInflater().inflate(R.layout.car_item, carsContainer, false);

                    ImageView img = card.findViewById(R.id.imgCar);
                    TextView title = card.findViewById(R.id.tvTitle);
                    TextView price = card.findViewById(R.id.tvPrice);
                    TextView year = card.findViewById(R.id.tvYear);

                    title.setText(car.getBrand() + " " + car.getModel());
                    price.setText("₪ " + car.getPrice());
                    year.setText("שנה: " + car.getYear());

                    if (car.getImage64() != null) {
                        Bitmap bitmap = ImageUtil.convertFrom64base(car.getImage64());
                        img.setImageBitmap(bitmap);
                    }

                    // לחיצה רגילה – מעבר לפרטי רכב
                    card.setOnClickListener(v -> {
                        Intent intent = new Intent(DeleteCar.this, CarDetailsActivity.class);
                        intent.putExtra("car", (Serializable) car);
                        startActivity(intent);
                    });

                    // לחיצה ארוכה – מחיקה
                    card.setOnLongClickListener(v -> {
                        showDeleteDialog(car);
                        return true;
                    });

                    carsContainer.addView(card);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DeleteCar.this,
                        "שגיאה בטעינת רכבים",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog(Car car) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת רכב")
                .setMessage("האם אתה בטוח שברצונך למחוק את הרכב:\n" +
                        car.getBrand() + " " + car.getModel() + "?")
                .setPositiveButton("כן, מחק", (dialog, which) -> deleteCar(car))
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void deleteCar(Car car) {
        databaseService.deleteCar(car.getId(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(DeleteCar.this,
                        "הרכב נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                loadCars(); // ריענון הרשימה
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DeleteCar.this,
                        "שגיאה במחיקת הרכב", Toast.LENGTH_SHORT).show();
            }
        });
    }
}