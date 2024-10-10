package com.ams.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.ams.adapters.UserAdapter;
import com.ams.dialogs.DeleteStudentDialog;
import com.ams.dialogs.EditStudentDialog;
import com.ams.dialogs.StudentViewDialog;
import com.ams.models.User;
import com.ams.services.UserService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageStudentsActivity extends AppCompatActivity implements UserAdapter.OnStudentClickListener {

    private RecyclerView recyclerViewStudents;
    private UserAdapter studentAdapter;
    private List<User> studentList;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        studentList = new ArrayList<>();
        studentAdapter = new UserAdapter(this, studentList, this);

        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStudents.setAdapter(studentAdapter);

        userService = new UserService();

        loadStudents();
    }

    private void loadStudents() {
        userService.getAllUsers().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User student = dataSnapshot.getValue(User.class);
                    if (student != null) {
                        studentList.add(student);
                    }
                }
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageStudentsActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewClick(User student) {
        showStudentDetails(student);
    }

    @Override
    public void onEditClick(User student) {
        editStudent(student);
    }

    @Override
    public void onDeleteClick(User student) {
        deleteStudent(student);
    }

    private void showStudentDetails(User student) {
        StudentViewDialog dialog = StudentViewDialog.newInstance(student);
        dialog.show(getSupportFragmentManager(), "StudentViewDialog");
    }


    private void editStudent(User student) {
        EditStudentDialog dialog = EditStudentDialog.newInstance(student);
        dialog.show(getSupportFragmentManager(), "EditStudentDialog");

    }

    private void deleteStudent(User student) {
        DeleteStudentDialog deleteDialog = new DeleteStudentDialog(this, student, userService);
        deleteDialog.show();
    }



}
