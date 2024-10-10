package com.ams.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.ams.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // Delay in milliseconds
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ROLL_NO = "rollNo";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ADMIN_USERNAME = "adminUsername";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay the execution of the checkUserStatus method
        new Handler().postDelayed(this::checkUserStatus, SPLASH_DELAY);
    }

    private void checkUserStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String rollNo = prefs.getString(KEY_ROLL_NO, null);
        String username = prefs.getString(KEY_USERNAME, null);
        String adminUsername = prefs.getString(KEY_ADMIN_USERNAME, null);

        // Determine the target activity based on the user status
        Intent intent = getNextActivityIntent(rollNo, username, adminUsername);
        startActivity(intent);
        finish(); // Close the splash activity
    }

    private Intent getNextActivityIntent(String rollNo, String username, String adminUsername) {
        if (rollNo != null) {
            return new Intent(this, StudentDashboardActivity.class);
        } else if (username != null) {
            return new Intent(this, TeacherDashboardActivity.class);
        } else if (adminUsername != null) {
            return new Intent(this, AdminDashboardActivity.class);
        } else {
            return new Intent(this, LoginActivity.class);
        }
    }
}
