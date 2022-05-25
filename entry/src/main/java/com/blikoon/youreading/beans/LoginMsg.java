package com.blikoon.youreading.beans;

public class LoginMsg {
    private String msg;
    private String token;

    public LoginMsg(String msg, String token) {
        this.msg = msg;
        this.token = token;
    }

    public LoginMsg() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
