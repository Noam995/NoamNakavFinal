package com.example.noamnakavfinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        // הגדרת ה-Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // הגדרת ריפוד למניעת חפיפה עם שורות המערכת
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_about), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- לוגיקת הסתרת כפתורים למשתמש שלא מחובר ---
        Button btnNavigate = findViewById(R.id.btnNavigate);
        Button btnMainPage = findViewById(R.id.btnMainPage);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // אם המשתמש לא מחובר, נסתיר את הכפתורים מהמסך
            if (btnNavigate != null) btnNavigate.setVisibility(View.GONE);
            if (btnMainPage != null) btnMainPage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // טעינת התפריט
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // בדיקה: אם המשתמש לא מחובר, נסתיר את כל אפשרויות התפריט
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            MenuItem home = menu.findItem(R.id.nav_home);
            MenuItem cars = menu.findItem(R.id.nav_cars);
            MenuItem profile = menu.findItem(R.id.nav_profile);

            if (home != null) home.setVisible(false);
            if (cars != null) cars.setVisible(false);
            if (profile != null) profile.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // כפתור ה"חזור" (החץ ב-Toolbar) נשאר פעיל תמיד
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(this, UserPage.class));
            finish();
            return true;
        } else if (id == R.id.nav_cars) {
            startActivity(new Intent(this, SearchAllCars.class));
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UpdateProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void navigateToDealership(View view) {
        String address = "ראשון לציון";
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);

        // מנסים לפתוח את אפליקציית הניווט ישירות כדי לעקוף את חסימות הנראות של אנדרואיד 11+
        try {
            startActivity(mapIntent);
        } catch (android.content.ActivityNotFoundException e) {
            // במידה ובאמת אין שום אפליקציית ניווט על המכשיר
            Toast.makeText(this, "לא נמצאה אפליקציית ניווט", Toast.LENGTH_SHORT).show();
        }
    }

    public void main(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}