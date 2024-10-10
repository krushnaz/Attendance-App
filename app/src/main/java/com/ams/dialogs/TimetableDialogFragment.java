package com.ams.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Subject;
import com.ams.services.TimetableService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimetableDialogFragment extends DialogFragment {

    public interface TimetableDialogListener {
        void onDialogSaveClick(String action, String entryId);
        void onDialogDeleteClick(String entryId, String day);
        void onTimetableDialogPositiveClick(String day, String entryId, Subject subject);
        void onTimetableDialogNegativeClick();
    }

    private TimetableDialogListener listener;
    private String action;
    private EditText etSubjectName;
    private EditText etSubjectCode;
    private Spinner spTeacherName;
    private Spinner spRoomName;
    private Spinner spLectureType;
    private Button btnDelete;
    private Button btnSave;
    private ImageButton btnCancel;
    private String entryId;
    private String courseName;
    private String semester;
    private String division;
    private String day;
    private String startTime;
    private String endTime;
    private TimetableService timetableService;
    private int rowId;

    public static TimetableDialogFragment newInstance(String action, String courseName,
                                                      String semester, String division, String day, String entryId,
                                                      String startTime, String endTime, int rowId) {
        TimetableDialogFragment fragment = new TimetableDialogFragment();
        Bundle args = new Bundle();
        args.putString("action", action);
        args.putString("courseName", courseName);
        args.putString("semester", semester);
        args.putString("division", division);
        args.putString("day", day);
        args.putString("entryId", entryId);
        args.putString("startTime", startTime);
        args.putString("endTime", endTime);
        args.putInt("rowId", rowId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        action = getArguments().getString("action");
        courseName = getArguments().getString("courseName");
        semester = getArguments().getString("semester");
        division = getArguments().getString("division");
        day = getArguments().getString("day");
        entryId = getArguments().getString("entryId");
        startTime = getArguments().getString("startTime");
        endTime = getArguments().getString("endTime");
        rowId = getArguments().getInt("rowId");
        timetableService = new TimetableService();

        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_timetable, null);
        dialog.setContentView(view);

        etSubjectName = view.findViewById(R.id.etSubjectName);
        etSubjectCode = view.findViewById(R.id.etSubjectCode);
        spTeacherName = view.findViewById(R.id.spTeacherName);
        spRoomName = view.findViewById(R.id.spRoomName);
        spLectureType = view.findViewById(R.id.spLectureType);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete.setVisibility(View.GONE); // Hide delete button if no entryId
        setupSpinners();

        if (entryId != null) {
            fetchSubjectDetails();
        } else {
            btnDelete.setVisibility(View.GONE); // Hide delete button if no entryId
        }

        btnSave.setOnClickListener(v -> {
            String subjectName = etSubjectName.getText().toString();
            String subjectCode = etSubjectCode.getText().toString();
            String teacherName = spTeacherName.getSelectedItem().toString();
            String roomName = spRoomName.getSelectedItem().toString();
            String lectureType = spLectureType.getSelectedItem().toString();

            Subject subject = new Subject(subjectName, subjectCode, teacherName, startTime, endTime, roomName, lectureType, rowId, day,"Deactivated",division);

            timetableService.saveOrUpdateEntry(courseName, semester, division, entryId, day, subject, new TimetableService.SaveOrUpdateCallback() {
                @Override
                public void onSuccess(String updatedEntryId) {
                    listener.onTimetableDialogPositiveClick(day, updatedEntryId, subject);
                    dismiss();
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failure (e.g., show a message to the user)
                }
            });
        });

        btnDelete.setOnClickListener(v -> {
            if (entryId != null) {
                timetableService.deleteEntry(courseName, semester, division, day, entryId, new TimetableService.DeleteEntryCallback() {
                    @Override
                    public void onSuccess(String deletedEntryId) {
                        Log.d("TimetableDialogFragment", "Course Name: " + courseName + " Semester: " + semester + " Division: " + division + " Day: " + day + " entryId: " + entryId);
                        listener.onDialogDeleteClick(deletedEntryId, day);
                        dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure (e.g., show a message to the user)
                    }
                });
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void setupSpinners() {
        // Set up Teacher Spinner
        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference("teachers");
        teacherRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> teacherNames = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String fullName = data.child("fullName").getValue(String.class);
                    if (fullName != null) {
                        teacherNames.add(fullName);
                    }
                }
                // Create an ArrayAdapter using the custom selected item layout
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.spinner_selected_item, teacherNames) {
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        // Use the custom dropdown item layout
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        textView.setTextColor(getResources().getColor(R.color.black));  // Ensure dropdown text color is black
                        return view;
                    }
                };

// Set the custom dropdown layout for the items when the dropdown is opened
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Set the adapter to the Spinner
                spTeacherName.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });

        // Set up Room Spinner
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms");
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> roomNames = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String roomName = data.child("roomName").getValue(String.class);
                    if (roomName != null) {
                        roomNames.add(roomName);
                    }
                }
                if (isAdded()) {
                    // Create an ArrayAdapter using the custom selected item layout
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.spinner_selected_item, roomNames) {
                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            // Use the custom dropdown item layout
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            // Set the text color for dropdown items (black in this case)
                            textView.setTextColor(getResources().getColor(R.color.black));
                            return view;
                        }
                    };

// Set the custom dropdown layout
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Set the adapter to the Spinner
                    spRoomName.setAdapter(adapter);

                } else {
                    Log.e("FragmentError", "Fragment is not attached to context.");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });

        // Set up Lecture Type Spinner
        // Create an ArrayAdapter using the custom selected item layout
        ArrayAdapter<CharSequence> lectureTypeAdapter = new ArrayAdapter<CharSequence>(requireContext(), R.layout.spinner_selected_item, getResources().getTextArray(R.array.lecture_types_array)) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                // Use the custom dropdown item layout
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Set the text color for dropdown items (black in this case)
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };

// Set the custom dropdown layout
        lectureTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Set the adapter to the Spinner
        spLectureType.setAdapter(lectureTypeAdapter);

    }

    private void fetchSubjectDetails() {
        Log.d("TimetableDialogFragment", "Fetching subject details for entryId: " + entryId+" and day: "+day);

        timetableService.getTimetableById(courseName, semester, division, day, entryId, new TimetableService.TimetableCallbackFetch() {
            @Override
            public void onSuccess(Map<String, Subject> entries) {
                Log.d("TimetableDialogFragment", "Data fetched successfully. Entries count: " + entries.size());

                String key = day+entryId;
                Subject subject = entries.get(key);
                if (subject != null) {
                    Log.d("TimetableDialogFragment", "Subject found: " + subject.toString());

                    etSubjectName.setText(subject.getSubjectName());
                    etSubjectCode.setText(subject.getSubjectCode());
                    setSpinnerSelection(spTeacherName, subject.getTeacherName());
                    setSpinnerSelection(spRoomName, subject.getRoom());
                    setSpinnerSelection(spLectureType, subject.getLectureType());

                    btnDelete.setVisibility(View.VISIBLE); // Show delete button if subject exists
                } else {
                    Log.d("TimetableDialogFragment", "No subject found for entryId: " + entryId);
                    btnDelete.setVisibility(View.GONE); // Hide delete button if no subject found
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TimetableDialogFragment", "Failed to fetch subject details: " + e.getMessage(), e);
            }
        });
    }


    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        } else {
            // Handle case where value is not found
            Log.w("TimetableDialogFragment", "Value not found in spinner: " + value);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (TimetableDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TimetableDialogListener");
        }
    }
}

