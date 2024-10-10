package com.ams.models;

import java.io.Serializable;

public class Teacher  implements Serializable {
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String department;
    private String photoPath;

    // Default constructor required for calls to DataSnapshot.getValue(Teacher.class)
    public Teacher() {}

    public Teacher(String fullName, String username, String password, String email, String phoneNumber, String department) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.photoPath = photoPath;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
