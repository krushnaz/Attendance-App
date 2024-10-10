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
import com.ams.models.Teacher;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private Context context;
    private List<Teacher> teacherList;
    private OnTeacherClickListener listener;

    public interface OnTeacherClickListener {
        void onViewClick(Teacher teacher);
        void onEditClick(Teacher teacher);
        void onDeleteClick(Teacher teacher);
    }

    public TeacherAdapter(Context context, List<Teacher> teacherList, OnTeacherClickListener listener) {
        this.context = context;
        this.teacherList = teacherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);
        holder.tvSrNo.setText(String.valueOf(position + 1)); // Serial number based on position
        holder.tvTeacherName.setText(teacher.getFullName());
        holder.tvDepartment.setText(teacher.getDepartment());

        // Handle click events for the view, edit, and delete buttons
        holder.btnViewTeacher.setOnClickListener(v -> listener.onViewClick(teacher));
        holder.btnEditTeacher.setOnClickListener(v -> listener.onEditClick(teacher));
        holder.btnDeleteTeacher.setOnClickListener(v -> listener.onDeleteClick(teacher));
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {

        TextView tvSrNo, tvTeacherName, tvDepartment;
        ImageButton btnViewTeacher, btnEditTeacher, btnDeleteTeacher;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            btnViewTeacher = itemView.findViewById(R.id.btnViewTeacher);
            btnEditTeacher = itemView.findViewById(R.id.btnEditTeacher);
            btnDeleteTeacher = itemView.findViewById(R.id.btnDeleteTeacher);
        }
    }
}

