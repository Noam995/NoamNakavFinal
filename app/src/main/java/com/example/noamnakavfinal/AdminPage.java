package com.example.noamnakavfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class AdminPage extends AppCompatActivity {

    Button Addnewcar,logout,allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Addnewcar=findViewById(R.id.btnAddcar);
        logout=findViewById(R.id.btnLogoutAdmin);
        allUsers=findViewById(R.id.btnUsers);
    }

    public void gotoAddnewcar(View view) {
        Intent go = new Intent(this,AddNewCar.class);
        startActivity(go);
    }

    public void logout(View view) {



            // התנתקות מ-Firebase
            FirebaseAuth.getInstance().signOut();

            // הודעה קצרה למשתמש
            Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            // מעבר למסך הכניסה
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    public void gotoallUsers(View view) {
        Intent go = new Intent(this,UserList.class);
        startActivity(go);
    }

    ///public void gotoallUsers(View view) {
     ///Intent go = new Intent(this,AllUserActivity.class);
    ///  startActivity(go);
  /// }
}
