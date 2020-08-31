package com.example.practiceiitu.registration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practiceiitu.R;
import com.example.practiceiitu.StudentDiaryActivity;
import com.example.practiceiitu.authorization.LoginStudentActivity;
import com.example.practiceiitu.pojo.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationStudentActivity extends AppCompatActivity {

    private TextView textViewLogin;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextGroup;
    private Spinner spinnerSpecialities;
    private Spinner spinnerCourses;
    private EditText editTextPhoneNumber;
    private EditText editTextFullName;

    private FirebaseAuth mAuth;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_student);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextName);
        editTextGroup = findViewById(R.id.editTextGroup);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        spinnerSpecialities = findViewById(R.id.spinnerSpeciality);
        spinnerCourses = findViewById(R.id.spinnerCourse);
        mAuth = FirebaseAuth.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationStudentActivity.this, LoginStudentActivity.class);
                startActivity(intent);
            }
        });
        ArrayAdapter adapterSpeciality = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.specialities)
        );
        adapterSpeciality.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinnerSpecialities.setAdapter(adapterSpeciality);

        ArrayAdapter adapterCourse = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.courses)
        );
        adapterCourse.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinnerCourses.setAdapter(adapterCourse);
    }

    public void onClickRegistrationStudent(View view) {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String fullName = editTextFullName.getText().toString().replaceAll("\\s+", " ").trim();
        final String group = editTextGroup.getText().toString().trim();
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        int positionSpeciality = spinnerSpecialities.getSelectedItemPosition();
        final String speciality = getResources().getStringArray(R.array.specialities)[positionSpeciality];
        int positionCourse = spinnerCourses.getSelectedItemPosition();
        final String course = getResources().getStringArray(R.array.courses)[positionCourse];

        if (email.isEmpty()) {
            editTextEmail.setError("Email is empty");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Email is invalid");
            editTextEmail.requestFocus();
            return;
        } if (password.isEmpty()) {
            Toast.makeText(RegistrationStudentActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8) {
            Toast.makeText(RegistrationStudentActivity.this, "Password should be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fullName.isEmpty()) {
            editTextFullName.setError("Name and surname required");
            editTextFullName.requestFocus();
            return;
        }
        if (fullName.split(" ").length != 2) {
            editTextFullName.setError("Write name and surname");
            editTextFullName.requestFocus();
            return;
        }
        if (!fullName.matches("[a-zA-Z ]+")) {
            editTextFullName.setError("Write full name in English");
            editTextFullName.requestFocus();
            return;
        }

        if (group.isEmpty()) {
            editTextGroup.setError("Group number required");
            editTextGroup.requestFocus();
            return;
        }
        if (group.length() != 4) {
            editTextGroup.setError("Group number have 4 digits");
            editTextGroup.requestFocus();
            return;
        } if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Phone number name is empty");
            editTextPhoneNumber.requestFocus();
            return;
        } if (!((phoneNumber.length() == 11 && phoneNumber.toCharArray()[0] == '8') || (phoneNumber.length() == 12 && phoneNumber.toCharArray()[0] == '+' && phoneNumber.toCharArray()[1] == '7'))){
            editTextPhoneNumber.setError("Invalid phone number");
            editTextPhoneNumber.requestFocus();
            return;
        }
        if(!isNetworkAvailable()){
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Student student = new Student(
                            email,
                            group,
                            fullName,
                            phoneNumber,
                            speciality, course);
                    FirebaseFirestore.getInstance().collection("Students").document(mAuth.getCurrentUser().getUid())
                            .set(student).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(RegistrationStudentActivity.this, "Registration completed successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegistrationStudentActivity.this, StudentDiaryActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                                preferences.edit().putString("student", fullName).apply();
                                preferences.edit().putString("account", "student").apply();
                                preferences.edit().putString("speciality", speciality).apply();
                                preferences.edit().putString("group", group).apply();
                            }
                            else {
                                Toast.makeText(RegistrationStudentActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(RegistrationStudentActivity.this, "The user is exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

