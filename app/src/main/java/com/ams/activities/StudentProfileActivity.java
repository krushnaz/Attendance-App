package com.ams.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ams.R;
import com.ams.dialogs.StudentProfileEditDialog;
import com.ams.models.User; // Make sure you have a User model class
import com.ams.services.UserService; // Import your UserService
import com.google.android.material.button.MaterialButton;

public class StudentProfileActivity extends AppCompatActivity {

    private TextView tvFullName, tvRollNumber, tvCourseName, tvMobileNumber, tvSemester, tvDivision;
    private MaterialButton btnEditProfile;
    private UserService userService; // Add UserService reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        // Initialize views
        tvFullName = findViewById(R.id.tvFullName);
        tvRollNumber = findViewById(R.id.tvRollNumber);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        tvSemester = findViewById(R.id.tvSemester);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        tvDivision = findViewById(R.id.tvDivision);

        // Initialize UserService
        userService = new UserService(); // Ensure you have a no-argument constructor

        // Retrieve roll number from shared preferences
        String rollNumber = getStoredRollNo();

        // Load student profile data
        loadStudentProfile(rollNumber);

        // Set click listener for Edit Profile button
        btnEditProfile.setOnClickListener(v -> {
            showEditStudentDialog(rollNumber);
        });
    }

    private void loadStudentProfile(String rollNumber) {
        userService.fetchUserByRollNo(rollNumber, new UserService.UserCallback() {
            @Override
            public void onSuccess(User student) {
                // Update UI with student details
                tvFullName.setText("Full Name: " + student.getFullName());
                tvRollNumber.setText("Roll Number: " + student.getRollNumber());
                tvDivision.setText("Division: " + student.getDivision());
                tvCourseName.setText("Course: " + student.getCourseName());
                tvMobileNumber.setText("Mobile Number: " + student.getMobileNumber());
                tvSemester.setText("Semester: " + student.getSemester());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("StudentProfileActivity", "Error fetching user: " + e.getMessage());
            }
        });
    }

    private void showEditStudentDialog(String rollNumber) {
        userService.fetchUserByRollNo(rollNumber, new UserService.UserCallback() {
            @Override
            public void onSuccess(User student) {
                StudentProfileEditDialog dialog = new StudentProfileEditDialog();
                Bundle args = new Bundle();
                args.putSerializable("student", student); // Pass the student object
                dialog.setArguments(args);
                dialog.setOnStudentUpdatedListener(() -> {
                    // Refresh the profile information after update
                    loadStudentProfile(rollNumber);
                });
                dialog.show(getSupportFragmentManager(), "StudentProfileEditDialog");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("StudentProfileActivity", "Error fetching user for dialog: " + e.getMessage());
            }
        });
    }

    private String getStoredRollNo() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("rollNo", null);
    }
}
