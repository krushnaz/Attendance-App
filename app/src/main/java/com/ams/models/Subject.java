package com.ams.models;

import java.io.Serializable;

public class Subject implements Serializable {
    private String subjectName;
    private String subjectCode;
    private String teacherName;
    private String startTime;
    private String endTime;
    private String room;            // Added room field
    private String lectureType;
    private String day;
    private int rowId;// Added lectureType field
    private String active;
    private String division;

    public Subject() {
        // Default constructor required for calls to DataSnapshot.getValue(Subject.class)
    }

    public Subject(String subjectName, String subjectCode, String teacherName) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.teacherName = teacherName;
        // Default constructor required for calls to DataSnapshot.getValue(Subject.class)
    }

    public Subject(String subjectName, String subjectCode, String teacherName, String startTime, String endTime, String room, String lectureType,int rowId,String day,String active,String division) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.teacherName = teacherName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.lectureType = lectureType;
        this.rowId = rowId;
        this.day = day;
        this.active = active;
        this.division = division;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getLectureType() {
        return lectureType;
    }

    public void setLectureType(String lectureType) {
        this.lectureType = lectureType;
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

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
