package com.ams.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.adapters.RoomAdapter;
import com.ams.dialogs.CreateRoomDialog;
import com.ams.dialogs.DeleteRoomDialog;
import com.ams.dialogs.EditRoomDialog;
import com.ams.dialogs.ViewRoomDialog;
import com.ams.models.Room;
import com.ams.services.RoomService;
import com.ams.services.TeacherService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageRoomsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;
    private List<Room> roomList;
    private DatabaseReference roomsDatabaseRef;
    private RoomService roomService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rooms);
        Log.d("ManageRoomsActivity", "onCreate called");

        recyclerView = findViewById(R.id.recyclerViewRooms);
        FloatingActionButton fabAddRoom = findViewById(R.id.fabAddRoom);

        roomsDatabaseRef = FirebaseDatabase.getInstance().getReference("rooms");
        roomService  = new RoomService();

        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(roomList, new RoomAdapter.OnItemClickListener() {
            @Override
            public void onViewClick(Room room) {
                Log.d("ManageRoomsActivity", "onViewClick called for room: " + room.getRoomName());
                showViewDialog(room);
            }

            @Override
            public void onEditClick(Room room) {
                Log.d("ManageRoomsActivity", "onEditClick called for room: " + room.getRoomName());
                showEditDialog(room);
            }

            @Override
            public void onDeleteClick(Room room) {
                Log.d("ManageRoomsActivity", "onDeleteClick called for room: " + room.getRoomName());
                showDeleteDialog(room);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(roomAdapter);

        fabAddRoom.setOnClickListener(v -> showCreateRoomDialog());
        Log.d("ManageRoomsActivity", "FloatingActionButton clicked");
        loadRooms();
    }

    private void loadRooms() {
        roomsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    if (room != null) {
                        roomList.add(room);
                    }
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManageRoomsActivity.this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateRoomDialog() {
        CreateRoomDialog.newInstance(new CreateRoomDialog.CreateRoomListener() {
            @Override
            public void onCreateRoom(String roomName, double latitude, double longitude) {
                // Room creation logic here
            }
        }, new RoomService()).show(getSupportFragmentManager(), "createRoomDialog");
    }

    private void showViewDialog(Room room) {
        ViewRoomDialog.newInstance(room.getRoomName(), room.getLatitude(), room.getLongitude())
                .show(getSupportFragmentManager(), "viewRoomDialog");
    }

    private void showEditDialog(Room room) {
        EditRoomDialog.newInstance(
                        room.getRoomId(),
                        room.getRoomName(),
                        room.getLatitude(),
                        room.getLongitude(),
                        new EditRoomDialog.EditRoomListener() {
                            @Override
                            public void onEditRoom(String roomId, String roomName, double latitude, double longitude) {
                                // Room editing logic here
                            }
                        }, new RoomService())
                .show(getSupportFragmentManager(), "editRoomDialog");
    }

    private void showDeleteDialog(Room room) {

        DeleteRoomDialog.newInstance(
                room.getRoomId(),
                new DeleteRoomDialog.DeleteRoomListener() {
                    @Override
                    public void onDeleteRoom() {
                        // Handle room deletion logic here
                        roomService.deleteRoom(room.getRoomId(), new RoomService.OnRoomOperationListener() {
                            @Override
                            public void onSuccess(String roomId) {
                                Toast.makeText(ManageRoomsActivity.this, "Room deleted", Toast.LENGTH_SHORT).show();
                                loadRooms(); // Refresh the room list after deletion
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ManageRoomsActivity.this, "Failed to delete room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                },
                new RoomService()
        ).show(getSupportFragmentManager(), "deleteRoomDialog");
    }


}