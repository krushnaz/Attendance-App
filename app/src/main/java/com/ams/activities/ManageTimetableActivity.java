package com.ams.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ams.R;
import com.ams.adapters.CourseAdapter;
import com.ams.dialogs.CreateCourseDialog;
import com.ams.dialogs.DeleteCourseDialog;
import com.ams.dialogs.EditCourseDialog;
import com.ams.dialogs.ViewCourseDialog;
import com.ams.models.Course;
import com.ams.services.CourseService;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ManageTimetableActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private CourseService courseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_timetable);

        recyclerView = findViewById(R.id.recyclerViewTimetables);
        MaterialButton btnCreateCourse = findViewById(R.id.btnCreateTimetable);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(new CourseAdapter.OnItemClickListener() {
            @Override
            public void onViewClick(int position) {
                Course course = courseAdapter.getCourse(position);
                if (course != null) {
                    // Show ViewCourseDialog
                    ViewCourseDialog dialog = ViewCourseDialog.newInstance(course);
                    dialog.show(getSupportFragmentManager(), "ViewCourseDialog");
                } else {
                    Log.d("ManageTimetableActivity", "Course data is not available");
                }
            }

            @Override
            public void onEditClick(int position) {
                Course course = courseAdapter.getCourse(position);
                if (course != null) {
                    // Show EditCourseDialog
                    EditCourseDialog dialog = EditCourseDialog.newInstance(course);
                    dialog.show(getSupportFragmentManager(), "EditCourseDialog");
                }
            }

            @Override
            public void onDeleteClick(int position) {
                Course course = courseAdapter.getCourse(position);
                if (course != null) {
                    // Show DeleteCourseDialog
                    DeleteCourseDialog dialog = DeleteCourseDialog.newInstance(course, new DeleteCourseDialog.OnCourseDeletedListener() {
                        @Override
                        public void onSuccess() {
                            // Refresh the list
                            loadCourses();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Handle error
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "DeleteCourseDialog");
                }
            }

            @Override
            public void onItemClick(int position) {
                Course course = courseAdapter.getCourse(position);
                if (course != null) {
                    // Navigate to CreateTimetableActivity
                    Intent intent = new Intent(ManageTimetableActivity.this, CreateTimetableActivity.class);
                    intent.putExtra("course", course);  // Pass the course data to the new activity
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(courseAdapter);

        courseService = new CourseService();
        loadCourses();

        btnCreateCourse.setOnClickListener(v -> {
            // Show CreateCourseDialog
            CreateCourseDialog dialog = new CreateCourseDialog(courseService);
            dialog.show(getSupportFragmentManager(), "CreateCourseDialog");
        });
    }

    private void loadCourses() {
        courseService.getCourses(new CourseService.CourseCallback() {
            @Override
            public void onSuccess(List<Course> courses) {
                courseAdapter.updateCourses(courses);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error
            }
        });
    }
}
