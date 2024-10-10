package com.ams.models;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AttendanceRecord  implements Serializable {
    private String attendanceId;
    private String studentRollNo;
    private String studentName;
    private String subjectName;
    private String date;
    private String time;
    private String teacherName;
    private String status; // "present" or "absent"
    private String day;
    private String roomName;
    private String courseName;
    private String semester;
    private String division;
    private String lectureType;

    private Map<String, String> lectureAttendanceMap = new HashMap<>();


    public AttendanceRecord() {
        // Default constructor required for Firebase
    }

    public AttendanceRecord(String attendanceId, String studentRollNo, String studentName, String subjectName, String date, String time, String teacherName, String status, String day,String roomName,String courseName,String semester,String division,String lectureType) {
        this.attendanceId = attendanceId;
        this.studentRollNo = studentRollNo;
        this.studentName = studentName;
        this.subjectName = subjectName;
        this.date = date;
        this.time = time;
        this.teacherName = teacherName;
        this.status = status;
        this.day = day;
        this.roomName = roomName;
        this.courseName = courseName;
        this.semester = semester;
        this.division = division;
        this.lectureType = lectureType;
    }

    // Getters and setters for all fields
    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }
    public String getStudentRollNo() { return studentRollNo; }
    public void setStudentRollNo(String studentRollNo) { this.studentRollNo = studentRollNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }


    // New: Method to add attendance status for a specific lecture
    public void addLectureAttendance(String lectureNumber, String status) {
        this.lectureAttendanceMap.put(lectureNumber, status);
    }

    // New: Getter for lecture-wise attendance map
    public Map<String, String> getLectureAttendanceMap() {
        return lectureAttendanceMap;
    }

    // New: Method to get the attendance status for a specific lecture number
    public String getAttendanceForLecture(String lectureNumber) {
        return lectureAttendanceMap.get(lectureNumber);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getLectureType() {
        return lectureType;
    }

    public void setLectureType(String lectureType) {
        this.lectureType = lectureType;
    }
}
