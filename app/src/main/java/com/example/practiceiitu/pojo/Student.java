package com.example.practiceiitu.pojo;

public class Student {
    private String email;
    private String group;
    private String FullName;
    private String phoneNumber;
    private String speciality;
    private String course;

    public Student(String email, String group, String fullName, String phoneNumber, String speciality, String course) {
        this.email = email;
        this.group = group;
        FullName = fullName;
        this.phoneNumber = phoneNumber;
        this.speciality = speciality;
        this.course = course;
    }

    public Student() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
