package com.ams.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.ams.R;
import com.ams.models.Teacher;
import com.ams.models.User;
import com.ams.services.TeacherService;
import com.ams.services.UserService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private Spinner spinnerUserType;
    private Button btnLogin;
    private TextInputLayout usernameLayout, rollNoLayout, passwordLayout;
    private TextInputEditText etUsername, etRollNo, etPassword;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private UserService userService;
    private TeacherService teacherService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        spinnerUserType = findViewById(R.id.spinnerUserType);
        btnLogin = findViewById(R.id.btnLogin);
        usernameLayout = findViewById(R.id.usernameLayout);
        rollNoLayout = findViewById(R.id.rollNoLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        etUsername = findViewById(R.id.etUsername);
        etRollNo = findViewById(R.id.etRollNo);
        etPassword = findViewById(R.id.etPassword);
        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);

        // Initialize services
        userService = new UserService();
        teacherService = new TeacherService();

        // Set up the user type spinner
        Spinner spinnerUserType = findViewById(R.id.spinnerUserType);

// Create an ArrayAdapter using the custom selected item layout
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_selected_item, getResources().getStringArray(R.array.user_types)) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                // Use the custom dropdown item layout
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Set the dropdown item's text color (black in this case)
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };

// Set the custom dropdown layout for the items when the dropdown is opened
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

// Apply the adapter to the spinner
        spinnerUserType.setAdapter(adapter);


        // Handle Spinner selection
        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();

                if (selectedType.equals("Student")) {
                    rollNoLayout.setVisibility(View.VISIBLE);
                    usernameLayout.setVisibility(View.GONE);
                } else if (selectedType.equals("Admin") || selectedType.equals("Teacher")) {
                    usernameLayout.setVisibility(View.VISIBLE);
                    rollNoLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                usernameLayout.setVisibility(View.GONE);
                rollNoLayout.setVisibility(View.GONE);
            }
        });




        // Handle Login button click
        btnLogin.setOnClickListener(v -> validateLogin());

        // Set the click listener for the "Go to Register" button
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void validateLogin() {
        String userType = spinnerUserType.getSelectedItem().toString();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String rollNo = etRollNo.getText().toString().trim();

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userType.equals("Student")) {
            if (rollNo.isEmpty()) {
                Toast.makeText(this, "Please enter your Roll No", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchStudentRollNoAndSignIn(rollNo, password);

        } else if (userType.equals("Admin")) {
            validateAdminLogin(username, password);
        } else if (userType.equals("Teacher")) {
            fetchTeacherUsernameAndSignIn(username, password);
        }
    }

    private void fetchTeacherUsernameAndSignIn(String username, String password) {
        teacherService.getTeacherByUsername(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Teacher teacher = dataSnapshot.getValue(Teacher.class);
                    if (teacher != null && teacher.getPassword().equals(password)) {
                        // Store the username in SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", username);
                        editor.putString("userType", "Teacher");
                        editor.apply();

                        // Navigate to Teacher Dashboard
                        navigateToDashboard("Teacher");
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Teacher Username or Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Teacher not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error fetching teacher data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStudentRollNoAndSignIn(String rollNo, String password) {
        userService.getUserByRollNo(rollNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getPassword().equals(password)) {
                            // Store the roll number in SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("rollNo", rollNo);
                            editor.putString("userType", "Student");
                            editor.apply();

                            // Navigate to Student Dashboard
                            navigateToDashboard("Student");
                            return;
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Invalid Roll No or Password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAdminLogin(String username, String password) {
        if (username.equals("admin") && password.equals("123")) {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("adminUsername", username);
            editor.putString("userType", "Admin");
            editor.apply();
            navigateToDashboard("Admin");
        } else {
            Toast.makeText(this, "Invalid Admin Username or Password", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        switch (userType) {
            case "Admin":
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                break;
            case "Student":
                intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                break;
            case "Teacher":
                intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Invalid user type selected", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard() {
        navigateToDashboard("Student"); // Default to Student Dashboard
    }

    private void printCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("LoginActivity", "Current user UID: " + currentUser.getUid());
        } else {
            Log.d("LoginActivity", "No current user.");
        }
    }
}
