package com.ams.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.dialogs.ViewStudentAttendaceDialog;
import com.ams.models.AttendanceRecord;

import java.util.List;

public class StudentAttendanceStatusAdapter extends RecyclerView.Adapter<StudentAttendanceStatusAdapter.AttendanceViewHolder> {

    private List<AttendanceRecord> attendanceList;
    private Context context; // Add context to the adapter
    private AttendanceStatusAdapter.OnStatusClickListener statusClickListener;


    public interface OnStatusClickListener {
        void onStatusClick(AttendanceRecord record);
    }
    // Pass the context to the constructor
    public StudentAttendanceStatusAdapter(Context context, List<AttendanceRecord> attendanceList, AttendanceStatusAdapter.OnStatusClickListener statusClickListener) {
        this.context = context;
        this.attendanceList = attendanceList;
        this.statusClickListener = statusClickListener;

    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_status, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceList.get(position);

        // Ensure that all text views are correctly initialized
        if (holder.rollNo != null) {
            holder.rollNo.setText(String.valueOf(record.getStudentRollNo()));
        }
        if (holder.studentName != null) {
            holder.studentName.setText(record.getStudentName());
        } else {
            Log.d("StudentAttendanceStatusAdapter", "studentName is null");
        }
        if (holder.subjectName != null) {
            holder.subjectName.setText(record.getSubjectName());
        }

        if (holder.lectureName != null) {
            holder.lectureName.setText(record.getLectureType());
        }

        // Set status and background color based on attendance status
        String status = record.getStatus();
        if (holder.status != null) {
            holder.status.setText(status);

            // Determine the background color based on the status value
            int colorResId;
            if ("Present".equalsIgnoreCase(status)) {
                colorResId = R.color.colorPrimaryDark; // Set this to your desired green color resource
            } else if ("Absent".equalsIgnoreCase(status)) {
                colorResId = R.color.red; // Set this to your desired red color resource
            } else {
                colorResId = R.color.grey; // Set this to your desired grey color resource
            }

            // Apply the background color using ContextCompat to support older devices
            holder.status.setBackgroundColor(ContextCompat.getColor(context, colorResId));

            // Add click listener to status
            holder.status.setOnClickListener(v -> {
                if (statusClickListener != null) {
                    statusClickListener.onStatusClick(record);
                }
            });;
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void updateAttendanceList(List<AttendanceRecord> newAttendanceList) {
        this.attendanceList = newAttendanceList;
        notifyDataSetChanged();
    }
    static class AttendanceViewHolder extends RecyclerView.ViewHolder {

        TextView rollNo, studentName, subjectName, lectureName, status;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            rollNo = itemView.findViewById(R.id.tv_roll_no);
            studentName = itemView.findViewById(R.id.tv_student_name);
            subjectName = itemView.findViewById(R.id.tv_subject);
            lectureName = itemView.findViewById(R.id.tv_lecture);
            status = itemView.findViewById(R.id.tv_status);
        }
    }
}
