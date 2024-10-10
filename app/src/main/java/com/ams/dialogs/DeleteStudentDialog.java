package com.ams.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ams.R;
import com.ams.models.User;
import com.ams.services.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class DeleteStudentDialog extends Dialog {

    private final User student;
    private final UserService userService;
    private final Context context;

    public DeleteStudentDialog(@NonNull Context context, User student, UserService userService) {
        super(context, R.style.CustomDialogTheme); // Apply custom dialog theme here
        this.context = context;
        this.student = student;
        this.userService = userService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_student);

        // Set up the dialog views
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        // Set title and confirmation message
        tvTitle.setText("Delete Student");
        tvConfirmationMessage.setText("Are you sure you want to delete " + student.getFullName() + "?");

        // Handle delete button click
        btnDelete.setOnClickListener(v -> {
            userService.deleteUserByRollNo(student.getRollNumber())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete student", Toast.LENGTH_SHORT).show();
                        }
                        dismiss(); // Close the dialog
                    });
        });

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> dismiss()); // Close the dialog
    }
}
