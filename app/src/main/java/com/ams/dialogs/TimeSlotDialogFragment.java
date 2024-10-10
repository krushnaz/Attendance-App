package com.ams.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.models.Subject;
import com.ams.models.TimeSlot;
import com.ams.services.SubjectService;
import com.ams.services.TimeSlotService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class TimeSlotDialogFragment extends DialogFragment {

    public interface TimeSlotDialogListener {
        void onDialogSaveClick(int rowId, String startTime, String endTime);
        void onDialogDeleteClick(int rowId);
    }

    private TimeSlotDialogListener listener;
    private TextInputEditText etStartTime, etEndTime;
    private Button btnSave, btnDelete;
    private ImageButton btnCancel;
    private TimeSlotService timeSlotService;
    private int rowId;
    private String courseName;
    private String semester;
    private String division;
    private SubjectService subjectService;
    public static TimeSlotDialogFragment newInstance(int rowId) {
        TimeSlotDialogFragment fragment = new TimeSlotDialogFragment();
        Bundle args = new Bundle();
        args.putInt("rowId", rowId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rowId = getArguments().getInt("rowId");
            courseName = getArguments().getString("courseName");
            semester = getArguments().getString("semester");
            division = getArguments().getString("division");
            Log.d("TimeSlotDialogFragment", "rowId : "+rowId+ " CourseName" + courseName + " Semester: " + semester + "Division " + division);

        }
        timeSlotService = new TimeSlotService();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_time_slot, null);

        builder.setView(view);

        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnCancel = view.findViewById(R.id.btnCancel);

        subjectService = new SubjectService();


        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        btnDelete.setVisibility(View.GONE); // Show delete button if time slot exists
        fetchTimeSlotDetails();

        btnSave.setOnClickListener(v -> {
            String startTime = etStartTime.getText().toString();
            String endTime = etEndTime.getText().toString();
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                TimeSlot timeSlot = new TimeSlot(rowId, startTime, endTime);

                timeSlotService.saveOrUpdateTimeSlot(timeSlot,courseName,semester,division, new TimeSlotService.SaveOrUpdateCallback() {
                    @Override
                    public void onSuccess(String id) {
                        // Once the time slot is saved, update all subjects in that row
                        if (listener != null) {
                            listener.onDialogSaveClick(rowId, startTime, endTime);
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Failed to save/update time slot", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnDelete.setOnClickListener(v -> {
            timeSlotService.deleteTimeSlot(rowId,courseName,semester,division, new TimeSlotService.DeleteCallback() {
                @Override
                public void onSuccess() {
                    if (listener != null) {
                        listener.onDialogDeleteClick(rowId);
                    }
                    dismiss();
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(), "Failed to delete time slot", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return builder.create();
    }

    private void showTimePicker(final TextInputEditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        boolean isAM = calendar.get(Calendar.AM_PM) == Calendar.AM;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    // Determine AM/PM based on hourOfDay
                    String period = hourOfDay >= 12 ? "PM" : "AM";
                    // Convert hour to 12-hour format
                    int displayHour = hourOfDay % 12;
                    if (displayHour == 0) {
                        displayHour = 12; // Adjust for 12 AM/PM
                    }
                    String time = String.format("%02d:%02d %s", displayHour, minuteOfHour, period);
                    timeInput.setText(time);
                },
                hour, minute, false // Set 'false' for 12-hour format
        );

        timePickerDialog.show();
    }


    private void fetchTimeSlotDetails() {
        timeSlotService.getTimeSlotByRowId(rowId,courseName,semester,division, new TimeSlotService.TimeSlotCallback() {
            @Override
            public void onSuccess(TimeSlot timeSlot) {
                if (timeSlot != null) {
                    etStartTime.setText(timeSlot.getStartTime());
                    etEndTime.setText(timeSlot.getEndTime());
                    btnDelete.setVisibility(View.VISIBLE); // Show delete button if time slot exists
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Failed to fetch time slot details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TimeSlotDialogListener) {
            listener = (TimeSlotDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TimeSlotDialogListener");
        }
    }



}
