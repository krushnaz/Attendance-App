package com.ams.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.models.Subject;
import com.ams.models.AttendanceRecord;
import com.ams.utils.AttendaceExcelReportGenerator;
import com.ams.services.AttendanceReportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherSubjectAdapter extends RecyclerView.Adapter<TeacherSubjectAdapter.ViewHolder> {

    private List<Subject> subjects;
    private Context context;
    private AttendanceReportService attendanceReportService;
    private String teacherFullName;
    private String courseName;
    private String lectureType;

    public TeacherSubjectAdapter(List<Subject> subjects, Context context,String teacherFullName,String courseName) {
        this.subjects = subjects;
        this.context = context;
        this.attendanceReportService = new AttendanceReportService(); // Initialize AttendanceReportService
        this.teacherFullName = teacherFullName; // Retrieve the stored username
        this.courseName = courseName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        if (subject == null) {
            Log.e("TeacherSubjectAdapter", "Subject is null at position: " + position);
            return;
        }

        holder.tvSrNo.setText(String.valueOf(position + 1));
        holder.tvSubjectName.setText(subject.getSubjectName() != null ? subject.getSubjectName() : "N/A");
        holder.tvLectureType.setText(subject.getLectureType() != null ? subject.getLectureType() : "N/A"); // Set Lecture Type
        holder.btnDownloadReport.setOnClickListener(v -> {
            lectureType = subject.getLectureType();
            if (teacherFullName != null) {
                fetchRecordsAndGenerateReport(subject.getSubjectName(),lectureType);
                Log.d("TeacherSubjectAdapter", "Fetching records for subject: " + subject.getSubjectName());
            } else {
                Log.e("TeacherSubjectAdapter", "Username is null");
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects != null ? subjects.size() : 0;
    }

    private void fetchRecordsAndGenerateReport(String subjectName, String lectureType) {
        if (subjectName == null || teacherFullName == null || courseName == null) {
            Log.e("TeacherSubjectAdapter", "One or more parameters are null");
            return;
        }

        attendanceReportService.getAttendanceRecordsByTeacherFullNameAndCourse(teacherFullName, courseName, subjectName, lectureType)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AttendanceRecord> records = task.getResult();
                        if (records != null && !records.isEmpty()) {
                            generateReport(subjectName, records);
                        } else {
                            Log.e("TeacherSubjectAdapter", "No attendance records found");
                            // Show Toast message
                            Toast.makeText(context, "No attendance records found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("TeacherSubjectAdapter", "Failed to fetch attendance records", task.getException());
                    }
                });
    }


    private void generateReport(String subjectName, List<AttendanceRecord> records) {
        // Create a map to hold the lecture status
        Map<String, String> lectureStatusMap = new HashMap<>();

        // Iterate through the records and populate the lectureStatusMap
        for (AttendanceRecord record : records) {
            String status = record.getStatus();
            // Combine the lecture number and roll number into a single key
//            String key = lectureNumber + "_" + record.getStudentRollNo();
//            lectureStatusMap.put(key, status);
        }

        // Generate the report with the collected data
        AttendaceExcelReportGenerator.generateAttendanceReport(context, records, lectureStatusMap, subjectName,lectureType);
        Log.d("TeacherSubjectAdapter", "Attendance report generated successfully for subject: " + subjectName);
    }

    private String getStoredUsername() {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString("username", null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo, tvSubjectName, tvLectureType; // Removed tvTeacherName
        ImageButton btnDownloadReport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvLectureType = itemView.findViewById(R.id.tvLectureType);
            btnDownloadReport = itemView.findViewById(R.id.downloadIcon);

            // Ensure views are properly initialized
            if (tvSrNo == null || tvSubjectName == null || tvLectureType == null || btnDownloadReport == null) {
                Log.e("TeacherSubjectAdapter", "ViewHolder views are not properly initialized");
            }
        }
    }


}
