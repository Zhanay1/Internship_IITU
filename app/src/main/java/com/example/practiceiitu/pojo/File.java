package com.example.practiceiitu.pojo;

public class File {
    private String name;
    private String date;
    private String extension;

    public File(String name, String date, String extension) {
        this.name = name;
        this.date = date;
        this.extension = extension;
    }

    public File() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
