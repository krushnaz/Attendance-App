package com.ams.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ams.R;
import com.ams.dialogs.TimeSlotDialogFragment;
import com.ams.dialogs.TimetableDialogFragment;
import com.ams.models.Course;
import com.ams.models.Subject;
import com.ams.models.TimeSlot;
import com.ams.services.TimetableService;
import com.ams.services.TimeSlotService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateTimetableActivity extends AppCompatActivity
        implements TimeSlotDialogFragment.TimeSlotDialogListener, TimetableDialogFragment.TimetableDialogListener {

    private TextView tvCourseName;
    private TextView tvSemester;
    private TextView tvDivision;
    private TableLayout tableLayout;
    private String courseName;
    private String semester;
    private String division;
    private TimeSlotService timeSlotService;
    private TimetableService timetableService;
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_timetable);

        // Initialize views
        tvCourseName = findViewById(R.id.tvCourseName);
        tvSemester = findViewById(R.id.tvSemester);
        tvDivision = findViewById(R.id.tvDivision);
        tableLayout = findViewById(R.id.tableLayout);

        // Initialize services
        timetableService = new TimetableService();
        timeSlotService = new TimeSlotService();

        // Retrieve and display the course data from the intent
        Intent intent = getIntent();
        Course course = (Course) intent.getSerializableExtra("course");
        if (course != null) {
            courseName = course.getCourseName();
            semester = course.getSemester();
            division = course.getDivision();

            tvCourseName.setText(String.format("Course Name: %s", courseName));
            tvSemester.setText(String.format("Semester: %s", semester));
            tvDivision.setText(String.format("Division: %s", division));

            // Fetch time slots and subjects
            timeSlotService.getAllTimeSlots(courseName,semester,division,new TimeSlotService.TimeSlotFetchCallback() {
                @Override
                public void onSuccess(List<TimeSlot> timeSlots) {
                    // Fetch subjects
                    timetableService.fetchSubjects(courseName, semester, division, new TimetableService.TimetableCallbackFetch() {
                        @Override
                        public void onSuccess(Map<String, Subject> entries) {
                            // Update table with fetched data
                            updateTableWithData(entries, timeSlots);
                        }

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateTimetableActivity.this, "Failed to fetch subjects: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateTimetableActivity.this, "Failed to fetch time slots: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        // Setup row click listener
        setupTableRowClickListener();
    }

    private void setupTableRowClickListener() {
        tableLayout.setOnClickListener(v -> {
            if (v instanceof TextView) {
                TextView textView = (TextView) v;
                String idName = getResources().getResourceEntryName(textView.getId());

                // Extract day and row number from the ID
                String[] parts = idName.split("(?<=\\D)(?=\\d)");
                if (parts.length == 2) {
                    String day = parts[0];
                    int rowId = Integer.parseInt(parts[1]);

                    // Fetch time slot for the rowId
                    fetchTimeSlot(rowId, day);
                }
            }
        });
    }

    private void fetchTimeSlot(int rowId, String day) {
        timeSlotService.getTimeSlotByRowId(rowId,courseName,semester,division, new TimeSlotService.TimeSlotCallback() {
            @Override
            public void onSuccess(TimeSlot timeSlot) {
                // Show the TimetableDialogFragment with the fetched data
                TimetableDialogFragment timetableDialog = TimetableDialogFragment.newInstance(
                        "edit",      // Action: "edit" for existing entries
                        courseName,
                        semester,
                        division,
                        day,
                        String.valueOf(rowId), // entryId
                        timeSlot.getStartTime(),
                        timeSlot.getEndTime(),
                        rowId
                );

                timetableDialog.show(getSupportFragmentManager(), "TimetableDialogFragment");
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateTimetableActivity.this, "Failed to fetch time slot: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void openTimeSlotDialog(View view) {
        String idName = getResources().getResourceEntryName(view.getId());
        String[] parts = idName.split("(?<=\\D)(?=\\d)");

        if (parts.length == 2) {
            String day = parts[0];
            int rowId = Integer.parseInt(parts[1]);

            // Show the TimeSlotDialogFragment for this rowId
            showTimeSlotDialogFragment(rowId);
        }
    }

    public void openInputDialog(View view) {
        String idName = getResources().getResourceEntryName(view.getId());
        String[] parts = idName.split("(?<=\\D)(?=\\d)");

        if (parts.length == 2) {
            String day = parts[0];
            int rowId = Integer.parseInt(parts[1]);

            // Show the TimetableDialogFragment
            fetchTimeSlot(rowId, day); // Assuming you want to fetch existing data first
        }
    }

    private void showTimeSlotDialogFragment(int rowId) {
        // Show the TimeSlotDialogFragment
        Bundle args = new Bundle();
        args.putInt("rowId",rowId);
        args.putString("courseName",courseName);
        args.putString("semester",semester);
        args.putString("division",division);

        Log.d("CreateTimetableActivity", "CourseName" + courseName + " Semester: " + semester + "Division " + division);

        TimeSlotDialogFragment timeSlotDialog = TimeSlotDialogFragment.newInstance(rowId);
        timeSlotDialog.setArguments(args); // Pass the bundle to the fragment
        timeSlotDialog.show(getSupportFragmentManager(), "TimeSlotDialogFragment");
    }

    private void updateTableWithData(Map<String, Subject> subjects, List<TimeSlot> timeSlots) {
        for (TimeSlot timeSlot : timeSlots) {
            int rowId = timeSlot.getRowId();
            String startTime = timeSlot.getStartTime();
            String endTime = timeSlot.getEndTime();
            Log.d("CreateTimetableActivity", "Row ID:" + rowId + " start time: " + startTime + " end time: " + endTime);

            // Find the row in the TableLayout by rowId (accounting for the header)
            TableRow row = (TableRow) tableLayout.getChildAt(rowId); // rowId adjusted to account for header

            if (row != null) {
                // Update time slot TextView
                TextView timeSlotView = row.findViewById(getResources().getIdentifier("tvTimeSlot" + rowId, "id", getPackageName()));
                if (timeSlotView != null) {
                    timeSlotView.setText(String.format("%s - %s", startTime, endTime));
                } else {
                    Log.d("CreateTimetableActivity", "Time slot TextView with ID tvTimeSlot" + rowId + " is null");
                }

                // Update day-specific TextViews
                for (String day : new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}) {
                    // Generate the key format that matches the one used in fetchSubjects
                    String key =day + rowId;
                    Subject subject = subjects.get(key);

                    Log.d("CreateTimetableActivity", key);

                    TextView dayView = row.findViewById(getResources().getIdentifier( day + rowId, "id", getPackageName()));
                    if (dayView != null) {
                        if (subject != null) {
                            dayView.setText(Html.fromHtml(String.format("<b>%s</b><br><small>(%s)</small>", subject.getSubjectName(), subject.getTeacherName())));
                        } else {
                            dayView.setText("No subject");
                            Log.d("CreateTimetableActivity", "Subject for key " + key + " is null");
                        }
                    } else {
                        Log.d("CreateTimetableActivity", "Day TextView with ID tv" + day + rowId + " is null");
                    }
                }
            } else {
                Log.d("CreateTimetableActivity", "TableRow with index " + rowId + " is null");
            }
        }
    }

    @Override
    public void onDialogSaveClick(String action, String entryId) {
        // This method is required by the TimetableDialogListener interface
        // Handle the TimeSlotDialogFragment save action based on the provided action and entryId
        Toast.makeText(this, "Dialog Save Clicked: Action: " + action + ", Entry ID: " + entryId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CreateTimetableActivity.class);
        // Optionally, pass any necessary data via intent extras
        startActivity(intent);
        // Optionally, you can implement saving logic here based on action (add/edit) and entryId
    }

    @Override
    public void onDialogDeleteClick(String entryId, String day) {
        // Adjust this call to match the deleteEntry method signature
        timetableService.deleteEntry(courseName, semester, division, day, entryId, new TimetableService.DeleteEntryCallback() {
            @Override
            public void onSuccess(String deletedEntryId) {
                Toast.makeText(CreateTimetableActivity.this, "Timetable entry deleted! Entry ID: " + deletedEntryId, Toast.LENGTH_SHORT).show();
                refreshTimetable();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateTimetableActivity.this, "Failed to delete entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onTimetableDialogPositiveClick(String day, String entryId, Subject subject) {
        // Handle the TimetableDialogFragment positive click
        timetableService.saveOrUpdateEntry(courseName, semester, division, entryId, day, subject, new TimetableService.SaveOrUpdateCallback() {
            @Override
            public void onSuccess(String updatedEntryId) {
                // Notify user of success
                Toast.makeText(CreateTimetableActivity.this, "Timetable entry saved!", Toast.LENGTH_SHORT).show();

                // Fetch the latest data and refresh the timetable
                refreshTimetable();
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
                Toast.makeText(CreateTimetableActivity.this, "Failed to save entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onTimetableDialogNegativeClick() {
        // Handle the TimetableDialogFragment negative click
        // Notify the user or handle the cancellation
    }

    @Override
    public void onDialogSaveClick(int rowId, String startTime, String endTime) {
        // Handle the save action here
        // For example, you might want to save this information to a database or update the UI



        // Example: Show a Toast message
        Toast.makeText(this, "Saved TimeSlot - Row: " + rowId + " Start: " + startTime + " End: " + endTime, Toast.LENGTH_LONG).show();

        // Update your data source or perform other actions as needed
        updateTimetable(rowId, startTime, endTime);
        updateRow(rowId, startTime, endTime);
        refreshTimetable();
    }

    private void updateTimetable(int rowId, String startTime, String endTime) {
        // Implementation to update the timetable with the new time slot
        // For instance, find the TextView for the given rowId and update its text
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View row = tableLayout.getChildAt(i);
            if (row instanceof TableRow) {
                TableRow tableRow = (TableRow) row;
                TextView timeSlotView = tableRow.findViewById(getResources().getIdentifier("tvTimeSlot" + rowId, "id", getPackageName()));
                if (timeSlotView != null) {
                    timeSlotView.setText(String.format("%s - %s", startTime, endTime));
                    break;
                }
            }
        }
    }

    private void refreshTimetable() {
        timeSlotService.getAllTimeSlots(courseName,semester,division,new TimeSlotService.TimeSlotFetchCallback() {
            @Override
            public void onSuccess(List<TimeSlot> updatedTimeSlots) {
                // Fetch subjects again to get the updated data
                timetableService.fetchSubjects(courseName, semester, division, new TimetableService.TimetableCallbackFetch() {
                    @Override
                    public void onSuccess(Map<String, Subject> updatedSubjects) {
                        // Update the table with the latest data
                        updateTableWithData(updatedSubjects, updatedTimeSlots);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateTimetableActivity.this, "Failed to fetch subjects: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateTimetableActivity.this, "Failed to fetch time slots: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDialogDeleteClick(int entryId) {
        // Implement the logic to handle deletion of a time slot here
        // You may want to call a service to delete the time slot from the database or update the UI accordingly

        // Example toast message (you should replace this with actual delete logic)
        Toast.makeText(this, "Delete clicked for entry ID: " + entryId, Toast.LENGTH_SHORT).show();
        clearRow(entryId);
        // Optionally, refresh the timetable to reflect changes
        refreshTimetable();
    }


    private void updateRow(int rowId, String startTime, String endTime) {
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View row = tableLayout.getChildAt(i);
            if (row instanceof TableRow) {
                TableRow tableRow = (TableRow) row;
                TextView startTimeView = tableRow.findViewById(getResources().getIdentifier("tvStartTime" + rowId, "id", getPackageName()));
                TextView endTimeView = tableRow.findViewById(getResources().getIdentifier("tvEndTime" + rowId, "id", getPackageName()));
                if (startTimeView != null && endTimeView != null) {
                    startTimeView.setText(startTime);
                    endTimeView.setText(endTime);
                    break;
                }
            }
        }
    }

    private void clearRow(int rowId) {
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View row = tableLayout.getChildAt(i);
            if (row instanceof TableRow) {
                TableRow tableRow = (TableRow) row;
                TextView startTimeView = tableRow.findViewById(getResources().getIdentifier("tvStartTime" + rowId, "id", getPackageName()));
                TextView endTimeView = tableRow.findViewById(getResources().getIdentifier("tvEndTime" + rowId, "id", getPackageName()));
                if (startTimeView != null && endTimeView != null) {
                    startTimeView.setText("");
                    endTimeView.setText("");
                    break;
                }
            }
        }
    }

}
