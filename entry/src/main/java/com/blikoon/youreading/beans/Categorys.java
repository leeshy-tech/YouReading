package com.blikoon.youreading.beans;

import java.util.ArrayList;
import java.util.List;

public class Categorys {
    private String msg;
    private List<Category> book_category = new ArrayList<>();

    public Categorys(String msg, List<Category> category12) {
        this.msg = msg;
        this.book_category = category12;
    }

    public Categorys() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Category> getBook_category() {
        return book_category;
    }

    public void setBook_category(List<Category> book_category) {
        this.book_category = book_category;
    }
}
