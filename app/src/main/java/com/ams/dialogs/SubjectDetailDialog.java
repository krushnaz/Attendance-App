package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Subject;
import com.ams.services.TimetableService;

public class SubjectDetailDialog extends DialogFragment {

    private Subject subject;
    private String courseName;
    private String semester;
    private String division;

    public static SubjectDetailDialog newInstance(Subject subject, String courseName, String semester, String division) {
        SubjectDetailDialog dialog = new SubjectDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable("subject", subject);
        args.putString("courseName", courseName);
        args.putString("semester", semester);
        args.putString("division", division);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments() != null) {
                subject = (Subject) getArguments().getSerializable("subject");
                courseName = getArguments().getString("courseName");
                semester = getArguments().getString("semester");
                division = getArguments().getString("division");
            }


        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_subject_detail, null);

        TextView tvSubjectName = view.findViewById(R.id.tv_SubjectName);
        TextView tvSubjectCode = view.findViewById(R.id.tv_SubjectCode);
        TextView tvTeacherName = view.findViewById(R.id.tv_TeacherName);
        TextView tvStartTime = view.findViewById(R.id.tv_StartTime);
        TextView tvEndTime = view.findViewById(R.id.tv_EndTime);
        TextView tvRoom = view.findViewById(R.id.tv_Room);
        TextView tvLectureType = view.findViewById(R.id.tv_LectureType);
        TextView tvDay = view.findViewById(R.id.tv_Day);
        TextView tvDivision = view.findViewById(R.id.tv_Division);
        TextView tvActive = view.findViewById(R.id.tv_Active);
        Button btnActivate = view.findViewById(R.id.btn_Activate);
        ImageButton btnCancel = view.findViewById(R.id.btn_Cancel);

        if (subject != null) {
            tvSubjectName.setText("Subject Name: " + subject.getSubjectName());
            tvSubjectCode.setText("Subject Code: " + subject.getSubjectCode());
            tvTeacherName.setText("Teacher Name: " + subject.getTeacherName());
            tvStartTime.setText("Start Time: " + subject.getStartTime());
            tvEndTime.setText("End Time: " + subject.getEndTime());
            tvRoom.setText("Room: " + subject.getRoom());
            tvLectureType.setText("Lecture Type: " + subject.getLectureType());
            tvDay.setText("Day: " + subject.getDay());
            tvDivision.setText("Division: " + subject.getDivision());
            tvActive.setText("Active: " + subject.getActive());

            // Set the status color
            if ("Activated".equals(subject.getActive())) {
                tvActive.setText("Activated");
                btnActivate.setBackgroundColor(getResources().getColor(R.color.successColor)); // Set to green color
            } else {
                tvActive.setText("Deactivated");
                btnActivate.setBackgroundColor(getResources().getColor(R.color.red)); // Set to red color
            }
        }

        btnActivate.setOnClickListener(v -> {
            String newStatus = subject.getActive().equals("Activated") ? "Deactivated" : "Activated";
            TimetableService timetableService = new TimetableService();
            timetableService.updateSubjectActiveStatus(
                    courseName,
                    semester,
                    division,
                    subject.getRowId(),
                    subject.getDay(),
                    newStatus,
                    new TimetableService.DatabaseCallback() {
                        @Override
                        public void onSuccess() {
                            subject.setActive(newStatus);
                            Toast.makeText(getContext(), "Subject status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                            dismiss();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);

        return builder.create();
    }
}
