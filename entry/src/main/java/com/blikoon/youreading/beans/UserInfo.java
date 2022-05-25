package com.blikoon.youreading.beans;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    private String id;
    private String img_url;
    private String user_name;
    private String msg;
    private List<LendRecord> lend_record = new ArrayList<>();

    public UserInfo(String id, String img_url, String user_name, String msg, List<LendRecord> lend_record) {
        this.id = id;
        this.img_url = img_url;
        this.user_name = user_name;
        this.msg = msg;
        this.lend_record = lend_record;
    }

    public UserInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<LendRecord> getLend_record() {
        return lend_record;
    }

    public void setLend_record(List<LendRecord> lend_record) {
        this.lend_record = lend_record;
    }
}
