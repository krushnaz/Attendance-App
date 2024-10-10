package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.User;
import com.ams.services.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditStudentDialog extends DialogFragment {

    private User student;
    private UserService userService;

    public static EditStudentDialog newInstance(User student) {
        EditStudentDialog dialog = new EditStudentDialog();
        Bundle args = new Bundle();
        args.putSerializable("student", student);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        userService = new UserService();
        student = (User) getArguments().getSerializable("student");

        // Create and customize dialog
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_edit_student);
        dialog.setCancelable(true);

        // Initialize views
        TextInputEditText editTextFullName = dialog.findViewById(R.id.editTextFullName);
        TextInputEditText editTextClass = dialog.findViewById(R.id.editTextClass);
        TextInputEditText editTextRollNo = dialog.findViewById(R.id.editTextRollNo);
        MaterialButton buttonSave = dialog.findViewById(R.id.buttonSave);
        View btnCancel = dialog.findViewById(R.id.btnCancel);

        // Populate fields with student data
        if (student != null) {
            editTextFullName.setText(student.getFullName());
            editTextClass.setText(student.getCourseName());
            editTextRollNo.setText(student.getRollNumber());
        }

        // Set up listeners
        buttonSave.setOnClickListener(v -> saveStudent(editTextFullName, editTextClass, editTextRollNo));
        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void saveStudent(TextInputEditText editTextFullName, TextInputEditText editTextClass, TextInputEditText editTextRollNo) {
        String fullName = editTextFullName.getText().toString().trim();
        String userClass = editTextClass.getText().toString().trim();
        String rollNumber = editTextRollNo.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(userClass) || TextUtils.isEmpty(rollNumber)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Student updated successfully", Toast.LENGTH_SHORT).show();
                dismiss(); // Close the dialog
            } else {
                Toast.makeText(getContext(), "Failed to update student", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
