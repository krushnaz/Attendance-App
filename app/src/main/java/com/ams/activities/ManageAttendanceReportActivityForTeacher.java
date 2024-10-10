package com.ams.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ams.R;
import com.ams.adapters.TeacherSubjectAdapter;
import com.ams.models.Subject;
import com.ams.services.SubjectService;
import com.ams.services.TeacherService;
import com.ams.services.TimetableService;

import java.util.List;

public class ManageAttendanceReportActivityForTeacher extends AppCompatActivity {

    private RecyclerView recyclerViewSubjects;
    private TeacherSubjectAdapter adapter;
    private SubjectService subjectService;
    private TeacherService teacherService;
    private TimetableService timetableService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance_report_for_teacher);

        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        subjectService = new SubjectService();
        teacherService = new TeacherService(); // Initialize the TeacherService
        timetableService = new TimetableService();
        fetchCurrentDaySubjects(); // Fetch subjects when the activity is created
    }

    private void fetchCurrentDaySubjects() {
        String teacherUsername = getStoredUsername();  // Get the username dynamically

        if (teacherUsername != null) {
            teacherService.fetchTeacherFullnameAndDepartmentByUsername(teacherUsername, new TeacherService.TeacherCallback() {
                @Override
                public void onSuccess(String fullName, String department) {
                    timetableService.fetchSubjectsByTeacherFullName(fullName, department, new TimetableService.SubjectsCallback() {
                        @Override
                        public void onSuccess(List<Subject> subjects) {
                            if (subjects.isEmpty()) {
                                Toast.makeText(ManageAttendanceReportActivityForTeacher.this, "No subjects scheduled for today", Toast.LENGTH_SHORT).show();
                            } else {
                                adapter = new TeacherSubjectAdapter(subjects, ManageAttendanceReportActivityForTeacher.this,fullName,department);
                                recyclerViewSubjects.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ManageAttendanceReportActivityForTeacher.this, "No subjects available", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ManageAttendanceReportActivityForTeacher.this, "Error fetching teacher data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getStoredUsername() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("username", null);
    }
}
