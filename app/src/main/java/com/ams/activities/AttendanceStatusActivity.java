package com.ams.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.adapters.AttendanceStatusAdapter;
import com.ams.dialogs.StudentAttendanceStatusDialog;
import com.ams.models.AttendanceRecord;
import com.ams.models.Subject;
import com.ams.services.AttendanceService;
import com.ams.services.SubjectService;
import com.ams.services.TeacherService;
import com.ams.services.TimetableService;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AttendanceStatusActivity extends AppCompatActivity {

    private TextInputEditText etDate;
    private EditText etSearchStudent;
    private Spinner spinnerSubject, spinnerLecture, spinnerStatus;
    private Button btnSearch;
    private AttendanceService attendanceService;
    private List<String> lectures;
    private RecyclerView recyclerViewAttendance;
    private AttendanceStatusAdapter adapter;
    private TimetableService timetableService;
    private String courseName;
    private String teacherFullName;
    private TeacherService teacherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_status);

        teacherService = new TeacherService();
        String teacherUsername = getStoredUsername();
        teacherService.fetchTeacherFullnameAndDepartmentByUsername(teacherUsername, new TeacherService.TeacherCallback() {
            @Override
            public void onSuccess(String fullName, String department) {
                teacherFullName = fullName;
                courseName = department;
                Log.e("onCreate", "teacherFullName: " + teacherFullName + "---- courseName :" + courseName);
                // Call populateSpinners() here after values are set
                populateSpinners();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TeacherFetchError", "Failed to fetch teacher details: " + e.getMessage());
            }
        });

        // Initialize views and services
        initializeViews();
        initializeRecyclerView();
        initializeServices();

        // Set up listeners (but don't call populateSpinners here)
        setupListeners();
    }

// Remove the initial call to populateSpinners() from onCreate()

    private void initializeViews() {
        etDate = findViewById(R.id.et_date);
        etSearchStudent = findViewById(R.id.et_search_student_input);
        spinnerSubject = findViewById(R.id.spinner_subject);
        spinnerLecture = findViewById(R.id.spinner_lecture);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnSearch = findViewById(R.id.btn_search_date);
        recyclerViewAttendance = findViewById(R.id.recyclerViewAttendance);
    }

    private void initializeRecyclerView() {
        Context context = this;

        // Initialize RecyclerView
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(context));

        // Initialize Adapter with an empty list and the status click listener
        adapter = new AttendanceStatusAdapter(context, new ArrayList<>(), record -> showStatusUpdateDialog(record));

        // Set the Adapter to the RecyclerView
        recyclerViewAttendance.setAdapter(adapter);
    }

    private void initializeServices() {
        String teacherUsername = getStoredUsername();
        attendanceService = new AttendanceService();
        lectures = new ArrayList<>();
        Log.d("AttendanceStatusActivity", "Teacher Username: " + teacherUsername);
    }

    private void setupListeners() {
        // Date Picker
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Search Button
        btnSearch.setOnClickListener(v -> searchAttendance());

        // Search Student on Enter
        etSearchStudent.setOnEditorActionListener((v, actionId, event) -> {
            searchStudent(etSearchStudent.getText().toString());
            return true;
        });

        // Spinner Listener to populate lectures based on subject selection
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                populateLectures(selectedSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action required
            }
        });
    }

    private String getStoredUsername() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("username", null);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AttendanceStatusActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void populateSpinners() {
        Log.e("populateSpinners", "teacherFullName: "+teacherFullName+"---- courseName :"+courseName);

        timetableService  = new TimetableService();  // Assuming SubjectService has this method
        timetableService.fetchSubjectsByTeacherFullName(teacherFullName, courseName, new TimetableService.SubjectsCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                if (subjects.isEmpty()) {
                    Toast.makeText(AttendanceStatusActivity.this, "No subjects available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Extract subject names to populate spinner
                List<String> subjectNames = new ArrayList<>();
                for (Subject subject : subjects) {
                    if (!subjectNames.contains(subject.getSubjectName())) {
                        subjectNames.add(subject.getSubjectName());  // Prevent duplicates
                    }
                }

                // Set up Subject Spinner with custom layout
                ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(AttendanceStatusActivity.this, R.layout.spinner_selected_item, subjectNames) {
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        // Set the text color for dropdown items (black in this case)
                        textView.setTextColor(getResources().getColor(R.color.black));
                        return view;
                    }
                };

// Set the custom dropdown layout for the items
                subjectAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Set the adapter to the Spinner
                spinnerSubject.setAdapter(subjectAdapter);

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AttendanceStatusActivity.this, "Error fetching subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Populate Status Spinner (No change here)
        String[] statusArray = {"Present", "Absent"};
        // Set up Status Spinner with custom layout
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(this, R.layout.spinner_selected_item, statusArray) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Set the text color for dropdown items (black in this case)
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };

// Set the custom dropdown layout for the items
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Set the adapter to the Spinner
        spinnerStatus.setAdapter(statusAdapter);

    }


    private void populateLectures(String selectedSubject) {

        SubjectService subjectService = new SubjectService();

        timetableService.fetchSubjectsByTeacherFullName(teacherFullName, courseName, new TimetableService.SubjectsCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                List<String> lectureTypes = new ArrayList<>();

                // Loop through subjects and find matching subject names
                for (Subject subject : subjects) {
                    if (subject.getSubjectName().equals(selectedSubject)) {
                        // Add lecture types to the list, avoiding duplicates
                        if (!lectureTypes.contains(subject.getLectureType())) {
                            lectureTypes.add(subject.getLectureType());
                        }
                    }
                }

                // Populate lecture spinner with the corresponding lecture types
                if (!lectureTypes.isEmpty()) {
                    // Set up Lecture Type Spinner with custom layout
                    ArrayAdapter<String> lectureAdapter = new ArrayAdapter<String>(AttendanceStatusActivity.this, R.layout.spinner_selected_item, lectureTypes) {
                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            // Set the text color for dropdown items (black in this case)
                            textView.setTextColor(getResources().getColor(R.color.black));
                            return view;
                        }
                    };

// Set the custom dropdown layout for the items
                    lectureAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Set the adapter to the Spinner
                    spinnerLecture.setAdapter(lectureAdapter);

                } else {
                    Toast.makeText(AttendanceStatusActivity.this, "No lectures available for selected subject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AttendanceStatusActivity.this, "Error fetching lectures: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void searchAttendance() {
        // Get selected values from the UI components
        // Get selected values from the UI components
        String date = etDate.getText().toString();
        String subject = (spinnerSubject.getSelectedItem() != null) ? spinnerSubject.getSelectedItem().toString() : "";
        String lecture = (spinnerLecture.getSelectedItem() != null) ? spinnerLecture.getSelectedItem().toString() : "";
        String status = (spinnerStatus.getSelectedItem() != null) ? spinnerStatus.getSelectedItem().toString() : "";

        String formattedDate = date.replace("/", "-");
        Log.e("formattedDate", "formattedDate"+formattedDate);


        // Check if date is selected
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if subject is selected
        if (subject.isEmpty()) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if lecture is selected
        if (lecture.isEmpty()) {
            Toast.makeText(this, "Please select a lecture", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if status is selected
        if (status.isEmpty()) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform search operation using the selected values
        attendanceService.getAttendanceRecords(courseName,formattedDate, subject, lecture, status, new AttendanceService.OnAttendanceRecordsFetchedListener() {
            @Override
            public void onSuccess(List<AttendanceRecord> records) {
                if (records == null || records.isEmpty()) {
                    Toast.makeText(AttendanceStatusActivity.this, "No records found for the selected criteria", Toast.LENGTH_SHORT).show();
                } else {
                    displayResults(records);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AttendanceStatusActivity.this, "Error fetching records: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("AttendanceSearchError", "Error fetching records", e);
            }
        });
    }

    private void displayResults(List<AttendanceRecord> records) {
        // Pass the current context and the list of records to the adapter
        adapter.updateData(records);
    }

    private void searchStudent(String studentName) {
        attendanceService.searchStudentByName(studentName, new AttendanceService.OnStudentSearchListener() {
            @Override
            public void onSuccess(List<AttendanceRecord> records) {
                if (records.isEmpty()) {
                    Toast.makeText(AttendanceStatusActivity.this, "No student found with this name", Toast.LENGTH_SHORT).show();
                } else {
                    displayResults(records);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AttendanceStatusActivity.this, "Error searching student: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showStatusUpdateDialog(AttendanceRecord record) {
        StudentAttendanceStatusDialog dialog = StudentAttendanceStatusDialog.newInstance(record);
        dialog.show(getSupportFragmentManager(), "StudentAttendanceStatusDialog");

    }
}
