package com.ams.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.models.Room;
import com.ams.R;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private final List<Room> rooms;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onViewClick(Room room);
        void onEditClick(Room room);
        void onDeleteClick(Room room);
    }

    public RoomAdapter(List<Room> rooms, OnItemClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rooms, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.tvRoomId.setText(String.valueOf(position + 1)); // Serial number
        holder.tvRoomName.setText(room.getRoomName());

        holder.btnView.setOnClickListener(v -> listener.onViewClick(room));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(room));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomId, tvRoomName;
        ImageButton btnView, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomId = itemView.findViewById(R.id.tvSrNo);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            btnView = itemView.findViewById(R.id.btnViewRoom);
            btnEdit = itemView.findViewById(R.id.btnEditRoom);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
        }
    }
}
