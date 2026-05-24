package com.example.noamnakavfinal.adapter;

// ייבוא הספריות הנדרשות מאנדרואיד (רכיבי תצוגה, רשימות) ומהפרויקט שלך (מודל הפגישה)
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.model.Meeting;

import java.util.List;

// מחלקת המתאם עבור הפגישות. היא יורשת מ-RecyclerView.Adapter ומשתמשת במחלקה פנימית בשם MeetingViewHolder
public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    // --- משתני המחלקה ---

    // הקשר (Context) - מייצג את המסך שבו הרשימה מוצגת. נדרש כדי "לנפח" (להמיר) קובץ XML לתצוגה חיה
    private Context context;

    // רשימת הנתונים - מכילה את כל אובייקטי הפגישות (Meeting) שיוצגו על המסך
    private List<Meeting> meetingList;

    // --- פעולה בונה (Constructor) ---
    // מופעלת כאשר אנחנו יוצרים מופע חדש של המתאם במסך (למשל ב-AdminMeetingsActivity)
    public MeetingAdapter(Context context, List<Meeting> meetingList) {
        this.context = context;
        this.meetingList = meetingList;
    }

    // --- יצירת תבנית התצוגה (ViewHolder) ---
    // פונקציה זו מופעלת בכל פעם שה-RecyclerView צריך ליצור "שורת פגישה" חדשה על המסך
    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "מנפחים" (טוענים) את קובץ ה-XML של שורה בודדת (row_meeting.xml) והופכים אותו לאובייקט View ב-Java
        View view = LayoutInflater.from(context).inflate(R.layout.row_meeting, parent, false);

        // מחזירים את ה-View החדש כשהוא עטוף בתוך MeetingViewHolder (שישמור את הרכיבים שלו)
        return new MeetingViewHolder(view);
    }

    // --- חיבור הנתונים לתצוגה ---
    // פונקציה זו מופעלת עבור כל שורה ברשימה, ותפקידה לשפוך את הנתונים הנכונים לתוך תיבות הטקסט
    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        // שולפים את הפגישה הספציפית מתוך הרשימה, לפי המיקום (position) הנוכחי שמצויר על המסך
        Meeting meeting = meetingList.get(position);

        // מציבים את הנתונים מהאובייקט אל תוך רכיבי התצוגה
        holder.tvEmail.setText(meeting.getUserEmail()); // הצגת אימייל הלקוח
        holder.tvDate.setText("תאריך: " + meeting.getDate()); // הצגת התאריך עם קידומת
        holder.tvTime.setText("שעה: " + meeting.getTime()); // הצגת השעה עם קידומת
    }

    // --- ספירת פריטים ---
    // פונקציה שמחזירה למערכת כמה פגישות יש בסך הכל ברשימה
    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    // --- מחלקה פנימית: MeetingViewHolder ---
    // תפקיד המחלקה הזו הוא לשמור "מטמון" (Cache) של רכיבי התצוגה שבתוך שורה אחת.
    // במקום שהאפליקציה תחפש את תיבות הטקסט (findViewById) שוב ושוב בכל פעם שגוללים למעלה ולמטה,
    // היא מוצאת אותם פעם אחת שומרת אותם כאן, מה שמשפר משמעותית את ביצועי האפליקציה.
    public static class MeetingViewHolder extends RecyclerView.ViewHolder {
        // המשתנים שייצגו את תיבות הטקסט בשורה הבודדת
        TextView tvEmail, tvDate, tvTime;

        // פעולה בונה שמקבלת שורה אחת שלמה (itemView) מהרשימה
        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור המשתנים אל ה-ID שלהם בקובץ row_meeting.xml
            tvEmail = itemView.findViewById(R.id.tvMeetingEmail);
            tvDate = itemView.findViewById(R.id.tvMeetingDate);
            tvTime = itemView.findViewById(R.id.tvMeetingTime);
        }
    }
}