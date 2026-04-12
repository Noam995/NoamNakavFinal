package com.example.noamnakavfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class UserPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_page);

        // הגדרת ה-Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("אזור אישי");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_user_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // טעינת התפריט (main_menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // טיפול בלחיצות על התפריט העליון
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish(); // חזרה למסך הקודם
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UpdateProfileActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_cars) {
            startActivity(new Intent(this, SearchAllCars.class));
            return true;
        } else if (id == R.id.nav_home) {
            Toast.makeText(this, "אתה כבר באזור האישי שלך 😊", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_about) {
            startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // פונקציה למעבר לדף תיאום פגישה (ArrangeMeeting)
    public void arrangeMeeting(View view) {
        Intent intent = new Intent(this, ArrangeMeeting.class);
        startActivity(intent);
    }

    // פונקציה למעבר להיסטוריית רכישות
    public void goToPurchases(View view) {
        Intent intent = new Intent(this, PurchaseActivity.class);
        startActivity(intent);
    }

    public void logootuser(View view) {
        // התנתקות מ-Firebase
        FirebaseAuth.getInstance().signOut();

        // הודעה קצרה למשתמש
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

        // מעבר למסך הכניסה
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void searchcars(View view) {
        Intent go = new Intent(this, SearchAllCars.class);
        startActivity(go);
    }
}