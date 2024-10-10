package com.ams.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ams.models.AttendanceRecord;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AttendanceService {

    private DatabaseReference databaseReference;

    public AttendanceService() {
        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance");
    }

    public interface OnAttendanceMarkedListener {
        void onSuccess();
        void onFailure();
    }

    public void markAttendance(AttendanceRecord attendanceRecord, OnAttendanceMarkedListener listener) {
        // Path: attendance/courseName/semester/division/attendanceId
        String courseName = attendanceRecord.getCourseName();
        String semester = attendanceRecord.getSemester();
        String division = attendanceRecord.getDivision();
        String rollNo = attendanceRecord.getStudentRollNo();
        String attendanceId = databaseReference.push().getKey();

        attendanceRecord.setAttendanceId(attendanceId);

        databaseReference.child(courseName)
                .child(semester)
                .child(division)
                .child(rollNo)
                .child(attendanceId)
                .setValue(attendanceRecord)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure());
    }

    // Interface for callback when fetching attendance records
    public interface OnAttendanceRecordsFetchedListener {
        void onSuccess(List<AttendanceRecord> records);
        void onFailure(Exception e);
    }


    // Interface for updating attendance status
    public interface OnAttendanceStatusUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void getAttendanceRecords(String courseName, String date, String subjectName, String lectureName, String status, OnAttendanceRecordsFetchedListener listener) {
        // Define the path for the specific course
        DatabaseReference courseRef = databaseReference.child(courseName);
        Log.d("AttendanceService", "Querying attendance for course: " + courseName + " on date: " + date);

        // Fetch attendance records for the entire course
        courseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<AttendanceRecord> filteredRecords = new ArrayList<>();
                Log.d("AttendanceService", "Query successful. Processing records...");

                // Loop through semesters
                for (DataSnapshot semesterSnapshot : task.getResult().getChildren()) {
                    String semester = semesterSnapshot.getKey();
                    Log.d("AttendanceService", "Processing semester: " + semester);

                    // Loop through divisions
                    for (DataSnapshot divisionSnapshot : semesterSnapshot.getChildren()) {
                        String division = divisionSnapshot.getKey();
                        Log.d("AttendanceService", "Processing division: " + division);

                        // Loop through students (by roll number)
                        for (DataSnapshot studentSnapshot : divisionSnapshot.getChildren()) {
                            String rollNumber = studentSnapshot.getKey();
                            Log.d("AttendanceService", "Processing roll number: " + rollNumber);

                            // Loop through attendance records
                            for (DataSnapshot recordSnapshot : studentSnapshot.getChildren()) {
                                AttendanceRecord record = recordSnapshot.getValue(AttendanceRecord.class);
                                if (record != null) {
                                    Log.d("AttendanceService", "Record found - Date: " + record.getDate() + ", Subject: " + record.getSubjectName());

                                    // Check if the record matches the provided criteria
                                    if (record.getDate().equals(date) &&
                                            record.getSubjectName().equals(subjectName) &&
                                            record.getLectureType().equals(lectureName) &&
                                            record.getStatus().equals(status)) {
                                        filteredRecords.add(record);
                                        Log.d("AttendanceService", "Record matches filter criteria. Adding to list.");
                                    }else{
                                        Log.d("AttendanceService", "Record matche not");

                                    }
                                }
                            }
                        }
                    }
                }

                // Check if any records were found
                if (filteredRecords.isEmpty()) {
                    Log.d("AttendanceService", "No records found after filtering");
                } else {
                    Log.d("AttendanceService", "Filtered records count: " + filteredRecords.size());
                }
                listener.onSuccess(filteredRecords);
            } else {
                Log.e("AttendanceService", "Error fetching records: " + task.getException());
                listener.onFailure(task.getException());
            }
        });
    }


    // Update attendance status
    public void updateAttendanceStatus(String courseName, String semester, String division, String rollNo, String attendanceId, String status, OnAttendanceStatusUpdatedListener listener) {
        // Define the full path to the specific attendance record
        DatabaseReference attendanceRef = databaseReference
                .child(courseName)
                .child(semester)
                .child(division)
                .child(rollNo)
                .child(attendanceId)
                .child("status");

        // Update the "status" field of the specified attendance record
        attendanceRef.setValue(status).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("AttendanceService", "Attendance status updated successfully for " +
                        "course: " + courseName + ", semester: " + semester + ", division: " + division +
                        ", rollNo: " + rollNo + ", attendanceId: " + attendanceId);
                listener.onSuccess();
            } else {
                Log.e("AttendanceService", "Failed to update attendance status: " + task.getException());
                listener.onFailure(task.getException());
            }
        });
    }


    public void searchStudentByName(String studentName, final OnStudentSearchListener listener) {
        Query query = databaseReference.orderByChild("studentName").equalTo(studentName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AttendanceRecord> records = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AttendanceRecord record = snapshot.getValue(AttendanceRecord.class);
                    if (record != null) {
                        records.add(record);
                    }
                }
                listener.onSuccess(records);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public interface OnStudentSearchListener {
        void onSuccess(List<AttendanceRecord> records);
        void onFailure(Exception e);
    }

    public interface OnStatusUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void getAttendanceByDateAndRollNo(String date, String rollNo, String courseName, String semester, String division, OnAttendanceFetchedListener listener) {
        // Debugging: Log the parameters
        Log.d("getAttendanceByDateAndRollNo", "Parameters - Date: " + date + ", RollNo: " + rollNo +
                ", CourseName: " + courseName + ", Semester: " + semester + ", Division: " + division);

        // Create a query to filter by courseName, semester, division, roll number, and date
        DatabaseReference attendanceRef = databaseReference.child(courseName).child(semester).child(division).child(rollNo);

        // Debugging: Log the reference path
        Log.d("getAttendanceByDateAndRollNo", "Database reference path: " + attendanceRef.toString());

        attendanceRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Debugging: Log the snapshot size (number of records returned)
                Log.d("getAttendanceByDateAndRollNo", "DataSnapshot has children: " + dataSnapshot.getChildrenCount());

                List<AttendanceRecord> attendanceList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AttendanceRecord record = snapshot.getValue(AttendanceRecord.class);

                    // Debugging: Log each attendance record found
                    if (record != null) {
                        Log.d("getAttendanceByDateAndRollNo", "Attendance Record found: " + record.toString());
                        attendanceList.add(record);
                    } else {
                        Log.d("getAttendanceByDateAndRollNo", "Attendance Record is null for snapshot: " + snapshot.getKey());
                    }
                }
                listener.onAttendanceFetched(attendanceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Debugging: Log the error message
                Log.e("getAttendanceByDateAndRollNo", "Error fetching attendance: " + databaseError.getMessage());

                listener.onFetchFailure(databaseError.getMessage());
            }
        });
    }

    public interface OnAttendanceFetchedListener {
        void onAttendanceFetched(List<AttendanceRecord> attendanceRecords);
        void onFetchFailure(String errorMessage);
    }

    public void hasAttendanceMarked(String courseName, String semester, String division, String rollNumber, String date, OnAttendanceCheckedListener listener) {
        databaseReference.child(courseName)
                .child(semester)
                .child(division)
                .child(rollNumber)
                .orderByChild("date")
                .equalTo(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasMarked = snapshot.exists();
                        listener.onChecked(hasMarked);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.toException());
                    }
                });
    }

    public interface OnAttendanceCheckedListener {
        void onChecked(boolean hasMarked);
        void onError(Exception e);
    }

    public void hasAttendanceMarkedForSubject(String courseName, String semester, String division, String rollNumber, String date, String subjectName, String lectureType, OnAttendanceCheckedListener listener) {
        databaseReference.child(courseName)
                .child(semester)
                .child(division)
                .child(rollNumber)
                .orderByChild("date")
                .equalTo(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasMarked = false;

                        // Check if any record exists for the given subject name and lecture type
                        for (DataSnapshot attendanceSnapshot : snapshot.getChildren()) {
                            AttendanceRecord record = attendanceSnapshot.getValue(AttendanceRecord.class);
                            if (record != null && record.getSubjectName().equals(subjectName) && record.getLectureType().equals(lectureType)) {
                                hasMarked = true;
                                break;
                            }
                        }

                        listener.onChecked(hasMarked);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.toException());
                    }
                });
    }


}
