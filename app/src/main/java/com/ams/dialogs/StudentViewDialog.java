package com.ams.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.User;

public class StudentViewDialog extends DialogFragment {

    private static final String ARG_STUDENT = "student";

    public static StudentViewDialog newInstance(User student) {
        StudentViewDialog dialog = new StudentViewDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STUDENT, student);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_student, container, false);

        // Initialize views
        TextView tvStudentDetails = view.findViewById(R.id.tvStudentDetails);
        AppCompatImageButton btnCancel = view.findViewById(R.id.btnCancel);

        // Get the student object from arguments
        User student = (User) getArguments().getSerializable(ARG_STUDENT);

        // Set student details in the TextView
        if (student != null) {
            String details = "Full Name: " + student.getFullName() + "\n" +
                    "Roll Number: " + student.getRollNumber() + "\n" +
                    "Class: " + student.getCourseName();
            tvStudentDetails.setText(details);
        }

        // Set up the cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Adjust the dialog size
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
