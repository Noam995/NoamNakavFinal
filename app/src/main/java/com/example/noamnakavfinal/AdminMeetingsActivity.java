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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // התיקון כאן: הוספתי את R.id.main_all_meetings במקום מה שהיה חסר
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_all_meetings), (v, insets) -> {
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
        loadMeetings();
    }

    private void loadMeetings() {
        databaseService.getMeetingList(new DatabaseService.DatabaseCallback<List<Meeting>>() {

            @Override
            public void onCompleted(List<Meeting> object) {
                meetingList.clear();

                if (object != null) {
                    Date currentDate = new Date();

                    for (Meeting meeting : object) {
                        Date meetingDate = parseDateTime(meeting.getDate(), meeting.getTime());

                        // אם התאריך פוענח בהצלחה והוא לפני הזמן הנוכחי - נמחק את הפגישה
                        if (meetingDate != null && meetingDate.before(currentDate)) {
                            databaseService.deleteMeeting(meeting.getMeetingId(), null);
                        } else {
                            // הפגישה עתידית או שיש שגיאת פורמט (נשמור אותה כדי לא לאבד נתונים סתם)
                            meetingList.add(meeting);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminMeetingsActivity.this, "שגיאה בטעינת פגישות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציית עזר לפענוח בטוח של התאריך והשעה בכמה פורמטים נפוצים
    private Date parseDateTime(String date, String time) {
        if (date == null || time == null || date.isEmpty() || time.isEmpty()) return null;
        String dateTimeStr = date + " " + time;
        // כיסוי של מספר פורמטים אפשריים
        String[] formats = {"dd/MM/yyyy HH:mm", "d/M/yyyy HH:mm", "dd/MM/yy HH:mm", "d/M/yy HH:mm", "dd-MM-yyyy HH:mm"};

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(dateTimeStr);
            } catch (ParseException ignored) {}
        }
        return null;
    }
}