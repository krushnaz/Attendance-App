package com.ams.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.adapters.StudentAttendanceStatusAdapter;
import com.ams.dialogs.ViewStudentAttendaceDialog;
import com.ams.models.AttendanceRecord;
import com.ams.models.User;
import com.ams.services.AttendanceService;
import com.ams.services.UserService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StudentAttendanceStatusActivity extends AppCompatActivity {

    private TextInputEditText inputDate;
    private Button buttonSearch;
    private RecyclerView recyclerViewAttendance;
    private StudentAttendanceStatusAdapter attendanceStatusAdapter;
    private List<AttendanceRecord> attendanceRecords;
    private AttendanceService attendanceService;
    private TextView noRecordsTextView;
    private Context context;
    private User user;
    private UserService userService;
    private String courseName;
    private String semester;
    private String division;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_status);
        context = this;

        // Initialize views
        inputDate = findViewById(R.id.et_date);
        buttonSearch = findViewById(R.id.btn_search_date);
        recyclerViewAttendance = findViewById(R.id.recyclerViewAttendance);
        noRecordsTextView = findViewById(R.id.tv_no_records);

        // Initialize Attendance Service
        attendanceService = new AttendanceService();
        userService = new UserService();

        // Setup RecyclerView
        attendanceRecords = new ArrayList<>();
        attendanceStatusAdapter = new StudentAttendanceStatusAdapter(context, attendanceRecords, record -> showStatusUpdateDialog(record));
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAttendance.setAdapter(attendanceStatusAdapter);

        // Set Date Picker Dialog
        inputDate.setOnClickListener(v -> showDatePickerDialog());

        // Set Search button click listener
        buttonSearch.setOnClickListener(v -> {
            String selectedDate = inputDate.getText().toString();
            if (!selectedDate.isEmpty()) {
                fetchAttendanceByDate(selectedDate);
            } else {
                Toast.makeText(StudentAttendanceStatusActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch user details and store the courseName, semester, and division
        getUserDetails(new OnUserDetailsFetchedCallback() {
            @Override
            public void onSuccess(User fetchedUser) {
                user = fetchedUser;
                courseName = user.getCourseName();
                semester = user.getSemester();
                division = user.getDivision();
            }
        });
    }

    private void showDatePickerDialog() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
                    inputDate.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void fetchAttendanceByDate(String date) {
        // Convert the date format from "dd/MM/yyyy" to "dd-MM-yyyy"
        String formattedDate = date.replace("/", "-");

        // Fetch student attendance by the formatted date using AttendanceService
        String rollNo = getStoredRollNo();
        if (rollNo == null) {
            Toast.makeText(StudentAttendanceStatusActivity.this, "Roll number not found", Toast.LENGTH_SHORT).show();
            return;
        }

        attendanceService.getAttendanceByDateAndRollNo(formattedDate, rollNo, courseName, semester, division, new AttendanceService.OnAttendanceFetchedListener() {
            @Override
            public void onAttendanceFetched(List<AttendanceRecord> records) {
                attendanceRecords.clear();
                attendanceRecords.addAll(records);

                if (records.isEmpty()) {
                    noRecordsTextView.setVisibility(View.VISIBLE);
                    recyclerViewAttendance.setVisibility(View.GONE); // Hide the RecyclerView
                } else {
                    noRecordsTextView.setVisibility(View.GONE); // Hide the message
                    recyclerViewAttendance.setVisibility(View.VISIBLE); // Show the RecyclerView
                    attendanceStatusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFetchFailure(@NonNull String errorMessage) {
                Toast.makeText(StudentAttendanceStatusActivity.this, "Failed to fetch attendance: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to fetch stored roll number
    private String getStoredRollNo() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("rollNo", null);
    }

    private void showStatusUpdateDialog(AttendanceRecord record) {
        ViewStudentAttendaceDialog dialog = ViewStudentAttendaceDialog.newInstance(record);
        dialog.show(getSupportFragmentManager(), "StudentAttendanceStatusDialog");
    }

    private void getUserDetails(final OnUserDetailsFetchedCallback callback) {
        String rollNo = getStoredRollNo();
        userService.fetchUserByRollNo(rollNo, new UserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StudentAttendanceStatusActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnUserDetailsFetchedCallback {
        void onSuccess(User user);
    }
}
