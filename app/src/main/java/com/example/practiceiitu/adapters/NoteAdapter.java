package com.example.practiceiitu.adapters;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practiceiitu.R;
import com.example.practiceiitu.StudentDiaryActivity;
import com.example.practiceiitu.pojo.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {


    private FirebaseFirestore db;
    public List<String> documentIds;
    public List<Note> notes;
    private Context context;
    private boolean isEditable;

    public NoteAdapter(Context context, boolean isEditable) {
        this.notes = new ArrayList<>();
        documentIds = new ArrayList<>();
        this.context = context;
        this.isEditable = isEditable;
        db = FirebaseFirestore.getInstance();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes, List<String> documentIds) {
        this.notes = notes;
        this.documentIds = documentIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder holder, final int position) {
        String x = String.valueOf(notes.get(position).getDate());
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        long milliSeconds= Long.parseLong(x);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        holder.textViewDate.setText(formatter.format(calendar.getTime()));
        holder.textViewDescription.setText(notes.get(position).getDescription());
        holder.textViewTypeOfWork.setText(notes.get(position).getTypeOfWork());
        final String date = formatter.format(calendar.getTime());
        final String typeOfWork = notes.get(position).getTypeOfWork();
        final String description = notes.get(position).getDescription();
        if(isEditable) {
            holder.textViewOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //creating a popup menu
                    context = new ContextThemeWrapper(context, R.style.styleOfPopUp);
                    PopupMenu popup = new PopupMenu(context, holder.textViewOptions);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.note_item_menu_options);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuItemChange:
                                    StudentDiaryActivity.changeNoteDialog(description, date, typeOfWork, documentIds.get(position));
                                    break;
                                case R.id.menuItemDelete:
                                    db.collection("Diaries").document(documentIds.get(position))
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                }
                                            });
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });
        }else{
            holder.textViewOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewDate;
        private TextView textViewDescription;
        private TextView textViewTypeOfWork;
        private TextView textViewOptions;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewGroup);
            textViewDescription = itemView.findViewById(R.id.textViewSpeciality);
            textViewTypeOfWork = itemView.findViewById(R.id.textViewName);
            textViewOptions = itemView.findViewById(R.id.textViewOptions);
        }
    }
}
