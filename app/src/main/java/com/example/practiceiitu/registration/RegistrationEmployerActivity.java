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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practiceiitu.ManagerActivity;
import com.example.practiceiitu.R;
import com.example.practiceiitu.authorization.LoginEmployerActivity;
import com.example.practiceiitu.pojo.Employer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationEmployerActivity extends AppCompatActivity {

    private TextView textViewLogin;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextCompanyName;
    private EditText editTextPhoneNumber;
    private EditText editTextRegCode;
    final String[] regCodeOfDatabase = {null};
    private SharedPreferences preferences;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_employer);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextCompanyName = findViewById(R.id.editTextCompanyName);
        editTextRegCode = findViewById(R.id.editTextRegCode);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationEmployerActivity.this, LoginEmployerActivity.class);
                startActivity(intent);
            }
        });
        db.collection("other data").document("reg code").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())  {
                    regCodeOfDatabase[0] = task.getResult().get("code").toString();
                }
            }
        });
    }

    public void onClickRegistrationEmployer(View view){
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String companyName = editTextCompanyName.getText().toString().trim();
        final String regCode = editTextRegCode.getText().toString().trim();
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Email is empty");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Email is invalid");
            editTextEmail.requestFocus();
            return;
        }if (password.isEmpty()) {
            Toast.makeText(RegistrationEmployerActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8) {
            Toast.makeText(RegistrationEmployerActivity.this, "Password should be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (companyName.isEmpty()) {
            editTextCompanyName.setError("Department name is required");
            editTextCompanyName.requestFocus();
            return;
        } if (regCode.isEmpty()) {
            editTextRegCode.setError("Registration code required");
            editTextRegCode.requestFocus();
            return;
        }
        if (!regCode.equals(regCodeOfDatabase[0])) {
            editTextRegCode.setError("Registration code invalid");
            editTextRegCode.requestFocus();
            return;
        }if (phoneNumber.isEmpty()) {
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
                    Employer employer = new Employer(
                            email,
                            companyName,
                            phoneNumber
                    );
                    Toast.makeText(RegistrationEmployerActivity.this, "Registration completed successfully", Toast.LENGTH_SHORT).show();
                    FirebaseFirestore.getInstance().collection("Employers").document(mAuth.getCurrentUser().getUid())
                            .set(employer).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                preferences.edit().putString("employer", companyName).apply();
                                preferences.edit().putString("account", "employer").apply();
                                Intent intent = new Intent(RegistrationEmployerActivity.this, ManagerActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                                Toast.makeText(RegistrationEmployerActivity.this, "Data added successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(RegistrationEmployerActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(RegistrationEmployerActivity.this, "The user is exist", Toast.LENGTH_SHORT).show();
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
