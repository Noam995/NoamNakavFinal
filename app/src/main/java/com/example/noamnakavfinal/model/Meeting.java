package com.example.noamnakavfinal.model;

public class Meeting {
    private String meetingId;
    private String userEmail;
    private String date;
    private String time;

    // קונסטרקטור ריק (חובה עבור Firebase)
    public Meeting() {
    }

    public Meeting(String meetingId, String userEmail, String date, String time) {
        this.meetingId = meetingId;
        this.userEmail = userEmail;
        this.date = date;
        this.time = time;
    }

    // Getters and Setters
    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    @Override
    public String toString() {
        return "Meeting{" +
                "meetingId='" + meetingId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
