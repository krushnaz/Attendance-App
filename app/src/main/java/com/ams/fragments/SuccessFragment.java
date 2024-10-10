package com.ams.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ams.activities.StudentDashboardActivity;
import com.ams.R;

import androidx.fragment.app.Fragment;

public class SuccessFragment extends Fragment {

    private static final String TAG = "SuccessFragment";

    private TextView textSuccessMessage;
    private TextView textStudentName;
    private TextView textRollNo;
    private TextView textDateTime;
    private TextView textLectureName;
    private TextView textSubjectName;
    private TextView textClassName;

    private Button buttonGoToDashboard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");

        View view = inflater.inflate(R.layout.success_fragment, container, false);

        if (getArguments() == null) {
            textStudentName.setText(getArguments().getString("studentName", "N/A"));
            textRollNo.setText(getArguments().getString("rollNo", "N/A"));
            textDateTime.setText(getArguments().getString("dateTime", "N/A"));
            textLectureName.setText(getArguments().getString("lectureName", "N/A"));
            textSubjectName.setText(getArguments().getString("subjectName", "N/A"));
            textClassName.setText(getArguments().getString("className", "N/A"));

        }

        textSuccessMessage = view.findViewById(R.id.text_success_message);
        textStudentName = view.findViewById(R.id.text_student_name);
        textRollNo = view.findViewById(R.id.text_roll_no);
        textDateTime = view.findViewById(R.id.text_date_time);
        textLectureName = view.findViewById(R.id.text_lecture_name);
        textSubjectName = view.findViewById(R.id.text_subject_name);
        textClassName = view.findViewById(R.id.text_class_name);
        buttonGoToDashboard = view.findViewById(R.id.button_go_to_dashboard);

        buttonGoToDashboard.setOnClickListener(v -> navigateToDashboard());

        // Retrieve arguments if available
        if (getArguments() != null) {
            String studentName = getArguments().getString("studentName");
            String rollNo = getArguments().getString("rollNo");
            String dateTime = getArguments().getString("dateTime");
            String subjectName = getArguments().getString("subjectName");
            String className = getArguments().getString("className");
            String division = getArguments().getString("division");

            textStudentName.setText("Student Name: "+studentName);
            textRollNo.setText("Student Roll No: "+rollNo);
            textDateTime.setText("Date-Time: "+dateTime);
            textLectureName.setText("Division: "+division);
            textSubjectName.setText("Subject Name: "+subjectName);
            textClassName.setText("Class Name: "+className);

        }

        return view;
    }

    private void navigateToDashboard() {
        // Navigate to the Student Dashboard Activity
        Intent intent = new Intent(getActivity(), StudentDashboardActivity.class);
        startActivity(intent);
        // Optionally, you can finish this fragment's activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    public static SuccessFragment newInstance(String studentName, String rollNo, String dateTime, String lectureName, String subjectName) {

        SuccessFragment fragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putString("studentName", studentName);
        args.putString("rollNo", rollNo);
        args.putString("dateTime", dateTime);
        args.putString("lectureName", lectureName);
        args.putString("subjectName", subjectName);
        fragment.setArguments(args);
        return fragment;
    }
}

