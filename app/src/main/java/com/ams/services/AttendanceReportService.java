package com.ams.services;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.ams.models.AttendanceRecord;

import java.util.ArrayList;
import java.util.List;

public class AttendanceReportService {

    private static final String TAG = "AttendanceReportService";
    private DatabaseReference databaseReference;

    public AttendanceReportService() {
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance");
    }
    private static DatabaseReference getAttendanceReference() {
        return FirebaseDatabase.getInstance().getReference("attendance");
    }

    public interface OnAttendanceRecordsFetchedListener {
        void onAttendanceRecordsFetched(List<AttendanceRecord> records);
        void onFailure(String error);
    }


    public void fetchAttendanceRecordsBySubjectAndStudent(String courseName, String semester, String division, String studentRollNo, String subjectName, String lectureType, OnAttendanceRecordsFetchedListener listener) {
        // Construct the path to the attendance records
        DatabaseReference attendanceRef = databaseReference.child(courseName).child(semester).child(division).child(studentRollNo);

        attendanceRef.orderByChild("subjectName").equalTo(subjectName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Raw record: " + dataSnapshot.getValue());

                        List<AttendanceRecord> records = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            AttendanceRecord record = snapshot.getValue(AttendanceRecord.class);
                            if (record != null && record.getLectureType().equals(lectureType)) {
                                records.add(record);
                            }
                        }
                        if (listener != null) {
                            listener.onAttendanceRecordsFetched(records);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to fetch attendance records: " + databaseError.getMessage());
                        if (listener != null) {
                            listener.onFailure(databaseError.getMessage());
                        }
                    }
                });
    }


    public Task<List<AttendanceRecord>> getAttendanceRecordsByTeacherFullNameAndCourse(String teacherFullName, String courseName, String subjectName, String lectureType) {
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("attendance").child(courseName);

        Log.d("AttendanceReportService", "Fetching records for Course: " + courseName + ", Subject: " + subjectName + ", Teacher: " + teacherFullName + ", Lecture Type: " + lectureType);

        return attendanceRef.get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<AttendanceRecord> records = new ArrayList<>();
                DataSnapshot courseSnapshot = task.getResult();

                // Loop through semesters
                for (DataSnapshot semesterSnapshot : courseSnapshot.getChildren()) {
                    Log.d("AttendanceReportService", "Processing semester: " + semesterSnapshot.getKey());
                    // Loop through divisions
                    for (DataSnapshot divisionSnapshot : semesterSnapshot.getChildren()) {
                        Log.d("AttendanceReportService", "Processing division: " + divisionSnapshot.getKey());
                        // Loop through student roll numbers
                        for (DataSnapshot studentSnapshot : divisionSnapshot.getChildren()) {
                            Log.d("AttendanceReportService", "Processing student: " + studentSnapshot.getKey());
                            // Loop through attendance records
                            for (DataSnapshot attendanceSnapshot : studentSnapshot.getChildren()) {
                                AttendanceRecord record = attendanceSnapshot.getValue(AttendanceRecord.class);

                                if (record != null) {
                                    Log.d("AttendanceReportService", "Checking record: " + record.getAttendanceId() +
                                            ", Subject: " + record.getSubjectName() +
                                            ", Teacher: " + record.getTeacherName() +
                                            ", Lecture Type: " + record.getLectureType());

                                    // Check if the record matches the subject name, teacher name, and lecture type
                                    if (record.getSubjectName().equals(subjectName)
                                            && record.getTeacherName().equals(teacherFullName)
                                            && record.getLectureType().equals(lectureType)) {
                                        records.add(record);
                                        Log.d("AttendanceReportService", "Record added: " + record.getAttendanceId());
                                    }
                                } else {
                                    Log.e("AttendanceReportService", "Record is null for attendance snapshot: " + attendanceSnapshot.getKey());
                                }
                            }
                        }
                    }
                }
                Log.d("AttendanceReportService", "Fetched " + records.size() + " records for subject: " + subjectName);
                return records;
            } else {
                Log.e("AttendanceReportService", "Error fetching attendance records", task.getException());
                throw task.getException();
            }
        });
    }



}
