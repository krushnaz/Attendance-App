package com.ams.services;

import com.ams.models.Course;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CourseService {

    private final DatabaseReference coursesRef;

    public CourseService() {
        // Reference to "courses" node in Firebase
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");
    }

    public void saveCourse(String courseName, String semester, String division, OnCourseSavedListener listener) {
        String courseId = coursesRef.push().getKey(); // Generate a unique ID for the course

        if (courseId != null) {
            // Create a Course object
            Course course = new Course(courseId, courseName, semester, division);

            // Save course to Firebase
            coursesRef.child(courseId).setValue(course)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onFailure(task.getException());
                        }
                    });
        } else {
            listener.onFailure(new Exception("Could not generate a unique ID for the course"));
        }
    }

    public void updateCourse(String courseId, String courseName, String semester, String division, OnCourseSavedListener listener) {
        // Create a Course object with updated details
        Course updatedCourse = new Course(courseId, courseName, semester, division);

        // Update course in Firebase
        coursesRef.child(courseId).setValue(updatedCourse)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void getCourses(CourseCallback callback) {
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Course> courseList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        courseList.add(course);
                    }
                }
                callback.onSuccess(courseList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }



    public void deleteCourse(String courseId, OnCourseDeletedListener listener) {
        // Reference to the specific course in Firebase
        DatabaseReference courseRef = coursesRef.child(courseId);

        courseRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void getCoursesByName(String courseName, CourseCallback callback) {
        // Initialize Firebase or database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("courses");

        databaseReference.orderByChild("courseName").equalTo(courseName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Course> courseList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        courseList.add(course);
                    }
                }
                callback.onSuccess(courseList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }


    public interface OnCourseSavedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnCourseDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface CourseCallback {
        void onSuccess(List<Course> courses);
        void onFailure(Exception e);
    }
}
