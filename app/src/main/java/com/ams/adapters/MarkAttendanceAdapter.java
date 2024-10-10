package com.ams.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.models.Subject;

import java.util.List;

public class MarkAttendanceAdapter extends RecyclerView.Adapter<MarkAttendanceAdapter.ViewHolder> {
    private List<Subject> subjects;
    private Context context; // Store context for navigation
    private OnItemClickListener listener; // Add listener for item click

    public interface OnItemClickListener {
        void onItemClick(Subject selectedSubject); // Ensure this method matches your implementation
    }

    public MarkAttendanceAdapter(Context context, List<Subject> subjects, OnItemClickListener listener) {
        this.context = context;
        this.subjects = subjects;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendace_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (subjects.isEmpty()) {
            holder.tvLectureName.setText("There are no scheduled lectures for this time.");
            holder.tvLectureNumber.setVisibility(View.GONE);
            holder.tvSubjectName.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
        } else {
            Subject subject = subjects.get(position);
            holder.bind(subject);
        }
    }

    @Override
    public int getItemCount() {
        return subjects.isEmpty() ? 1 : subjects.size(); // Return 1 if subjects is empty
    }

    public void updateSubjects(List<Subject> newSubjects) {
        this.subjects = newSubjects;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLectureNumber;
        private TextView tvLectureName;
        private TextView tvSubjectName;
        private TextView tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLectureNumber = itemView.findViewById(R.id.tvLectureNumber);
            tvLectureName = itemView.findViewById(R.id.tvLectureName);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(final Subject subject) {
            tvLectureNumber.setVisibility(View.VISIBLE);
            tvSubjectName.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.VISIBLE);

            tvLectureNumber.setText(String.valueOf(subject.getRowId()));
            tvLectureName.setText(subject.getSubjectName());
            tvSubjectName.setText(subject.getLectureType());
            tvTime.setText(subject.getStartTime() + "-" + subject.getEndTime());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(subject);
                    }
                }
            });
        }
    }
}
