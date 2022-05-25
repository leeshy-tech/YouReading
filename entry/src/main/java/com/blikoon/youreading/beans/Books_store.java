package com.blikoon.youreading.beans;

import java.util.ArrayList;
import java.util.List;

public class Books_store {
    private String msg;
    private List<Book_store> books_store = new ArrayList<>();

    public Books_store(String msg, List<Book_store> books_store) {
        this.msg = msg;
        this.books_store = books_store;
    }

    public Books_store() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Book_store> getBooks_store() {
        return books_store;
    }

    public void setBooks_store(List<Book_store> books_store) {
        this.books_store = books_store;
    }
}
