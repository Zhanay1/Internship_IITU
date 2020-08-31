package com.example.practiceiitu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.practiceiitu.adapters.FilesAdapter;
import com.example.practiceiitu.pojo.File;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ManagerFileViewActivity extends AppCompatActivity {

    private static FilesAdapter filesAdapter;

    private static FirebaseStorage storage;
    private static StorageReference reference;

    private static String folderName;

    private static Context context;

    private String name;
    private String group;
    private String speciality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_file_view);

        context = getApplicationContext();

        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name") && intent.hasExtra("speciality") && intent.hasExtra("group")
                && intent.hasExtra("course") && intent.hasExtra("email") && intent.hasExtra("phoneNumber")) {
            name = intent.getStringExtra("name");
            speciality = intent.getStringExtra("speciality");
            group = intent.getStringExtra("group");
        } else {
            finish();
        }

        folderName = name + "(" + speciality + "-" + group + ")";
        RecyclerView recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));
        filesAdapter = new FilesAdapter(this, folderName, false);
        recyclerViewFiles.setAdapter(filesAdapter);

        displayFiles();

    }

    public static void displayFiles() {
        reference.child("files/" + folderName + "/").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                if (listResult != null && !listResult.getItems().isEmpty()) {
                    filesAdapter.clearFiles();
                    for (int i = 0; i < listResult.getItems().size(); i++) {
                        final StorageReference referenceToFile = listResult.getItems().get(i);
                        referenceToFile.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                if (storageMetadata != null) {
                                    // Converter of timeMills to DateFormat
                                    long time = storageMetadata.getCreationTimeMillis();
                                    String x = String.valueOf(time);
                                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                                    long milliSeconds = Long.parseLong(x);
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(milliSeconds);

                                    final String date = formatter.format(calendar.getTime());
                                    String fileName = storageMetadata.getName();
                                    String extension = fileName.substring(fileName.lastIndexOf("."));

                                    File file = new File(fileName, date, extension);
                                    filesAdapter.addFile(file);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public static void downloadFile(final String fileName) {
        // Getting URL To download
        StorageReference storageReference = storage.getReference();
        StorageReference referenceToDownload = storageReference.child("files/" + folderName + "/" + fileName);
        referenceToDownload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                download(context, fileName, DIRECTORY_DOWNLOADS, uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Download error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void download(Context context, String fileName, String directory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, directory, fileName);
        downloadManager.enqueue(request);
    }

}
