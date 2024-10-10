package com.ams.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.adapters.MarkAttendanceAdapter;
import com.ams.fragments.BiometricVerificationFragment;
import com.ams.fragments.GPSStatusFragment;
import com.ams.fragments.GPSStatusListener;
import com.ams.fragments.SuccessFragment;
import com.ams.models.Subject;
import com.ams.models.User;
import com.ams.services.RoomService;
import com.ams.services.TimetableService;
import com.ams.services.UserService;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MarkAttendanceActivity extends AppCompatActivity implements GPSStatusListener, BiometricVerificationFragment.OnBiometricAuthListener {

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1002;
    private static final int GPS_REQUEST_CODE = 1001; // Define request code for GPS verification
    private static final float CLASSROOM_RADIUS = 10.0f; // 10 meters

    private double roomLatitute;
    private double roomLongitude;
    private RecyclerView recyclerViewLectures;
    private MarkAttendanceAdapter markAttendanceAdapter;
    private ImageButton btnScanQRCode;
    private int currentStep = 1; // Track the current step
    private TextView stepNamesTextView; // TextView to show step names
    private Button buttonPreviousStep; // Previous Step button
    private List<Subject> subjectsList;
    private RoomService roomService;
    private double lectureLatitude;
    private double lectureLongitude;
    private Subject subject;
    private User user;
    private TimetableService timetableService;
    private UserService userService;
    private String courseName;
    private  String semester;
    private String division;
    private String dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        checkLocationSettings();
        // Initialize UI components
        btnScanQRCode = findViewById(R.id.button_qr_code);
        stepNamesTextView = findViewById(R.id.step_names); // TextView for step names
        recyclerViewLectures = findViewById(R.id.recyclerViewLectures);
        recyclerViewLectures.setLayoutManager(new LinearLayoutManager(this));
        buttonPreviousStep = findViewById(R.id.exit);

        user = new User();
        timetableService = new TimetableService();
        subjectsList = new ArrayList<>();
        userService = new UserService();
        roomService = new RoomService();

        String time = new SimpleDateFormat("HH:mm a", Locale.getDefault()).format(new Date()); // Get current time

        // Initialize adapter for subjects
        markAttendanceAdapter = new MarkAttendanceAdapter(this,subjectsList, new MarkAttendanceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Subject selectedSubject) {
                // Handle item click
                subject = selectedSubject; // Assuming lecture is a Subject here
                String roomName = subject.getRoom(); // Make sure this returns a String

                roomService.fetchRoomDetailsByName(roomName, new RoomService.OnRoomDetailsFetchedCallback() {
                    @Override
                    public void onSuccess(double latitude, double longitude) {
                        // Update latitude and longitude
                        roomLatitute = latitude;
                        roomLongitude = longitude;

                        // Proceed to GPS Verification step after fetching room details
                        currentStep = 2; // Set current step to GPS Verification
                        updateStepProgress(currentStep);
                        updateStepText();
                        handleStepTransition(currentStep);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MarkAttendanceActivity.this, "Failed to fetch room details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
        recyclerViewLectures.setAdapter(markAttendanceAdapter);

        // Fetch user details and update UI
        getUserDetails(new OnUserDetailsFetchedCallback() {
            @Override
            public void onSuccess(User fetchedUser) {
                // Store user details
                user = fetchedUser;

                // Fetch and display subjects
                 courseName = user.getCourseName();
                 semester = user.getSemester();
                 division = user.getDivision();
                 dayOfWeek = getCurrentDayOfWeek();
                String rollNo = getStoredRollNo(); // Replace with the actual roll number
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                fetchActiveAndTodaysSubject(courseName, semester, division, dayOfWeek, rollNo, currentDate);
            }
        });

        // Handle button click to scan QR code
        btnScanQRCode.setOnClickListener(v -> openQRCodeScanActivity());

        // Handle Previous Step button click
        buttonPreviousStep.setOnClickListener(v -> {
            // Show a toast message
            Toast.makeText(MarkAttendanceActivity.this, "Navigating to Student Dashboard....", Toast.LENGTH_SHORT).show();

            // Navigate to the student dashboard
            navigateToStudentDashboard();
        });

        // Initialize step progress
        updateStepProgress(currentStep);
        updateStepText();
    }


    private void openQRCodeScanActivity() {
        Intent intent = new Intent(MarkAttendanceActivity.this, QRCodeScanFromDashboardActivity.class);
        startActivity(intent);
    }

    private void getUserDetails(final OnUserDetailsFetchedCallback callback) {
        String rollNo = getStoredRollNo();
        userService.fetchUserByRollNo(rollNo, new UserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // Pass the User object to the callback

                callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MarkAttendanceActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnUserDetailsFetchedCallback {
        void onSuccess(User user);
    }

        private void fetchActiveAndTodaysSubject(String courseName, String semester, String division, String dayOfWeek, String rollNo, String currentDate) {
        timetableService.fetchActivatedSubjects(courseName, dayOfWeek, semester, division, rollNo, currentDate, new TimetableService.TimetableCallbackFetch() {
            @Override
            public void onSuccess(Map<String, Subject> entries) {
                subjectsList.clear();
                subjectsList.addAll(entries.values());

                if (subjectsList.isEmpty()) {
                    // Show a message or update UI to indicate no subjects are scheduled

                    Toast.makeText(MarkAttendanceActivity.this, "No subjects scheduled for today", Toast.LENGTH_SHORT).show();
                    // Optionally, show an empty view or placeholder
                } else {
                    // Notify the adapter that data has changed
                    markAttendanceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MarkAttendanceActivity.this, "No Subject Available", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateStepProgress(int step) {
        View line1 = findViewById(R.id.line1);
        View line2 = findViewById(R.id.line2);
        View line3 = findViewById(R.id.line3);

        // Set lines color based on the current step
        line1.setBackground(ContextCompat.getDrawable(this, R.drawable.line_drawable));
        line2.setBackground(ContextCompat.getDrawable(this, R.drawable.line_drawable));
        line3.setBackground(ContextCompat.getDrawable(this, R.drawable.line_drawable));

        if (step > 1) {
            line1.setBackground(ContextCompat.getDrawable(this, R.drawable.line_green));
        }
        if (step > 2) {
            line2.setBackground(ContextCompat.getDrawable(this, R.drawable.line_green));
        }
        if (step > 3) {
            line3.setBackground(ContextCompat.getDrawable(this, R.drawable.line_green));
        }

        // Update step circles and text color
        updateStepCircleAndTextColor(R.id.step1_number, step > 1);
        updateStepCircleAndTextColor(R.id.step2_number, step > 2);
        updateStepCircleAndTextColor(R.id.step3_number, step > 3);
        updateStepCircleAndTextColor(R.id.step4_number, step >= 4);

        // Handle visibility of RecyclerView and fragment containers
        handleStepTransition(step);
    }


    private void updateStepCircleAndTextColor(int circleId, boolean isCompleted) {
        TextView stepNumber = findViewById(circleId);

        if (stepNumber != null) {
            if (isCompleted) {
                stepNumber.setBackgroundResource(R.drawable.step_circle_checkmark);
                stepNumber.setText("");
            } else {
                stepNumber.setBackgroundResource(R.drawable.step_circle_background);
                String stepText = getResources().getResourceEntryName(circleId).replace("step", "").replace("_number", "");
                stepNumber.setText(stepText);
            }
        }
    }

    private void handleStepTransition(int step) {
        switch (step) {
            case 1:
                // Show the lectures RecyclerView
                recyclerViewLectures.setVisibility(View.VISIBLE);
                // Ensure the GPS fragment container is hidden initially
                findViewById(R.id.gps_verification_container).setVisibility(View.GONE);
                findViewById(R.id.biometric_verification_container).setVisibility(View.GONE); // Hide biometric container
//                buttonPreviousStep.setVisibility(View.GONE); // Hide Previous Step button
                break;
            case 2:
                // Hide the lectures RecyclerView
                recyclerViewLectures.setVisibility(View.GONE);
                // Show the GPS fragment
                navigateToGPSVerification();
                findViewById(R.id.biometric_verification_container).setVisibility(View.GONE); // Hide biometric container
//                buttonPreviousStep.setVisibility(View.VISIBLE); // Show Previous Step button
                break;
            case 3:
                // Hide the GPS fragment container
                findViewById(R.id.gps_verification_container).setVisibility(View.GONE);
                // Show the Biometric fragment
                navigateToBiometricVerification();
//                buttonPreviousStep.setVisibility(View.VISIBLE); // Show Previous Step button
                break;
            case 4:
                // Hide the Biometric fragment container
                findViewById(R.id.biometric_verification_container).setVisibility(View.GONE);
                // Show the success message
                navigateToSuccessFragment();
                buttonPreviousStep.setVisibility(View.GONE); // Hide Previous Step button
                break;
        }
    }


    private void navigateToGPSVerification() {
        // Ensure the fragment container is visible
        View gpsVerificationContainer = findViewById(R.id.gps_verification_container);
        if (gpsVerificationContainer != null) {
            gpsVerificationContainer.setVisibility(View.VISIBLE);
        }

        // Check if the fragment already exists
        GPSStatusFragment existingFragment = (GPSStatusFragment) getSupportFragmentManager().findFragmentByTag("GPSStatusFragment");
        if (existingFragment == null) {
            // Create the GPSStatusFragment and set arguments
            GPSStatusFragment gpsStatusFragment = new GPSStatusFragment();
            Bundle args = new Bundle();
            args.putDouble("lectureLatitude", roomLatitute);
            args.putDouble("lectureLongitude", roomLongitude);
            args.putSerializable("subject", subject); // Pass the lecture object
            Log.d("navigateToGPSVerification", " lectureLatitude " + roomLatitute + " lectureLongitude  " +roomLongitude);

            gpsStatusFragment.setArguments(args);

            // Begin fragment transaction
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.gps_verification_container, gpsStatusFragment, "GPSStatusFragment");
            transaction.addToBackStack(null); // Optionally add to back stack
            transaction.commit();
        }
    }

    private void navigateToBiometricVerification() {
        Log.d("navigateToBiometricVerification", "Navigating to BiometricVerificationFragment");

        // Ensure the biometric fragment container is visible
        View biometricVerificationContainer = findViewById(R.id.biometric_verification_container);
        if (biometricVerificationContainer != null) {
            biometricVerificationContainer.setVisibility(View.VISIBLE);
        }

        BiometricVerificationFragment biometricVerificationFragment = new BiometricVerificationFragment();
        Bundle args = new Bundle();
        args.putSerializable("subject", subject); // Pass the lecture object
        args.putDouble("latitude", roomLatitute); // Pass latitude
        args.putDouble("longitude", roomLongitude); // Pass longitude
        args.putSerializable("user", user); // Pass the lecture object

        biometricVerificationFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.biometric_verification_container, biometricVerificationFragment);
        transaction.addToBackStack(null); // Optionally add to back stack
        transaction.commit();
    }

    private void navigateToSuccessFragment() {
        // Ensure the success fragment container is present in the layout
        View successFragmentContainer = findViewById(R.id.fragment_container);
        if (successFragmentContainer == null) {
            Log.e("NavigateToSuccess", "Fragment container not found in the layout.");
            return; // Prevent further execution if the container is missing
        }

        // Make sure the container is visible if it was hidden
        successFragmentContainer.setVisibility(View.VISIBLE);

        // Check if SuccessFragment is already in the fragment manager
        SuccessFragment existingFragment = (SuccessFragment) getSupportFragmentManager().findFragmentByTag("SuccessFragment");

        if (existingFragment == null) {
            // Directly use the user object details
            String rollNo = user.getRollNumber(); // Assuming user object is available
            String fullName = user.getFullName();
            String className = user.getCourseName();
            String division = user.getDivision();

            // Create a new SuccessFragment instance
            SuccessFragment successFragment = new SuccessFragment();

            // Set arguments
            Bundle args = new Bundle();
            args.putString("studentName", fullName); // Use fetched full name
            args.putString("rollNo", rollNo); // Use the roll number from the user object
            args.putString("dateTime", subject.getStartTime() + " to " + subject.getEndTime()); // Assuming subject object is available
            args.putString("subjectName", subject.getSubjectName()); // Assuming subject object is available
            args.putString("className", className);// Use className from the user object
            args.putString("division", division);

            successFragment.setArguments(args);

            // Begin fragment transaction
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, successFragment, "SuccessFragment");
            transaction.addToBackStack(null); // Optionally add to back stack
            transaction.commit();
        }
    }




    private void updateStepText() {
        switch (currentStep) {
            case 1:
                stepNamesTextView.setText("Select Lecture");
                break;
            case 2:
                stepNamesTextView.setText("GPS Verification");
                break;
            case 3:
                stepNamesTextView.setText("Biometric Verification");
                break;
            case 4:
                stepNamesTextView.setText("Attendace Status");
                break;
        }
    }

    @Override
    public void onCountdownComplete(Subject subject, double latitude, double longitude) {
        Log.d("MarkAttendanceActivity", "onCountdownComplete called");

        // Handle countdown completion
        this.subject = subject;
        this.lectureLatitude = latitude;
        this.lectureLongitude = longitude;
        currentStep = 3; // Move to the biometric verification step
        updateStepProgress(currentStep);
        updateStepText(); // Update step text for the next step

        // Navigate to the biometric verification fragment
        navigateToBiometricVerification();

    }

    @Override
    public void onBiometricAuthSuccess() {
        // Handle success case
        Log.d("MarkAttendanceActivity", "Biometric Authentication successful");
        currentStep = 4;
        updateStepProgress(currentStep);
        updateStepText();
        navigateToSuccessFragment();
    }

    @Override
    public void onBiometricAuthFailure() {
        // Handle failure case, e.g., retry or show an error message
    }

    private String getStoredRollNo() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("rollNo", null);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        // Check if the current fragment is one of the specific fragments
        if (currentFragment instanceof GPSStatusFragment ||
                currentFragment instanceof BiometricVerificationFragment ||
                currentFragment instanceof SuccessFragment) {

            // Show the toast message
            Toast.makeText(this, "Attendance is being marked, don't press back", Toast.LENGTH_SHORT).show();
        } else {
            // If not one of the specific fragments, handle the back press normally
            super.onBackPressed();
        }
    }

    private void navigateToStudentDashboard() {
        // Replace with your actual dashboard activity
        Intent intent = new Intent(MarkAttendanceActivity.this, StudentDashboardActivity.class);
        startActivity(intent);
        finish(); // Optionally finish the current activity
    }

    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "";
        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // Check if the location settings are satisfied (i.e., if GPS is enabled)
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // Location settings are satisfied, no need to show dialog
            // Proceed with your functionality
        }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    // Location settings are not satisfied, show the user a dialog to enable GPS
                    ((ResolvableApiException) e).startResolutionForResult(this, LOCATION_SETTINGS_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            } else {
                // Location settings are not satisfied and cannot be resolved automatically
                Toast.makeText(this, "Please enable location services to proceed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // The user enabled location services
                Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show();
                // Proceed with the attendance marking functionality or any other location-based functionality
            } else {
                // The user did not enable location services, navigate to home page (Student Dashboard)
                Toast.makeText(this, "Location services are need to enable to mark attendance.", Toast.LENGTH_SHORT).show();

                // Navigate to Student Dashboard Activity
                Intent intent = new Intent(this, StudentDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear backstack if necessary
                startActivity(intent);
                finish();  // Close current activity
            }
        }
    }

}
