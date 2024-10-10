package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Teacher;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TeacherDetailDialog extends DialogFragment {

    private Teacher teacher;

    public static TeacherDetailDialog newInstance(Teacher teacher) {
        TeacherDetailDialog dialog = new TeacherDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable("teacher", teacher); // Use Serializable
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teacher = (Teacher) getArguments().getSerializable("teacher"); // Use Serializable
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_teacher_detail, null);

        // Find views and set data
        TextView tvFullName = view.findViewById(R.id.tvFullName);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        TextView tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        TextView tvDepartment = view.findViewById(R.id.tvDepartment);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        if (teacher != null) {
            tvFullName.setText("Full Name: " + teacher.getFullName());
            tvUsername.setText("Username: " + teacher.getUsername());
            tvEmail.setText("Email: " + teacher.getEmail());
            tvPhoneNumber.setText("Phone Number: " + teacher.getPhoneNumber());
            tvDepartment.setText("Department: " + teacher.getDepartment());
        }

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> dismiss());

        // Create and return the dialog
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialogTheme);
        dialog.setContentView(view);
        dialog.setCancelable(true); // Allows the dialog to be dismissed by tapping outside

        return dialog;
    }
}
