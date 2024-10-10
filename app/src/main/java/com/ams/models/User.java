package com.ams.models;

import java.io.Serializable;

public class User implements Serializable {
    private String fullName;
    private String courseName;
    private String rollNumber;
    private String division;
    private String mobileNumber;
    private String password; // Added password field
    private String userType; // Added userType field
    private String semester;
    private String faceImageUrl; // Added faceImageUrl field

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String fullName, String courseName, String rollNumber, String division, String mobileNumber, String password, String semester, String faceImageUrl) {
        this.fullName = fullName;
        this.courseName = courseName;
        this.rollNumber = rollNumber;
        this.division = division;
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.semester = semester;
        this.faceImageUrl = faceImageUrl; // Initialize faceImageUrl
    }

    // Getter and Setter for fullName
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Getter and Setter for courseName
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Getter and Setter for rollNumber
    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    // Getter and Setter for division
    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    // Getter and Setter for mobileNumber
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    // Getter and Setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and Setter for userType
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    // Getter and Setter for semester
    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    // Getter and Setter for faceImageUrl
    public String getFaceImageUrl() {
        return faceImageUrl;
    }

    public void setFaceImageUrl(String faceImageUrl) {
        this.faceImageUrl = faceImageUrl;
    }
}
