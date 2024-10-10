package com.ams.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ams.models.Subject;
import com.ams.models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TimetableService {

    private final DatabaseReference databaseReference;
    private final DatabaseReference teachersReference;
    private final DatabaseReference attendanceReference;

    public TimetableService() {
        databaseReference = FirebaseDatabase.getInstance().getReference("timetables");
        teachersReference = FirebaseDatabase.getInstance().getReference("teachers");
        attendanceReference =  FirebaseDatabase.getInstance().getReference("attendance");
    }

    public interface SaveOrUpdateCallback {
        void onSuccess(String entryId);
        void onFailure(@NonNull Exception e);
    }

    public interface DeleteEntryCallback {
        void onSuccess(String deletedEntryId);
        void onFailure(@NonNull Exception e);
    }

    public interface TimetableCallbackFetch {
        void onSuccess(Map<String, Subject> entries);
        void onFailure(@NonNull Exception e);
    }

    // Save or update timetable entry
    public void saveOrUpdateEntry(String courseName, String semester, String division, String entryId, String day,
                                  Subject subject, SaveOrUpdateCallback callback) {
        DatabaseReference entryRef = databaseReference
                .child(courseName)
                .child(semester)
                .child(division)
                .child(entryId)
                .child(day);

        entryRef.setValue(subject)
                .addOnSuccessListener(aVoid -> callback.onSuccess(entryId))
                .addOnFailureListener(callback::onFailure);
    }

    // Delete timetable entry
    public void deleteEntry(String courseName, String semester, String division, String day, String rowId, DeleteEntryCallback callback) {
        DatabaseReference entryRef = databaseReference
                .child(courseName)
                .child(semester)
                .child(division)
                .child(rowId)
                .child(day);

        entryRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeleteEntry", "Successfully deleted entry at path: " + entryRef.toString());
                    callback.onSuccess(rowId);
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteEntry", "Failed to delete entry at path: " + entryRef.toString(), e);
                    callback.onFailure(e);
                });
    }


    // Get timetable entries by course, semester, division, and day
    public void getTimetableById(String courseName, String semester, String division, String day, String rowId, TimetableCallbackFetch callback) {
        DatabaseReference entriesRef = databaseReference
                .child(courseName)
                .child(semester)
                .child(division)
                .child(rowId)
                .child(day);

        entriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Subject> entries = new HashMap<>();
                    // Only one entry should be present in this structure
                    Subject subject = dataSnapshot.getValue(Subject.class);
                    if (subject != null) {
                        String key = day+""+rowId;
                        entries.put(key, subject); // Store subject under the day key
                        Log.d("TimetableService","getTimetableById --- key "+key);
                    }
                    callback.onSuccess(entries);
                } else {
                    callback.onFailure(new Exception("No data found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    // Fetch subjects for a specific day and rowId
    public void fetchSubjects(String courseId, String semester, String division, TimetableCallbackFetch callback) {
        DatabaseReference entriesRef = databaseReference
                .child(courseId)
                .child(semester)
                .child(division);

        entriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Subject> timetableEntries = new HashMap<>();

                    // Loop through each entry (entryId)
                    for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                        String entryId = entrySnapshot.getKey(); // Get entryId (e.g., 1, 2, 3, etc.)

                        // Loop through each day of the week within the entry
                        for (DataSnapshot daySnapshot : entrySnapshot.getChildren()) {
                            String dayOfWeek = daySnapshot.getKey(); // Get the day of the week (e.g., Monday, Tuesday)

                            // Retrieve the fields of the subject for that day
                            String subjectName = daySnapshot.child("subjectName").getValue(String.class);
                            String subjectCode = daySnapshot.child("subjectCode").getValue(String.class);
                            String teacherName = daySnapshot.child("teacherName").getValue(String.class);
                            String startTime = daySnapshot.child("startTime").getValue(String.class);
                            String endTime = daySnapshot.child("endTime").getValue(String.class);
                            String room = daySnapshot.child("room").getValue(String.class);
                            String lectureType = daySnapshot.child("lectureType").getValue(String.class);
                            Integer rowId = daySnapshot.child("rowId").getValue(Integer.class);
                            // Ensure that the required fields are not null before creating a Subject object
                            if (subjectName != null && subjectCode != null && teacherName != null && startTime != null &&
                                    endTime != null && room != null && lectureType != null && rowId != null) {

                                // Create the Subject object
                                Subject subject = new Subject(subjectName, subjectCode, teacherName, startTime, endTime, room, lectureType, rowId, dayOfWeek,"Deactivated",division);

                                // Use a unique key combining entryId and dayOfWeek to avoid conflicts
//                                String key = entryId + "_" + dayOfWeek;
                                String key = dayOfWeek + "" +entryId;
                                Log.d("TimetableService",subject.toString());
                                Log.d("TimetableService",key);

                                // Add the subject to the map
                                timetableEntries.put(key, subject);
                            }
                        }
                    }
                    // Return the populated timetableEntries map
                    callback.onSuccess(timetableEntries);
                } else {
                    // No data found for this course, semester, and division
                    callback.onFailure(new Exception("No data found for the specified course, semester, and division"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the Firebase query fails
                callback.onFailure(databaseError.toException());
            }
        });
    }



    //################################################# For Teacher Module ###############################################################################

    public void fetchSubjectsByTeacher(String teacherFullName, String courseName, String day, String semester, String division, TimetableCallbackFetch callback) {
        Log.d("FetchSubjects", "Starting fetch for teacher: " + teacherFullName + ", course: " + courseName + ", semester: " + semester + ", division: " + division + ", day: " + day);

        // Reference to the specific course, semester, and division
        DatabaseReference divisionRef = databaseReference.child(courseName).child(semester).child(division);

        divisionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot divisionSnapshot) {
                Log.d("FetchSubjects", "onDataChange called.");
                if (divisionSnapshot.exists()) {
                    Log.d("FetchSubjects", "Data found for course: " + courseName + ", semester: " + semester + ", division: " + division);

                    Map<String, Subject> timetableEntries = new HashMap<>();
                    boolean found = false;

                    // Loop through each entryId within the division
                    for (DataSnapshot entrySnapshot : divisionSnapshot.getChildren()) {
                        Log.d("FetchSubjects", "Processing entry: " + entrySnapshot.getKey());

                        // Check if the day exists in this entry
                        DataSnapshot daySnapshot = entrySnapshot.child(day);
                        if (daySnapshot.exists()) {
                            Log.d("FetchSubjects", "Day matched: " + day);

                            // Parse the data for the matched day
                            try {
                                Map<String, Object> subjectData = (Map<String, Object>) daySnapshot.getValue();
                                String subjectName = (String) subjectData.get("subjectName");
                                String subjectCode = (String) subjectData.get("subjectCode");
                                String teacherName = (String) subjectData.get("teacherName");
                                String startTime = (String) subjectData.get("startTime");
                                String endTime = (String) subjectData.get("endTime");
                                String room = (String) subjectData.get("room");
                                String lectureType = (String) subjectData.get("lectureType");
                                int rowId = ((Long) subjectData.get("rowId")).intValue();
                                String dayName = (String) subjectData.get("day");
                                String active = (String) subjectData.get("active");
                                String subjectDivision = (String) subjectData.get("division");
                                Subject subject = new Subject(subjectName, subjectCode, teacherName, startTime, endTime, room, lectureType, rowId, dayName, active, subjectDivision);

                                // Check teacher name
                                if (teacherFullName.equals(subject.getTeacherName())) {
                                    String key = day + "_" + entrySnapshot.getKey(); // Use entryId as key
                                    timetableEntries.put(key, subject);
                                    Log.d("FetchSubjects", "Added subject: " + subject.getSubjectName() + " with key: " + key);
                                    found = true;
                                } else {
                                    Log.d("FetchSubjects", "Teacher name does not match: " + subject.getTeacherName());
                                }
                            } catch (ClassCastException e) {
                                Log.e("FetchSubjects", "Error parsing subject data: " + e.getMessage());
                            }
                        }
                    }

                    if (!found) {
                        Log.d("FetchSubjects", "No subjects found for the specified course, semester, division, day, and teacher.");
                    }

                    // Callback with results
                    callback.onSuccess(timetableEntries);
                } else {
                    Log.d("FetchSubjects", "No data found for course: " + courseName + ", semester: " + semester + ", division: " + division);
                    callback.onFailure(new Exception("No data found for course: " + courseName + ", semester: " + semester + ", division: " + division));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("FetchSubjects", "Error fetching data: " + databaseError.getMessage());
                callback.onFailure(databaseError.toException());
            }
        });
    }


    public void updateSubjectActiveStatus(String courseName, String semester, String division, int rowId, String day, String newStatus, DatabaseCallback callback) {
        DatabaseReference subjectRef = databaseReference
                .child(courseName)
                .child(semester)
                .child(division)
                .child(String.valueOf(rowId))
                .child(day);

        subjectRef.child("active").setValue(newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public interface DatabaseCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return format.format(new Date()); // Returns the current time in "hh:mm a" format
    }

    public void fetchActivatedSubjects(String courseName, String day, String semester, String division, String rollNo, String currentDate, TimetableCallbackFetch callback) {
        Log.d("FetchSubjects", "Starting fetch for course: " + courseName + ", semester: " + semester + ", division: " + division + ", day: " + day);

        // Reference to the specific course, semester, and division
        DatabaseReference divisionRef = databaseReference.child(courseName).child(semester).child(division);
        Log.d("FetchSubjects", "Database reference path: " + divisionRef.toString());

        divisionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot divisionSnapshot) {
                Log.d("FetchSubjects", "onDataChange called.");
                if (divisionSnapshot.exists()) {
                    Log.d("FetchSubjects", "Data found for course: " + courseName + ", semester: " + semester + ", division: " + division);

                    Map<String, Subject> timetableEntries = new HashMap<>();
                    List<Subject> subjectsToCheck = new ArrayList<>();
                    String currentTime = getCurrentTime(); // Get the current time

                    // Loop through each entryId within the division
                    for (DataSnapshot entrySnapshot : divisionSnapshot.getChildren()) {
                        Log.d("FetchSubjects", "Processing entry: " + entrySnapshot.getKey());

                        // Check if the day exists in this entry
                        DataSnapshot daySnapshot = entrySnapshot.child(day);
                        Log.d("FetchSubjects", "Day: " + day);

                        if (daySnapshot.exists()) {
                            Log.d("FetchSubjects", "Day matched: " + day);

                            // Parse the data for the matched day
                            try {
                                Map<String, Object> subjectData = (Map<String, Object>) daySnapshot.getValue();
                                if (subjectData == null) {
                                    Log.e("FetchSubjects", "Subject data is null for entry: " + entrySnapshot.getKey());
                                    continue; // Skip this entry
                                }

                                String subjectName = (String) subjectData.get("subjectName");
                                String subjectCode = (String) subjectData.get("subjectCode");
                                String teacherName = (String) subjectData.get("teacherName");
                                String startTime = (String) subjectData.get("startTime");
                                String endTime = (String) subjectData.get("endTime");
                                String room = (String) subjectData.get("room");
                                String lectureType = (String) subjectData.get("lectureType");
                                int rowId = ((Long) subjectData.get("rowId")).intValue();
                                String dayName = (String) subjectData.get("day");
                                String active = (String) subjectData.get("active");
                                String division = (String) subjectData.get("division");

                                // Only add activated subjects for today and within the current time range
                                if ("Activated".equals(active) && isTimeInRange(currentTime, startTime, endTime)) {
                                    Subject subject = new Subject(subjectName, subjectCode, teacherName, startTime, endTime, room, lectureType, rowId, dayName, active, division);
                                    subjectsToCheck.add(subject);
                                    Log.d("FetchSubjects", "Activated and in-time subject added: " + subjectName);
                                } else {
                                    Log.d("FetchSubjects", "Subject not activated or not in current time range: " + subjectName);
                                }

                            } catch (ClassCastException e) {
                                Log.e("FetchSubjects", "Error parsing subject data: " + e.getMessage(), e);
                            }
                        } else {
                            Log.d("FetchSubjects", "Day not found in entry: " + entrySnapshot.getKey());
                        }
                    }

                    // Now check attendance for the collected subjects
                    checkAttendanceForSubjects(subjectsToCheck, rollNo, courseName, semester, division, currentDate, timetableEntries, callback);
                } else {
                    Log.d("FetchSubjects", "No data found for course: " + courseName + ", semester: " + semester + ", division: " + division);
                    callback.onFailure(new Exception("No data found for course: " + courseName + ", semester: " + semester + ", division: " + division));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("FetchSubjects", "Error fetching data: " + databaseError.getMessage());
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void checkAttendanceForSubjects(List<Subject> subjects, String rollNo, String courseName, String semester, String division, String currentDate, Map<String, Subject> timetableEntries, TimetableCallbackFetch callback) {
        AtomicInteger subjectsToCheckCount = new AtomicInteger(subjects.size());

        for (Subject subject : subjects) {
            hasAttendanceMarked(rollNo, courseName, semester, division, currentDate, subject.getStartTime(), subject.getEndTime(), isMarked -> {
                if (!isMarked) {
                    // Attendance not marked; add the subject to the timetable entries
                    String key = subject.getDay() + "_" + subject.getRowId(); // Assuming rowId can be used as a unique key
                    timetableEntries.put(key, subject);
                    Log.d("FetchSubjects", "Added activated subject: " + subject.getSubjectName() + " with key: " + key);
                } else {
                    Log.d("FetchSubjects", "Attendance already marked for: " + subject.getSubjectName() + ", status: Present");
                }

                // Check if all subjects have been processed
                if (subjectsToCheckCount.decrementAndGet() == 0) {
                    callback.onSuccess(timetableEntries);
                }
            });
        }
    }

    private void hasAttendanceMarked(String rollNo, String courseName, String semester, String division, String date, String startTime, String endTime, AttendanceCheckCallback callback) {
        // Reference to the attendance collection
        DatabaseReference attendanceRef = attendanceReference.child(courseName).child(semester).child(division).child(rollNo);

        // Log the attendance reference path
        Log.d("AttendanceCheck", "Checking attendance for: " +
                "Roll No: " + rollNo +
                ", Course: " + courseName +
                ", Semester: " + semester +
                ", Division: " + division +
                ", Date: " + date);
        Log.d("AttendanceCheck", "Attendance reference path: " + attendanceRef.toString());

        // Check if attendance has been marked for the current date under the roll number
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot attendanceSnapshot) {
                Log.d("AttendanceCheck", "onDataChange called for attendance reference.");

                // Check if there are attendance records for the roll number
                if (attendanceSnapshot.exists()) {
                    Log.d("AttendanceCheck", "Attendance data exists for rollNo: " + rollNo);

                    // Loop through attendance records for the roll number
                    for (DataSnapshot recordSnapshot : attendanceSnapshot.getChildren()) {
                        // Get the attendance date and match with the current date
                        String recordDate = recordSnapshot.child("date").getValue(String.class);

                        // Log the found record date
                        Log.d("AttendanceCheck", "Processing record for date: " + recordDate);

                        if (date.equals(recordDate)) {
                            Log.d("AttendanceCheck", "Found attendance record for the matching date: " + date);

                            String time = recordSnapshot.child("time").getValue(String.class);
                            String status = recordSnapshot.child("status").getValue(String.class);
                            String subjectName = recordSnapshot.child("subjectName").getValue(String.class);

                            // Log the attendance record details
                            Log.d("AttendanceCheck", "Processing record - Time: " + time +
                                    ", Status: " + status +
                                    ", Subject Name: " + subjectName);

                            // Check if the attendance time is between the subject's start and end times and the status is Present
                            if (isTimeInRange(time, startTime, endTime)) {
                                Log.d("AttendanceCheck", "Time " + time + " is within range: " + startTime + " to " + endTime);

                                if ("Present".equals(status)) {
                                    Log.d("AttendanceCheck", "Attendance marked as 'Present' for subject: " + subjectName);
                                    callback.onChecked(true); // Attendance marked
                                    return;
                                } else {
                                    Log.d("AttendanceCheck", "Attendance status is not 'Present' for subject: " + subjectName + ". Status: " + status);
                                }
                            } else {
                                Log.d("AttendanceCheck", "Time " + time + " is outside of the range: " + startTime + " to " + endTime);
                            }
                        } else {
                            Log.d("AttendanceCheck", "No matching attendance record found for the date: " + date);
                        }
                    }
                } else {
                    Log.d("AttendanceCheck", "No attendance records found for roll number: " + rollNo);
                }

                callback.onChecked(false); // Attendance not marked
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AttendanceCheck", "Error fetching attendance data: " + databaseError.getMessage());
                callback.onChecked(false); // In case of error, assume attendance not marked
            }
        });
    }


//    // Utility method to check if time is in the specified range
//    // Utility method to check if time is in the specified range (24-hour format)
    private boolean isTimeInRange(String timeToCheck, String startTime, String endTime) {
        SimpleDateFormat format24 = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            Date checkTime = format24.parse(timeToCheck); // Parse all times in 24-hour format
            Date start = format24.parse(startTime);
            Date end = format24.parse(endTime);

            Log.d("TimeCheck", "Checking if " + timeToCheck + " is between " + startTime + " and " + endTime);

            if (checkTime != null && start != null && end != null) {
                if (end.before(start)) {
                    // Handle overnight time ranges
                    end = new Date(end.getTime() + 24 * 60 * 60 * 1000); // Add 24 hours to end time
                }

                boolean isInRange = checkTime.after(start) && checkTime.before(end);
                Log.d("TimeCheck", "Result: " + isInRange);
                return isInRange;
            } else {
                Log.e("TimeCheck", "One of the parsed times is null. Check time: " + checkTime + ", Start: " + start + ", End: " + end);
                return false;
            }
        } catch (ParseException e) {
            Log.e("TimeCheck", "ParseException occurred while parsing time: " + e.getMessage(), e);
            return false;
        }
    }






    public void fetchSubjectDetails(String courseName, String semester, String division, String day, String subjectName, String rowId, OnSubjectFetchedListener listener) {
        DatabaseReference timetableRef = databaseReference
                .child(courseName)
                .child(semester)
                .child(division)
                .child(rowId)
                .child(day);

        Log.d("fetchSubjectDetails", "Fetching subject details for: " +
                "Course Name: " + courseName +
                ", Semester: " + semester +
                ", Division: " + division +
                ", Day: " + day +
                ", Subject Name: " + subjectName +
                ", Row Id: " + rowId);
        Log.d("fetchSubjectDetails", "Reference Path: " + timetableRef.toString());

        timetableRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("fetchSubjectDetails", "DataSnapshot: " + dataSnapshot);
                if (dataSnapshot.exists()) {
                    String fetchedSubjectName = dataSnapshot.child("subjectName").getValue(String.class);
                    if (fetchedSubjectName != null && fetchedSubjectName.equals(subjectName)) {
                        Subject subject = dataSnapshot.getValue(Subject.class);
                        Log.d("fetchSubjectDetails", "Subject fetched: " + subject);
                        listener.onSubjectFetched(subject);
                    } else {
                        Log.d("fetchSubjectDetails", "No subject found with the given name: " + subjectName);
                        listener.onSubjectFetched(null);
                    }
                } else {
                    Log.d("fetchSubjectDetails", "No data found at the specified path.");
                    listener.onSubjectFetched(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("fetchSubjectDetails", "Error fetching subject details: " + databaseError.getMessage());
                listener.onSubjectFetchFailed(databaseError.toException());
            }
        });
    }


    //#########################################################################################################################

    public void fetchAllSubjectsWithLectureType(String courseName, String semester, String division, OnAttendaceSubjectsFetchedListener callback) {
        Log.d("FetchSubjects", "Starting fetch for course: " + courseName + ", semester: " + semester + ", division: " + division);

        // Reference to the specific course, semester, and division
        DatabaseReference divisionRef = databaseReference.child(courseName).child(semester).child(division);

        divisionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot divisionSnapshot) {
                Log.d("FetchSubjects", "onDataChange called.");
                if (divisionSnapshot.exists()) {
                    Log.d("FetchSubjects", "Data found for course: " + courseName + ", semester: " + semester + ", division: " + division);

                    Map<String, Subject> timetableEntries = new HashMap<>();

                    // Loop through each entryId within the division
                    for (DataSnapshot entrySnapshot : divisionSnapshot.getChildren()) {
                        Log.d("FetchSubjects", "Processing entry: " + entrySnapshot.getKey());

                        // Loop through all day entries for each subject
                        for (DataSnapshot daySnapshot : entrySnapshot.getChildren()) {
                            Log.d("FetchSubjects", "Processing day: " + daySnapshot.getKey());

                            // Parse the data for each day
                            try {
                                Map<String, Object> subjectData = (Map<String, Object>) daySnapshot.getValue();
                                String subjectName = (String) subjectData.get("subjectName");
                                String lectureType = (String) subjectData.get("lectureType");

                                String subjectKey = subjectName + "_" + lectureType; // Unique key for subject+lecture type

                                if (!timetableEntries.containsKey(subjectKey)) {
                                    String subjectCode = (String) subjectData.get("subjectCode");
                                    String teacherName = (String) subjectData.get("teacherName");
                                    String startTime = (String) subjectData.get("startTime");
                                    String endTime = (String) subjectData.get("endTime");
                                    String room = (String) subjectData.get("room");
                                    int rowId = ((Long) subjectData.get("rowId")).intValue();
                                    String dayName = (String) subjectData.get("day");
                                    String active = (String) subjectData.get("active");
                                    String subjectDivision = (String) subjectData.get("division");

                                    Subject subject = new Subject(subjectName, subjectCode, teacherName, startTime, endTime, room, lectureType, rowId, dayName, active, subjectDivision);
                                    timetableEntries.put(subjectKey, subject);
                                    Log.d("FetchSubjects", "Added subject: " + subject.getSubjectName() + " with lectureType: " + subject.getLectureType());
                                } else {
                                    Log.d("FetchSubjects", "Duplicate subject skipped: " + subjectName + " with lectureType: " + lectureType);
                                }
                            } catch (ClassCastException e) {
                                Log.e("FetchSubjects", "Error parsing subject data: " + e.getMessage());
                            }
                        }
                    }

                    if (timetableEntries.isEmpty()) {
                        Log.d("FetchSubjects", "No subjects found for the specified course, semester, and division.");
                        callback.onSuccess(new ArrayList<>()); // Return an empty list if no subjects found
                    } else {
                        // Convert timetable entries to a list of subjects
                        List<Subject> subjectsList = new ArrayList<>(timetableEntries.values());
                        callback.onSubjectsFetched(subjectsList); // Callback with the subjects
                        // Optionally call onSuccess for subject names
                        List<String> subjectNames = new ArrayList<>();
                        for (Subject subject : subjectsList) {
                            subjectNames.add(subject.getSubjectName());
                        }
                        callback.onSuccess(subjectNames); // Optionally return subject names
                    }
                } else {
                    Log.d("FetchSubjects", "No data found for course: " + courseName + ", semester: " + semester + ", division: " + division);
                    callback.onFailure(new Exception("No data found for course: " + courseName + ", semester: " + semester + ", division: " + division));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("FetchSubjects", "Error fetching data: " + databaseError.getMessage());
                callback.onFailure(databaseError.toException());
            }
        });
    }


    public void fetchSubjectsByTeacherFullName(String teacherFullName, String courseName, SubjectsCallback callback) {
        Log.d("FetchSubjects", "Starting fetch for teacher: " + teacherFullName + ", course: " + courseName);

        // Reference to the specific course
        DatabaseReference courseRef = databaseReference.child(courseName);

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot courseSnapshot) {
                Log.d("FetchSubjects", "onDataChange called.");
                if (courseSnapshot.exists()) {
                    Log.d("FetchSubjects", "Data found for course: " + courseName);

                    Map<String, Subject> timetableEntries = new HashMap<>();
                    Set<String> addedSubjectNames = new HashSet<>(); // Track added subject names
                    boolean found = false;

                    // Loop through each semester and division
                    for (DataSnapshot semesterSnapshot : courseSnapshot.getChildren()) {
                        for (DataSnapshot divisionSnapshot : semesterSnapshot.getChildren()) {
                            // Loop through each entryId within the division
                            for (DataSnapshot entrySnapshot : divisionSnapshot.getChildren()) {
                                Log.d("FetchSubjects", "Processing entry: " + entrySnapshot.getKey());

                                // Loop through the days
                                for (DataSnapshot daySnapshot : entrySnapshot.getChildren()) {
                                    Log.d("FetchSubjects", "Processing day: " + daySnapshot.getKey());

                                    // Parse the data for the matched day
                                    try {
                                        Map<String, Object> subjectData = (Map<String, Object>) daySnapshot.getValue();
                                        String subjectName = (String) subjectData.get("subjectName");
                                        String subjectCode = (String) subjectData.get("subjectCode");
                                        String teacherName = (String) subjectData.get("teacherName");
                                        String startTime = (String) subjectData.get("startTime");
                                        String endTime = (String) subjectData.get("endTime");
                                        String room = (String) subjectData.get("room");
                                        String lectureType = (String) subjectData.get("lectureType");
                                        int rowId = ((Long) subjectData.get("rowId")).intValue();
                                        String dayName = (String) subjectData.get("day");

                                        Subject subject = new Subject(subjectName, subjectCode, teacherName, startTime, endTime, room, lectureType, rowId, dayName, "", ""); // Ignore active status

                                        // Check teacher name
                                        if (teacherFullName.equals(subject.getTeacherName())) {
                                            // Add to the timetable only if the subject name is not already added (allow different lecture types)
                                            if (!addedSubjectNames.contains(subjectName)) {
                                                timetableEntries.put(daySnapshot.getKey() + "_" + entrySnapshot.getKey(), subject);
                                                addedSubjectNames.add(subjectName); // Track that this subject name has been added
                                                Log.d("FetchSubjects", "Added subject: " + subject.getSubjectName() + " with key: " + daySnapshot.getKey() + "_" + entrySnapshot.getKey());
                                                found = true;
                                            } else {
                                                // If the subject name is already added, check if the lecture type is different
                                                String existingLectureType = timetableEntries.values().stream()
                                                        .filter(s -> s.getSubjectName().equals(subjectName))
                                                        .map(Subject::getLectureType)
                                                        .findFirst()
                                                        .orElse(null);

                                                if (existingLectureType == null || !existingLectureType.equals(lectureType)) {
                                                    timetableEntries.put(daySnapshot.getKey() + "_" + entrySnapshot.getKey(), subject);
                                                    Log.d("FetchSubjects", "Added duplicate lecture type for subject: " + subject.getSubjectName());
                                                    found = true;
                                                } else {
                                                    Log.d("FetchSubjects", "Duplicate subject with same lecture type skipped: " + subjectName);
                                                }
                                            }
                                        } else {
                                            Log.d("FetchSubjects", "Teacher name does not match: " + subject.getTeacherName());
                                        }
                                    } catch (ClassCastException e) {
                                        Log.e("FetchSubjects", "Error parsing subject data: " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }

                    if (!found) {
                        Log.d("FetchSubjects", "No subjects found for the specified course and teacher.");
                        callback.onFailure(new Exception("No subjects found for the specified course and teacher."));
                    } else {
                        // Callback with results
                        callback.onSuccess(new ArrayList<>(timetableEntries.values()));
                    }
                } else {
                    Log.d("FetchSubjects", "No data found for course: " + courseName);
                    callback.onFailure(new Exception("No data found for course: " + courseName));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FetchSubjects", "Error fetching data: " + databaseError.getMessage());
                callback.onFailure(databaseError.toException());
            }
        });
    }


    public interface SubjectsCallback {
        void onSuccess(List<Subject> subjects);
        void onFailure(Exception e);
    }



    public interface OnAttendaceSubjectsFetchedListener {
        void onSubjectsFetched(List<Subject> subjects);
        void onSuccess(List<String> subjectNames); // For just names if needed
        void onFailure(Exception e);
    }



    public interface OnSubjectFetchedListener {
        void onSubjectFetched(Subject subject);
        void onSubjectFetchFailed(Exception exception);
    }

    public interface AttendanceCheckCallback {
        void onChecked(boolean isMarked);
    }

    //############################################# total classes count ############################################################################

    public void fetchCountOfTodayClasses(String teacherFullName, String courseName, String day, ClassCountCallback callback) {
        databaseReference.child(courseName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int classCount = 0;

                for (DataSnapshot semesterSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot divisionSnapshot : semesterSnapshot.getChildren()) {
                        for (DataSnapshot rowSnapshot : divisionSnapshot.getChildren()) {
                            DataSnapshot daySnapshot = rowSnapshot.child(day);

                            if (daySnapshot.exists()) {
                                String teacherName = daySnapshot.child("teacherName").getValue(String.class);
                                String activeStatus = daySnapshot.child("active").getValue(String.class);

                                if (teacherFullName.equals(teacherName) && "activated".equalsIgnoreCase(activeStatus)) {
                                    classCount++;
                                }
                            }
                        }
                    }
                }

                // Return the class count using the callback
                callback.onSuccess(classCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TimetableService", "Error fetching classes", error.toException());
                callback.onFailure(error.toException()); // Handle failure via callback
            }
        });
    }


    public interface ClassCountCallback {
        void onSuccess(int classCount);
        void onFailure(Exception e);
    }

}


