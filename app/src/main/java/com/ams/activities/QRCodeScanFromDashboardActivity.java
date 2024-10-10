package com.ams.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.ams.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ams.models.AttendanceRecord;
import com.ams.models.Subject;
import com.ams.models.User;
import com.ams.services.AttendanceService;
import com.ams.services.RoomService;
import com.ams.services.TimetableService;
import com.ams.services.UserService;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QRCodeScanFromDashboardActivity extends AppCompatActivity {

    private static final String TAG = "QR CodeMark Attendace Activity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1002;

    private Subject subject;
    private FusedLocationProviderClient fusedLocationClient;
    private UserService userService;
    private AttendanceService attendanceService;
    private double roomLatitude;
    private double roomLongitude;
    private double currentLatitude;
    private double currentLongitude;
    private DecoratedBarcodeView barcodeView;
    private View scanLine;
    private Handler scanLineHandler;
    private Runnable scanLineRunnable;
    private boolean isProcessing = false; // Flag to prevent multiple scans
//    private MediaPlayer mediaPlayer;
    private TimetableService timetableService;
    private String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scan);
        userService = new UserService();
        attendanceService = new AttendanceService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        barcodeView = findViewById(R.id.barcode_scanner);
        scanLine = findViewById(R.id.scan_line);
        // Initialize MediaPlayer for beep sound
//        mediaPlayer = MediaPlayer.create(this, com.google.zxing.client.android.R.raw.zxing_beep);
        timetableService = new TimetableService();

        startScanLineAnimation();

        if (!isLocationEnabled()) {
            checkLocationSettings();
        } else {
            fetchCurrentLocation();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startQRCodeScan();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission granted");
            fetchCurrentLocation();
        } else {
            Log.d(TAG, "Location permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }



    }

    private void startQRCodeScan() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && !isProcessing) {
                    String qrCodeResult = result.getText();
                    Log.d(TAG, "Scanned QR Code: " + qrCodeResult); // Log the QR code result
                    isProcessing = true; // Set flag to true to prevent further scans
//                    if (mediaPlayer != null) {
//                        mediaPlayer.start();
//                    }
                    handleQRCodeScan(qrCodeResult);
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Handle possible result points if needed
            }
        });
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void startScanLineAnimation() {
        scanLineHandler = new Handler();
        scanLineRunnable = new Runnable() {
            @Override
            public void run() {
                scanLine.setTranslationY(scanLine.getTranslationY() + 10);
                if (scanLine.getTranslationY() > 800) { // Height of scan box
                    scanLine.setTranslationY(-scanLine.getHeight());
                }
                scanLineHandler.postDelayed(this, 20);
            }
        };
        scanLineHandler.post(scanLineRunnable);
    }


    private void handleQRCodeScan(String qrCodeResult) {
        if (qrCodeResult != null) {
            // Extract details from the QR code
            String subjectName = extractValueFromQRCode(qrCodeResult, "Subject");
            String courseName = extractValueFromQRCode(qrCodeResult, "Course");
            String semester = extractValueFromQRCode(qrCodeResult, "Semester");
            String division = extractValueFromQRCode(qrCodeResult, "Division");
            String day = extractValueFromQRCode(qrCodeResult, "Day");
            String startTime = extractValueFromQRCode(qrCodeResult, "Start Time");
            String endTime = extractValueFromQRCode(qrCodeResult, "End Time");
            String lectureType = extractValueFromQRCode(qrCodeResult, "Lecture Type");
            String teacher = extractValueFromQRCode(qrCodeResult, "Teacher");
            String rowId = extractValueFromQRCode(qrCodeResult, "Row Id");
            String room = extractValueFromQRCode(qrCodeResult, "Room");

            if (room != null && !room.isEmpty()) {
                roomName = room;
                Log.d(TAG, "Assigned Room: " + roomName); // Check if roomName is assigned properly

                // Fetch the room's latitude and longitude after roomName has been assigned
                RoomService roomService = new RoomService();
                roomService.getRoomLatLngByRoomName(roomName, new RoomService.OnRoomLocationFetchedListener() {
                    @Override
                    public void onSuccess(double latitude, double longitude) {
                        // Handle success
                        roomLatitude = latitude;
                        roomLongitude = longitude;
                        Log.d("RoomLocation", "Latitude: " + latitude + ", Longitude: " + longitude);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure
                        Log.e("RoomLocation", "Error fetching room: " + roomName + " location: " + e.getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Room value is null or empty.");
            }

            Log.d(TAG, "Extracted from QR Code - " +
                    "Subject: " + subjectName +
                    ", Course Name: " + courseName +
                    ", Semester: " + semester +
                    ", Division: " + division +
                    ", Day: " + day +
                    ", Start Time: " + startTime +
                    ", End Time: " + endTime +
                    ", Lecture Type: " + lectureType +
                    ", Teacher: " + teacher +
                    ", Row Id: " + rowId +
                    ", Room: " + room);

            // Fetch the subject details from the timetable collection using TimetableService
            timetableService.fetchSubjectDetails(courseName, semester, division, day, subjectName, rowId, new TimetableService.OnSubjectFetchedListener() {
                @Override
                public void onSubjectFetched(Subject subject) {
                    if (subject != null) {
                        // Format subject details into the expectedSubjectDetails string
                        String expectedSubjectDetails = String.format(
                                "Subject: %s\n" +
                                        "Course: %s\n" +
                                        "Semester: %s\n" +
                                        "Division: %s\n" +
                                        "Day: %s\n" +
                                        "Start Time: %s\n" +
                                        "End Time: %s\n" +
                                        "Lecture Type: %s\n" +
                                        "Room: %s\n" +
                                        "Row Id: %d\n" +
                                        "Teacher: %s",
                                subject.getSubjectName(),
                                courseName,
                                semester,
                                division,
                                subject.getDay(),
                                subject.getStartTime(),
                                subject.getEndTime(),
                                subject.getLectureType(),
                                subject.getRoom(),
                                subject.getRowId(),
                                subject.getTeacherName()
                        );

                        Log.d(TAG, "QR Code Result: " + qrCodeResult.trim());
                        Log.d(TAG, "Expected Subject Details: " + expectedSubjectDetails.trim());

                        // Validate the QR code with the fetched subject details
                        if (validateQRCode(qrCodeResult, expectedSubjectDetails)) {
                            Log.d(TAG, "QR code and subject details match.");

                            if (isSubjectMatching(subject, qrCodeResult)) {
                                showSuccessDialog(subject);
                            } else {
                                showErrorDialog("Scanned details do not match. Please try again");
                            }
                        } else {
                            showErrorDialog("QR Code does not match expected subject details.");
                        }
                    } else {
                        showErrorDialog("No matching subject found.");
                    }
                }

                @Override
                public void onSubjectFetchFailed(Exception exception) {
                    showErrorDialog("Failed to fetch subject details.");
                }
            });
        }
    }



    private String extractValueFromQRCode(String qrCodeResult, String key) {
        String[] lines = qrCodeResult.split("\n");
        for (String line : lines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(key)) {
                return parts[1].trim();
            }
        }
        return "";
    }

    private void showErrorDialog(String message) {
        runOnUiThread(() -> {
            Toast.makeText(QRCodeScanFromDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
            isProcessing = false; // Reset flag on failure
        });
    }



    private Map<String, String> extractDetailsFromQRCode(String qrCodeResult) {
        Map<String, String> qrCodeDetails = new HashMap<>();
        String[] lines = qrCodeResult.split("\n");
        for (String line : lines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                qrCodeDetails.put(parts[0].trim(), parts[1].trim());
            }
        }
        return qrCodeDetails;
    }


    private boolean isSubjectMatching(Subject subject, String qrCodeResult) {
        // Extract details from the QR code
        String[] qrCodeLines = qrCodeResult.split("\n");
        Map<String, String> qrCodeDetails = new HashMap<>();
        for (String line : qrCodeLines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                qrCodeDetails.put(parts[0].trim(), parts[1].trim());
            }
        }

        // Validate each detail
        boolean nameMatches = subject.getSubjectName().equalsIgnoreCase(qrCodeDetails.get("Subject"));
        boolean dayMatches = subject.getDay().equalsIgnoreCase(qrCodeDetails.get("Day"));

        // Parse and compare start and end times
        LocalTime startTime = parseTime(subject.getStartTime());
        LocalTime endTime = parseTime(subject.getEndTime());
        boolean startTimeMatches = startTime.equals(parseTime(qrCodeDetails.get("Start Time")));
        boolean endTimeMatches = endTime.equals(parseTime(qrCodeDetails.get("End Time")));

        // Validate time range
        LocalTime now = LocalTime.now();
        boolean timeMatches = startTime != null && endTime != null && !now.isBefore(startTime) && !now.isAfter(endTime);

        Log.d(TAG, "start time: " + startTime);
        Log.d(TAG, "end time: " + endTime);


        // Validate location (assuming roomLatitude and roomLongitude are provided)
        boolean locationMatches = isLocationMatching(roomLatitude, roomLongitude);

        if(locationMatches){
            Toast.makeText(QRCodeScanFromDashboardActivity.this, "Location Matched Successfully", Toast.LENGTH_SHORT).show();

        }
        // Log mismatches for debugging
        Log.d(TAG, "Name Matches: " + nameMatches);
        Log.d(TAG, "Day Matches: " + dayMatches);
        Log.d(TAG, "Start Time Matches: " + startTimeMatches);
        Log.d(TAG, "End Time Matches: " + endTimeMatches);
        Log.d(TAG, "Time Range Matches: " + timeMatches);
        Log.d(TAG, "Location Matches: " + locationMatches);

        // Return true if all details match
        return nameMatches && dayMatches && startTimeMatches && endTimeMatches  && locationMatches && timeMatches;
    }



    private boolean validateQRCode(String qrCodeResult, String expectedLectureDetails) {
        String[] expectedLines = expectedLectureDetails.split("\n");
        String[] resultLines = qrCodeResult.split("\n");

        Map<String, String> expectedDetailsMap = new HashMap<>();
        for (String line : expectedLines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                expectedDetailsMap.put(parts[0].trim(), parts[1].trim());
            }
        }

        for (String line : resultLines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                if (!expectedDetailsMap.containsKey(key) || !expectedDetailsMap.get(key).equals(value)) {
                    return false;
                }
            }
        }

        return true;
    }

    private LocalTime parseTime(String timeString) {
        DateTimeFormatter formatter12Hour = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        DateTimeFormatter formatter24Hour = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
        try {
            return LocalTime.parse(timeString, formatter12Hour);
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(timeString, formatter24Hour);
            } catch (DateTimeParseException e2) {
                // Handle parsing failure (return null or throw an exception as needed)
                Log.e(TAG, "Time parsing failed: " + e2.getMessage());
                return null;
            }
        }
    }


    private boolean isLocationMatching(double lectureLatitude, double lectureLongitude) {
        // Allow for minor deviations in location due to GPS accuracy
        Log.d(TAG, " isLocationMatching: " + "room location : "+lectureLatitude +" ----"+lectureLongitude);
        Log.d(TAG, " isLocationMatching: " + "Current location : "+currentLatitude +" ----"+currentLongitude);


        final double LOCATION_TOLERANCE = 0.0001;
        return Math.abs(currentLatitude - lectureLatitude) <= LOCATION_TOLERANCE &&
                Math.abs(currentLongitude - lectureLongitude) <= LOCATION_TOLERANCE;
    }


    private void showSuccessDialog(Subject subject) {
        AlertDialog successDialog = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("You are in the class room. Attendance is being marked...")
                .setCancelable(false) // Prevent dialog from being dismissed by user
                .show();

        // Delay marking attendance and navigation by 3 seconds
        new Handler().postDelayed(() -> {
            successDialog.dismiss(); // Dismiss the dialog
            markAttendance(subject);
        }, 3000); // 3000 milliseconds = 3 seconds
    }

    private void markAttendance(Subject subject) {
        String rollNo = getStoredRollNo();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        if (rollNo != null) {
            // Fetch user details first
            userService.getUserByRollNo(rollNo).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot result = task.getResult();
                    if (result != null && result.hasChildren()) {
                        DataSnapshot userSnapshot = result.getChildren().iterator().next();
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            // User details retrieved, now check attendance
                            attendanceService.hasAttendanceMarked(user.getCourseName(), user.getSemester(), subject.getDivision(), rollNo, currentDate, new AttendanceService.OnAttendanceCheckedListener() {
                                @Override
                                public void onChecked(boolean hasMarked) {
                                    if (hasMarked) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(QRCodeScanFromDashboardActivity.this, "Attendance has already been marked for today.", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        // Attendance not marked, proceed to mark it
                                        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
                                        AttendanceRecord record = new AttendanceRecord(
                                                null, rollNo, user.getFullName(), subject.getSubjectName(),
                                                currentDate, currentTime, subject.getTeacherName(),
                                                "Present", subject.getDay(), subject.getRoom(),
                                                user.getCourseName(), user.getSemester(), user.getDivision(),
                                                subject.getLectureType()
                                        );

                                        attendanceService.markAttendance(record, new AttendanceService.OnAttendanceMarkedListener() {
                                            @Override
                                            public void onSuccess() {
                                                runOnUiThread(() -> {
                                                    Toast.makeText(QRCodeScanFromDashboardActivity.this, "Attendance marked successfully.", Toast.LENGTH_SHORT).show();
                                                    finish(); // Close the activity after marking attendance
                                                });
                                            }

                                            @Override
                                            public void onFailure() {
                                                runOnUiThread(() -> {
                                                    Toast.makeText(QRCodeScanFromDashboardActivity.this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(Exception exception) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(QRCodeScanFromDashboardActivity.this, "Error checking attendance: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(QRCodeScanFromDashboardActivity.this, "User details are incomplete or null", Toast.LENGTH_SHORT).show());
                            Log.e("markAttendance", "User object is null for rollNo: " + rollNo);
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(QRCodeScanFromDashboardActivity.this, "No user found for roll number: " + rollNo, Toast.LENGTH_SHORT).show());
                        Log.e("markAttendance", "No user found for rollNo: " + rollNo);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(QRCodeScanFromDashboardActivity.this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show());
                    Log.e("markAttendance", "Task failed", task.getException());
                }
            });
        } else {
            Toast.makeText(this, "Roll number not available", Toast.LENGTH_SHORT).show();
            Log.e("markAttendance", "Roll number is null");
        }
    }


    private String getStoredRollNo() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("rollNo", null);
    }

    private void fetchCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Show a dialog to prompt the user to enable GPS
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                        } else {
                            Log.e(TAG, "Location not available");
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRCodeScan();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // Request high accuracy location

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);  // Add location request to builder

        // Check whether the required location settings are satisfied
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        // If location settings are satisfied
        task.addOnSuccessListener(locationSettingsResponse -> {
            // Proceed with your functionality since location settings are satisfied
            Toast.makeText(this, "Location settings are already enabled.", Toast.LENGTH_SHORT).show();
            // You can now proceed to mark attendance or any location-based functionality
        }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but can be resolved by showing a dialog
                try {
                    // Show the user a dialog to enable GPS
                    ((ResolvableApiException) e).startResolutionForResult(this, LOCATION_SETTINGS_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            } else {
                // Location settings are not satisfied and cannot be resolved automatically
                Toast.makeText(this, "Location services are required for this functionality. Please enable them.", Toast.LENGTH_SHORT).show();
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

