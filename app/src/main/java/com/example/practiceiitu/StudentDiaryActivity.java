package com.example.practiceiitu;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practiceiitu.adapters.NoteAdapter;
import com.example.practiceiitu.pojo.Note;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class StudentDiaryActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_PERMISSION_STORAGE = 123;
    private static final int RC_GET_FILE = 777;

    private TextView textViewHelloUser;
    private ImageView imageViewSignOut;
    private ImageView imageViewExport;
    private RecyclerView recyclerViewNotes;
    private FloatingActionButton floatingActionButton;
    private static Dialog dialog;

    private String description;
    int position;
    private String typeOfWork;

    private static boolean isChanging;
    private static String changingDocumentId;

    private static EditText editTextDescription;
    private static TextView textViewDate;
    private static Spinner spinnerTypeOfWork;
    private Button buttonSave;
    private Button buttonNotNow;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference reference;

    private NoteAdapter notesAdapter;

    private SharedPreferences preferences;
    private String fullName;
    private String speciality;
    private String group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_diary);

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        fullName = preferences.getString("student", "User");
        group = preferences.getString("group", "none");
        speciality = preferences.getString("speciality", "none");

        isChanging = false;
        changingDocumentId = null;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        textViewHelloUser = findViewById(R.id.textViewHelloUser);
        imageViewSignOut = findViewById(R.id.imageViewLogOut);
        imageViewExport = findViewById(R.id.imageViewExport);
        floatingActionButton = findViewById(R.id.buttonAdd);

        textViewHelloUser.setText("Hi, " + fullName);

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        notesAdapter = new NoteAdapter(this, true);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotes.setAdapter(notesAdapter);

        recyclerViewNotes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    floatingActionButton.hide();
                } else {
                    floatingActionButton.show();
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        dialog = new Dialog(this);
        dialogCreation();

        imageViewSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(StudentDiaryActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    preferences.edit().putString("student", "User").apply();
                                    preferences.edit().putString("account", "none").apply();
                                    preferences.edit().putString("group", "none").apply();
                                    preferences.edit().putString("speciality", "none").apply();

                                    Intent intent = new Intent(StudentDiaryActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });

        imageViewExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Listener to get all notes from Database at real-time changes
        db.collection("Diaries").whereEqualTo("author", mAuth.getCurrentUser().getEmail()).orderBy("date").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    List<String> documentIds = new ArrayList<>();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        documentIds.add(queryDocumentSnapshots.getDocuments().get(i).getId().toString());
                    }
                    List<Note> notes = queryDocumentSnapshots.toObjects(Note.class); // We get Data then convert it to necessary object class
                    notesAdapter.setNotes(notes, documentIds);
                }
            }
        });
    }

    private void dialogCreation() {
        dialog.setContentView(R.layout.dialog_create_note_of_diary);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        editTextDescription = dialog.findViewById(R.id.editTextDescription);
        textViewDate = dialog.findViewById(R.id.textViewGroup);
        spinnerTypeOfWork = dialog.findViewById(R.id.spinnerTypeOfWork);
        buttonSave = dialog.findViewById(R.id.buttonSave);
        buttonNotNow = dialog.findViewById(R.id.buttonNotNow);

        // Setting Spinner design
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.typeOfDiary)
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinnerTypeOfWork.setAdapter(adapter);

        // Working with Calendar
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(StudentDiaryActivity.this, R.style.my_dialog_theme, new DatePickerDialog.OnDateSetListener() {
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

                        textViewDate.setText(date);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        buttonNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = editTextDescription.getText().toString().trim();
                position = spinnerTypeOfWork.getSelectedItemPosition();
                typeOfWork = getResources().getStringArray(R.array.typeOfDiary)[position];
                String date = textViewDate.getText().toString().trim();

                if (description.isEmpty()) {
                    editTextDescription.setError("Password should be at least 8 characters");
                    editTextDescription.requestFocus();
                    return;
                } else if (date.equals("Date")) {
                    Toast.makeText(StudentDiaryActivity.this, "Choose start date", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date dateInMills = null;
                try {
                    dateInMills = sdf.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Note note = new Note(mAuth.getCurrentUser().getEmail(), description, typeOfWork, dateInMills.getTime());

                if (isChanging) {
                    db.collection("Diaries").document(changingDocumentId).update("description", description,
                            "typeOfWork", typeOfWork,
                            "date", dateInMills.getTime()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(StudentDiaryActivity.this, "Note is changed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(StudentDiaryActivity.this, "Error, note is not changed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    db.collection("Diaries")
                            .add(note)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(StudentDiaryActivity.this, "Note is added", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(StudentDiaryActivity.this, "Error, note is not added", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                dialog.hide();
            }
        });
    }

    public void onClickOpenCreateNoteDialog(View view) {
        isChanging = false;
        editTextDescription.setText("");
        spinnerTypeOfWork.setSelection(0);
        textViewDate.setText("Date");
        dialog.show();
    }

    public static void changeNoteDialog(String description, String date, String typeOfWorkForDiary, String documentId) {
        int spinnerSelectedItemPosition = 0;
        String[] typesOfWorkForDiary = {"courses", "practical work", "project work"};
        for (int i = 0; i < typesOfWorkForDiary.length; i++) {
            if (typesOfWorkForDiary[i].equals(typeOfWorkForDiary)) {
                spinnerSelectedItemPosition = i;
            }
        }

        isChanging = true;
        changingDocumentId = documentId;

        editTextDescription.setText(description);
        textViewDate.setText(date);
        spinnerTypeOfWork.setSelection(spinnerSelectedItemPosition);
        dialog.show();
    }

    @AfterPermissionGranted(RC_PERMISSION_STORAGE)
    private void requestPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
//            Toast.makeText(this, "Permissions opened", Toast.LENGTH_SHORT).show();
            chooseMenuOptions();
        } else {
            EasyPermissions.requestPermissions(this, "We need permissions to work with Docx documents",
                    RC_PERMISSION_STORAGE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void chooseMenuOptions() {
        //creating a popup menu
        Context context = new ContextThemeWrapper(this, R.style.styleOfPopUp);
        PopupMenu popup = new PopupMenu(context, imageViewExport);
        //inflating menu from xml resource
        popup.inflate(R.menu.student_menu_options);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuItemCertificatesAndContracts:
                        Intent intent = new Intent(StudentDiaryActivity.this, FilesUploadActivity.class);
                        preferences.edit().putString("student", fullName).apply();
                        preferences.edit().putString("group", group).apply();
                        preferences.edit().putString("speciality", speciality).apply();
                        startActivity(intent);
                        break;
//                    case R.id.menuItemDownloadContract:
//                        downloadContract();
//                        break;
//                    case R.id.menuItemDeleteContract:
//                        deleteContract();
//                        break;
                    case R.id.menuItemExportTable:
                        createTable();
                        break;
                }
                return false;
            }
        });

        //displaying the popup
        popup.show();

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_GET_FILE && resultCode == RESULT_OK) {
//            if (data != null) {
//                Uri uri = data.getData();
//                if (uri != null) {
//                    uploadContract(uri);
//                }
//            }
//        }
//    }

//    private void uploadContract(Uri uri) {
//        String res = FileUtils.getRealPath(this, uri);
//        final String filePath = res;
//        final String extension = filePath.substring(filePath.lastIndexOf("."));
//        String fileName = fullName + "(" + speciality + "-" + group + ")";
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Uploading...");
//        progressDialog.show();
//        StorageReference storageReference = storage.getReference();
//        final StorageReference referenceToFile = storageReference.child("contracts/" + fileName + extension); // Last Segment is file name without all way to given file
//        referenceToFile.putFile(uri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        progressDialog.dismiss();
//                        Map<String, Object> map = new HashMap<>();
//                        map.put("extension", extension);
//                        db.collection("contracts")
//                                .document(mAuth.getCurrentUser().getEmail())
//                                .set(map);
//                        Toast.makeText(StudentDiaryActivity.this, "Contract is uploaded", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        progressDialog.dismiss();
//                        Toast.makeText(StudentDiaryActivity.this, "Error, Contract is not uploaded", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                        //calculating progress percentage
//                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        //displaying percentage in progress dialog
//                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
//                    }
//                });
//    }

//    private void deleteContract() {
//        db.collection("contracts").document(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()))
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    assert document != null;
//                    if (document.exists()) {
//                        // Getting extension of Contract
//                        String fileName = fullName + "(" + speciality + "-" + group + ")";
//                        String extension = Objects.requireNonNull(document.get("extension")).toString();
//
//                        // Deleting Contracts file
//                        StorageReference storageReference = storage.getReference();
//                        StorageReference referenceToDelete = storageReference.child("contracts/" + fileName + extension);
//                        referenceToDelete.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    db.collection("contracts").document(Objects.requireNonNull(mAuth.getCurrentUser().getEmail())).delete();
//                                    Toast.makeText(StudentDiaryActivity.this, "Contract is deleted", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(StudentDiaryActivity.this, "Contract is not deleted", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//                    } else {
//                        Toast.makeText(StudentDiaryActivity.this, "Your contract is empty", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(StudentDiaryActivity.this, "Your contract is empty", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//    }

//    private void downloadContract() {
//        db.collection("contracts").document(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()))
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    assert document != null;
//                    if (document.exists()) {
//                        // Getting extension of Contract
//                        final String fileName = fullName + "(" + speciality + "-" + group + ")";
//                        final String extension = Objects.requireNonNull(document.get("extension")).toString();
//
//                        // Getting URL To download
//                        StorageReference storageReference = storage.getReference();
//                        StorageReference referenceToDownload = storageReference.child("contracts/" + fileName + extension);
//                        referenceToDownload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                downloadFile(StudentDiaryActivity.this, fileName, extension, DIRECTORY_DOWNLOADS, uri.toString());
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(StudentDiaryActivity.this, "Download error", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                    else {
//                        Toast.makeText(StudentDiaryActivity.this, "Your contract is empty", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(StudentDiaryActivity.this, "Your contract is empty", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//    }

//    private void downloadFile(Context context, String fileName, String fileExtension, String directory, String url) {
//        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        Uri uri = Uri.parse(url);
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalFilesDir(context, directory, fileName + fileExtension);
//        downloadManager.enqueue(request);
//    }


    private void createTable() {
        String folderName = "Internship IITU";

        File folderPath = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        List<Note> noteList = notesAdapter.getNotes();
        if (noteList == null || noteList.size() == 0) {
            Toast.makeText(this, "Your diary is empty", Toast.LENGTH_SHORT).show();
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
            File path = new File(String.valueOf(folderPath), "/" + fullName + date + ".docx");
            document = new XWPFDocument();
            outputStream = new FileOutputStream(path);
            XWPFParagraph paragraph = document.createParagraph();
            XWPFParagraph paragraph00 = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            paragraph00.setAlignment(ParagraphAlignment.LEFT);
            setRun(paragraph.createRun(), "Times New Roman", 11, "000000", "INDUSTRIAL PRACTICE WORK RECORD", true, false);
            setRun(paragraph00.createRun(), "Times New Roman", 11, "000000", "ЗАПИСИ О РАБОТАХ, ВЫПОЛНЕННЫХ НА ПРАКТИКЕ", false, false);

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
            setRun(paragraph0.createRun(), "Times New Roman", 11, "000000", "Month, date", true, false);
            setRun(paragraph01.createRun(), "Times New Roman", 11, "000000", "Месяц, число", false, false);

            // Creating the Other Cells for the First Row
            XWPFTableCell tableCell1 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph1 = tableRow0.getCell(1).addParagraph();
            XWPFParagraph paragraph11 = tableRow0.getCell(1).addParagraph();
            paragraph1.setAlignment(ParagraphAlignment.CENTER);
            paragraph11.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph1.createRun(), "Times New Roman", 11, "000000", "Activities", true, false);
            setRun(paragraph11.createRun(), "Times New Roman", 11, "000000", "Краткое содержание выполненных работ", false, false);

            XWPFTableCell tableCell2 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph2 = tableRow0.getCell(2).addParagraph();
            XWPFParagraph paragraph22 = tableRow0.getCell(2).addParagraph();
            paragraph2.setAlignment(ParagraphAlignment.CENTER);
            paragraph22.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph2.createRun(), "Times New Roman", 11, "000000", "Supervisor signature", true, false);
            setRun(paragraph22.createRun(), "Times New Roman", 11, "000000", "Подпись руководителя", false, false);

            for (int i = 0; i < noteList.size(); i++) {
                // Creating the Next Rows and Cells
                String x = String.valueOf(noteList.get(i).getDate());
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                long milliSeconds = Long.parseLong(x);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(milliSeconds);

                final String time = formatter.format(calendar.getTime());

                XWPFTableRow tableRow = table.createRow();
                XWPFParagraph paragraph3 = tableRow.getCell(0).addParagraph();
                setRun(paragraph3.createRun(), "Times New Roman", 11, "000000", time, false, false);

                XWPFParagraph paragraph4 = tableRow.getCell(1).addParagraph();
                setRun(paragraph4.createRun(), "Times New Roman", 11, "000000", noteList.get(i).getDescription(), false, false);

                XWPFParagraph paragraph5 = tableRow.getCell(2).addParagraph();
                setRun(paragraph5.createRun(), "Times New Roman", 11, "000000", "   ", false, false);
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

//    public String getFileName(Uri uri) {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//        if (result == null) {
//            result = uri.getPath();
//            int cut = result.lastIndexOf('/');
//            if (cut != -1) {
//                result = result.substring(cut + 1);
//            }
//        }
//        return result;
//    }

}
