package com.example.practiceiitu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import android.app.DatePickerDialog;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practiceiitu.pojo.Report;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StudentReportActivity extends AppCompatActivity {

    private FloatingActionButton buttonAdd;
    private Dialog dialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report);
        db = FirebaseFirestore.getInstance();
        dialog = new Dialog(this);
        buttonAdd = findViewById(R.id.buttonAdd);
    }


    public void onClickCreateReport(View view) {
        dialog.setContentView(R.layout.dialog_create_report);

        final EditText editTextNameOfWork = dialog.findViewById(R.id.editTextNameOfWork);
        final EditText editTextDescription = dialog.findViewById(R.id.editTextDescription);
        final TextView textViewStartDate = dialog.findViewById(R.id.textViewGroup);
        final TextView textViewEndDate = dialog.findViewById(R.id.textViewEndDate);
        final Spinner spinnerTypeOfWork = dialog.findViewById(R.id.spinnerTypeOfWork);
        Button buttonSave = dialog.findViewById(R.id.buttonSave);
        Button buttonNotNow = dialog.findViewById(R.id.buttonNotNow);
        buttonNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });


        ArrayAdapter adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.typeOfReport)
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinnerTypeOfWork.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        textViewStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(StudentReportActivity.this, R.style.my_dialog_theme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String date;
                        if (dayOfMonth < 10) {
                            date = "0" + dayOfMonth;
                        } else {
                            date = String.valueOf(dayOfMonth);
                        }

                        if (month < 10) {
                            date = date + "-0" + month + "-" + year;
                        } else {
                            date = date + "-" + month + "-" + year;
                        }

                        textViewStartDate.setText(date);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        textViewEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(StudentReportActivity.this, R.style.my_dialog_theme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String date;
                        if (dayOfMonth < 10) {
                            date = "0" + dayOfMonth;
                        } else {
                            date = String.valueOf(dayOfMonth);
                        }

                        if (month < 10) {
                            date = date + "-0" + month + "-" + year;
                        } else {
                            date = date + "-" + month + "-" + year;
                        }
                        textViewEndDate.setText(date);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOfWork = editTextNameOfWork.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                String startDate = textViewStartDate.getText().toString().trim();
                String endDate = textViewEndDate.getText().toString().trim();
                int position = spinnerTypeOfWork.getSelectedItemPosition();
                String typeOfWork = getResources().getStringArray(R.array.typeOfReport)[position];

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date comparisonStartDate = null;
                Date comparisonEndDate = null;
                try {
                    comparisonStartDate = sdf.parse(startDate);
                    comparisonEndDate = sdf.parse(endDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(nameOfWork.isEmpty()){
                    editTextNameOfWork.setError("Name of work is required");
                    editTextNameOfWork.requestFocus();
                    return;
                }
                if(description.isEmpty()){
                    editTextDescription.setError("Description is required");
                    editTextDescription.requestFocus();
                    return;
                }
                if(startDate.equals("Start date")){
                    Toast.makeText(StudentReportActivity.this, "Choose start date", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(endDate.equals("End date")){
                    Toast.makeText(StudentReportActivity.this, "Choose end date", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!endDate.isEmpty() && comparisonEndDate.compareTo(comparisonStartDate) < 0) {
                    Toast.makeText(StudentReportActivity.this, "Invalid end date", Toast.LENGTH_SHORT).show();
                    return;
                }
                Report report = new Report(nameOfWork, description, typeOfWork, startDate, endDate, System.currentTimeMillis());
                db.collection("Reports").add(report).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(StudentReportActivity.this, "Reports added to data", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(StudentReportActivity.this, "Reports not added to data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.hide();
            }
        });


        dialog.show();
    }
}
