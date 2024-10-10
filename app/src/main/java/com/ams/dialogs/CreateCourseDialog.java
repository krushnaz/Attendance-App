package com.ams.dialogs;

import android.app.Dialog;
import android.content.Context;
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
import com.ams.services.CourseService;
import com.google.android.material.button.MaterialButton;

public class CreateCourseDialog extends DialogFragment {

    private EditText etCourseName, etSemester, etDivision;
    private MaterialButton btnSaveCourse;
    private ImageButton btnCancel;
    private CourseService courseService;

    // Constructor should not pass Context directly
    public CreateCourseDialog(CourseService courseService) {
        this.courseService = courseService;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create a dialog using the custom layout
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_course, null);
        dialog.setContentView(view);

        // Initialize the views
        etCourseName = view.findViewById(R.id.etCourseName);
        etSemester = view.findViewById(R.id.etSemester);
        etDivision = view.findViewById(R.id.etDivision);
        btnSaveCourse = view.findViewById(R.id.btnSaveCourse);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Handle the cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        // Handle save button click
        btnSaveCourse.setOnClickListener(v -> {
            String courseName = etCourseName.getText().toString().trim();
            String semester = etSemester.getText().toString().trim();
            String division = etDivision.getText().toString().trim();

            if (courseName.isEmpty() || semester.isEmpty() || division.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save course using CourseService
                courseService.saveCourse(courseName, semester, division, new CourseService.OnCourseSavedListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Course created successfully", Toast.LENGTH_SHORT).show();
                        dismiss();  // Close dialog on success
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to create course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return dialog;
    }
}
