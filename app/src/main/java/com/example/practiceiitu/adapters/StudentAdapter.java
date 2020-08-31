package com.example.practiceiitu.adapters;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practiceiitu.R;
import com.example.practiceiitu.pojo.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends  RecyclerView.Adapter<StudentAdapter.StudentViewHolder> implements Filterable {

    List<Student> studentList;
    List<Student> AllstudentList;
    private ClickOnStudentListener clickOnStudentListener;

    private String queryText = "";

    public StudentAdapter() {
        this.studentList = new ArrayList<>();
        this.AllstudentList = new ArrayList<>();
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
        this.AllstudentList = new ArrayList<>(studentList);
        notifyDataSetChanged();
    }

    public List<Student> getAllstudentList() {
        return AllstudentList;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public interface ClickOnStudentListener{
        void ClickOnStudent(int position); //to click the poster image
    }

    public void setClickOnStudentListener(ClickOnStudentListener clickOnStudentListener) {
        this.clickOnStudentListener = clickOnStudentListener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.textViewSpeciality.setText(studentList.get(position).getSpeciality());
        holder.textViewGroup.setText(studentList.get(position).getGroup());
        holder.textViewCourse.setText(studentList.get(position).getCourse());
        String fullName = studentList.get(position).getFullName();
        String group = studentList.get(position).getGroup();

        queryText = queryText.replaceAll("\\s+", " ").replaceAll("[-+.^:,_]","").trim();

        String queryFullName = queryText.replaceAll("\\d", "").replaceAll("\\s+", " ").trim(); // Removing all digits
        String queryGroup = queryText.replaceAll("\\D+", ""); // Getting all digits

        if (fullName.toLowerCase().contains(queryFullName.toLowerCase()) && !queryFullName.isEmpty() && queryFullName != null) {
            int start = fullName.toLowerCase().indexOf(queryFullName.toLowerCase());
            int end = start + queryFullName.length();
            Spannable span = new SpannableString(fullName);
            span.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.textViewName.setText(span);
        } else {
            holder.textViewName.setText(fullName);
        }

        if (group.toLowerCase().contains(queryGroup.toLowerCase()) && !queryGroup.isEmpty() && queryGroup != null) {
            int start = group.toLowerCase().indexOf(queryGroup.toLowerCase());
            int end = start + queryGroup.length();
            Spannable span = new SpannableString(group);
            span.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.textViewGroup.setText(span);
        } else {
            holder.textViewGroup.setText(group);
        }
        holder.textViewSpeciality.setText(studentList.get(position).getSpeciality());
        holder.textViewCourse.setText(studentList.get(position).getCourse());
    }

    public void filterBySpecialityAndCourse(String speciality, String[] allSpecialities, String course, String[] allCourses) {
        List<Student> filteredStudentsList  = new ArrayList<>();
        if (speciality.equals(allSpecialities[0]) && course.equals(allCourses[0])) {
            filteredStudentsList = new ArrayList<>(AllstudentList);
        } else if (!speciality.equals(allSpecialities[0]) && course.equals(allCourses[0])) {
            for (Student student: AllstudentList) {
                if (student.getSpeciality().equals(speciality)) {
                    filteredStudentsList.add(student);
                }
            }
        } else if (speciality.equals(allSpecialities[0]) && !course.equals(allCourses[0])) {
            for (Student student: AllstudentList) {
                if (student.getCourse().equals(course)) {
                    filteredStudentsList.add(student);
                }
            }
        } else {
            for (Student student: AllstudentList) {
                if (student.getSpeciality().equals(speciality) && student.getCourse().equals(course)) {
                    filteredStudentsList.add(student);
                }
            }
        }

        studentList.clear();
        studentList.addAll(filteredStudentsList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    @Override
    public Filter getFilter() {
        return notesFilter;
    }
    private Filter notesFilter = new Filter() {
        // Run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Student> filteredStudentsList  = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredStudentsList.addAll(AllstudentList);
            } else {
                String queryText = constraint.toString().replaceAll("\\s+", " ").replaceAll("[-+.^:,_]","").trim();
                String queryFullName = queryText.replaceAll("\\d", "").replaceAll("\\s+", " ").trim(); // Removing all digits
                String queryGroup = queryText.replaceAll("\\D+", "").replaceAll("\\s+", " ").trim(); ; // Getting all digits
                for (Student student : AllstudentList) {
                    if (student.getFullName().toLowerCase().contains(queryFullName.toLowerCase()) && student.getGroup().contains(queryGroup)) {
                        filteredStudentsList.add(student);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredStudentsList;
            return results;
        }
        // Run on UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            studentList.clear();
            studentList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    class StudentViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;
        private TextView textViewSpeciality;
        private TextView textViewGroup;
        private TextView textViewCourse;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickOnStudentListener != null){
                        clickOnStudentListener.ClickOnStudent(getAdapterPosition());
                    }
                }
            });
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCourse = itemView.findViewById(R.id.textViewCourse);
            textViewGroup = itemView.findViewById(R.id.textViewGroup);
            textViewSpeciality = itemView.findViewById(R.id.textViewSpeciality);
        }
    }
}
