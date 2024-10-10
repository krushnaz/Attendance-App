package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.ams.R;
import com.ams.models.Teacher;
import com.ams.services.TeacherService;

public class TeacherProfileEditDialog extends AppCompatDialogFragment {

    private EditText etFullName, etUsername, etEmail, etPhoneNumber, etDepartment, etPassword;
    private Button btnSaveTeacher;
    private ImageButton  btnCancelTeacher;
    private TeacherService teacherService;
    private Teacher teacher;
    private OnTeacherUpdatedListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the custom layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_teacher_profile, null);

        // Initialize views
        etFullName = view.findViewById(R.id.etFullName);
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etDepartment = view.findViewById(R.id.etDepartment);
        etPassword = view.findViewById(R.id.etPassword);
        btnSaveTeacher = view.findViewById(R.id.btnSave);
        btnCancelTeacher = view.findViewById(R.id.btnCancel);

        teacherService = new TeacherService();

        // Retrieve the teacher object from arguments
        if (getArguments() != null) {
            teacher = (Teacher) getArguments().getSerializable("teacher");
            if (teacher != null) {
                // Populate the fields with the current data
                etFullName.setText(teacher.getFullName());
                etUsername.setText(teacher.getUsername());
                etEmail.setText(teacher.getEmail());
                etPhoneNumber.setText(teacher.getPhoneNumber());
                etDepartment.setText(teacher.getDepartment());
                etPassword.setText(teacher.getPassword());
            }
        }

        // Create and configure the dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        // Set dialog size
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        // Handle Save button click
        btnSaveTeacher.setOnClickListener(v -> {
            if (validateInputs()) {
                saveTeacherDetails();
            }
        });

        // Handle Cancel button click
        btnCancelTeacher.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Full Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
            etUsername.setError("Username is required");
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())) {
            etPhoneNumber.setError("Phone Number is required");
            return false;
        }
        if (TextUtils.isEmpty(etDepartment.getText().toString().trim())) {
            etDepartment.setError("Department is required");
            return false;
        }
        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            etPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    private void saveTeacherDetails() {
        // Update teacher details
        teacher.setFullName(etFullName.getText().toString().trim());
        teacher.setUsername(etUsername.getText().toString().trim());
        teacher.setEmail(etEmail.getText().toString().trim());
        teacher.setPhoneNumber(etPhoneNumber.getText().toString().trim());
        teacher.setDepartment(etDepartment.getText().toString().trim());
        teacher.setPassword(etPassword.getText().toString().trim());

        // Save updated teacher data to Firebase
        teacherService.addTeacher(teacher).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Teacher details updated successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onTeacherUpdated();
                }
                dismiss(); // Close the dialog
            } else {
                Toast.makeText(getActivity(), "Failed to update teacher details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setOnTeacherUpdatedListener(OnTeacherUpdatedListener listener) {
        this.listener = listener;
    }

    public interface OnTeacherUpdatedListener {
        void onTeacherUpdated();
    }
}
