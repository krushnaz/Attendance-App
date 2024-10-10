package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.activities.ManageScheduleActivity;
import com.ams.models.Subject;
import com.ams.services.SubjectService;

public class SubjectEditDialog extends DialogFragment {

    private Subject subject;
    private SubjectService subjectService;
    private EditText etSubjectName, etSubjectCode;

    public static SubjectEditDialog newInstance(Subject subject) {
        SubjectEditDialog dialog = new SubjectEditDialog();
        Bundle args = new Bundle();
        args.putSerializable("subject", subject);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subject = getArguments().getParcelable("subject");
        subjectService = new SubjectService(); // Ensure you initialize this properly in your context
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_subject, null);

        etSubjectName = view.findViewById(R.id.etSubjectName);
        etSubjectCode = view.findViewById(R.id.etSubjectCode);
        Button btnSave = view.findViewById(R.id.btnSave);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        if (subject != null) {
            etSubjectName.setText(subject.getSubjectName());
            etSubjectCode.setText(subject.getSubjectCode());
        }

        btnSave.setOnClickListener(v -> saveSubject());
        btnCancel.setOnClickListener(v -> dismiss()); // Close the dialog

        builder.setView(view);

        return builder.create();
    }

    private void saveSubject() {
        String newName = etSubjectName.getText().toString().trim();
        String code = etSubjectCode.getText().toString().trim();

        if (newName.isEmpty() || code.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String oldName = subject.getSubjectName();
        String username = getStoredUsername();

        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "You need to log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if subject name has changed
        if (!newName.equals(oldName)) {
            // Delete the old entry
            subjectService.deleteSubject(username, oldName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Set new values and add new entry
                            subject.setSubjectName(newName);
                            subject.setSubjectCode(code);
                            addNewSubject(username, subject);
                        } else {
                            Toast.makeText(getContext(), "Update failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // If name hasn't changed, just update the existing entry
            subject.setSubjectCode(code);
            updateExistingSubject(username, subject);
        }
    }

    private void updateExistingSubject(String username, Subject subject) {
        subjectService.updateSubject(username, subject.getSubjectName(), subject)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Subject updated", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Update failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addNewSubject(String username, Subject subject) {
        subjectService.updateSubject(username, subject.getSubjectName(), subject)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Subject updated", Toast.LENGTH_SHORT).show();
                        navigateToManageScheduleActivity(); // Navigate back to refresh
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Update failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getStoredUsername() {
        return getActivity().getSharedPreferences("app_prefs", getActivity().MODE_PRIVATE)
                .getString("username", null);
    }

    private void navigateToManageScheduleActivity() {
        // Assuming ManageScheduleActivity is the activity that manages the schedule
        Intent intent = new Intent(getActivity(), ManageScheduleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
