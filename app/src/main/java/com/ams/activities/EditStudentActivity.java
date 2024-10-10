package com.ams.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ams.R;
import com.ams.models.User;
import com.ams.services.UserService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditStudentActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextClass, editTextRollNo;
    private Button buttonSave;
    private UserService userService;
    private User student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextClass = findViewById(R.id.editTextClass);
        editTextRollNo = findViewById(R.id.editTextRollNo);
        buttonSave = findViewById(R.id.buttonSave);

        userService = new UserService();

        // Retrieve the User object from the intent
        student = (User) getIntent().getSerializableExtra("student");

        if (student != null) {
            // Populate the fields with existing student data
            editTextFullName.setText(student.getFullName());
            editTextClass.setText(student.getCourseName());
            editTextRollNo.setText(student.getRollNumber());

            buttonSave.setOnClickListener(v -> saveStudent());
        } else {
            Toast.makeText(this, "No student data found", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveStudent() {
        String fullName = editTextFullName.getText().toString().trim();
        String userClass = editTextClass.getText().toString().trim();
        String rollNumber = editTextRollNo.getText().toString().trim();

        if (fullName.isEmpty() || userClass.isEmpty() || rollNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update student details
        student.setFullName(fullName);
        student.setCourseName(userClass);
        student.setRollNumber(rollNumber);

        // Save updated student data to Firebase
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("users").child(student.getFullName());
        studentRef.setValue(student).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditStudentActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            } else {
                Toast.makeText(EditStudentActivity.this, "Failed to update student", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
