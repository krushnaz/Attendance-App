package com.ams.utils;

public class TimeSlotManager {

    private static TimeSlotManager instance;
    private String startTime;
    private String endTime;

    private TimeSlotManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized TimeSlotManager getInstance() {
        if (instance == null) {
            instance = new TimeSlotManager();
        }
        return instance;
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
