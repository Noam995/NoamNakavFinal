package com.example.noamnakavfinal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // הגדרת Insets (מסך מלא)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // מציאת הרכיבים
        ImageView logo = findViewById(R.id.ivLogo);
        TextView appName = findViewById(R.id.tvAppName);
        TextView slogan = findViewById(R.id.tvSlogan);

        // טעינת האנימציה
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.animation);

        // הפעלת האנימציה על הרכיבים
        logo.startAnimation(anim);
        appName.startAnimation(anim);
        slogan.startAnimation(anim);

        // מעבר למסך הבא אחרי 3.5 שניות (קצת יותר זמן ליהנות מהאנימציה)
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3500);
    }
}
