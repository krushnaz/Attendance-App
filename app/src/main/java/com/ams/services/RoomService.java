package com.ams.services;

import android.util.Log;

import com.ams.models.Room;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomService {

    private static final String TAG = "RoomService";
    private DatabaseReference databaseReference;

    public RoomService() {
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");
    }

    // Add a new room
    public void addRoom(String roomName, double latitude, double longitude, OnRoomOperationListener listener) {
        String roomId = databaseReference.push().getKey();
        if (roomId != null) {
            Room room = new Room(roomId, roomName, latitude, longitude);
            databaseReference.child(roomId).setValue(room)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(roomId))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add room", e);
                        listener.onFailure(e);
                    });
        }
    }

    // Update room details
    public void editRoom(String roomId, String roomName, double latitude, double longitude, OnRoomOperationListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("roomName", roomName);
        updates.put("latitude", latitude);
        updates.put("longitude", longitude);

        databaseReference.child(roomId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess(roomId))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update room", e);
                    listener.onFailure(e);
                });
    }

    // Delete a room
    public void deleteRoom(String roomId, OnRoomOperationListener listener) {
        databaseReference.child(roomId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess(roomId))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete room", e);
                    listener.onFailure(e);
                });
    }

    // Fetch all room details
    public void fetchAllRoomDetails(OnRoomDetailsFetchedListener listener) {
        databaseReference.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Map<String, double[]> roomDetailsMap = new HashMap<>();
                        List<String> roomNames = new ArrayList<>();

                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                            String roomName = roomSnapshot.child("roomName").getValue(String.class);
                            double latitude = roomSnapshot.child("latitude").getValue(Double.class);
                            double longitude = roomSnapshot.child("longitude").getValue(Double.class);

                            roomNames.add(roomName);
                            roomDetailsMap.put(roomName, new double[]{latitude, longitude});
                        }

                        listener.onRoomDetailsFetched(roomNames, roomDetailsMap);
                    } else {
                        listener.onFailure(new Exception("No rooms found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch room details", e);
                    listener.onFailure(e);
                });
    }

    // Fetch room details by name
    public void fetchRoomDetailsByName(String roomName, OnRoomDetailsFetchedCallback callback) {
        databaseReference.orderByChild("roomName").equalTo(roomName)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            DataSnapshot roomSnapshot = dataSnapshot.getChildren().iterator().next();
                            double latitude = roomSnapshot.child("latitude").getValue(Double.class);
                            double longitude = roomSnapshot.child("longitude").getValue(Double.class);
                            callback.onSuccess(latitude, longitude);
                        } else {
                            callback.onFailure(new Exception("Room not found"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(databaseError.toException());
                    }
                });
    }

    public void getRoomLatLngByRoomName(String roomName, final OnRoomLocationFetchedListener listener) {
        Query query = databaseReference.orderByChild("roomName").equalTo(roomName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                        double latitude = roomSnapshot.child("latitude").getValue(Double.class);
                        double longitude = roomSnapshot.child("longitude").getValue(Double.class);
                        listener.onSuccess(latitude, longitude);
                        return;
                    }
                } else {
                    listener.onFailure(new Exception("Room not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    // Listener interface for callback
    public interface OnRoomLocationFetchedListener {
        void onSuccess(double latitude, double longitude);
        void onFailure(Exception e);
    }

    public interface OnRoomOperationListener {
        void onSuccess(String roomId);
        void onFailure(Exception e);
    }

    public interface OnRoomFetchListener {
        void onRoomFetched(Room room);
        void onFailure(Exception e);
    }

    public interface OnRoomDetailsFetchedListener {
        void onRoomDetailsFetched(List<String> roomNames, Map<String, double[]> roomDetailsMap);
        void onFailure(Exception e);
    }

    public interface OnRoomDetailsFetchedCallback {
        void onSuccess(double latitude, double longitude);
        void onFailure(Exception e);
    }
}
