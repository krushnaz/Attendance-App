package com.ams.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ams.dialogs.StudentAttendanceStatusDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.ams.R;
import com.ams.services.UserService;
import com.ams.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class StudentDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView welcomeMessage;
    private UserService userService;
    private ImageButton logoutButton, qrCodeButton;
    private MaterialButton btnMarkAttendance, btnViewAttendanceReports, btnViewAttendace, btnProfile;
    private MaterialTextView tvTodaysClassesCount, tvAttendanceSummaryPercentage;

    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UserService
        userService = new UserService();
        user = new User();
        // Find and update the welcome message TextView
        welcomeMessage = findViewById(R.id.welcome_message);
        String rollNo = getStoredRollNo();
        if (rollNo != null) {
            fetchAndDisplayUserName(rollNo);
        } else {
            Log.e("StudentDashboardActivity", "Roll number not found in SharedPreferences");
            welcomeMessage.setText("Welcome, User");
            Toast.makeText(this, "User not found, redirecting to login", Toast.LENGTH_LONG).show();
            redirectToLogin();
        }

        // Initialize Logout button and set click listener
        logoutButton = findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> logoutUser());

        // Initialize QR Code button and set click listener
        qrCodeButton = findViewById(R.id.button_qr_code);
        qrCodeButton.setOnClickListener(v -> openQRCodeScanner());

        // Initialize other UI elements
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnViewAttendanceReports = findViewById(R.id.btnViewAttendanceReports);
        btnViewAttendace = findViewById(R.id.btnViewAttendace);
        btnProfile = findViewById(R.id.btnProfile);

        tvTodaysClassesCount = findViewById(R.id.tvTodaysClassesCount);
        tvAttendanceSummaryPercentage = findViewById(R.id.tvAttendanceSummaryPercentage);

        TextView faceverfication = findViewById(R.id.tvAttendanceSummaryLabel);

        // Set up button click listeners (if needed)
        btnMarkAttendance.setOnClickListener(v -> markAttendance());
        btnViewAttendanceReports.setOnClickListener(v -> viewAttendanceReports());
        btnViewAttendace.setOnClickListener(v -> viewAttendace() );
        btnProfile.setOnClickListener(v -> viewProfile());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle toolbar menu item clicks if any
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }

    // Method to fetch stored roll number
    private String getStoredRollNo() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("rollNo", null);
    }

    // Method to fetch and display the user's full name
    private void fetchAndDisplayUserName(String rollNo) {
        userService.getUserByRollNo(rollNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                     user = snapshot.getChildren().iterator().next().getValue(User.class);
                    if (user != null) {
                        welcomeMessage.setText("Welcome, " + user.getFullName());
                    }
                } else {
                    Log.e("StudentDashboardActivity", "User not found for Roll No: " + rollNo);
                    welcomeMessage.setText("Welcome, User");
                    Toast.makeText(StudentDashboardActivity.this, "User not found, redirecting to login", Toast.LENGTH_LONG).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentDashboardActivity", "Error fetching user data", error.toException());
                welcomeMessage.setText("Welcome, User");
                Toast.makeText(StudentDashboardActivity.this, "Error fetching data, redirecting to login", Toast.LENGTH_LONG).show();
                redirectToLogin();
            }
        });
    }

    // Method to logout user
    private void logoutUser() {
        // Fetch the stored roll number before clearing SharedPreferences
        String rollNo = getStoredRollNo();

        // Clear SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Show a toast message indicating logout success
        if (rollNo != null) {
            Toast.makeText(this, "Logout successful. Roll No: " + rollNo, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Logout successful.", Toast.LENGTH_LONG).show();
        }

        // Redirect to login page
        redirectToLogin();
    }

    // Method to redirect to login page
    private void redirectToLogin() {
        Intent loginIntent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    // Methods for button actions
    private void markAttendance() {
        Intent intent = new Intent(StudentDashboardActivity.this, MarkAttendanceActivity.class);
        startActivity(intent);
    }


    private void viewAttendanceReports() {
        Intent intent = new Intent(StudentDashboardActivity.this, ManageAttendanceReportActivity.class); // Replace with your actual class
        startActivity(intent);
    }



    private void viewProfile() {
        Intent intent = new Intent(StudentDashboardActivity.this, StudentProfileActivity.class); // Replace with your actual class
        startActivity(intent);
    }

    private void viewAttendace() {
        Intent intent = new Intent(StudentDashboardActivity.this, StudentAttendanceStatusActivity.class); // Replace with your actual class
        startActivity(intent);
    }

    // Method to open QR Code scanner activity
    private void openQRCodeScanner() {
        Intent intent = new Intent(StudentDashboardActivity.this, QRCodeScanFromDashboardActivity.class);
        startActivity(intent);
    }

}
