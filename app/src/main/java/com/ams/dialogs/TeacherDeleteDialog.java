package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Teacher;
import com.ams.services.TeacherService;

public class TeacherDeleteDialog extends DialogFragment {

    private Teacher teacher;
    private TeacherService teacherService;

    public static TeacherDeleteDialog newInstance(Teacher teacher) {
        TeacherDeleteDialog dialog = new TeacherDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable("teacher", teacher);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teacher = (Teacher) getArguments().getSerializable("teacher");
        }
        teacherService = new TeacherService(); // Initialize your TeacherService
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_teacher_delete, null);

        TextView tvConfirmationMessage = view.findViewById(R.id.tvConfirmationMessage);
        if (teacher != null) {
            tvConfirmationMessage.setText("Are you sure you want to delete " + teacher.getFullName() + "?");
        }

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (teacher != null) {
                teacherService.deleteTeacherByUsername(teacher.getUsername())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Notify user of success
                                Toast.makeText(getActivity(), "Teacher deleted successfully", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                // Notify user of failure
                                Toast.makeText(getActivity(), "Failed to delete teacher", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
