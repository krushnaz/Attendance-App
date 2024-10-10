package com.ams.models;

import java.util.HashMap;
import java.util.Map;

public class Timetable {
    private String courseName;
    private String division;
    private Map<String, Map<String, Subject>> days; // day -> subjectName -> Subject

    public Timetable() {
        // Default constructor required for calls to DataSnapshot.getValue(Timetable.class)
    }

    public Timetable(String courseName, String division) {
        this.courseName = courseName;
        this.division = division;
        this.days = new HashMap<>();
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public Map<String, Map<String, Subject>> getDays() {
        return days;
    }

    public void setDays(Map<String, Map<String, Subject>> days) {
        this.days = days;
    }

    public void addSubject(String day, String subjectName, Subject subject) {
        if (this.days == null) {
            this.days = new HashMap<>();
        }
        if (!this.days.containsKey(day)) {
            this.days.put(day, new HashMap<>());
        }
        this.days.get(day).put(subjectName, subject);
    }
}
