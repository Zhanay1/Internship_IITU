package com.example.practiceiitu.pojo;

public class Note {
    private String author;
    private String description;
    private String typeOfWork;
    private long date;

    public Note(String author, String description, String typeOfWork, long date) {
        this.author = author;
        this.description = description;
        this.typeOfWork = typeOfWork;
        this.date = date;
    }
    public Note() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeOfWork() {
        return typeOfWork;
    }

    public void setTypeOfWork(String typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
