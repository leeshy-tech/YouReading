package com.blikoon.youreading.beans;

import java.util.ArrayList;
import java.util.List;

public class Books_info {
    private String msg;
    private List<Book_info> book_info_list = new ArrayList<>();

    public Books_info(String msg, List<Book_info> book_info_list) {
        this.msg = msg;
        this.book_info_list = book_info_list;
    }

    public Books_info() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Book_info> getBook_info_list() {
        return book_info_list;
    }

    public void setBook_info_list(List<Book_info> book_info_list) {
        this.book_info_list = book_info_list;
    }
}
