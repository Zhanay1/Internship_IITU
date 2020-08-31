package com.example.practiceiitu.adapters;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practiceiitu.FilesUploadActivity;
import com.example.practiceiitu.ManagerFileViewActivity;
import com.example.practiceiitu.R;
import com.example.practiceiitu.pojo.File;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    private List<File> files;
    private Context context;
    private boolean isEditable;

    private StorageReference reference;

    private String folderName;

    public FilesAdapter(Context context, String folderName, boolean isEditable) {
        this.files = new ArrayList<>();

        this.context = context;
        this.folderName = folderName;
        this.isEditable = isEditable;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
    }

    public List<File> getFiles() {
        return files;
    }

    public void addFile(File file) {
        this.files.add(file);
        notifyItemChanged(this.files.size() - 1);
    }

    public void clearFiles() {
        files.clear();
        notifyDataSetChanged();
    }

    public void setFiles(List<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilesViewHolder holder, final int position) {
        final String fileName = files.get(position).getName();
        final String extension = files.get(position).getExtension();
        holder.textViewFileName.setText(fileName);
        holder.textViewDate.setText(files.get(position).getDate());

        if (isEditable) {
            holder.textViewMenuOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    context = new ContextThemeWrapper(context, R.style.styleOfPopUp);
                    PopupMenu popup = new PopupMenu(context, holder.textViewMenuOptions);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.file_menu_options);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuItemDownloadFile:
                                    FilesUploadActivity.downloadFile(fileName);
                                    break;
                                case R.id.menuItemDeleteFile:
                                    deleteFile(fileName);
                                    break;
                            }
                            return false;
                        }
                    });

                    //displaying the popup
                    popup.show();
                }
            });
        }
        else {
            holder.textViewMenuOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    context = new ContextThemeWrapper(context, R.style.styleOfPopUp);
                    PopupMenu popup = new PopupMenu(context, holder.textViewMenuOptions);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.manager_view_file_menu_options);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuItemDownloadFile:
                                    ManagerFileViewActivity.downloadFile(fileName);
                                    break;
                            }
                            return false;
                        }
                    });

                    //displaying the popup
                    popup.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class FilesViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewFileName;
        private TextView textViewDate;
        private TextView textViewMenuOptions;

        public FilesViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewMenuOptions = itemView.findViewById(R.id.textViewMenuOptions);
        }
    }

    private void deleteFile(String fileName) {
        reference.child("files/" + folderName + "/" + fileName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "File is deleted", Toast.LENGTH_SHORT).show();
                FilesUploadActivity.displayFiles();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error, File is not deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}