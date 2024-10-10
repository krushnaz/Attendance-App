package com.ams.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ams.models.Subject;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectService {

    private static final String SUBJECTS_COLLECTION = "subjects";
    private static final String TEACHERS_COLLECTION = "teachers";
    private final DatabaseReference subjectsReference;
    private final DatabaseReference teachersReference;
    private final DatabaseReference roomsReference; // Added reference for rooms
    private String teacherUsername;
    public SubjectService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        subjectsReference = database.getReference(SUBJECTS_COLLECTION);
        teachersReference = database.getReference(TEACHERS_COLLECTION);
        roomsReference = database.getReference("rooms"); // Assuming there's a "rooms" collection
    }

    public SubjectService(String teacherUsername) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        subjectsReference = database.getReference(SUBJECTS_COLLECTION);
        teachersReference = database.getReference(TEACHERS_COLLECTION);
        roomsReference = database.getReference("rooms"); // Assuming there's a "rooms" collection
        this.teacherUsername = teacherUsername;
    }

    public Task<Void> addSubject(String username, String subjectId, Subject subject) {
        return getTeacherIdByUsername(username).continueWithTask(task -> {
            if (task.isSuccessful()) {
                String teacherId = task.getResult();
                if (teacherId != null) {
                    return subjectsReference.child(teacherId).child(subjectId).setValue(subject);
                } else {
                    return Tasks.forException(new Exception("Teacher ID not found"));
                }
            } else {
                return Tasks.forException(task.getException());
            }
        });
    }

    public Task<DataSnapshot> getSubjectsByUsername(String username) {
        return getTeacherIdByUsername(username).continueWithTask(task -> {
            if (task.isSuccessful()) {
                String teacherId = task.getResult();
                if (teacherId != null) {
                    return subjectsReference.child(teacherId).get();
                } else {
                    return Tasks.forException(new Exception("Teacher ID not found"));
                }
            } else {
                return Tasks.forException(task.getException());
            }
        });
    }

    private Task<String> getTeacherIdByUsername(String username) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        Query query = teachersReference.orderByChild("username").equalTo(username).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String teacherId = dataSnapshot.getChildren().iterator().next().getKey();
                    taskCompletionSource.setResult(teacherId);
                } else {
                    taskCompletionSource.setResult(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public Task<DataSnapshot> getSubjectsByTeacherUsername(String username) {
        return getSubjectsByUsername(username);
    }

    public Task<Void> updateSubject(String username, String subjectName, Subject subject) {
        return subjectsReference.child(username).child(subjectName).setValue(subject);
    }

    public Task<Void> deleteSubject(String username, String subjectName) {
        return subjectsReference.child(username).child(subjectName).removeValue();
    }


    public Task<List<String>> getSubjectNamesByLectureNumber(String lectureNumber) {
        TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();
        Query query = subjectsReference.orderByChild("lectureNumber").equalTo(lectureNumber);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> subjectNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String subjectName = snapshot.child("subjectName").getValue(String.class);
                    if (subjectName != null) {
                        subjectNames.add(subjectName);
                    }
                }
                taskCompletionSource.setResult(subjectNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }



    public void getSubjectsByTeacherUsername(final OnSubjectsFetchedListener listener) {
        if (teacherUsername == null) {
            throw new IllegalArgumentException("Teacher username cannot be null");
        }

        // Reference to the teacher's node
        DatabaseReference teacherSubjectsRef = subjectsReference.child(teacherUsername);
        Log.d("Subject Service", "Query path: " + subjectsReference.toString());

        teacherSubjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("LectureService", "DataSnapshot received: " + dataSnapshot.toString());
                Set<String> subjects = new HashSet<>();
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subjectName = subjectSnapshot.child("subjectName").getValue(String.class);
                    if (subjectName != null) {
                        subjects.add(subjectName);
                    }
                }
                if (listener != null) {
                    listener.onSuccess(new ArrayList<>(subjects));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null) {
                    listener.onFailure(databaseError.toException());
                }
            }
        });
    }

    public void fetchAllSubjects(OnAttendaceSubjectsFetchedListener listener) {
        subjectsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Subject> subjects = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                        Subject subject = subjectSnapshot.getValue(Subject.class);
                        if (subject != null) {
                            subjects.add(subject);
                        }
                    }
                }
                if (listener != null) {
                    listener.onSubjectsFetched(subjects); // Notify listener with fetched subjects
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("SubjectService", "Failed to fetch subjects: " + databaseError.getMessage());
                if (listener != null) {
                    listener.onFailure(databaseError.toException()); // Notify listener on failure
                }
            }
        });
    }



    public interface OnSubjectsFetchedListener {
        void onSuccess(List<String> subjects);
        void onFailure(Exception e);
    }

    public interface OnAttendaceSubjectsFetchedListener {
        void onSubjectsFetched(List<Subject> subjects); // This method is used for fetching subjects
        void onSuccess(List<String> subjects);
        void onFailure(Exception e);
    }

    private static DatabaseReference getSubjectsReference() {
        return FirebaseDatabase.getInstance().getReference("subjects");
    }

    public Task<DataSnapshot> getSubjectsByTeacherId(String teacherId) {
        return getSubjectsReference().child(teacherId).get();
    }

    // Method to fetch all subjects for a given teacher username
    public void fetchAllSubjects(String username, final SubjectsCallback callback) {
        getTeacherIdByUsername(username).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String teacherId = task.getResult();
                if (teacherId != null) {
                    getSubjectsByTeacherId(teacherId).addOnCompleteListener(subjectTask -> {
                        if (subjectTask.isSuccessful()) {
                            DataSnapshot dataSnapshot = subjectTask.getResult();
                            List<Subject> subjects = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Subject subject = snapshot.getValue(Subject.class);
                                if (subject != null) {
                                    subjects.add(subject);
                                }
                            }
                            callback.onSubjectsFetched(subjects);
                        } else {
                            callback.onSubjectsFetched(new ArrayList<>()); // Handle error
                        }
                    });
                } else {
                    callback.onSubjectsFetched(new ArrayList<>()); // Handle error
                }
            } else {
                callback.onSubjectsFetched(new ArrayList<>()); // Handle error
            }
        });
    }


    // Callback interface for fetching subjects
    public interface SubjectsCallback {
        void onSubjectsFetched(List<Subject> subjects);
    }


}
