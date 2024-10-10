package com.ams.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.ams.R;

public class ViewRoomDialog extends DialogFragment {

    private static final String ARG_ROOM_NAME = "roomName";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";

    public static ViewRoomDialog newInstance(String roomName, double latitude, double longitude) {
        ViewRoomDialog dialog = new ViewRoomDialog();
        Bundle args = new Bundle();
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
        View dialogView = inflater.inflate(R.layout.dialog_view_room, null);

        TextView tvRoomName = dialogView.findViewById(R.id.tvRoomName);
        TextView tvLatitude = dialogView.findViewById(R.id.tvLatitude);
        TextView tvLongitude = dialogView.findViewById(R.id.tvLongitude);
        ImageButton btnClose = dialogView.findViewById(R.id.btnCancel);

        Bundle args = getArguments();
        if (args != null) {
            tvRoomName.setText(args.getString(ARG_ROOM_NAME));
            tvLatitude.setText(String.valueOf(args.getDouble(ARG_LATITUDE)));
            tvLongitude.setText(String.valueOf(args.getDouble(ARG_LONGITUDE)));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);

        btnClose.setOnClickListener(v -> dismiss());

        return builder.create();
    }
}
