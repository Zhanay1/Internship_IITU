package com.example.practiceiitu.pojo;

public class Report {
    private String nameOfWork;
    private String description;
    private String typeOfWork;
    private String startDate;
    private String endDate;
    private String mark;
    private String feedback;
    private long dateOfMarking;
    private long dateOfReport;
    public Report(String nameOfWork, String description, String typeOfWork, String startDate, String endDate, long dateOfReport) {
        this.nameOfWork = nameOfWork;
        this.description = description;
        this.typeOfWork = typeOfWork;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateOfReport = dateOfReport;
    }

    public String getNameOfWork() {
        return nameOfWork;
    }

    public void setNameOfWork(String nameOfWork) {
        this.nameOfWork = nameOfWork;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public long getDateOfReport() {
        return dateOfReport;
    }

    public void setDateOfReport(long dateOfReport) {
        this.dateOfReport = dateOfReport;
    }

    public long getDateOfMarking() {
        return dateOfMarking;
    }

    public void setDateOfMarking(long dateOfMarking) {
        this.dateOfMarking = dateOfMarking;
    }
}
