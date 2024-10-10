package com.ams.dialogs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.activities.ManageRoomsActivity;
import com.ams.services.RoomService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

public class EditRoomDialog extends DialogFragment {

    private static final String ARG_ROOM_ID = "roomId";
    private static final String ARG_ROOM_NAME = "roomName";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    public interface EditRoomListener {
        void onEditRoom(String roomId, String roomName, double latitude, double longitude);
    }

    private EditRoomListener listener;
    private RoomService roomService;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public static EditRoomDialog newInstance(String roomId, String roomName, double latitude, double longitude, EditRoomListener listener, RoomService roomService) {
        EditRoomDialog dialog = new EditRoomDialog();
        dialog.listener = listener;
        dialog.roomService = roomService;
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putString(ARG_ROOM_NAME, roomName);
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_edit_room, null);

        TextInputEditText etRoomName = dialogView.findViewById(R.id.editTextRoomName);
        TextInputEditText etLatitude = dialogView.findViewById(R.id.editTextLatitude);
        TextInputEditText etLongitude = dialogView.findViewById(R.id.editTextLongitude);
        Button btnSaveRoom = dialogView.findViewById(R.id.buttonSaveRoom);
        Button btnFetchLocation = dialogView.findViewById(R.id.buttonFetchLocation);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Disable editing latitude and longitude
        etLatitude.setEnabled(false);
        etLongitude.setEnabled(false);

        Bundle args = getArguments();
        if (args != null) {
            etRoomName.setText(args.getString(ARG_ROOM_NAME));
            etLatitude.setText(String.valueOf(args.getDouble(ARG_LATITUDE)));
            etLongitude.setText(String.valueOf(args.getDouble(ARG_LONGITUDE)));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView);

        // Fetch location button click listener
        btnFetchLocation.setOnClickListener(v -> fetchLocation(etLatitude, etLongitude));
        btnCancel.setOnClickListener(v -> dismiss());

        btnSaveRoom.setOnClickListener(v -> {
            // Get values from the arguments and input fields
            String roomId = args.getString(ARG_ROOM_ID);
            String updatedRoomName = etRoomName.getText().toString();
            double updatedLatitude = Double.parseDouble(etLatitude.getText().toString());
            double updatedLongitude = Double.parseDouble(etLongitude.getText().toString());

            // Check if the listener is set
            if (listener != null) {
                // Notify listener about the room edit
                listener.onEditRoom(roomId, updatedRoomName, updatedLatitude, updatedLongitude);

                // Call the RoomService to update the room details in the database
                roomService.editRoom(roomId, updatedRoomName, updatedLatitude, updatedLongitude, new RoomService.OnRoomOperationListener() {
                    @Override
                    public void onSuccess(String roomId) {
                        // Show a toast message and navigate back to ManageRoomsActivity
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireActivity(), "Room updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireActivity(), ManageRoomsActivity.class);
                            startActivity(intent);
                        });
                        dismiss(); // Dismiss the dialog after success
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Show a failure message if the room update fails
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireActivity(), "Failed to update room", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                // Handle case where listener is not set
                Toast.makeText(requireActivity(), "Listener is not set", Toast.LENGTH_SHORT).show();
            }
        });


        return builder.create();
    }

    private void fetchLocation(TextInputEditText etLatitude, TextInputEditText etLongitude) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                etLatitude.setText(String.valueOf(location.getLatitude()));
                etLongitude.setText(String.valueOf(location.getLongitude()));
            } else {
                Toast.makeText(requireActivity(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation(getView().findViewById(R.id.editTextLatitude), getView().findViewById(R.id.editTextLongitude));
            } else {
                Toast.makeText(requireActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
