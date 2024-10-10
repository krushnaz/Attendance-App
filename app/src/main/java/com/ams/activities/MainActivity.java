package com.ams.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ams.R;
import com.ams.workers.AttendanceWorker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister, btnContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnContact = findViewById(R.id.btnContact);

        // Start the periodic work for attendance
//        startAttendanceWorker();

        // Navigate to Login Activity
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Register Activity
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Contact Activity

    }

//    private void startAttendanceWorker() {
//        // Create a periodic work request for AttendanceWorker
//        PeriodicWorkRequest attendanceWorkRequest = new PeriodicWorkRequest.Builder(
//                AttendanceWorker.class,
//                15, // Repeat interval (in minutes)
//                TimeUnit.MINUTES)
//                .build();
//
//        // Enqueue the work request
//        WorkManager.getInstance(this).enqueue(attendanceWorkRequest);
//    }
}
