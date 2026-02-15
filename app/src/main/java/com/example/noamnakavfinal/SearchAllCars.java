package com.example.noamnakavfinal;

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

import com.example.noamnakavfinal.adapter.CarsAdapter;
import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.service.DatabaseService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchAllCars extends AppCompatActivity {

    RecyclerView rvCars;
    EditText etSearch;
    Spinner spSort;

    DatabaseService databaseService;
    CarsAdapter adapter;

    List<Car> allCars = new ArrayList<>();
    List<Car> currentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all_cars);

        // ======= Toolbar (הכרחי כדי שהתפריט יופיע) =======
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // אתחול רכיבים
        rvCars = findViewById(R.id.rvCars);
        etSearch = findViewById(R.id.etSearch);
        spSort = findViewById(R.id.spSort);

        // הגדרת RecyclerView
        rvCars.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarsAdapter(this, new ArrayList<>(), car -> {
            Intent intent = new Intent(this, CarDetailsActivity.class);
            intent.putExtra("car", (Serializable) car);
            startActivity(intent);
        });
        rvCars.setAdapter(adapter);

        // הגדרת הספינר (מיון)
        ArrayAdapter<CharSequence> sortAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.sort_options,
                        android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        // טעינת נתונים
        databaseService = DatabaseService.getInstance();
        loadCars();

        // האזנה לשינויים בתיבת החיפוש
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCars(s.toString());
            }
        });

        // האזנה לבחירה בספינר
        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortCars(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ==========================================
    //           טיפול בתפריט (Menu)
    // ==========================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // מעבר למסך הבית (UserPage)
            Intent intent = new Intent(this, UserPage.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.nav_cars) {
            // אנחנו כבר במסך רכבים
            Toast.makeText(this, "אתה כבר צופה ברכבים", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.nav_profile) {
            // מעבר לפרופיל
            Intent intent = new Intent(this, UserDetailsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_about) {
            // מעבר לאודות
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ==========================================
    //           לוגיקה (טעינה, סינון, מיון)
    // ==========================================

    private void loadCars() {
        databaseService.getCarList(new DatabaseService.DatabaseCallback<List<Car>>() {
            @Override
            public void onCompleted(List<Car> cars) {
                allCars = cars;
                currentList = new ArrayList<>(cars);
                // עדכון ראשוני של הרשימה
                adapter.updateList(currentList);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(SearchAllCars.this,
                        "שגיאה בטעינת רכבים",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציית עזר לניקוי רווחים ותווים מיוחדים לצורך חיפוש
    private String normalize(String text) {
        if (text == null) return "";
        return text.replace("י","")
                .replace("ו","")
                .replace("ה","")
                .replace("א","")
                .replace(" ","");
    }

    private void filterCars(String query) {
        String nq = normalize(query);
        currentList.clear();

        for (Car car : allCars) {
            // חיפוש לפי יצרן, דגם או שנה
            if (normalize(car.getBrand()).contains(nq) ||
                    normalize(car.getModel()).contains(nq) ||
                    car.getYear().contains(query)) {

                currentList.add(car);
            }
        }
        // מיון הרשימה המסוננת לפי הבחירה הנוכחית בספינר
        sortCars(spSort.getSelectedItemPosition());
    }

    private void sortCars(int option) {
        switch (option) {
            case 1: // מחיר: נמוך לגבוה
                Collections.sort(currentList,
                        Comparator.comparingDouble(Car::getPrice));
                break;
            case 2: // מחיר: גבוה לנמוך
                Collections.sort(currentList,
                        (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case 3: // ק"מ: נמוך לגבוה
                Collections.sort(currentList,
                        Comparator.comparingDouble(Car::getKm));
                break;
            case 4: // ק"מ: גבוה לנמוך
                Collections.sort(currentList,
                        (a, b) -> Double.compare(b.getKm(), a.getKm()));
                break;
        }
        // עדכון המתאם (Adapter) להצגת הרשימה החדשה
        adapter.updateList(currentList);
    }
}