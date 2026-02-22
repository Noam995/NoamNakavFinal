package com.example.noamnakavfinal;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.adapter.MeetingAdapter;
import com.example.noamnakavfinal.model.Meeting;
import com.example.noamnakavfinal.service.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class AdminMeetingsActivity extends AppCompatActivity {

    private RecyclerView rvMeetings;
    private MeetingAdapter adapter;
    private List<Meeting> meetingList;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_meetings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvMeetings = findViewById(R.id.rvMeetings);
        rvMeetings.setLayoutManager(new LinearLayoutManager(this));

        meetingList = new ArrayList<>();
        adapter = new MeetingAdapter(this, meetingList);
        rvMeetings.setAdapter(adapter);

        databaseService = DatabaseService.getInstance();

        // קריאה לפונקציה שמביאה את הפגישות
        loadMeetings();
    }

    private void loadMeetings() {
        databaseService.getMeetingList(new DatabaseService.DatabaseCallback<List<Meeting>>() {
            @Override
            public void onCompleted(List<Meeting> object) {
                meetingList.clear();
                if (object != null) {
                    meetingList.addAll(object);
                }
                adapter.notifyDataSetChanged(); // עדכון הרשימה במסך
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminMeetingsActivity.this, "שגיאה בטעינת פגישות", Toast.LENGTH_SHORT).show();
            }
        });
    }
}