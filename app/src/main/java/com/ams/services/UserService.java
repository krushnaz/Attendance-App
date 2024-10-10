package com.ams.services;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.ams.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserService {

    private static final String USERS_COLLECTION = "users";
    private final DatabaseReference usersReference;
    private static final String TAG = "UserService";

    public UserService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersReference = database.getReference(USERS_COLLECTION);
    }

    public Task<Void> registerUser(User user) {
        return usersReference.child(user.getFullName()).setValue(user);
    }

    public Query getUserByRollNo(String rollNo) {
        return usersReference.orderByChild("rollNumber").equalTo(rollNo).limitToFirst(1);
    }

    public Query getUserByUsername(String username) {
        return usersReference.orderByChild("username").equalTo(username).limitToFirst(1);
    }

    public Query getAllUsers() {
        return usersReference;
    }

    public Task<Void> deleteUserByRollNo(String rollNo) {
        return getUserByRollNo(rollNo).get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().hasChildren()) {
                DataSnapshot snapshot = task.getResult().getChildren().iterator().next();
                return snapshot.getRef().removeValue();
            } else {
                Log.e(TAG, "User not found for rollNo: " + rollNo);
                throw new IllegalArgumentException("User not found");
            }
        });
    }


    public void fetchUserByRollNo(String rollNo, final UserCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query userQuery = usersRef.orderByChild("rollNumber").equalTo(rollNo);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Extract user data
                User user = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user = snapshot.getValue(User.class);
                    break; // Assuming rollNumber is unique, so only one user will be returned
                }
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }


    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public void fetchStudentCount(final CountCallback callback) {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long studentCount = dataSnapshot.getChildrenCount();
                callback.onCountFetched(studentCount);
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

    public Task<Void> updateUser(User user) {
        // Update user details based on their roll number
        return usersReference.child(user.getFullName()).setValue(user);
    }

}
