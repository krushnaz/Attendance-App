package com.ams.fragments;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ams.R;
import com.ams.activities.MarkAttendanceActivity;
import com.ams.models.AttendanceRecord;
import com.ams.models.Subject;
import com.ams.models.User;
import com.ams.services.AttendanceService;
import com.ams.services.UserService;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BiometricVerificationFragment extends Fragment {

    private static final String TAG = "BiometricVerificationFragment";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_RADIUS_OFFSET = 1;
    private static final int COUNTDOWN_DELAY_MS = 1500; // 2 seconds
    private static final int COUNTDOWN_START_TIME = 0; // Countdown starts from 3 seconds
    private static final String CHANNEL_ID = "attendance_channel";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100; // Choose a unique request code

    private BiometricPrompt biometricPrompt;
    private Executor executor;
    private TextView textStatusMessage;
    private Button buttonBiometricAction;
    private ImageView imageBiometricIcon;
    private Handler mainHandler;
    private View circleBackground;
    private double latitude;
    private double longitude;
    private Subject subject;

    private boolean countdownStarted = false;
    private TextView buttonStatusAction;
    private OnBiometricAuthListener onBiometricAuthListener;
    private ImageView imageStatusIcon;

    public interface OnBiometricAuthListener {
        void onBiometricAuthSuccess();

        void onBiometricAuthFailure();
    }

    public static BiometricVerificationFragment newInstance( double latitude, double longitude) {
        BiometricVerificationFragment fragment = new BiometricVerificationFragment();
        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        createNotificationChannel();

        View view = inflater.inflate(R.layout.biometric_verification_fragment, container, false);

        textStatusMessage = view.findViewById(R.id.text_status_message);
        buttonStatusAction = view.findViewById(R.id.button_biometric_action);
        imageBiometricIcon = view.findViewById(R.id.image_biometric_icon);
        imageStatusIcon = view.findViewById(R.id.imageStatusIcon);
        circleBackground = view.findViewById(R.id.circleBackground);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show your toast message
                Toast.makeText(requireContext(), "Attendance is being marked. Please don't press back.", Toast.LENGTH_SHORT).show();
                // Prevent the back button from working by not calling super.onBackPressed()
            }
        });

        if (getArguments() != null) {
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
            subject = (Subject) getArguments().getSerializable("subject"); // Update to Subject
        }

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        biometricPrompt = new BiometricPrompt(getActivity(), executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                mainHandler.post(() -> updateStatus("Authentication error: " + errString, R.drawable.ic_cross, R.color.errorColor));
                notifyFailure();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                markAttendance();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                mainHandler.post(() -> updateStatus("Authentication failed", R.drawable.ic_cross, R.color.errorColor));
                notifyFailure();
            }
        });

        startCountdown();
        buttonStatusAction.setOnClickListener(v -> reloadFragment());
        return view;
    }

    private void startCountdown() {
        if (countdownStarted) {
            return;
        }
        countdownStarted = true;

        final int[] countdown = {COUNTDOWN_START_TIME}; // Start countdown from COUNTDOWN_START_TIME seconds

        buttonStatusAction.setText("Verifying...");
        circleBackground.setVisibility(View.GONE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                countdown[0]--;
                if (countdown[0] > 0) {
                    buttonStatusAction.setText("Proceeding");
                    mainHandler.postDelayed(this, 500); // Repeat every 1 second
                } else {
                    buttonStatusAction.setText("Proceeding...");
                    performBiometricAuthentication();
                    circleBackground.setVisibility(View.VISIBLE);
                }
            }
        };

        mainHandler.postDelayed(runnable, 500); // Start after 1 second delay
    }

    private void performBiometricAuthentication() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Please authenticate using biometric credentials")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void markAttendance() {
        if (subject == null || latitude == 0.0 || longitude == 0.0) {
            updateStatus("Subject or location details are missing", R.drawable.ic_cross, R.color.errorColor);
            mainHandler.postDelayed(() -> notifyFailure(), COUNTDOWN_DELAY_MS);
            return;
        }

        String rollNo = getStoredRollNo();
        if (rollNo == null) {
            updateStatus("No roll number found", R.drawable.ic_cross, R.color.errorColor);
            mainHandler.postDelayed(() -> notifyFailure(), COUNTDOWN_DELAY_MS);
            return;
        }

        UserService userService = new UserService();
        AttendanceService attendanceService = new AttendanceService();

        userService.getUserByRollNo(rollNo).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().hasChildren()) {
                User user = task.getResult().getChildren().iterator().next().getValue(User.class);
                if (user != null) {
                    processAttendance(user);
                } else {
                    updateStatus("User not found", R.drawable.ic_cross, R.color.errorColor);
                    mainHandler.postDelayed(() -> notifyFailure(), COUNTDOWN_DELAY_MS);
                }
            } else {
                updateStatus("Failed to fetch user details", R.drawable.ic_cross, R.color.errorColor);
                mainHandler.postDelayed(() -> notifyFailure(), COUNTDOWN_DELAY_MS);
            }
        });
    }

    private void processAttendance(User user) {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = formatter.format(date);

        AttendanceRecord record = new AttendanceRecord(
                null, getStoredRollNo(), user.getFullName(), subject.getSubjectName(),
                currentDate, currentTime, subject.getTeacherName(),
                "Present", subject.getDay(), subject.getRoom(), user.getCourseName(), user.getSemester(), user.getDivision(), subject.getLectureType()
        );

        AttendanceService attendanceService = new AttendanceService();
        attendanceService.markAttendance(record, new AttendanceService.OnAttendanceMarkedListener() {
            @Override
            public void onSuccess() {
                mainHandler.post(() -> {
                    updateStatus("Attendance marked successfully!", R.drawable.ic_check, R.color.successColor);
                    notifySuccessNotification(subject.getSubjectName(), subject.getLectureType(), currentDate,currentTime); // Call success notification
                    notifySuccess();
                });
            }

            @Override
            public void onFailure() {
                mainHandler.post(() -> {
                    notifyFailureNotification(); // Call failure notification
                    updateStatus("Failed to mark attendance", R.drawable.ic_cross, R.color.errorColor);
                    notifyFailure();
                });
            }
        });
    }

    private String getStoredRollNo() {
        return getActivity().getSharedPreferences("app_prefs", getActivity().MODE_PRIVATE)
                .getString("rollNo", null);
    }


    private void updateStatus(String message, int iconResId, int colorResId) {
        textStatusMessage.setText(message);
        imageStatusIcon.setImageResource(iconResId);
        imageBiometricIcon.setColorFilter(ContextCompat.getColor(getActivity(), colorResId), android.graphics.PorterDuff.Mode.SRC_IN);
        circleBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), colorResId));

//        circleBackground.setBackgroundColor(colorResId);
    }

    private void notifySuccess() {
        if (onBiometricAuthListener != null) {
            onBiometricAuthListener.onBiometricAuthSuccess();
        }
    }

    private void notifyFailure() {
        if (onBiometricAuthListener != null) {
            onBiometricAuthListener.onBiometricAuthFailure();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBiometricAuthListener) {
            onBiometricAuthListener = (OnBiometricAuthListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnBiometricAuthListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onBiometricAuthListener = null;
    }

    private void reloadFragment() {
        BiometricVerificationFragment newFragment = newInstance(
                latitude,
                longitude
        );

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newFragment) // Update with your actual container ID
                .addToBackStack(null) // Optional
                .commit();
    }

    private void checkNotificationPermissionAndNotify(int notificationId, Notification notification) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.notify(notificationId, notification);
        } else {
            // Permission not granted, request permission
            requestNotificationPermission();
        }
    }

    private void requestNotificationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // Show an explanation to the user
            Toast.makeText(requireContext(), "Notification permission is required to show attendance notifications.", Toast.LENGTH_LONG).show();
        }

        // Request the permission
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Attendance Notifications";
            String description = "Channel for attendance notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notifySuccessNotification(String subjectName, String lectureType, String date, String time) {
        if (onBiometricAuthListener != null) {
            onBiometricAuthListener.onBiometricAuthSuccess();
        }

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Attendance Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Create the intent to handle notification click
        Intent intent = new Intent(requireContext(), MarkAttendanceActivity.class); // Replace with your target activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Use FLAG_IMMUTABLE for the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Create success notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)  // Ensure this icon is properly set up
                .setContentTitle("Attendance Marked")
                .setContentText("Your attendance for " + subjectName + " has been marked successfully!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Subject: " + subjectName +
                                "\nLecture Type: " + lectureType +
                                "\nDate: " + date +
                                "\nTime: " + time))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)  // Set the pending intent to handle click events
                .setAutoCancel(true);  // Removes notification when clicked

        // Show the notification
        notificationManager.notify(1, builder.build());  // Unique notification ID
    }



    private void notifyFailureNotification() {
        if (onBiometricAuthListener != null) {
            onBiometricAuthListener.onBiometricAuthFailure();
        }

        // Create failure notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_icon)  // Set an appropriate icon
                .setContentTitle("Attendance Failed")
                .setContentText("Failed to mark your attendance. Please try again.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);  // Removes notification when clicked

        // Check permission and show the notification
        checkNotificationPermissionAndNotify(2, builder.build());  // Unique notification ID
    }



}
