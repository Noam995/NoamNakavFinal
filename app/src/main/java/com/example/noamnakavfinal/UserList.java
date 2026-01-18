package com.example.noamnakavfinal;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.adapter.UserAdapter;
import com.example.noamnakavfinal.model.User;
import com.example.noamnakavfinal.service.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class UserList extends AppCompatActivity {

    RecyclerView rvUsers;
    UserAdapter adapter;
    ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, this);
        rvUsers.setAdapter(adapter);

        // טעינת המשתמשים מה־Firebase
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> object) {
                userList.clear();
                userList.addAll(object);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserList.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}