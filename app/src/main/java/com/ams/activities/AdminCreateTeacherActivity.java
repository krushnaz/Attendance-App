package com.ams.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ams.R;
import com.ams.models.Teacher;
import com.ams.services.TeacherService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

public class AdminCreateTeacherActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private EditText etFullName, etUsername, etPassword, etEmail, etPhoneNumber, etDepartment;
    private Button btnUploadPhoto, btnSaveTeacher;
    private ImageView ivPhoto;
    private TeacherService teacherService;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_teacher);

        // Initialize Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Initialize UI components
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDepartment = findViewById(R.id.etDepartment);
        btnSaveTeacher = findViewById(R.id.btnSaveTeacher);
        ivPhoto = findViewById(R.id.ivPhoto);

        // Initialize TeacherService
        teacherService = new TeacherService();

        btnSaveTeacher.setOnClickListener(v -> {

                saveTeacher(""); // Call with empty photoPath if no image selected

        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    private void saveTeacher(String photoPath) {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Teacher teacher = new Teacher(fullName, username, password, email, phoneNumber, department);
        teacherService.addTeacher(teacher).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Teacher added successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminCreateTeacherActivity.this, AdminDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to add teacher", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
