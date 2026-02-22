package com.example.noamnakavfinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class About extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void navigateToDealership(View view) {
        // 1. הכתובת של סוכנות הרכבים (בחרתי ראשון לציון, אבל אפשר לרשום כתובת מדויקת כמו "רוטשילד 1, תל אביב")
        String address = "ראשון לציון";

        // 2. המרת הכתובת לפורמט שמפות מבינות (geo URI)
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address));

        // 3. יצירת בקשה (Intent) לפתיחת מפה
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);

        // 4. בדיקה שיש למשתמש אפליקציית ניווט על הטלפון (כדי שהאפליקציה לא תקרוס)
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent); // פותח גוגל מפות / ווייז
        } else {
            Toast.makeText(this, "לא נמצאה אפליקציית ניווט במכשיר", Toast.LENGTH_SHORT).show();
        }
    }

    public void main(View view) {
        Intent go=new Intent(this, MainActivity.class);
        startActivity(go);
    }
}