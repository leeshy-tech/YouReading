package com.blikoon.youreading.beans;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String category1;
    private List<String> category2 = new ArrayList<>();

    public Category(String category1, List<String> category2) {
        this.category1 = category1;
        this.category2 = category2;
    }

    public Category() {
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public List<String> getCategory2() {
        return category2;
    }

    public void setCategory2(List<String> category2) {
        this.category2 = category2;
    }
}
