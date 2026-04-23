package com.example.noamnakavfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.adapter.SalesAdapter;
import com.example.noamnakavfinal.model.Sale;
import com.example.noamnakavfinal.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyPurchasesActivity extends AppCompatActivity {

    private RecyclerView rvMyPurchases;
    private TextView tvNoPurchases;

    private SalesAdapter adapter;
    private List<Sale> mySalesList;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_purchases);

        // אתחול רכיבי UI
        rvMyPurchases = findViewById(R.id.rvMyPurchases);
        tvNoPurchases = findViewById(R.id.tvNoPurchases);

        // הגדרת RecyclerView
        rvMyPurchases.setLayoutManager(new LinearLayoutManager(this));
        mySalesList = new ArrayList<>();

        // שימוש במתאם הקיים שלך לרכישות
        adapter = new SalesAdapter( mySalesList);
        rvMyPurchases.setAdapter(adapter);

        databaseService = DatabaseService.getInstance();

        // טעינת הנתונים
        loadMyPurchases();
    }

    private void loadMyPurchases() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "עליך להתחבר כדי לצפות ברכישות", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUid = currentUser.getUid();

        // קריאה לפונקציה שמביאה את כל הרכישות (השם עשוי להיות getAllSales או getSalesHistory בהתאם למה שכתבת ב-DatabaseService)
        databaseService.getAllSales(new DatabaseService.DatabaseCallback<List<Sale>>() {
            @Override
            public void onCompleted(List<Sale> allSales) {
                mySalesList.clear();

                // סינון הרכישות - רק מה ששייך למשתמש הנוכחי
                for (Sale sale : allSales) {
                    // מוודאים שיש יוזר מקושר ושה-ID שלו תואם למשתמש המחובר
                    if (sale.getUser() != null && currentUid.equals(sale.getUser().getId())) {
                        mySalesList.add(sale);
                    }
                }

                // בדיקה אם הרשימה ריקה
                if (mySalesList.isEmpty()) {
                    tvNoPurchases.setVisibility(View.VISIBLE);
                    rvMyPurchases.setVisibility(View.GONE);
                } else {
                    tvNoPurchases.setVisibility(View.GONE);
                    rvMyPurchases.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MyPurchasesActivity.this, "שגיאה בטעינת הרכישות: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}