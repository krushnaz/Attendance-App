package com.ams.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.ams.R;
import com.ams.models.User;
import com.ams.services.UserService;

public class StudentProfileEditDialog extends AppCompatDialogFragment {

    private TextView tvRollNumber;
    private EditText etFullName, etCourseName, etMobileNumber, etDivision, etSemester, etPassword;
    private Button btnSaveStudent;
    private ImageButton btnCancelStudent;
    private UserService userService;
    private User user;
    private OnStudentUpdatedListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the custom layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_student_profile, null);

        // Initialize views
//        tvRollNumber = view.findViewById(R.id.tvRollNumber);
        etFullName = view.findViewById(R.id.etFullName);
        etCourseName = view.findViewById(R.id.etCourseName);
        etMobileNumber = view.findViewById(R.id.etMobileNumber);
        etDivision = view.findViewById(R.id.etDivision);
        etSemester = view.findViewById(R.id.etSemester);
        etPassword = view.findViewById(R.id.etPassword);
        btnSaveStudent = view.findViewById(R.id.btnSave);
        btnCancelStudent = view.findViewById(R.id.btnCancel);

        userService = new UserService();

        // Retrieve the user object from arguments
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("student");
            if (user != null) {
                // Populate the fields with the current data
//                tvRollNumber.setText("Roll Number: " + user.getRollNumber());
                etFullName.setText(user.getFullName());
                etCourseName.setText(user.getCourseName());
                etMobileNumber.setText(user.getMobileNumber());
                etDivision.setText(user.getDivision());
                etSemester.setText(user.getSemester());
                etPassword.setText(user.getPassword());
            }
        }

        // Create and configure the dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        // Set dialog size
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.85), // Width
                (int) (getResources().getDisplayMetrics().heightPixels * 0.75) // Height
        );

        // Handle Save button click
        btnSaveStudent.setOnClickListener(v -> {
            if (validateInputs()) {
                saveStudentDetails();
            }
        });

        // Handle Cancel button click
        btnCancelStudent.setOnClickListener(v -> dismiss());

        return dialog;
    }


    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Full Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etCourseName.getText().toString().trim())) {
            etCourseName.setError("Course Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etMobileNumber.getText().toString().trim())) {
            etMobileNumber.setError("Mobile Number is required");
            return false;
        }
        if (TextUtils.isEmpty(etDivision.getText().toString().trim())) {
            etDivision.setError("Division is required");
            return false;
        }
        if (TextUtils.isEmpty(etSemester.getText().toString().trim())) {
            etSemester.setError("Semester is required");
            return false;
        }
        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            etPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    private void saveStudentDetails() {
        // Update student details
        user.setFullName(etFullName.getText().toString().trim());
        user.setCourseName(etCourseName.getText().toString().trim());
        user.setMobileNumber(etMobileNumber.getText().toString().trim());
        user.setDivision(etDivision.getText().toString().trim());
        user.setSemester(etSemester.getText().toString().trim());
        user.setPassword(etPassword.getText().toString().trim());

        // Save updated student data to Firebase
        userService.updateUser(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Student details updated successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onStudentUpdated();
                }
                dismiss(); // Close the dialog
            } else {
                Toast.makeText(getActivity(), "Failed to update student details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setOnStudentUpdatedListener(OnStudentUpdatedListener listener) {
        this.listener = listener;
    }

    public interface OnStudentUpdatedListener {
        void onStudentUpdated();
    }
}
