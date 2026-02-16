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

        // אתחול ה-RecyclerView
        rvSales = findViewById(R.id.rvSalesHistory);
        if (rvSales != null) {
            rvSales.setLayoutManager(new LinearLayoutManager(this));
        }

        db = DatabaseService.getInstance();
        loadHistory();
    }

    private void loadHistory() {
        db.getAllSales(new DatabaseService.DatabaseCallback<List<Sale>>() {
            @Override
            public void onCompleted(List<Sale> sales) {
                // חייבים לעדכן את ה-UI על ה-Main Thread
                runOnUiThread(() -> {
                    if (sales != null && !sales.isEmpty()) {
                        SalesAdapter adapter = new SalesAdapter(sales);
                        rvSales.setAdapter(adapter);
                    } else {
                        Toast.makeText(SalesHistoryActivity.this, "אין היסטוריית רכישות", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(SalesHistoryActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}