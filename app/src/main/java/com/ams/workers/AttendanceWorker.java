package com.ams.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ams.models.AttendanceRecord;
import com.ams.models.User;
import com.ams.services.AttendanceService;
import com.ams.services.UserService;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceWorker extends Worker {
    private static final String TAG = "AttendanceWorker";

    public AttendanceWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String courseName = getInputData().getString("courseName");
        String semester = getInputData().getString("semester");
        String division = getInputData().getString("division");
        String rollNumber = getInputData().getString("rollNumber");
        String day = getInputData().getString("day");
        String lectureType = getInputData().getString("lectureType");
        String roomName = getInputData().getString("roomName");
        String subjectName = getInputData().getString("subjectName");
        String teacherName = getInputData().getString("teacherName");
        String time = getInputData().getString("time");
        String studentName = getInputData().getString("studentName");
        markAbsentees(courseName, semester, division, rollNumber,studentName, day, lectureType, roomName, subjectName, teacherName, time);
        return Result.success();
    }

    private void markAbsentees(String courseName, String semester, String division, String rollNumber,String studentName,
                               String day, String lectureType, String roomName, String subjectName,
                               String teacherName, String time) {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        AttendanceService attendanceService = new AttendanceService();

        attendanceService.hasAttendanceMarked(courseName, semester, division, rollNumber, currentDate, new AttendanceService.OnAttendanceCheckedListener() {
            @Override
            public void onChecked(boolean hasMarked) {
                if (!hasMarked) {
//                    String attendanceId = attendanceService.generateAttendanceId(); // Method to generate unique ID
                    AttendanceRecord record = new AttendanceRecord(
                            null,                  // attendanceId
                            rollNumber,           // studentRollNo
                            studentName,          // studentName (ensure you have this variable defined)
                            subjectName,          // subjectName
                            currentDate,          // date
                            time,                 // time
                            teacherName,          // teacherName
                            "Absent",             // status
                            day,                  // day
                            roomName,             // roomName
                            courseName,           // courseName
                            semester,             // semester
                            division,             // division
                            lectureType           // lectureType
                    );


                    attendanceService.markAttendance(record, new AttendanceService.OnAttendanceMarkedListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Attendance marked as absent for roll number: " + rollNumber);
                        }

                        @Override
                        public void onFailure() {
                            Log.e(TAG, "Failed to mark attendance for roll number: " + rollNumber);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to check attendance for roll number: " + rollNumber + ": " + e.getMessage());
            }
        });
    }
}

