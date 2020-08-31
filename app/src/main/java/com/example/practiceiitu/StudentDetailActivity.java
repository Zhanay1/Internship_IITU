package com.example.practiceiitu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practiceiitu.adapters.NoteAdapter;
import com.example.practiceiitu.pojo.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class StudentDetailActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final int RC_PERMISSION_STORAGE = 124;

    private TextView textViewFullName;
    private TextView textViewSpeciality;
    private TextView textViewGroup;
    private TextView textViewCourse;
    private TextView textViewEmail;
    private TextView textViewPhoneNumber;
    private ImageView imageViewExport;

    private RecyclerView recyclerViewNotes;
    private NoteAdapter notesAdapter;
    private FirebaseStorage storage;

    private String name, speciality, group, course, email, phoneNumber;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        textViewFullName = findViewById(R.id.textViewUser);
        textViewSpeciality = findViewById(R.id.textViewSpeciality);
        textViewGroup = findViewById(R.id.textViewGroup);
        textViewCourse = findViewById(R.id.textViewCourse);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NoteAdapter(this, false);
        recyclerViewNotes.setAdapter(notesAdapter);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name") && intent.hasExtra("speciality") && intent.hasExtra("group")
                && intent.hasExtra("course") && intent.hasExtra("email") && intent.hasExtra("phoneNumber")) {
            name = intent.getStringExtra("name");
            speciality = intent.getStringExtra("speciality");
            group = intent.getStringExtra("group");
            course = intent.getStringExtra("course");
            email = intent.getStringExtra("email");
            phoneNumber = intent.getStringExtra("phoneNumber");
        } else {
            finish();
        }

        textViewFullName.setText(name);
        textViewSpeciality.setText(speciality);
        textViewGroup.setText(group);
        textViewCourse.setText(course);
        textViewEmail.setText(email);
        textViewPhoneNumber.setText(phoneNumber);

        imageViewExport = findViewById(R.id.imageViewExport);
        imageViewExport.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
        db.collection("Diaries").whereEqualTo("author", email).orderBy("date").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
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

    private void createTable(){

        String folderName = "Internship IITU";

        File path = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!path.exists()) {
            path.mkdirs();
        }

        List<Note> noteList = notesAdapter.getNotes();
        if(noteList == null || noteList.size() == 0){
            Toast.makeText(this, "Student diary is empty", Toast.LENGTH_SHORT).show();
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
            document = new XWPFDocument();
            outputStream = new FileOutputStream(new File(String.valueOf(path), "/" + name  + date + ".docx"));
            XWPFParagraph paragraph = document.createParagraph();
            XWPFParagraph paragraph00 = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            paragraph00.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun run = paragraph.createRun();
            run.setFontFamily("Times New Roman");
            run.setBold(true);
            run.setFontSize(12);
            run.setText("INDUSTRIAL PRACTICE WORK RECORD");
            XWPFRun run0 = paragraph00.createRun();
            run0.setFontFamily("Times New Roman");
            run0.setBold(false);
            run0.setFontSize(12);
            run0.setText("ЗАПИСИ О РАБОТАХ, ВЫПОЛНЕННЫХ НА ПРАКТИКЕ");
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
            setRun(paragraph0.createRun() , "Times New Roman" , 11, "000000" , "Month, date" , true, false);
            setRun(paragraph01.createRun() , "Times New Roman" , 11, "000000" , "Месяц, число" , false, false);

            // Creating the Other Cells for the First Row
            XWPFTableCell tableCell1 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph1 = tableRow0.getCell(1).addParagraph();
            XWPFParagraph paragraph11 = tableRow0.getCell(1).addParagraph();
            paragraph1.setAlignment(ParagraphAlignment.CENTER);
            paragraph11.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph1.createRun() , "Times New Roman" , 11, "000000" , "Activities" , true, false);
            setRun(paragraph11.createRun() , "Times New Roman" , 11, "000000" , "Краткое содержание выполненных работ" , false, false);

            XWPFTableCell tableCell2 = tableRow0.addNewTableCell();
            XWPFParagraph paragraph2 = tableRow0.getCell(2).addParagraph();
            XWPFParagraph paragraph22 = tableRow0.getCell(2).addParagraph();
            paragraph2.setAlignment(ParagraphAlignment.CENTER);
            paragraph22.setAlignment(ParagraphAlignment.CENTER);
            setRun(paragraph2.createRun() , "Times New Roman" , 11, "000000" , "Supervisor signature" , true, false);
            setRun(paragraph22.createRun() , "Times New Roman" , 11, "000000" , "Подпись руководителя" , false, false);

            // Creating the Next Rows and Cells
//            XWPFTableRow tableRow0 = table.createRow();
//            tableRow0.getCell(0).setText(" Row 1 Column 0 ");
//            tableRow0.getCell(1).setText(" Row 1 Column 1 ");
//            tableRow0.getCell(2).setText(" Row 1 Column 2 ");

            for (int i = 0; i < noteList.size(); i++) {
                // Creating the Next Rows and Cells
                String x = String.valueOf(noteList.get(i).getDate());
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                long milliSeconds = Long.parseLong(x);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(milliSeconds);

                final String time = formatter.format(calendar.getTime());

                XWPFTableRow tableRow1 = table.createRow();
//                tableRow1.getCell(0).setText(time);
                XWPFParagraph paragraph3 = tableRow1.getCell(0).addParagraph();
                setRun(paragraph3.createRun() , "Times New Roman" , 11, "000000" , time , false, false);
//                tableRow1.getCell(1).setText(noteList.get(i).getDescription());
                XWPFParagraph paragraph4 = tableRow1.getCell(1).addParagraph();
                setRun(paragraph4.createRun() , "Times New Roman" , 11, "000000" , noteList.get(i).getDescription() , false, false);
//                tableRow1.getCell(2).setText(noteList.get(i).getTypeOfWork());
                XWPFParagraph paragraph5 = tableRow1.getCell(2).addParagraph();
                setRun(paragraph5.createRun() , "Times New Roman" , 11, "000000" , "   " , false, false);
            }


            document.write(outputStream);

            Toast.makeText(this, "Saved in Internship IITU folder", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "We had an error while creating the Word Doc" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    private static void setRun (XWPFRun run , String fontFamily , int fontSize , String colorRGB , String text , boolean bold , boolean addBreak) {
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);
        run.setColor(colorRGB);
        run.setText(text);
        run.setBold(bold);
        if (addBreak) run.addBreak();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @AfterPermissionGranted(123)
    private void requestPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            chooseMenuOptions();
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
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

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

    private void chooseMenuOptions() {
        //creating a popup menu
        Context context = new ContextThemeWrapper(this, R.style.styleOfPopUp);
        PopupMenu popup = new PopupMenu(context, imageViewExport);
        //inflating menu from xml resource
        popup.inflate(R.menu.student_detail_menu_options);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuItemCertificatesAndContractsDetail:
                        Intent intent = new Intent(StudentDetailActivity.this, ManagerFileViewActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("group", group);
                        intent.putExtra("speciality", speciality);
                        intent.putExtra("course", course);
                        intent.putExtra("email", email);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                        //downloadFile(StudentDiaryActivity.this, "Mobile", ".pdf", DIRECTORY_DOWNLOADS, "https://www.tutorialspoint.com/java/java_tutorial.pdf");
                        break;
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

//    private void downloadContract() {
//        db.collection("contracts").document(email)
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        // Getting extension of Contract
//                        final String fileName = name + "(" + speciality + "-" + group + ")";
//                        final String extension = document.get("extension").toString();
//
//                        // Getting URL To download
//                        StorageReference storageReference = storage.getReference();
//                        StorageReference referenceToDownload = storageReference.child("contracts/" + fileName + extension);
//                        referenceToDownload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                downloadFile(StudentDetailActivity.this, fileName, extension, DIRECTORY_DOWNLOADS, uri.toString());
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(StudentDetailActivity.this, "Download error", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else {
//                        Toast.makeText(StudentDetailActivity.this, "Student contract is empty", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(StudentDetailActivity.this, "Student contract is empty", Toast.LENGTH_SHORT).show();
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
}
