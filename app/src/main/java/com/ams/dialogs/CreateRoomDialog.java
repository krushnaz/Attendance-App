package com.ams.dialogs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.activities.ManageRoomsActivity;
import com.ams.services.RoomService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.textfield.TextInputEditText;

public class CreateRoomDialog extends DialogFragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "CreateRoomDialog";

    public interface CreateRoomListener {
        void onCreateRoom(String roomName, double latitude, double longitude);
    }

    private CreateRoomListener listener;
    private RoomService roomService;

    public static CreateRoomDialog newInstance(CreateRoomListener listener, RoomService roomService) {
        CreateRoomDialog dialog = new CreateRoomDialog();
        dialog.listener = listener;
        dialog.roomService = roomService;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_create_room, null);

        TextInputEditText etRoomName = dialogView.findViewById(R.id.etRoomName);
        TextInputEditText etLatitude = dialogView.findViewById(R.id.etLatitude);
        TextInputEditText etLongitude = dialogView.findViewById(R.id.etLongitude);
        Button btnFetchLocation = dialogView.findViewById(R.id.btnFetchLocation);
        Button btnSaveRoom = dialogView.findViewById(R.id.btnSaveRoom);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnFetchLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    fetchLocation(fusedLocationClient, etLatitude, etLongitude);
                } else {
                    showEnableLocationDialog();
                }
            } else {
                requestLocationPermission();
            }
        });

        btnSaveRoom.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString();
            double latitude;
            double longitude;

            try {
                latitude = Double.parseDouble(etLatitude.getText().toString());
                longitude = Double.parseDouble(etLongitude.getText().toString());
            } catch (NumberFormatException e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (listener != null) {
                listener.onCreateRoom(roomName, latitude, longitude);
                roomService.addRoom(roomName, latitude, longitude, new RoomService.OnRoomOperationListener() {
                    @Override
                    public void onSuccess(String roomId) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "Room added with ID: " + roomId, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), ManageRoomsActivity.class);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "Activity is null, cannot start ManageRoomsActivity");
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "Failed to add room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Log.e(TAG, "Room addition failed", e);
                    }
                });
            } else {
                Log.e(TAG, "Listener is null");
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return builder.create();
    }

    private void fetchLocation(FusedLocationProviderClient fusedLocationClient,
                               TextInputEditText etLatitude, TextInputEditText etLongitude) {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000) // 10 seconds
                    .setFastestInterval(5000); // 5 seconds

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                        Location location = locationResult.getLastLocation();
                        Log.d(TAG, "Location fetched successfully: Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude());
                        if (etLatitude != null && etLongitude != null) {
                            etLatitude.setText(String.valueOf(location.getLatitude()));
                            etLongitude.setText(String.valueOf(location.getLongitude()));
                        }
                    } else {
                        Log.w(TAG, "Location is null");
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "Location data is not available. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Location permission is not granted", Toast.LENGTH_SHORT).show();
            }
            Log.w(TAG, "Location permission is not granted");
        }
    }

    private boolean isLocationEnabled() {
        android.location.LocationManager locationManager = (android.location.LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
        Log.d(TAG, "GPS Enabled: " + isGpsEnabled + ", Network Enabled: " + isNetworkEnabled);
        return isGpsEnabled || isNetworkEnabled;
    }

    private void showEnableLocationDialog() {
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Enable Location Services")
                    .setMessage("Location services are required to fetch your current location. Please enable them in the settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "Location services are required", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(requireActivity())
                    .setTitle("Location Permission Needed")
                    .setMessage("This app requires location permission to fetch your current location.")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE))
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted");
                fetchLocation(LocationServices.getFusedLocationProviderClient(requireActivity()),
                        getView().findViewById(R.id.etLatitude),
                        getView().findViewById(R.id.etLongitude));
            } else {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Location permission is required to fetch location", Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "Location permission denied");
            }
        }
    }
}
