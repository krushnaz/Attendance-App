package com.ams.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ams.R;
import com.ams.adapters.SubjectItemAdapter;
import com.ams.models.AttendanceRecord;
import com.ams.models.Subject;
import com.ams.models.User;
import com.ams.services.AttendanceReportService;
import com.ams.services.UserService;
import com.ams.services.TimetableService;
import com.ams.utils.ExcelReportGenerator;

import java.util.ArrayList;
import java.util.List;

public class ManageAttendanceReportActivity extends AppCompatActivity implements SubjectItemAdapter.OnItemClickListener {

    private RecyclerView recyclerViewSubjects;
    private SubjectItemAdapter subjectItemAdapter;
    private List<Subject> subjectList = new ArrayList<>();
    private String studentRollNo;
    private User user;
    private UserService userService;
    private String courseName;
    private String semester;
    private String division;
    private TimetableService timetableService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance_reports);

        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));

        timetableService = new TimetableService();
        userService = new UserService(); // Initialize user service
        studentRollNo = getStoredRollNo(); // Retrieve student roll number

        // Fetch user details and initialize course info
        getUserDetails(new OnUserDetailsFetchedCallback() {
            @Override
            public void onSuccess(User fetchedUser) {
                user = fetchedUser;
                courseName = user.getCourseName();
                semester = user.getSemester();
                division = user.getDivision();

                // Fetch subjects after user details are fetched
                fetchSubjects();
            }
        });
    }

    private void fetchSubjects() {
        timetableService.fetchAllSubjectsWithLectureType(courseName, semester, division, new TimetableService.OnAttendaceSubjectsFetchedListener() {
            @Override
            public void onSubjectsFetched(List<Subject> subjects) {
                Log.d("FetchSubjects", "Fetched subjects: " + subjects.toString());

                // Initialize the adapter if it's not already initialized
                if (subjectItemAdapter == null) {
                    subjectItemAdapter = new SubjectItemAdapter(ManageAttendanceReportActivity.this, subjectList, studentRollNo, ManageAttendanceReportActivity.this);
                    recyclerViewSubjects.setAdapter(subjectItemAdapter);
                }

                // Update the subject list and notify the adapter
                subjectList.clear();
                subjectList.addAll(subjects);
                subjectItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSuccess(List<String> subjectNames) {
                Toast.makeText(ManageAttendanceReportActivity.this, "Subject names fetched: " + subjectNames.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageAttendanceReportActivity.this, "Failed to fetch subjects: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onViewAttendanceReport(Subject subject) {
        String subjectName = subject.getSubjectName();
        Log.d("ManageReportActivity", "Subject Name: " + subjectName);
        Log.d("ManageReportActivity", "Student Roll No: " + studentRollNo);
        // Handle attendance report viewing logic here
    }

    @Override
    public void onDownloadAttendanceReport(Subject subject) {
        AttendanceReportService attendanceReportService = new AttendanceReportService();

        // Fetch attendance records with the new parameters
        attendanceReportService.fetchAttendanceRecordsBySubjectAndStudent(
                user.getCourseName(),    // courseName
                user.getSemester(),      // semester
                user.getDivision(),      // division
                studentRollNo,          // studentRollNo
                subject.getSubjectName(), // subjectName
                subject.getLectureType(), // lectureType
                new AttendanceReportService.OnAttendanceRecordsFetchedListener() {
                    @Override
                    public void onAttendanceRecordsFetched(List<AttendanceRecord> records) {
                        ExcelReportGenerator.generateAttendanceReport(
                                ManageAttendanceReportActivity.this,
                                records,
                                subject.getSubjectName(),
                                subject.getLectureType()
                        );
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ManageAttendanceReportActivity.this, "Failed to fetch attendance records: " + error, Toast.LENGTH_LONG).show();
                    }
                }
        );
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
                Toast.makeText(ManageAttendanceReportActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnUserDetailsFetchedCallback {
        void onSuccess(User user);
    }

    private String getStoredRollNo() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getString("rollNo", null);
    }
}
