package com.example.noamnakavfinal;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView; // הוספנו ייבוא לרכיב החיפוש
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.adapter.MeetingAdapter;
import com.example.noamnakavfinal.model.Meeting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyMeetingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MeetingAdapter adapter;
    private ArrayList<Meeting> meetingsList;
    private ArrayList<Meeting> fullMeetingsList; // רשימה מלאה שתשמור את כל הפגישות המקוריות מהשרת
    private DatabaseReference meetingsRef;
    private String currentUserEmail;
    private SearchView searchView; // משתנה עבור רכיב החיפוש

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_meetings);

        // הגדרת סרגל הכלים (Toolbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("הפגישות שלי");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // הגדרת שוליים למסך
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_my_meetings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // הגדרת ה-SearchView והאזנה לשינויי טקסט
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMeetings(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMeetings(newText);
                return true;
            }
        });

        // הגדרת הרשימה
        recyclerView = findViewById(R.id.meetingsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        meetingsList = new ArrayList<>();
        fullMeetingsList = new ArrayList<>(); // אתחול רשימת המקור השמורה
        adapter = new MeetingAdapter(this, meetingsList);
        recyclerView.setAdapter(adapter);

        // שליפת האימייל של המשתמש המחובר
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        meetingsRef = FirebaseDatabase.getInstance().getReference("meetings");
        loadMeetings();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMeetings() {
        meetingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullMeetingsList.clear(); // מנקים את הרשימה המלאה השמורה בכל עדכון מהמסד
                Date currentDate = new Date();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Meeting meeting = dataSnapshot.getValue(Meeting.class);

                    // בדיקה שהפגישה והאימיילים לא ריקים
                    if (meeting != null && currentUserEmail != null && meeting.getUserEmail() != null) {

                        // ניקוי רווחים מיותרים והתעלמות מאותיות גדולות/קטנות
                        String emailFromDB = meeting.getUserEmail().trim();
                        String myEmail = currentUserEmail.trim();

                        if (myEmail.equalsIgnoreCase(emailFromDB)) {

                            Date meetingDate = parseDateTime(meeting.getDate(), meeting.getTime());

                            if (meetingDate != null && meetingDate.before(currentDate)) {
                                // מחיקת פגישה שעבר התאריך שלה מהמסד נתונים
                                dataSnapshot.getRef().removeValue();
                            } else {
                                // הוספה לרשימה המלאה השמורה
                                fullMeetingsList.add(meeting);
                            }
                        }
                    }
                }

                // הפעלת הסינון הנוכחי (או הצגת כל הרשימה במידה ושדה החיפוש ריק)
                filterMeetings(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyMeetingsActivity.this, "שגיאה בטעינת פגישות: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה חדשה לסינון הפגישות בזמן אמת לפי תאריך או שעה
    private void filterMeetings(String text) {
        meetingsList.clear();
        if (text == null || text.trim().isEmpty()) {
            meetingsList.addAll(fullMeetingsList);
        } else {
            String filterPattern = text.toLowerCase().trim();
            for (Meeting meeting : fullMeetingsList) {
                // סינון גמיש לפי תאריך או שעה
                if ((meeting.getDate() != null && meeting.getDate().toLowerCase().contains(filterPattern)) ||
                        (meeting.getTime() != null && meeting.getTime().toLowerCase().contains(filterPattern))) {
                    meetingsList.add(meeting);
                }
            }
        }
        adapter.notifyDataSetChanged();

        // הצגת הודעה במידה ואין פגישות בכלל (רק כשהחיפוש ריק)
        if (meetingsList.isEmpty() && (text == null || text.trim().isEmpty())) {
            Toast.makeText(MyMeetingsActivity.this, "אין לך פגישות עתידיות כרגע", Toast.LENGTH_SHORT).show();
        }
    }

    // פונקציית עזר לפענוח בטוח של התאריך עם תיקון באגים נפוצים
    private Date parseDateTime(String date, String time) {
        if (date == null || time == null || date.trim().isEmpty() || time.trim().isEmpty()) return null;

        String fixedDate = date;

        // מתקן מצב שבו המשתמש הכניס שנה בת 2 ספרות (למשל 26 במקום 2026)
        try {
            String[] dateParts = date.split("[/-]");
            if (dateParts.length == 3 && dateParts[2].length() == 2) {
                dateParts[2] = "20" + dateParts[2]; // הופך את 26 ל-2026
                fixedDate = dateParts[0] + "/" + dateParts[1] + "/" + dateParts[2];
            }
        } catch (Exception ignored) {}

        String dateTimeStr = fixedDate + " " + time;
        String[] formats = {"dd/MM/yyyy HH:mm", "d/M/yyyy HH:mm", "dd-MM-yyyy HH:mm"};

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