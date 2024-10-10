package com.ams.models;

public class TimeSlot {
    private String id;      // Firebase key (unique identifier)
    private int rowId;      // Row ID from the timetable
    private String day;     // Day of the week (e.g., Monday, Tuesday)
    private String startTime;
    private String endTime;

    public TimeSlot() {}

    public TimeSlot(int rowId, String startTime, String endTime) {
        this.rowId = rowId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
