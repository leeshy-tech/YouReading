package com.blikoon.youreading.beans;

public class Book_store {
    private String lib;
    private String shelf;
    private String state;

    public Book_store(String lib, String shelf, String state) {
        this.lib = lib;
        this.shelf = shelf;
        this.state = state;
    }

    public Book_store() {
    }

    public String getLib() {
        return lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
