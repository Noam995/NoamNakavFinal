package com.example.noamnakavfinal;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noamnakavfinal.adapter.SalesAdapter;
import com.example.noamnakavfinal.model.Sale;
import com.example.noamnakavfinal.service.DatabaseService;
import java.util.List;

public class SalesHistoryActivity extends AppCompatActivity {
    private RecyclerView rvSales;
    private DatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        rvSales = findViewById(R.id.rvSales);
        rvSales.setLayoutManager(new LinearLayoutManager(this));
        db = DatabaseService.getInstance();

        loadSalesHistory();
    }

    private void loadSalesHistory() {
        // קריאה לפונקציה שמוסיפים ל-DatabaseService (הסבר למטה)
        db.getAllSales(new DatabaseService.DatabaseCallback<List<Sale>>() {
            @Override
            public void onCompleted(List<Sale> sales) {
                if (sales != null) {
                    SalesAdapter adapter = new SalesAdapter(sales);
                    rvSales.setAdapter(adapter);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(SalesHistoryActivity.this, "שגיאה בטעינת היסטוריה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}