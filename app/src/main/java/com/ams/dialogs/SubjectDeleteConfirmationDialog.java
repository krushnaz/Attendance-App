package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.activities.ManageScheduleActivity;
import com.ams.models.Subject;
import com.ams.services.SubjectService;

public class SubjectDeleteConfirmationDialog extends DialogFragment {

    private Subject subject;
    private SubjectService subjectService;

    public static SubjectDeleteConfirmationDialog newInstance(Subject subject) {
        SubjectDeleteConfirmationDialog dialog = new SubjectDeleteConfirmationDialog();
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
        View view = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView tvConfirmationMessage = view.findViewById(R.id.tvConfirmationMessage);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (subject != null) {
            tvConfirmationMessage.setText("Are you sure you want to delete the subject: " + subject.getSubjectName() + "?");
        }

        btnDelete.setOnClickListener(v -> deleteSubject());
        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);

        return builder.create();
    }

    private void deleteSubject() {
        String username = getStoredUsername(); // Ensure you have a method to get the current username

        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "You need to log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        subjectService.deleteSubject(username, subject.getSubjectName())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Subject deleted", Toast.LENGTH_SHORT).show();
                        navigateToManageScheduleActivity(); // Navigate back to refresh
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getStoredUsername() {
        // Fetch the stored username from SharedPreferences or another source
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
