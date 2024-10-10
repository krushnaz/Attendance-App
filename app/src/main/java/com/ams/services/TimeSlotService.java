package com.ams.services;

import androidx.annotation.NonNull;

import com.ams.models.TimeSlot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotService {

    private final DatabaseReference timeSlotRef;

    public TimeSlotService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        timeSlotRef = database.getReference("TimeSlots");  // Reference to the "TimeSlots" node in Firebase
    }

    // Save a new time slot or update an existing time slot in Firebase
// Save or update a time slot based on rowId
    public void saveOrUpdateTimeSlot(TimeSlot timeSlot, String courseName, String semester, String division, SaveOrUpdateCallback callback) {
        // Define the path to the time slot under courseName/semester/division
        DatabaseReference timeSlotRef = FirebaseDatabase.getInstance().getReference()
                .child("TimeSlots")
                .child(courseName)
                .child(semester)
                .child(division);

        // First, find the existing time slot by rowId
        timeSlotRef.orderByChild("rowId").equalTo(timeSlot.getRowId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id;
                if (dataSnapshot.exists()) {
                    // Get the existing ID if it exists
                    id = dataSnapshot.getChildren().iterator().next().getKey();
                } else {
                    // Create a new ID if it doesn't exist
                    id = timeSlotRef.push().getKey();
                }
                timeSlot.setId(id); // Set the ID in the timeSlot object
                timeSlotRef.child(id).setValue(timeSlot)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(id))
                        .addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }



    // Delete a time slot from Firebase
// Delete a time slot based on rowId
    public void deleteTimeSlot(int rowId, String courseName, String semester, String division, DeleteCallback callback) {
        // Define the path to the time slot under courseName/semester/division
        DatabaseReference timeSlotRef = FirebaseDatabase.getInstance().getReference()
                .child("TimeSlots")
                .child(courseName)
                .child(semester)
                .child(division);

        // Find the time slot by rowId and delete it
        timeSlotRef.orderByChild("rowId").equalTo(rowId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String id = snapshot.getKey();
                        timeSlotRef.child(id).removeValue()
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                        return; // Exit after the first deletion
                    }
                } else {
                    callback.onFailure(new Exception("No TimeSlot found for rowId " + rowId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public interface SaveOrUpdateCallback {
        void onSuccess(String id);
        void onFailure(@NonNull Exception e);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }

    public interface TimeSlotCallback {
        void onSuccess(TimeSlot timeSlot);
        void onFailure(@NonNull Exception e);
    }

    public void getAllTimeSlots(String courseName, String semester, String division, TimeSlotFetchCallback callback) {
        // Define the path to the time slots under courseName/semester/division
        DatabaseReference timeSlotRef = FirebaseDatabase.getInstance().getReference()
                .child("TimeSlots")
                .child(courseName)
                .child(semester)
                .child(division);

        // Fetch all time slots for the specified course, semester, and division
        timeSlotRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TimeSlot> timeSlots = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeSlot timeSlot = snapshot.getValue(TimeSlot.class);
                    if (timeSlot != null) {
                        timeSlots.add(timeSlot);
                    }
                }
                callback.onSuccess(timeSlots); // Pass the list of time slots to the callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException()); // Handle failure
            }
        });
    }


    public interface TimeSlotFetchCallback {
        void onSuccess(List<TimeSlot> timeSlots);
        void onFailure(@NonNull Exception e);
    }

    public void getTimeSlotByRowId(int rowId, String courseName, String semester, String division, TimeSlotCallback callback) {
        // Define the path to the specific course, semester, and division
        DatabaseReference timeSlotRef = FirebaseDatabase.getInstance().getReference()
                .child("TimeSlots")
                .child(courseName)
                .child(semester)
                .child(division);

        // Query the time slot with the specific rowId
        timeSlotRef.orderByChild("rowId").equalTo(rowId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        TimeSlot timeSlot = snapshot.getValue(TimeSlot.class);
                        if (timeSlot != null) {
                            callback.onSuccess(timeSlot);
                            return; // Exit after finding the first matching time slot
                        }
                    }
                    callback.onFailure(new Exception("TimeSlot not found"));
                } else {
                    callback.onFailure(new Exception("TimeSlot not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }


}
