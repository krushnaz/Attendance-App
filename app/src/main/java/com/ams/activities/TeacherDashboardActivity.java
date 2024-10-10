package com.ams.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ams.models.Teacher;
import com.ams.services.TeacherService;
import com.ams.services.TimetableService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.ams.R;

import java.util.Calendar;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private TextView todayClassesCount;
    private TimetableService timetableService;
    private String teacherFullName;
    private String courseName;
    private TeacherService teacherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize services
        timetableService = new TimetableService();  // Make sure this is done before any method that uses it
        teacherService = new TeacherService();

        // Initialize UI components
        MaterialButton btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        MaterialButton btnManageSchedule = findViewById(R.id.btnManageSchedule);
        MaterialButton btnViewReports = findViewById(R.id.btnViewReports);
        MaterialButton btnProfile = findViewById(R.id.btnProfileSettings);
        ImageButton logoutButton = findViewById(R.id.button_logout);
        welcomeMessage = findViewById(R.id.welcome_message);
        todayClassesCount = findViewById(R.id.tvTodaysClassesCount);

        // Set up button click listeners
        logoutButton.setOnClickListener(v -> logoutUser());
        btnMarkAttendance.setOnClickListener(v -> startActivity(new Intent(this, AttendanceStatusActivity.class)));
        btnManageSchedule.setOnClickListener(v -> startActivity(new Intent(this, ManageScheduleActivity.class)));
        btnViewReports.setOnClickListener(v -> startActivity(new Intent(this, ManageAttendanceReportActivityForTeacher.class)));
        btnProfile.setOnClickListener(v -> {
            String teacherUsername = getStoredUsername();
            Intent intent = new Intent(this, TeacherProfileActivity.class);
            intent.putExtra("username", teacherUsername);
            startActivity(intent);
        });

        // Fetch and display teacher details
        fetchAndDisplayTeacherDetails();
    }


    private void fetchAndDisplayTeacherDetails() {
        String teacherUsername = getStoredUsername();
        teacherService.fetchTeacherFullnameAndDepartmentByUsername(teacherUsername, new TeacherService.TeacherCallback() {
            @Override
            public void onSuccess(String fullName, String department) {
                courseName = department;
                teacherFullName = fullName;
                welcomeMessage.setText("Welcome, " + fullName);
                // Call to display today's class count now that we have the necessary data
                displayTodayClassCount();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TeacherDashboardActivity", "Error fetching teacher details", e);
                welcomeMessage.setText("Welcome, User");
                Toast.makeText(TeacherDashboardActivity.this, "Error fetching data, redirecting to login", Toast.LENGTH_LONG).show();
                redirectToLogin();
            }
        });
    }



    private String getStoredUsername() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("username", null);
    }


    // Method to logout user
    private void logoutUser() {
        // Fetch the stored roll number before clearing SharedPreferences
        String username = getStoredUsername();

        // Clear SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Show a toast message indicating logout success
        if (username != null) {
            Toast.makeText(this, "Logout successful. Username: " + username, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Logout successful.", Toast.LENGTH_LONG).show();
        }
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(TeacherDashboardActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void displayTodayClassCount() {
         // Replace this with actual full name
         // Replace with the actual course name
        String day = getCurrentDayOfWeek();  // You need a method to get the current day
        Log.d("fetchCountOfTodayClasses","teacherFullName : "+teacherFullName);
        Log.d("fetchCountOfTodayClasses","courseName : "+courseName);

        timetableService.fetchCountOfTodayClasses(teacherFullName, courseName, day, new TimetableService.ClassCountCallback() {
            @Override
            public void onSuccess(int classCount) {
                // Update the UI with the class count
                todayClassesCount.setText("" + classCount);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TeacherDashboardActivity", "Failed to fetch class count", e);
                Toast.makeText(TeacherDashboardActivity.this, "Failed to fetch today's class count", Toast.LENGTH_LONG).show();
            }
        });
    }


    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "";
        }
    }
}
