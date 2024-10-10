package com.ams.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ams.R;
import com.ams.adapters.TeacherAdapter;
import com.ams.dialogs.TeacherAddDialog;
import com.ams.dialogs.TeacherDeleteDialog;
import com.ams.dialogs.TeacherDetailDialog;
import com.ams.dialogs.TeacherEditDialog;
import com.ams.models.Teacher;
import com.ams.services.TeacherService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageTeachersActivity extends AppCompatActivity implements TeacherAdapter.OnTeacherClickListener, TeacherEditDialog.OnTeacherEditListener {

    private FloatingActionButton btnAddTeacher;
    private RecyclerView recyclerViewTeachers;
    private TeacherAdapter teacherAdapter;
    private List<Teacher> teacherList;
    private TeacherService teacherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_teachers);

        btnAddTeacher = findViewById(R.id.fabAddTeacher);
        recyclerViewTeachers = findViewById(R.id.recyclerViewTeachers);
        teacherList = new ArrayList<>();
        teacherAdapter = new TeacherAdapter(this, teacherList, this);

        recyclerViewTeachers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTeachers.setAdapter(teacherAdapter);

        teacherService  = new TeacherService();

        loadTeachers();

        // Handle "Add Teacher" button click
        btnAddTeacher.setOnClickListener(v -> {
            TeacherAddDialog dialog = TeacherAddDialog.newInstance();
            dialog.show(getSupportFragmentManager(), "TeacherAddDialog");
        });

    }

    private void loadTeachers() {
        teacherService.getAllTeachers().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teacherList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Teacher teacher = dataSnapshot.getValue(Teacher.class);
                    if (teacher != null) {
                        teacherList.add(teacher);
                    }
                }
                teacherAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageTeachersActivity.this, "Failed to load teachers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewClick(Teacher teacher) {
        showTeacherDetails(teacher);
    }

    @Override
    public void onEditClick(Teacher teacher) {
        showEditDialog(teacher);
    }

    @Override
    public void onDeleteClick(Teacher teacher) {
        showDeleteDialog(teacher);
    }

    private void showTeacherDetails(Teacher teacher) {
        TeacherDetailDialog dialog = TeacherDetailDialog.newInstance(teacher);
        dialog.setTargetFragment(null, 0);
        dialog.show(getSupportFragmentManager(), "TeacherDetailDialog");
    }

    private void showEditDialog(Teacher teacher) {
        TeacherEditDialog dialog = TeacherEditDialog.newInstance(teacher);
        dialog.setTargetFragment(null, 0);
        dialog.show(getSupportFragmentManager(), "TeacherEditDialog");
    }

    private void showDeleteDialog(Teacher teacher) {
        TeacherDeleteDialog dialog = TeacherDeleteDialog.newInstance(teacher);
        dialog.setTargetFragment(null, 0);
        dialog.show(getSupportFragmentManager(), "TeacherDeleteDialog");
    }

    @Override
    public void onTeacherEdited(Teacher teacher) {
        teacherService.updateTeacher(teacher)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManageTeachersActivity.this, "Teacher updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ManageTeachersActivity.this, "Failed to update teacher", Toast.LENGTH_SHORT).show();
                        Log.e("ManageTeachersActivity", "Error updating teacher", task.getException());
                    }
                });
    }
}
