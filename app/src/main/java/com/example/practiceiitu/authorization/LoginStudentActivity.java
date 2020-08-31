package com.example.practiceiitu.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practiceiitu.R;
import com.example.practiceiitu.StudentDiaryActivity;
import com.example.practiceiitu.registration.RegistrationStudentActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginStudentActivity extends AppCompatActivity {

    private TextView textViewRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_student);
        mAuth = FirebaseAuth.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        db = FirebaseFirestore.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginStudentActivity.this, RegistrationStudentActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onClickLoginStudent(View view){
            final String email = editTextEmail.getText().toString().trim();
            final String password = editTextPassword.getText().toString().trim();
            if (email.isEmpty()) {
            editTextEmail.setError("Email is empty");
            editTextEmail.requestFocus();
            return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                editTextEmail.setError("Email is invalid");
                editTextEmail.requestFocus();
                return;
            }
        if (password.isEmpty()) {
            Toast.makeText(LoginStudentActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isNetworkAvailable()){
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    db.collection("Students").document(mAuth.getCurrentUser().getUid())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            preferences.edit().putString("student", task.getResult().get("fullName").toString()).apply();
                                            preferences.edit().putString("account", "student").apply();
                                            preferences.edit().putString("speciality", task.getResult().get("speciality").toString()).apply();
                                            preferences.edit().putString("group", task.getResult().get("group").toString()).apply();
                                            Toast.makeText(LoginStudentActivity.this, "You are student", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginStudentActivity.this, StudentDiaryActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(LoginStudentActivity.this, "You are not student", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }else {
                    Toast.makeText(LoginStudentActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Check Internet Connection
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
