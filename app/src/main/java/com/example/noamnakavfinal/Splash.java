package com.example.noamnakavfinal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ביטול שורת המשימות למסך מלא נקי
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivLogo);
        TextView appName = findViewById(R.id.tvAppName);

        // טעינת האנימציה (וודא שהקובץ קיים ב res/anim/splash_anim.xml)
        try {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.animation);
            logo.startAnimation(anim);
            appName.startAnimation(anim);
        } catch (Exception e) {
            // אם האנימציה חסרה, האפליקציה לא תקרוס
        }

        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, MainActivity.class));
            finish();
        }, 3000);
    }
}