package com.ams.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnStudentClickListener clickListener;

    public interface OnStudentClickListener {
        void onViewClick(User student);
        void onEditClick(User student);
        void onDeleteClick(User student);
    }

    public UserAdapter(Context context, List<User> userList, OnStudentClickListener clickListener) {
        this.userList = userList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvSrNo.setText(String.valueOf(position + 1)); // Setting serial number
        holder.tvStudentName.setText(user.getFullName());
        holder.tvRollNumber.setText(user.getRollNumber());

        holder.btnViewStudent.setOnClickListener(v -> clickListener.onViewClick(user));
        holder.btnEditStudent.setOnClickListener(v -> clickListener.onEditClick(user));
        holder.btnDeleteStudent.setOnClickListener(v -> clickListener.onDeleteClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo;
        TextView tvStudentName;
        TextView tvRollNumber;
        ImageButton btnViewStudent;
        ImageButton btnEditStudent;
        ImageButton btnDeleteStudent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvRollNumber = itemView.findViewById(R.id.tvRollNumber);
            btnViewStudent = itemView.findViewById(R.id.btnViewStudent);
            btnEditStudent = itemView.findViewById(R.id.btnEditStudent);
            btnDeleteStudent = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}


