package com.ams.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ams.R;
import com.ams.models.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>();
    private final OnItemClickListener onItemClickListener;

    // Interface to handle click events
    public interface OnItemClickListener {
        void onViewClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onItemClick(int position);  // Add this method for item click

    }


    // Constructor
    public CourseAdapter(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Method to set new data for the adapter
    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged(); // Notify that the data has changed
    }

    // Method to get a specific course
    public Course getCourse(int position) {
        if (position >= 0 && position < courses.size()) {
            return courses.get(position);
        }
        return null; // Or handle this case as needed
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvSerialNumber.setText(String.valueOf(position + 1));
        holder.tvCourseName.setText(course.getCourseName());
        holder.tvDivision.setText(course.getDivision());


        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);  // Handle item click
            }
        });

        // Handle view button click
        holder.btnView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onViewClick(position);
            }
        });

        // Handle edit button click
        holder.btnEdit.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onEditClick(position);
            }
        });

        // Handle delete button click
        holder.btnDelete.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    // ViewHolder class
    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvSerialNumber, tvCourseName, tvDivision;
        ImageButton btnView, btnEdit, btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDivision = itemView.findViewById(R.id.tvDivision);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
