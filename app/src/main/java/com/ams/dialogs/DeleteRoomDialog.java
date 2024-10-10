package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.ams.R;
import com.ams.services.RoomService;

public class DeleteRoomDialog extends DialogFragment {

    private static final String ARG_ROOM_ID = "roomId";

    public interface DeleteRoomListener {
        void onDeleteRoom();
    }

    private DeleteRoomListener listener;
    private RoomService roomService;
    private String roomId;

    public static DeleteRoomDialog newInstance(String roomId, DeleteRoomListener listener, RoomService roomService) {
        DeleteRoomDialog dialog = new DeleteRoomDialog();
        dialog.listener = listener;
        dialog.roomService = roomService;
        dialog.roomId = roomId;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        View dialogView = inflater.inflate(R.layout.dialog_delete_room, null);

        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView);

        btnDelete.setOnClickListener(v -> {
            if (listener != null && roomId != null) {
                listener.onDeleteRoom();
                roomService.deleteRoom(roomId, new RoomService.OnRoomOperationListener() {
                    @Override
                    public void onSuccess(String roomId) {
                        if (isAdded() && getActivity() != null) {
                            Toast.makeText(getActivity(), "Room deleted", Toast.LENGTH_SHORT).show();
                            // Optionally notify the activity or refresh the view
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (isAdded() && getActivity() != null) {
                            Toast.makeText(getActivity(), "Failed to delete room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return builder.create();
    }
}
