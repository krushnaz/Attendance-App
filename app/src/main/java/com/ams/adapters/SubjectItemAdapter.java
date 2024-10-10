package com.ams.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.models.Subject;

import java.util.List;

public class SubjectItemAdapter extends RecyclerView.Adapter<SubjectItemAdapter.SubjectViewHolder> {

    public interface OnItemClickListener {
        void onViewAttendanceReport(Subject subject);
        void onDownloadAttendanceReport(Subject subject);
    }

    private List<Subject> subjectList;
    private Context context;
    private String studentRollNo;
    private OnItemClickListener listener;

    public SubjectItemAdapter(Context context, List<Subject> subjectList, String studentRollNo, OnItemClickListener listener) {
        this.context = context;
        this.subjectList = subjectList;
        this.studentRollNo = studentRollNo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        // Set data to the row
        holder.serialNumber.setText(String.valueOf(position + 1));
        holder.subjectName.setText(subject.getSubjectName());
        holder.lectureType.setText(subject.getLectureType());
        holder.teacherName.setText(subject.getTeacherName());

        // Handle Download button click
        holder.downloadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadAttendanceReport(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView serialNumber, subjectName, teacherName ,lectureType;
        ImageButton downloadButton;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            serialNumber = itemView.findViewById(R.id.tvSerialNumber);
            subjectName = itemView.findViewById(R.id.tvSubjectName);
            lectureType = itemView.findViewById(R.id.tvLectureType);
            teacherName = itemView.findViewById(R.id.tvTeacherName);
            downloadButton = itemView.findViewById(R.id.downloadIcon);
        }
    }
}
