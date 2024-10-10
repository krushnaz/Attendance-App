package com.ams.models;

import java.io.Serializable;

public class Course implements Serializable {

    private String courseId;
    private String courseName;
    private String semester;
    private String division;

    // Default constructor required for calls to DataSnapshot.getValue(Course.class)
    public Course() {
    }

    public Course(String courseId, String courseName, String semester, String division) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.semester = semester;
        this.division = division;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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
}
