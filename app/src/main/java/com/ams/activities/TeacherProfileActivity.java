package com.ams.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ams.R;
import com.ams.dialogs.TeacherProfileEditDialog;
import com.ams.models.Teacher;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherProfileActivity extends AppCompatActivity {

    private CircleImageView ivProfilePicture; // You can remove this line if you don't need the view at all
    private TextView tvFullName, tvUsername, tvEmail, tvPhoneNumber, tvDepartment;
    private MaterialButton btnEditProfile;
    private DatabaseReference teacherRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        // Initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture); // You can remove this line if you don't need the view at all
        tvFullName = findViewById(R.id.tvFullName);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvDepartment = findViewById(R.id.tvDepartment);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Retrieve username from intent
        String teacherUsername = getIntent().getStringExtra("username");

        // Set up Firebase reference using username
        teacherRef = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUsername);

        // Load teacher profile data
        loadTeacherProfile();

        // Set click listener for Edit Profile button
        btnEditProfile.setOnClickListener(v -> {
            showEditTeacherDialog(teacherUsername);
        });
    }

    private void loadTeacherProfile() {
        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Teacher teacher = snapshot.getValue(Teacher.class);
                if (teacher != null) {
                    tvFullName.setText("" + teacher.getFullName());
                    tvUsername.setText("Username: " + teacher.getUsername());
                    tvEmail.setText("Email: " + teacher.getEmail());
                    tvPhoneNumber.setText("Phone Number: " + teacher.getPhoneNumber());
                    tvDepartment.setText("Department: " + teacher.getDepartment());

                    // Profile picture logic removed
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void showEditTeacherDialog(String username) {
        // Retrieve teacher details from Firebase
        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Teacher teacher = snapshot.getValue(Teacher.class);
                if (teacher != null) {
                    TeacherProfileEditDialog dialog = new TeacherProfileEditDialog();
                    Bundle args = new Bundle();
                    args.putSerializable("teacher", teacher); // Pass the teacher object
                    dialog.setArguments(args);
                    dialog.setOnTeacherUpdatedListener(() -> {
                        // Refresh the profile information after update
                        loadTeacherProfile();
                    });
                    dialog.show(getSupportFragmentManager(), "EditTeacherDialog");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }
}
