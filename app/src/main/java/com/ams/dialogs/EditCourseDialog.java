package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.ams.R;
import com.ams.models.Course;
import com.ams.services.CourseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditCourseDialog extends DialogFragment {

    private Course course;
    private CourseService courseService;

    // Factory method to create a new instance of the dialog
    public static EditCourseDialog newInstance(Course course) {
        EditCourseDialog dialog = new EditCourseDialog();
        Bundle args = new Bundle();
        args.putSerializable("course", course);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve arguments
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable("course");
        }

        if (course == null) {
            Toast.makeText(getContext(), "Course data is missing", Toast.LENGTH_SHORT).show();
            dismiss();
            return super.onCreateDialog(savedInstanceState);
        }

        // Initialize CourseService
        courseService = new CourseService();

        // Create dialog using custom layout
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_course, null);
        dialog.setContentView(view);

        TextInputEditText etCourseName = view.findViewById(R.id.editTextCourseName);
        TextInputEditText etSemester = view.findViewById(R.id.editTextSemester);
        TextInputEditText etDivision = view.findViewById(R.id.editTextDivision);
        MaterialButton btnSave = view.findViewById(R.id.buttonSaveCourse);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        // Ensure the views are not null before setting text
        if (etCourseName != null) {
            etCourseName.setText(course.getCourseName());
        }
        if (etSemester != null) {
            etSemester.setText(course.getSemester());
        }
        if (etDivision != null) {
            etDivision.setText(course.getDivision());
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String courseName = etCourseName.getText().toString().trim();
            String semester = etSemester.getText().toString().trim();
            String division = etDivision.getText().toString().trim();

            if (courseName.isEmpty() || semester.isEmpty() || division.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Update course
                if (courseService != null) {
                    courseService.updateCourse(course.getCourseId(), courseName, semester, division, new CourseService.OnCourseSavedListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Course updated successfully", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to update course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "CourseService is not initialized", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return dialog;
    }
}
