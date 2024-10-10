package com.ams.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ams.R;
import com.ams.models.Teacher;
import com.ams.services.TeacherService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class EditTeacherActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etEmail, etPhoneNumber, etDepartment, etPassword;
    private Button btnSaveTeacher;
    private TeacherService teacherService;
    private Teacher teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDepartment = findViewById(R.id.etDepartment);
        etPassword = findViewById(R.id.etPassword);
        btnSaveTeacher = findViewById(R.id.btnSaveTeacher);

        teacherService = new TeacherService();

        // Get the passed Teacher object
        teacher = (Teacher) getIntent().getSerializableExtra("teacher");

        if (teacher != null) {
            // Populate the fields with the current data
            etFullName.setText(teacher.getFullName());
            etUsername.setText(teacher.getUsername());
            etEmail.setText(teacher.getEmail());
            etPhoneNumber.setText(teacher.getPhoneNumber());
            etDepartment.setText(teacher.getDepartment());
            etPassword.setText(teacher.getPassword());
        }

        // Handle save button click
        btnSaveTeacher.setOnClickListener(v -> {
            if (validateInputs()) {
                saveTeacherDetails();
            }
        });
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
        teacherService.addTeacher(teacher).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditTeacherActivity.this, "Teacher details updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                } else {
                    Toast.makeText(EditTeacherActivity.this, "Failed to update teacher details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
