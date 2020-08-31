package com.example.practiceiitu;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practiceiitu.adapters.StudentAdapter;
import com.example.practiceiitu.pojo.Student;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ManagerActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private Spinner spinnerSpecialities;
    private Spinner spinnerCourses;
    private FirebaseFirestore db;
    private List<Student> studentList;
    private StudentAdapter studentAdapter;
    private RecyclerView recyclerViewStudents;
    private SharedPreferences preferences;

    private static final int RC_PERMISSION_STORAGE = 125;

    private String name;
    private String group;
    private String speciality;
    private String course;
    private String email;
    private String phoneNumber;
    private String courseOfSpinner;
    private String specialityOfSpinner;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        db = FirebaseFirestore.getInstance();
        spinnerCourses = findViewById(R.id.spinnerCourses);
        spinnerSpecialities = findViewById(R.id.spinnerSpecialities);
        ArrayAdapter<String> adapterSpecialities = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.specialitiesOfManagerActivity)
        );
        adapterSpecialities.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinnerSpecialities.setAdapter(adapterSpecialities);

        ArrayAdapter<String> adapterCourses = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.coursesOfManagerActivity)
        );
        adapterCourses.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinnerCourses.setAdapter(adapterCourses);
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter();
        recyclerViewStudents.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        recyclerViewStudents.setAdapter(studentAdapter);

        studentAdapter.setClickOnStudentListener(new StudentAdapter.ClickOnStudentListener() {
            @Override
            public void ClickOnStudent(int position) {
                Intent intent = new Intent(ManagerActivity.this, StudentDetailActivity.class);
                name = studentList.get(position).getFullName();
                group = studentList.get(position).getGroup();
                speciality = studentList.get(position).getSpeciality();
                course = studentList.get(position).getCourse();
                email = studentList.get(position).getEmail();
                phoneNumber = studentList.get(position).getPhoneNumber();
                intent.putExtra("name", name);
                intent.putExtra("group", group);
                intent.putExtra("speciality", speciality);
                intent.putExtra("course", course);
                intent.putExtra("email", email);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });

        spinnerSpecialities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerCourses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        MenuInflater inflaterMenu = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);
        inflaterMenu.inflate(R.menu.menu_item, menu);
        MenuItem searchItem = menu.findItem(R.id.menuItemSearch);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                spinnerCourses.setSelection(0);
                spinnerSpecialities.setSelection(0);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                studentAdapter.getFilter().filter(query);
                studentAdapter.setQueryText(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                studentAdapter.getFilter().filter(newText);
                studentAdapter.setQueryText(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.signOutItem) {
            AuthUI.getInstance()
                    .signOut(ManagerActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // We open FireBase Authorization UI Page
                                preferences.edit().putString("employer", "Company").apply();
                                preferences.edit().putString("account", "none").apply();
                                Intent intent = new Intent(ManagerActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    });
        }else if (id == R.id.exportItem) {
            requestPermission();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Listener to get all notes from Database at real-time changes
        db.collection("Students").orderBy("fullName").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    studentList = queryDocumentSnapshots.toObjects(Student.class); // We get Data then convert it to necessary object class
                    studentAdapter.setStudentList(studentList);
                    setFilters();
                }
            }
        });
    }

    @AfterPermissionGranted(123)
    private void requestPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
//            Toast.makeText(this, "Permissions opened", Toast.LENGTH_SHORT).show();
            createTable();
        } else {
            EasyPermissions.requestPermissions(this, "We need permissions to work with Docx documents",
                    123, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {}

    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
        }
    }

    private void createTable() {
        String folderName = "Internship IITU";

        File folderPath = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        List<Student> studentList = studentAdapter.getAllstudentList();
        if(studentList == null || studentList.size() == 0){
            Toast.makeText(this, "Students list is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        XWPFDocument document = null;
        FileOutputStream outputStream = null;

        DateTimeFormatter dtf = null;
        LocalDateTime now = null;
        String date;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("(dd-MM-yyyy HH-mm-ss)");
            now = LocalDateTime.now();
            date = dtf.format(now);
        } else {
            date = "";
        }

        try {
            File path = new File(String.valueOf(folderPath), "/" + "All Students" + date + ".docx");
            document = new XWPFDocument();
            outputStream = new FileOutputStream(path);

            // Create a Simple Table using the document.
            XWPFTable table = document.createTable();
            table.setCellMargins(0, 150, 0, 150);

            // Now add Rows and Columns to the Table.
            // Creating the First Row
            XWPFTableRow tableRow0 = table.getRow(0);
            // Creating the First Cell
            XWPFTableCell tableCell0 = tableRow0.getCell(0);
            XWPFParagraph paragraph0 = tableRow0.getCell(0).addParagraph();
            XWPFParagraph paragraph01 = tableRow0.getCell(0).addParagraph();
            paragraph0.setAlignment(ParagraphAlignment.CENTER);
            paragraph01.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph0.createRun(), "Times New Roman", 11, "000000", "Students", true, false);
            setRun(paragraph01.createRun(), "Times New Roman", 11, "000000", "Студенты", false, false);

            // Creating the Other Cells for the First Row
            XWPFTableCell tableCell1 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph1 = tableRow0.getCell(1).addParagraph();
            XWPFParagraph paragraph11 = tableRow0.getCell(1).addParagraph();
            paragraph1.setAlignment(ParagraphAlignment.CENTER);
            paragraph11.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph1.createRun(), "Times New Roman", 11, "000000", "Group", true, false);
            setRun(paragraph11.createRun(), "Times New Roman", 11, "000000", "Группа", false, false);

            XWPFTableCell tableCell2 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph2 = tableRow0.getCell(2).addParagraph();
            XWPFParagraph paragraph22 = tableRow0.getCell(2).addParagraph();
            paragraph2.setAlignment(ParagraphAlignment.CENTER);
            paragraph22.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph2.createRun(), "Times New Roman", 11, "000000", "Course", true, false);
            setRun(paragraph22.createRun(), "Times New Roman", 11, "000000", "Курс", false, false);

            XWPFTableCell tableCell3 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph3 = tableRow0.getCell(3).addParagraph();
            XWPFParagraph paragraph33 = tableRow0.getCell(3).addParagraph();
            paragraph3.setAlignment(ParagraphAlignment.CENTER);
            paragraph33.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph3.createRun(), "Times New Roman", 11, "000000", "Email", true, false);
            setRun(paragraph33.createRun(), "Times New Roman", 11, "000000", "Почта", false, false);

            XWPFTableCell tableCell4 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph4 = tableRow0.getCell(4).addParagraph();
            XWPFParagraph paragraph44 = tableRow0.getCell(4).addParagraph();
            paragraph4.setAlignment(ParagraphAlignment.CENTER);
            paragraph44.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph4.createRun(), "Times New Roman", 11, "000000", "Phone number", true, false);
            setRun(paragraph44.createRun(), "Times New Roman", 11, "000000", "Телефон", false, false);

            for (int i = 0; i < studentList.size(); i++) {
                // Creating the Next Rows and Cells
                XWPFTableRow tableRow1 = table.createRow();

                XWPFParagraph paragraph5 = tableRow1.getCell(0).addParagraph();
                setRun(paragraph5.createRun(), "Times New Roman", 11, "000000", studentList.get(i).getFullName(), false, false);

                XWPFParagraph paragraph6 = tableRow1.getCell(1).addParagraph();
                setRun(paragraph6.createRun(), "Times New Roman", 11, "000000", studentList.get(i).getSpeciality() + "-" + studentList.get(i).getGroup(), false, false);

                XWPFParagraph paragraph7 = tableRow1.getCell(2).addParagraph();
                setRun(paragraph7.createRun(), "Times New Roman", 11, "000000", studentList.get(i).getCourse(), false, false);

                XWPFParagraph paragraph8 = tableRow1.getCell(3).addParagraph();
                setRun(paragraph8.createRun(), "Times New Roman", 11, "000000", studentList.get(i).getEmail(), false, false);

                XWPFParagraph paragraph9 = tableRow1.getCell(4).addParagraph();
                setRun(paragraph9.createRun(), "Times New Roman", 11, "000000", studentList.get(i).getPhoneNumber(), false, false);
            }

            document.write(outputStream);

            Toast.makeText(this, "Saved in Internship IITU folder", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "We had an error while creating the Word Doc " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private static void setRun(XWPFRun run, String fontFamily, int fontSize, String colorRGB, String text, boolean bold, boolean addBreak) {
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);
        run.setColor(colorRGB);
        run.setText(text);
        run.setBold(bold);
        if (addBreak) run.addBreak();
    }

    private int getColumnCount() {
        int orientation = getResources().getConfiguration().orientation;
        int columnNumber;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape Mode
            columnNumber = 2;
        } else {
            // Portrait Mode
            columnNumber = 1;
        }
        return columnNumber;
    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(1);
    }
    private void setFilters(){
        specialityOfSpinner = spinnerSpecialities.getSelectedItem().toString();
        courseOfSpinner = spinnerCourses.getSelectedItem().toString();

        String[] specialities = getResources().getStringArray(R.array.specialitiesOfManagerActivity);
        String[] courses = getResources().getStringArray(R.array.coursesOfManagerActivity);

        studentAdapter.filterBySpecialityAndCourse(specialityOfSpinner, specialities, courseOfSpinner, courses);
    }
}
