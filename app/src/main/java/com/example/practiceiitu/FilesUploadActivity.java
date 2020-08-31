package com.example.practiceiitu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import com.example.practiceiitu.adapters.FilesAdapter;
import com.example.practiceiitu.pojo.File;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import pub.devrel.easypermissions.AppSettingsDialog;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


public class FilesUploadActivity extends AppCompatActivity {

    private static final int RC_GET_FILE = 708;

    private static FilesAdapter filesAdapter;
    private static FloatingActionButton floatingActionButtonAddFile;

    private static FirebaseStorage storage;
    private static StorageReference reference;

    private static String folderName;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_upload);

        context = getApplicationContext();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String fullName = preferences.getString("student", "User");
        String group = preferences.getString("group", "none");
        String speciality = preferences.getString("speciality", "none");
        folderName = fullName + "(" + speciality + "-" + group + ")";

        floatingActionButtonAddFile = findViewById(R.id.floatingActionButtonAddFile);

        RecyclerView recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));
        filesAdapter = new FilesAdapter(this, folderName, true);
        recyclerViewFiles.setAdapter(filesAdapter);

        displayFiles();

        recyclerViewFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    floatingActionButtonAddFile.hide();
                } else {
                    floatingActionButtonAddFile.show();
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void onClickUploadFile(View view) {
        // Choosing file types
        String[] mimeTypes =
                {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip",
                        "image/*"};

        // Intent to choose file for Upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        }
        startActivityForResult(intent, RC_GET_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
        }

        if (requestCode == RC_GET_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            uploadFile(uri);
        }
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
                else{
                    filesAdapter.clearFiles();
                }
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileSize(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadFile(Uri uri) {
        String fileName = getFileName(uri);
        int fileSize = Integer.parseInt(getFileSize(uri));

        // Checking file size
        if (fileSize > 10485760) {
            Toast.makeText(context, "Max upload file size 10 mb", Toast.LENGTH_SHORT).show();
            return;
        }


        // Progress Dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final StorageReference referenceToFile = reference.child("files/" + folderName + "/" + fileName); // Last Segment is file name without all way to given file
        referenceToFile.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(FilesUploadActivity.this, "File is uploaded", Toast.LENGTH_SHORT).show();
                        displayFiles();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(FilesUploadActivity.this, "Error, File is not uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        //calculating progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //displaying percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
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