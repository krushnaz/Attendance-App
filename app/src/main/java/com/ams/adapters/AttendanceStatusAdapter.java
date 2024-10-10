package com.ams.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.models.AttendanceRecord;

import java.util.List;

public class AttendanceStatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_RECORD = 1;
    private static final int VIEW_TYPE_EMPTY = 0;

    private List<AttendanceRecord> attendanceRecords;
    private Context context;
    private OnStatusClickListener statusClickListener;

    public interface OnStatusClickListener {
        void onStatusClick(AttendanceRecord record);
    }

    public static class AttendanceStatusViewHolder extends RecyclerView.ViewHolder {
        public TextView tvRollNo, tvStudentName, tvSubject, tvLecture, tvStatus;

        public AttendanceStatusViewHolder(View itemView) {
            super(itemView);
            tvRollNo = itemView.findViewById(R.id.tv_roll_no);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvLecture = itemView.findViewById(R.id.tv_lecture);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvEmptyMessage;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            tvEmptyMessage = itemView.findViewById(R.id.tv_empty_message); // Add this TextView in the empty layout
        }
    }

    public AttendanceStatusAdapter(Context context, List<AttendanceRecord> attendanceRecords, OnStatusClickListener statusClickListener) {
        this.context = context;
        this.attendanceRecords = attendanceRecords;
        this.statusClickListener = statusClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_RECORD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false); // Empty state layout
            return new EmptyViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_status, parent, false); // Normal record layout
            return new AttendanceStatusViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AttendanceStatusViewHolder) {
            AttendanceRecord record = attendanceRecords.get(position);
            AttendanceStatusViewHolder recordHolder = (AttendanceStatusViewHolder) holder;

            recordHolder.tvRollNo.setText(String.valueOf(record.getStudentRollNo()));
            recordHolder.tvStudentName.setText(record.getStudentName());
            recordHolder.tvSubject.setText(record.getSubjectName());
            recordHolder.tvLecture.setText(record.getLectureType());
            String status = record.getStatus();
            recordHolder.tvStatus.setText(status);

            int color;
            if ("Present".equalsIgnoreCase(status)) {
                color = R.color.colorPrimaryDark;
            } else if ("Absent".equalsIgnoreCase(status)) {
                color = R.color.red;
            } else {
                color = R.color.grey;
            }
            recordHolder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, color));

            recordHolder.tvStatus.setOnClickListener(v -> {
                if (statusClickListener != null) {
                    statusClickListener.onStatusClick(record);
                }
            });
        } else if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyHolder = (EmptyViewHolder) holder;
            emptyHolder.tvEmptyMessage.setText("No attendance records found.");
        }
    }

    @Override
    public int getItemCount() {
        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            return 1; // One item for empty view
        } else {
            return attendanceRecords.size();
        }
    }

    // Method to update the data
    public void updateData(List<AttendanceRecord> newRecords) {
        this.attendanceRecords = newRecords;
        notifyDataSetChanged();
    }
}
