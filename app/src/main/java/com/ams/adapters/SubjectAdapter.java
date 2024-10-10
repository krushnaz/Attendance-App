package com.ams.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.models.Subject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectsList;
    private OnActionClickListener onActionClickListener;

    public SubjectAdapter(List<Subject> subjectsList, OnActionClickListener listener) {
        this.subjectsList = subjectsList;
        this.onActionClickListener = listener;
        sortSubjectsByRowId(); // Sort the subjects list by rowId when initializing
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectsList.get(position);

        // Set serial number, time slot, and subject name
        holder.tvSerialNumber.setText(String.valueOf(position + 1));
        holder.tvTimeSlot.setText(subject.getStartTime() + " - " + subject.getEndTime());
        holder.tvSubjectName.setText(subject.getSubjectName());

        // Set the activate button color based on the active status
        if ("Activated".equals(subject.getActive())) {
            holder.btnActivate.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.successColor)); // Set to green
        } else {
            holder.btnActivate.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.red)); // Set to red
        }

        // Handle action buttons
        holder.btnGenerateQRCode.setOnClickListener(v -> onActionClickListener.onGenerateQRCodeClick(subject));
        holder.btnActivate.setOnClickListener(v -> onActionClickListener.onActivateClick(subject));
        holder.btnView.setOnClickListener(v -> onActionClickListener.onViewClick(subject));
    }

    @Override
    public int getItemCount() {
        return subjectsList.size();
    }

    // Method to sort subjects by rowId
    private void sortSubjectsByRowId() {
        Collections.sort(subjectsList, new Comparator<Subject>() {
            @Override
            public int compare(Subject s1, Subject s2) {
                return Integer.compare(s1.getRowId(), s2.getRowId()); // Sorting by rowId
            }
        });
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSerialNumber, tvTimeSlot, tvSubjectName;
        ImageButton btnGenerateQRCode, btnActivate, btnView;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            btnGenerateQRCode = itemView.findViewById(R.id.btnGenerateQRCode);
            btnActivate = itemView.findViewById(R.id.btnActivate);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }

    // Define an interface to handle action button clicks
    public interface OnActionClickListener {
        void onGenerateQRCodeClick(Subject subject);
        void onActivateClick(Subject subject);
        void onViewClick(Subject subject);
    }
}
