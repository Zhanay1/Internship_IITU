package com.example.practiceiitu.pojo;

public class Employer {
    private String email;
    private String companyName;
    private String phoneNumber;

    public Employer(String email, String companyName, String phoneNumber) {
        this.email = email;
        this.companyName = companyName;
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
