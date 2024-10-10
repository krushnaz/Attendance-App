package com.ams.dialogs;

import android.app.Dialog;
import android.content.Context;
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
import com.ams.models.AttendanceRecord;
import com.ams.services.AttendanceService;

public class ViewStudentAttendaceDialog extends DialogFragment {

    private AttendanceRecord attendanceRecord;
    private AttendanceService attendanceService;

    public static ViewStudentAttendaceDialog newInstance(AttendanceRecord attendanceRecord) {
        ViewStudentAttendaceDialog dialog = new ViewStudentAttendaceDialog();
        Bundle args = new Bundle();
        args.putSerializable("attendance_record", attendanceRecord); // Use a Parcelable or Serializable object
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        attendanceRecord = (AttendanceRecord) getArguments().getSerializable("attendance_record");
        attendanceService = new AttendanceService();

        Dialog dialog = new Dialog(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view_student_attendace, null);
        dialog.setContentView(view);

        // Initialize UI elements
        TextView tvStudentName = view.findViewById(R.id.tv_student_name);
        TextView tvRollNo = view.findViewById(R.id.tv_student_roll);
        TextView tvSubjectName = view.findViewById(R.id.tv_subject_name);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvTeacherName = view.findViewById(R.id.tv_teacher_name);
        TextView tvStatus = view.findViewById(R.id.tv_status);
        TextView tvClassName = view.findViewById(R.id.tv_class_name);
        TextView tvRoomName = view.findViewById(R.id.tv_room_name);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        // Set data to the views
        tvStudentName.setText("Student Name: " + attendanceRecord.getStudentName());
        tvRollNo.setText("Roll No: " + attendanceRecord.getStudentRollNo());
        tvSubjectName.setText("Subject: " + attendanceRecord.getSubjectName());
        tvDate.setText("Date: " + attendanceRecord.getDate());
        tvTime.setText("Time: " + attendanceRecord.getTime());
        tvTeacherName.setText("Teacher: " + attendanceRecord.getTeacherName());
        tvStatus.setText("Status: " + attendanceRecord.getStatus());
        tvClassName.setText("Class: " + attendanceRecord.getCourseName());
        tvRoomName.setText("Room: " + attendanceRecord.getRoomName());

        btnCancel.setOnClickListener(v -> dismiss());
        return dialog;
    }
}
