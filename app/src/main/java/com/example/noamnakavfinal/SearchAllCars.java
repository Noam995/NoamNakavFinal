package com.example.noamnakavfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
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

        // ======= Toolbar =======
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvCars = findViewById(R.id.rvCars);
        etSearch = findViewById(R.id.etSearch);
        spSort = findViewById(R.id.spSort);

        rvCars.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarsAdapter(this, new ArrayList<>(), car -> {
            Intent intent = new Intent(this, CarDetailsActivity.class);
            intent.putExtra("car", (Serializable) car);
            startActivity(intent);
        });
        rvCars.setAdapter(adapter);

        ArrayAdapter<CharSequence> sortAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.sort_options,
                        android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        databaseService = DatabaseService.getInstance();
        loadCars();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCars(s.toString());
            }
        });

        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                sortCars(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ======= Menu (שלוש נקודות) =======
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }



    private void loadCars() {
        databaseService.getCarList(new DatabaseService.DatabaseCallback<List<Car>>() {
            @Override
            public void onCompleted(List<Car> cars) {
                allCars = cars;
                currentList = new ArrayList<>(cars);
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
            if (normalize(car.getBrand()).contains(nq) ||
                    normalize(car.getModel()).contains(nq) ||
                    car.getYear().contains(query)) {

                currentList.add(car);
            }
        }

        sortCars(spSort.getSelectedItemPosition());
    }

    private void sortCars(int option) {
        switch (option) {
            case 1:
                Collections.sort(currentList,
                        Comparator.comparingDouble(Car::getPrice));
                break;
            case 2:
                Collections.sort(currentList,
                        (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case 3:
                Collections.sort(currentList,
                        Comparator.comparingDouble(Car::getKm));
                break;
            case 4:
                Collections.sort(currentList,
                        (a, b) -> Double.compare(b.getKm(), a.getKm()));
                break;
        }

        adapter.updateList(currentList);
    }
}