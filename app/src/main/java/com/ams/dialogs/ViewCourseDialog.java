package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.ams.R;
import com.ams.models.Course;

public class ViewCourseDialog extends DialogFragment {

    private Course course;

    // Factory method to create a new instance of the dialog
    public static ViewCourseDialog newInstance(Course course) {
        ViewCourseDialog dialog = new ViewCourseDialog();
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

        // Create dialog using custom layout
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view_course, null);
        dialog.setContentView(view);

        TextView tvCourseName = view.findViewById(R.id.tvCourseName);
        TextView tvSemester = view.findViewById(R.id.tvSemester);
        TextView tvDivision = view.findViewById(R.id.tvDivision);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);
        // Populate fields with course details
        tvCourseName.setText(course.getCourseName());
        tvSemester.setText(course.getSemester());
        tvDivision.setText(course.getDivision());
        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }
}
