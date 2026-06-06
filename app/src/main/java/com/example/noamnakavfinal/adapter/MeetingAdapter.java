package com.example.noamnakavfinal.adapter;

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

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    private Context context;
    private List<Meeting> meetingList;

    public MeetingAdapter(Context context, List<Meeting> meetingList) {
        this.context = context;
        this.meetingList = meetingList;
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // כאן אנחנו טוענים את קובץ העיצוב של השורה שעשית
        View view = LayoutInflater.from(context).inflate(R.layout.row_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);

        if (meeting != null) {
            // הכנסת הנתונים לתוך ה-TextViews
            holder.tvUserEmail.setText(meeting.getUserEmail());
            holder.tvMeetingDate.setText(meeting.getDate());
            holder.tvMeetingTime.setText(meeting.getTime());
        }
    }

    @Override
    public int getItemCount() {
        // התיקון הקריטי! אם זה מחזיר 0, המסך יהיה לבן וריק לגמרי.
        if (meetingList == null) {
            return 0;
        }
        return meetingList.size();
    }

    public static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvMeetingDate, tvMeetingTime;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            // חיבור המזהים (IDs) בדיוק כמו שקראת להם בקובץ row_meeting.xml
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvMeetingDate = itemView.findViewById(R.id.tvMeetingDate);
            tvMeetingTime = itemView.findViewById(R.id.tvMeetingTime);
        }
    }
}