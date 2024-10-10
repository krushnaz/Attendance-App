package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button; // Ensure Button is used
import android.widget.EditText;
import android.widget.ImageButton; // Ensure ImageButton is used
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Teacher;

public class TeacherEditDialog extends DialogFragment {

    public interface OnTeacherEditListener {
        void onTeacherEdited(Teacher teacher);
    }

    private Teacher teacher;

    public static TeacherEditDialog newInstance(Teacher teacher) {
        TeacherEditDialog dialog = new TeacherEditDialog();
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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_teacher_edit, null);

        // Initialize UI components and set up dialog
        TextView tvName = view.findViewById(R.id.tvName);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etDepartment = view.findViewById(R.id.etDepartment);
        Button btnSave = view.findViewById(R.id.btnSave); // Use Button
        ImageButton btnCancel = view.findViewById(R.id.btnCancel); // Use ImageButton

        if (teacher != null) {
            etFullName.setText(teacher.getFullName());
            etEmail.setText(teacher.getEmail());
            etDepartment.setText(teacher.getDepartment());
        }

        btnSave.setOnClickListener(v -> {
            if (teacher != null) {
                teacher.setFullName(etFullName.getText().toString());
                teacher.setEmail(etEmail.getText().toString());
                teacher.setDepartment(etDepartment.getText().toString());
                OnTeacherEditListener listener = (OnTeacherEditListener) getActivity();
                if (listener != null) {
                    listener.onTeacherEdited(teacher);
                }
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
