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
        View view = LayoutInflater.from(context).inflate(R.layout.row_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);
        holder.tvEmail.setText(meeting.getUserEmail());
        holder.tvDate.setText("תאריך: " + meeting.getDate());
        holder.tvTime.setText("שעה: " + meeting.getTime());
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvDate, tvTime;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvMeetingEmail);
            tvDate = itemView.findViewById(R.id.tvMeetingDate);
            tvTime = itemView.findViewById(R.id.tvMeetingTime);
        }
    }
}
