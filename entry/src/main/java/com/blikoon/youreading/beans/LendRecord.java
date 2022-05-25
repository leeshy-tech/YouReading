package com.blikoon.youreading.beans;

public class LendRecord {
    private String ISBN;
    private String cover_img;
    private String name;
    private String operation;
    private String record_time;
    private String number;
    private String estimated_return_time;

    public LendRecord(String ISBN, String cover_img, String name, String operation, String record_time, String number, String estimated_return_time) {
        this.ISBN = ISBN;
        this.cover_img = cover_img;
        this.name = name;
        this.operation = operation;
        this.record_time = record_time;
        this.number = number;
        this.estimated_return_time = estimated_return_time;
    }

    public LendRecord() {
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getCover_img() {
        return cover_img;
    }

    public void setCover_img(String cover_img) {
        this.cover_img = cover_img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRecord_time() {
        return record_time;
    }

    public void setRecord_time(String record_time) {
        this.record_time = record_time;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEstimated_return_time() {
        return estimated_return_time;
    }

    public void setEstimated_return_time(String estimated_return_time) {
        this.estimated_return_time = estimated_return_time;
    }
}
