package com.ams.activities;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.adapters.SubjectAdapter;
import com.ams.dialogs.QrCodeDialog;
import com.ams.dialogs.SubjectDetailDialog;
import com.ams.models.AttendanceRecord;
import com.ams.models.Course;
import com.ams.models.Subject;
import com.ams.models.User;
import com.ams.receivers.NotificationReceiver;
import com.ams.services.AttendanceService;
import com.ams.services.CourseService;
import com.ams.services.TimetableService;
import com.ams.services.TeacherService;
import com.ams.services.UserService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageScheduleActivity extends AppCompatActivity {

    private static final long GRACE_PERIOD_MILLIS = 15 * 60 * 1000; // 15 minutes in milliseconds
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private List<Subject> subjectsList;
    private TimetableService timetableService;
    private TeacherService teacherService;
    private CourseService courseService;
    // Add spinners for courseName, semester, and division
    private Spinner  spinnerSemester, spinnerDivision;
    private String  selectedSemester, selectedDivision;
    private ImageButton btnSearch;
    private String courseName;
    private Button markAbsenteesButton;
    private AttendanceService attendanceService; // Add this line for attendance tracking
    private long gracePeriodMillis = 15 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_schedule);

        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));

        // Initialize subjects list and adapter
        attendanceService = new AttendanceService(); // Initialize in onCreate
        subjectsList = new ArrayList<>();
        checkButtonVisibility(subjectsList);
        scheduleNotificationAfterLastSubject(subjectsList);  // Schedule notification
        subjectAdapter = new SubjectAdapter(subjectsList, new SubjectAdapter.OnActionClickListener() {

            @Override
            public void onGenerateQRCodeClick(Subject subject) {
                // Extract details from the Subject object
                String subjectName = subject.getSubjectName();
                String division = subject.getDivision();
                String startTime = subject.getStartTime();
                String endTime = subject.getEndTime();
                String day = subject.getDay();
                String lectureType = subject.getLectureType();
                String room = subject.getRoom();
                String teacherName = subject.getTeacherName();
                int entryId = subject.getRowId();

                // Create the QR code dialog
                QrCodeDialog qrCodeDialog = new QrCodeDialog(ManageScheduleActivity.this, subjectName,selectedSemester, division, courseName, day, startTime, endTime, lectureType, room,teacherName,entryId);
                qrCodeDialog.show();
            }


            @Override
            public void onActivateClick(Subject subject) {
                // Handle subject activation
            }

            @Override
            public void onViewClick(Subject subject) {
                // Create a new instance of SubjectDetailDialog
                SubjectDetailDialog dialog = SubjectDetailDialog.newInstance(subject,courseName,selectedSemester,selectedDivision);

                // Show the dialog
                dialog.show(getSupportFragmentManager(), "SubjectDetailDialog");
            }
        });

        recyclerViewSubjects.setAdapter(subjectAdapter);

        timetableService = new TimetableService();
        teacherService = new TeacherService();
        courseService = new CourseService();
        // Initialize spinners
        spinnerSemester = findViewById(R.id.spinner_semester);
        spinnerDivision = findViewById(R.id.spinner_division);
        btnSearch = findViewById(R.id.btn_search);
        markAbsenteesButton = findViewById(R.id.markAbsenteesButton);


        // Fetch and populate spinners (Implement this using the previous logic)
        populateSpinners();

        // Fetch today's timetable data from Firebase
        // Handle the search button click
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCurrentDaySubjects();
            }
        });

        markAbsenteesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch the list of subjects that have already passed their end time
                List<Subject> passedSubjects = getPassedSubjects(subjectsList); // Assuming subjectsList is available

                if (!passedSubjects.isEmpty()) {
                    for (Subject subject : passedSubjects) {
                        // Call markAbsentees for each passed subject
                        markAbsentees(subject);
                    }
                    Log.d("Attendance", "Absentees marked for all passed subjects.");
                    Toast.makeText(getApplicationContext(), "Absentees marked for all passed subjects.", Toast.LENGTH_SHORT).show();

                } else {
                    Log.e("Attendance", "No subjects have passed their end time. Cannot mark absentees.");

                    // Optionally show a message to the user
                    Toast.makeText(getApplicationContext(), "No subjects have passed their end time.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void populateSpinners() {
        String teacherUsername = getStoredUsername();

        // Fetch teacher details to get the department
        teacherService.fetchTeacherFullnameAndDepartmentByUsername(teacherUsername, new TeacherService.TeacherCallback() {
            @Override
            public void onSuccess(String fullName, String department) {
                courseName = department;
                // Fetch courses based on the department
                courseService.getCoursesByName(department, new CourseService.CourseCallback() {
                    @Override
                    public void onSuccess(List<Course> courseList) {
                        // Extract and bind semester and division to spinners
                        setupSpinners(courseList);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ManageScheduleActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageScheduleActivity.this, "Failed to fetch teacher details", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupSpinners(List<Course> courseList) {
        List<String> semesters = new ArrayList<>();
        List<String> divisions = new ArrayList<>();

        for (Course course : courseList) {
            if (!semesters.contains(course.getSemester())) {
                semesters.add(course.getSemester());
            }
            if (!divisions.contains(course.getDivision())) {
                divisions.add(course.getDivision());
            }
        }

        // Set up Semester Spinner with custom layout
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<String>(this, R.layout.spinner_selected_item, semesters) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Set the text color for dropdown items (black in this case)
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };
        semesterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

// Set up Division Spinner with custom layout
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<String>(this, R.layout.spinner_selected_item, divisions) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Set the text color for dropdown items (black in this case)
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };
        divisionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDivision.setAdapter(divisionAdapter);

    }


    private void fetchCurrentDaySubjects() {
        String teacherUsername = getStoredUsername();
        String dayOfWeek = getCurrentDayOfWeek();
        selectedSemester = spinnerSemester.getSelectedItem().toString();
        selectedDivision = spinnerDivision.getSelectedItem().toString();

        teacherService.fetchTeacherFullnameAndDepartmentByUsername(teacherUsername, new TeacherService.TeacherCallback() {
            @Override
            public void onSuccess(String fullName, String department) {
                timetableService.fetchSubjectsByTeacher(fullName, department, dayOfWeek, selectedSemester, selectedDivision, new TimetableService.TimetableCallbackFetch() {
                    @Override
                    public void onSuccess(Map<String, Subject> entries) {
                        subjectsList.clear();
                        subjectsList.addAll(entries.values());
                        if (subjectsList.isEmpty()) {
                            Toast.makeText(ManageScheduleActivity.this, "No subjects scheduled for today", Toast.LENGTH_SHORT).show();
                        } else {
                            subjectAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ManageScheduleActivity.this, "No Subject Available", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageScheduleActivity.this, "Error fetching teacher data", Toast.LENGTH_SHORT).show();
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
        }
        return "";
    }

    private String getStoredUsername() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("username", null);
    }

//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$


    private void markAbsentees(Subject subject) {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Log.d("Attendance", "Current Date: " + currentDate);
        Log.d("Attendance", "Subject: " + subject.getSubjectName());

        attendanceService.getAttendanceRecords(courseName, currentDate, subject.getSubjectName(), subject.getLectureType(), subject.getActive(), new AttendanceService.OnAttendanceRecordsFetchedListener() {
            @Override
            public void onSuccess(List<AttendanceRecord> records) {
                Log.d("Attendance", "Fetched attendance records: " + records.size() + " for subject: " + subject.getSubjectName());

                List<String> presentRollNos = new ArrayList<>();
                for (AttendanceRecord record : records) {
                    presentRollNos.add(record.getStudentRollNo());
                }

                Log.d("Attendance", "Present Roll Numbers: " + presentRollNos);

                // Fetch all students to check who is absent
                UserService userService = new UserService();
                userService.getAllUsers().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("Attendance", "Total Users Fetched: " + dataSnapshot.getChildrenCount());
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                Log.d("Attendance", "Checking user: " + user.getFullName() + " (Roll No: " + user.getRollNumber() + ")");

                                // Check if the user is already marked as present
                                if (!presentRollNos.contains(user.getRollNumber())) {
                                    attendanceService.hasAttendanceMarkedForSubject(courseName, user.getSemester(), user.getDivision(), user.getRollNumber(), currentDate, subject.getSubjectName(), subject.getLectureType(), new AttendanceService.OnAttendanceCheckedListener() {
                                        @Override
                                        public void onChecked(boolean hasMarked) {
                                            if (!hasMarked) {
                                                // Only mark attendance if not already marked
                                                AttendanceRecord attendanceRecord = new AttendanceRecord();
                                                attendanceRecord.setStudentRollNo(user.getRollNumber());
                                                attendanceRecord.setStudentName(user.getFullName());
                                                attendanceRecord.setSubjectName(subject.getSubjectName());
                                                attendanceRecord.setDate(currentDate);
                                                attendanceRecord.setTime(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                                                attendanceRecord.setTeacherName(subject.getTeacherName());
                                                attendanceRecord.setStatus("Absent");
                                                attendanceRecord.setCourseName(courseName);
                                                attendanceRecord.setSemester(user.getSemester());
                                                attendanceRecord.setDivision(subject.getDivision());
                                                attendanceRecord.setLectureType(subject.getLectureType());
                                                attendanceRecord.setDay(subject.getDay());

                                                Log.d("Attendance", "Marking absent: " + user.getFullName() + " (Roll No: " + user.getRollNumber() + ")");
                                                attendanceService.markAttendance(attendanceRecord, new AttendanceService.OnAttendanceMarkedListener() {
                                                    @Override
                                                    public void onSuccess() {
                                                        Log.d("Attendance", "Successfully marked absentee for: " + user.getFullName());
                                                    }

                                                    @Override
                                                    public void onFailure() {
                                                        Log.e("Attendance", "Failed to mark absentee for: " + user.getFullName());
                                                    }
                                                });
                                            } else {
                                                Log.d("Attendance", user.getFullName() + " has already marked attendance.");
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.e("Attendance", "Error checking attendance for: " + user.getFullName() + " - " + e.getMessage());
                                        }
                                    });
                                } else {
                                    Log.d("Attendance", user.getFullName() + " is present.");
                                }
                            } else {
                                Log.e("Attendance", "User data is null for snapshot: " + snapshot);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Attendance", "Error fetching users: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Attendance", "Failed to fetch attendance records: " + e.getMessage());
            }
        });
    }



    private void checkButtonVisibility(List<Subject> subjects) {
        // Get the last subject's end time and the first subject's start time in milliseconds
        long lastEndTimeMillis = getLastSubjectEndTime(subjects);
        long firstStartTimeMillis = getFirstSubjectStartTime(subjects);

        // Get the current system time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Log the current, last end time, and first start time for debugging
        Log.d("Attendance", "Current Time (ms): " + currentTimeMillis + " | " + new Date(currentTimeMillis));
        Log.d("Attendance", "Last Subject End Time (ms): " + lastEndTimeMillis + " | " + new Date(lastEndTimeMillis));
        Log.d("Attendance", "First Subject Start Time (ms): " + firstStartTimeMillis + " | " + new Date(firstStartTimeMillis));

        Button markAbsenteesButton = findViewById(R.id.markAbsenteesButton);

        // Show the button if the current time is after the last subject's end time AND before the first subject's start time
        if (currentTimeMillis > lastEndTimeMillis && currentTimeMillis < firstStartTimeMillis) {
            markAbsenteesButton.setVisibility(View.VISIBLE);
            Log.d("Attendance", "Mark Absentees Button is now VISIBLE after last subject end time and before first subject start time.");
        } else {
            markAbsenteesButton.setVisibility(View.GONE);
            Log.d("Attendance", "Mark Absentees Button is now GONE.");
        }
    }

    // Helper method to get the last subject's end time in milliseconds
    private long getLastSubjectEndTime(List<Subject> subjects) {
        long lastEndTimeMillis = 0;

        for (Subject subject : subjects) {
            long endTimeMillis = convertTimeToMillis(subject.getEndTime());
            if (endTimeMillis > lastEndTimeMillis) {
                lastEndTimeMillis = endTimeMillis; // Update with the latest end time
            }
        }

        return lastEndTimeMillis;
    }

    // Helper method to get the first subject's start time in milliseconds
    private long getFirstSubjectStartTime(List<Subject> subjects) {
        long firstStartTimeMillis = Long.MAX_VALUE; // Initialize with max possible value

        for (Subject subject : subjects) {
            long startTimeMillis = convertTimeToMillis(subject.getStartTime());
            if (startTimeMillis < firstStartTimeMillis) {
                firstStartTimeMillis = startTimeMillis; // Update with the earliest start time
            }
        }

        return firstStartTimeMillis;
    }

    // Helper method to convert a time string (e.g., "10:55 PM") to milliseconds
    private long convertTimeToMillis(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String todayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

            // Combine today's date with the subject's time (e.g., "10:55 PM")
            String fullTimeStr = todayDate + " " + timeStr;
            SimpleDateFormat fullFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

            // Parse the full date-time string
            Date date = fullFormat.parse(fullTimeStr);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("Attendance", "Error parsing time: " + timeStr + " - " + e.getMessage());
            return 0;
        }
    }


    private List<Subject> getPassedSubjects(List<Subject> subjects) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.d("Attendance", "Current Time (ms): " + currentTimeMillis);

        List<Subject> passedSubjects = new ArrayList<>();

        for (Subject subject : subjects) {
            long endTimeMillis = convertTimeToMillis(subject.getEndTime());

            Log.d("Attendance", "Checking Subject: " + subject.getSubjectName() +
                    " | End Time: " + subject.getEndTime() + " (" + endTimeMillis + " ms)");

            // Check if the current time is greater than the end time of the subject
            if (currentTimeMillis > endTimeMillis) {
                Log.d("Attendance", "Subject passed: " + subject.getSubjectName());
                passedSubjects.add(subject); // Add to the list of passed subjects
            }
        }

        Log.d("Attendance", "Total Passed Subjects found: " + passedSubjects.size());
        return passedSubjects; // Return the list of passed subjects
    }


    private void scheduleNotificationAfterLastSubject(List<Subject> subjects) {
        long lastEndTimeMillis = getLastSubjectEndTime(subjects);  // Get the end time of the last subject

        // Create an intent for the notification
        Intent intent = new Intent(this, NotificationReceiver.class);
        // Use FLAG_IMMUTABLE for security
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Schedule the notification using AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Ensure lastEndTimeMillis is in the future
        if (System.currentTimeMillis() < lastEndTimeMillis) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, lastEndTimeMillis, pendingIntent);
        }
    }


}
