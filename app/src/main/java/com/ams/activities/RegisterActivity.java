package com.ams.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ams.R;
import com.ams.models.Course;
import com.ams.models.User;
import com.ams.services.UserService;
import com.ams.services.CourseService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101; // Request code for camera intent
    private static final String TAG = "RegisterActivity";

    private EditText etFullName, etRollNumber, etPassword, etMobileNumber;
    private Spinner spinnerCourseName, spinnerSemester, spinnerDivision;
    private Button btnRegister, btnGoToLogin, btnCaptureFace;
    private String faceImageUrl; // Variable to hold the uploaded image URL
    private UserService userService;
    private CourseService courseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userService = new UserService();
        courseService = new CourseService();

        // Initialize UI components
        initUIComponents();

        // Request camera permissions if not already granted
        if (checkCameraPermission()) {
            // Permission granted, can directly capture image
        } else {
            requestCameraPermission();
        }

        // Set button click listeners
        btnGoToLogin.setOnClickListener(view -> goToLogin());
        btnCaptureFace.setOnClickListener(view -> captureFaceImage());

        btnRegister.setOnClickListener(view -> registerUser());

        // Initially hide the register button
        btnRegister.setVisibility(View.GONE);
    }

    private void initUIComponents() {
        etFullName = findViewById(R.id.etFullName);
        etRollNumber = findViewById(R.id.etRollNumber);
        etPassword = findViewById(R.id.etPassword);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        spinnerCourseName = findViewById(R.id.spinnerClassName);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerDivision = findViewById(R.id.spinnerDivision);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnCaptureFace = findViewById(R.id.btnCaptureFace);

        setupSpinners();
    }

    private void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupSpinners() {
        courseService.getCourses(new CourseService.CourseCallback() {
            @Override
            public void onSuccess(List<Course> courses) {
                HashSet<String> courseNamesSet = new HashSet<>();
                HashSet<String> semestersSet = new HashSet<>();
                HashSet<String> divisionsSet = new HashSet<>();

                for (Course course : courses) {
                    courseNamesSet.add(course.getCourseName());
                    semestersSet.add(course.getSemester());
                    divisionsSet.add(course.getDivision());
                }

                setSpinnerAdapter(spinnerCourseName, new ArrayList<>(courseNamesSet));
                setSpinnerAdapter(spinnerSemester, new ArrayList<>(semestersSet));
                setSpinnerAdapter(spinnerDivision, new ArrayList<>(divisionsSet));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RegisterActivity.this, "Failed to load course data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerAdapter(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.spinner_selected_item, items) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String rollNumber = etRollNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String courseName = spinnerCourseName.getSelectedItem().toString().trim();
        String semester = spinnerSemester.getSelectedItem().toString().trim();
        String division = spinnerDivision.getSelectedItem().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(rollNumber) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(mobileNumber) || TextUtils.isEmpty(courseName) ||
                TextUtils.isEmpty(semester) || TextUtils.isEmpty(division)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidMobileNumber(mobileNumber)) {
            Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(
                fullName,
                courseName,
                rollNumber,
                division,
                mobileNumber,
                password,
                semester,
                faceImageUrl
        );

        userService.registerUser(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidMobileNumber(String mobileNumber) {
        return mobileNumber.length() == 10 && TextUtils.isDigitsOnly(mobileNumber);
    }

    private boolean checkCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void captureFaceImage() {
        if (checkCameraPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Uri photoUri = getImageUri(imageBitmap);
            uploadImageToFirebase(photoUri);
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg");
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
        }
        return Uri.fromFile(photoFile);
    }

    private void uploadImageToFirebase(Uri photoUri) {
        // Ensure the roll number is initialized (replace 'etRollNumber' with your EditText variable)
        String rollNumber = etRollNumber.getText().toString().trim();
        if (rollNumber.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Roll number is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a unique file name using the entered roll number
        String uniqueFileName = rollNumber + ".jpg"; // Using the roll number as the file name
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("faces/" + uniqueFileName);

        // Start the upload
        UploadTask uploadTask = storageReference.putFile(photoUri);

        // Handle successful upload
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                Toast.makeText(RegisterActivity.this, "Image uploaded successfully: " + imageUrl, Toast.LENGTH_SHORT).show();
                // Store the image URL and make the register button visible
                faceImageUrl = imageUrl;
                btnRegister.setVisibility(View.VISIBLE); // Show the register button
            }).addOnFailureListener(e -> {
                Toast.makeText(RegisterActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(RegisterActivity.this, "Failed to upload image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureFaceImage(); // Capture image after permission is granted
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

