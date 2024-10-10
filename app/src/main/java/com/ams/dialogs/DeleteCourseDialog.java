package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.ams.R;
import com.ams.models.Course;
import com.ams.services.CourseService;

public class DeleteCourseDialog extends DialogFragment {

    private Course course;
    private OnCourseDeletedListener listener;
    private CourseService courseService;

    // Factory method to create a new instance of the dialog
    public static DeleteCourseDialog newInstance(Course course, OnCourseDeletedListener listener) {
        DeleteCourseDialog dialog = new DeleteCourseDialog();
        Bundle args = new Bundle();
        args.putSerializable("course", course);
        dialog.setArguments(args);
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve arguments
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable("course");
        }

        // Create dialog using custom layout
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_delete_course, null);
        dialog.setContentView(view);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button buttonDelete = view.findViewById(R.id.buttonDelete);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        // Set the message
        tvMessage.setText("Are you sure you want to delete the course: " + course.getCourseName() + "?");

        // Handle the cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        // Handle delete button click
        buttonDelete.setOnClickListener(v -> {
            if (course != null) {
                courseService = new CourseService();
                courseService.deleteCourse(course.getCourseId(), new CourseService.OnCourseDeletedListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Course deleted successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to delete course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onFailure(e);
                        }
                    }
                });
            }
        });

        return dialog;
    }

    public interface OnCourseDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
