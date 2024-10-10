package com.ams.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.ams.R;
import com.ams.services.TeacherService;
import com.ams.services.UserService;
import com.google.android.material.button.MaterialButton;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialButton btnManageStudents;
    private MaterialButton btnManageTeachers;
    private MaterialButton btnManageSchedule;
    private MaterialButton btnManageRooms;
    private TextView tvStudentCount, tvTeacherCount; // Add TextViews to display counts

    private UserService userService;
    private TeacherService teacherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        btnManageStudents = findViewById(R.id.btnManageStudents);
        btnManageTeachers = findViewById(R.id.btnManageTeachers);
        btnManageSchedule = findViewById(R.id.btnManageTimeTableSchedule);
        btnManageRooms = findViewById(R.id.btnManageRooms);
        tvStudentCount = findViewById(R.id.tvTotalStudents); // Reference for student count TextView
        tvTeacherCount = findViewById(R.id.tvTotalTeachers); // Reference for teacher count TextView

        // Initialize Services
        userService = new UserService();
        teacherService = new TeacherService();

        // Set up the toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Fetch and display student and teacher counts
        fetchStudentCount();
        fetchTeacherCount();

        // Handle "Manage Students" button click
        btnManageStudents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageStudentsActivity.class);
            startActivity(intent);
        });

        // Handle "Manage Teachers" button click
        btnManageTeachers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageTeachersActivity.class);
            startActivity(intent);
        });

        // Handle "Manage Schedule" button click
        btnManageSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageTimetableActivity.class);
            startActivity(intent);
        });

        // Handle "Manage Rooms" button click
        btnManageRooms.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageRoomsActivity.class);
            startActivity(intent);
        });

        // Handle Logout Button
        ImageButton logoutButton = findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    // Fetch and display student count
    private void fetchStudentCount() {
        userService.fetchStudentCount(new UserService.CountCallback() {
            @Override
            public void onCountFetched(long count) {
                tvStudentCount.setText(""+count);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error fetching student count: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch and display teacher count
    private void fetchTeacherCount() {
        teacherService.fetchTeacherCount(new TeacherService.CountCallback() {
            @Override
            public void onCountFetched(long count) {
                tvTeacherCount.setText(""+count);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error fetching teacher count: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStoredUsername() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("username", "Admin");
    }

    // Method to logout user
    private void logoutUser() {
        String username = getStoredUsername();
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logout successful. Username: " + (username != null ? username : "Admin"), Toast.LENGTH_LONG).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
