package com.ams.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ams.R;
import com.ams.models.Timetable;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder> {

    private List<Timetable> timetables;

    @NonNull
    @Override
    public TimetableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new TimetableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableViewHolder holder, int position) {
        Timetable timetable = timetables.get(position);

        // Bind data to UI
        holder.tvSerialNumber.setText(String.valueOf(position + 1));
        holder.tvCourseName.setText(timetable.getCourseName());
        holder.tvDivision.setText(timetable.getDivision());

        // Set up action buttons (implement listeners as needed)
        holder.btnView.setOnClickListener(v -> {
            // Handle view action
        });

        holder.btnEdit.setOnClickListener(v -> {
            // Handle edit action
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Handle delete action
        });
    }

    @Override
    public int getItemCount() {
        return timetables != null ? timetables.size() : 0;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables;
        notifyDataSetChanged();
    }

    static class TimetableViewHolder extends RecyclerView.ViewHolder {
        TextView tvSerialNumber;
        TextView tvCourseName;
        TextView tvDivision;
        ImageButton btnView;
        ImageButton btnEdit;
        ImageButton btnDelete;

        TimetableViewHolder(View itemView) {
            super(itemView);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDivision = itemView.findViewById(R.id.tvDivision);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
