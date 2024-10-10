package com.ams.services;

import androidx.annotation.NonNull;

import com.ams.models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherService {

    private static final String TEACHERS_COLLECTION = "teachers";
    private final DatabaseReference teachersReference;

    public TeacherService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        teachersReference = database.getReference(TEACHERS_COLLECTION);
    }

    public Query getTeacherByUsername(String username) {
        return FirebaseDatabase.getInstance().getReference("teachers").child(username);
    }

    public Task<Void> addTeacher(Teacher teacher) {
        return teachersReference.child(teacher.getUsername()).setValue(teacher);
    }

    public Query getAllTeachers() {
        return teachersReference.orderByKey();
    }

    public Task<Void> updateTeacher(Teacher teacher) {
        return teachersReference.child(teacher.getUsername()).setValue(teacher);
    }

    public Task<Void> deleteTeacherByUsername(String username) {
        return teachersReference.child(username).removeValue();
    }

    public Query getTeachersByDepartment(String department) {
        return teachersReference.orderByChild("department").equalTo(department);
    }

    // Fetch all teacher names
    public Task<List<String>> getTeacherNames() {
        TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();
        teachersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> teacherNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String teacherName = snapshot.child("name").getValue(String.class); // Adjust key as needed
                    if (teacherName != null) {
                        teacherNames.add(teacherName);
                    }
                }
                taskCompletionSource.setResult(teacherNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });
        return taskCompletionSource.getTask();
    }

    public interface TeacherCallback {
        void onSuccess(String fullName, String department);
        void onFailure(@NonNull Exception e);
    }

    // Fetch teacher's full name and department by username
    public void fetchTeacherFullnameAndDepartmentByUsername(String username, TeacherCallback callback) {
        teachersReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Assuming there's only one teacher with the given username
                            DataSnapshot teacherSnapshot = dataSnapshot.getChildren().iterator().next();
                            String fullName = teacherSnapshot.child("fullName").getValue(String.class);
                            String department = teacherSnapshot.child("department").getValue(String.class);
                            if (fullName != null && department != null) {
                                callback.onSuccess(fullName, department);
                            } else {
                                callback.onFailure(new Exception("Full name or department data is null"));
                            }
                        } else {
                            callback.onFailure(new Exception("No teacher found with the username: " + username));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onFailure(databaseError.toException());
                    }
                });
    }

    // Fetch teacher's full name by username
    public void getFullNameByUsername(String username, final OnCompleteListener<String> listener) {
        Query query = teachersReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Teacher user = userSnapshot.getValue(Teacher.class);
                        if (user != null && user.getFullName() != null) {
                            taskCompletionSource.setResult(user.getFullName());
                            listener.onComplete(taskCompletionSource.getTask());
                            return; // Exit after finding the user
                        }
                    }
                }
                taskCompletionSource.setException(new Exception("User not found"));
                listener.onComplete(taskCompletionSource.getTask());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
                taskCompletionSource.setException(error.toException());
                listener.onComplete(taskCompletionSource.getTask());
            }
        });
    }



    public void fetchTeacherCount(final CountCallback callback) {
        teachersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long teacherCount = dataSnapshot.getChildrenCount();
                callback.onCountFetched(teacherCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }
    public interface CountCallback {
        void onCountFetched(long count);
        void onFailure(String errorMessage);
    }
}


