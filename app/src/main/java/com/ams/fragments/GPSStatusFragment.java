package com.ams.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ams.R;
import com.ams.models.Subject;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;

public class GPSStatusFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1002;
    private static final float CLASSROOM_RADIUS = 10.0f; // 10 meters

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private double roomLatitute;
    private double roomLongitude;
    private CardView cardViewStatus;
    private ImageView imageStatusIcon;
    private TextView textStatusMessage;
    private Button buttonStatusAction;
    private View circleBackground; // Reference to circle background
    private Subject subject;
    private GPSStatusListener gpsStatusListener; // Interface for communication
    private boolean countdownStarted = false;

    public GPSStatusFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("GPSStatusFragment", "Fragment ID: " + this.toString());
        View rootView = inflater.inflate(R.layout.gps_status_fragment, container, false);
        checkLocationPermissions();
        imageStatusIcon = rootView.findViewById(R.id.imageStatusIcon);
        textStatusMessage = rootView.findViewById(R.id.textStatusMessage);
        buttonStatusAction = rootView.findViewById(R.id.buttonStatusAction);
        cardViewStatus = rootView.findViewById(R.id.cardViewStatus);
        circleBackground = rootView.findViewById(R.id.circleBackground); // Initialize circleBackground
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Handle back press in Fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show your toast message
                Toast.makeText(requireContext(), "Attendance is being marked. Please don't press back.", Toast.LENGTH_SHORT).show();
                // Prevent the back button from working by not calling super.onBackPressed()
            }
        });


        Bundle args = getArguments();
        if (args != null) {
            subject = (Subject) args.getSerializable("subject");
            roomLatitute = args.getDouble("lectureLatitude");
            roomLongitude = args.getDouble("lectureLongitude");
            Log.d("GPSStatusFragment", " lectureLatitude " + roomLatitute + " lectureLongitude  " +roomLongitude);
            // Perform GPS verification logic here
            checkLocationPermissions();
        }else{
            Log.d("GPSStatusFragment", " lectureLatitude is null ");

        }

        return rootView;
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            } else {
                Toast.makeText(requireActivity(), "Location permission is required.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> getLastLocation())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(requireActivity(), LOCATION_SETTINGS_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            sendIntentException.printStackTrace();
                        }
                    } else {
                        showLocationSettingsDialog();
                    }
                });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentLocation = task.getResult();
                        verifyUserLocation();
                    } else {
                        // Request location updates if last location is unavailable
                        requestLocationUpdates();
                    }
                });
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // 1 second
        locationRequest.setFastestInterval(500); // 0.5 second

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                currentLocation = locationResult.getLastLocation();
                verifyUserLocation();
                // Stop location updates after getting the result
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, Looper.getMainLooper());
    }

    private void verifyUserLocation() {
        if (currentLocation != null) {
            float[] results = new float[1];
            // Calculate distance to the room
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), roomLatitute, roomLongitude, results);
            float distance = results[0];

            // Logging the results
            Log.d("GPSStatusFragment", "Distance to room: " + distance + " meters");
            Log.d("GPSStatusFragment", "Current Location: (" + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + ")");
            Log.d("GPSStatusFragment", "Room Location: (" + roomLatitute + ", " + roomLongitude + ")");

            // Check if within classroom radius
            if (distance <= CLASSROOM_RADIUS) {
                updateStatusCard(true); // User is within 10 meters
            } else {
                updateStatusCard(false); // User is outside 10 meters
            }
        } else {
            Toast.makeText(requireActivity(), "Unable to get user location.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateStatusCard(boolean isSuccess) {
        if (isSuccess) {
            imageStatusIcon.setImageResource(R.drawable.ic_check);
            updateCircleColor(true); // Set success color
            textStatusMessage.setText("Location verified successfully.\nYou are within the classroom.");
            buttonStatusAction.setVisibility(View.VISIBLE); // Show the button for countdown
            startCountdown(); // Start the countdown on the button
        } else {
            imageStatusIcon.setImageResource(R.drawable.ic_cross);
            updateCircleColor(false); // Set error color
            textStatusMessage.setText("Failed to verify location.");
            buttonStatusAction.setText("Retry");
            buttonStatusAction.setVisibility(View.VISIBLE); // Show the button for retrying
            buttonStatusAction.setOnClickListener(v -> reloadFragment()); // Reload fragment on retry
        }
        cardViewStatus.setVisibility(View.VISIBLE); // Show the status card
    }

    private void reloadFragment() {
        Bundle args = new Bundle();
        args.putSerializable("subject", subject); // Pass the subject object
        args.putDouble("lectureLatitude", roomLatitute); // Pass the latitude
        args.putDouble("lectureLongitude", roomLongitude); // Pass the longitude

        GPSStatusFragment newFragment = new GPSStatusFragment();
        newFragment.setArguments(args); // Set the arguments

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment); // Replace with the new instance
        transaction.addToBackStack(null); // Optional: Add to back stack
        transaction.commit(); // Commit the transaction
    }




    private void updateCircleColor(boolean isSuccess) {
        int color = isSuccess ? ContextCompat.getColor(getContext(), R.color.successColor) : ContextCompat.getColor(getContext(), R.color.errorColor);
        GradientDrawable drawable = (GradientDrawable) circleBackground.getBackground();
        drawable.setColor(color);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof GPSStatusListener) {
            gpsStatusListener = (GPSStatusListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement GPSStatusListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gpsStatusListener = null;
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private void startCountdown() {
        if (countdownStarted) {
            return;
        }
        countdownStarted = true;

        final int[] countdown = {2}; // Start countdown from 3 seconds

        buttonStatusAction.setText("Verifying...");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                countdown[0]--;
                if (countdown[0] > 0) {
                    buttonStatusAction.setText("Proceeding...");
                    handler.postDelayed(this, 500); // Repeat every 1 second
                } else {
                    buttonStatusAction.setText("Proceeding...");
                    if (gpsStatusListener != null) {
                        gpsStatusListener.onCountdownComplete(subject, roomLatitute, roomLongitude);
                    }
                }
            }
        };

        handler.postDelayed(runnable, 500); // Start after 1 second delay
    }

    private void showLocationSettingsDialog() {
        new AlertDialog.Builder(requireActivity())
                .setTitle("Location Required")
                .setMessage("Please enable GPS to continue.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("GPSStatusFragment", "onResume called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("GPSStatusFragment", "onPause called");
    }

}
