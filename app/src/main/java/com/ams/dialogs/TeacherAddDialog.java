package com.ams.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;  // Changed from Button to ImageButton
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Teacher;
import com.ams.services.TeacherService;

import java.io.InputStream;

public class TeacherAddDialog extends DialogFragment {

    private EditText etFullName, etUsername, etPassword, etEmail, etPhoneNumber, etDepartment;
    private Button  btnSaveTeacher;
    private TeacherService teacherService;
    private ImageButton btnCancel;  // Changed to ImageButton

    public static TeacherAddDialog newInstance() {
        return new TeacherAddDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_teacher, null);

        etFullName = view.findViewById(R.id.etFullName);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etDepartment = view.findViewById(R.id.etDepartment);
        btnSaveTeacher = view.findViewById(R.id.btnSaveTeacher);
        btnCancel = view.findViewById(R.id.btnCancel);  // Initialize as ImageButton

        teacherService = new TeacherService();

        btnCancel.setOnClickListener(v -> dismiss());  // Handle cancel button click
        btnSaveTeacher.setOnClickListener(v -> saveTeacher());

        builder.setView(view);

        Dialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        });

        return dialog;
    }

    private void saveTeacher() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || department.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Teacher teacher = new Teacher(fullName, username, password, email, phoneNumber, department);
        teacherService.addTeacher(teacher).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Teacher added successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Failed to add teacher", Toast.LENGTH_SHORT).show();
                Log.e("TeacherAddDialog", "Error adding teacher", task.getException());
            }
        });
    }
}
