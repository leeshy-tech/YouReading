package com.blikoon.youreading.beans;

public class Book_info {
    private String ISBN;
    private String cover_img;
    private String name;
    private String press;
    private String author;
    private String category;
    private int collection;
    private int can_borrow;

    public Book_info(String ISBN, String cover_img, String name, String press, String author, String category, int collection, int can_borrow) {
        this.ISBN = ISBN;
        this.cover_img = cover_img;
        this.name = name;
        this.press = press;
        this.author = author;
        this.category = category;
        this.collection = collection;
        this.can_borrow = can_borrow;
    }

    public Book_info() {
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

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCollection() {
        return collection;
    }

    public void setCollection(int collection) {
        this.collection = collection;
    }

    public int getCan_borrow() {
        return can_borrow;
    }

    public void setCan_borrow(int can_borrow) {
        this.can_borrow = can_borrow;
    }
}
