package com.blikoon.youreading.utils;

public class Data {
    static final String IP = "124.223.167.22";
    static final String Port = "8899";
    static final String Url_prefix = "http://" + IP + ":" + Port;
    static final String Url_login =  Url_prefix + "/user/login";
    static final String Url_get_info =  Url_prefix + "/user/info";
    static final String Url_get_books =  Url_prefix + "/book/recommended";
    static final String Url_get_book_detail =  Url_prefix + "/book/detail";
    static final String Url_get_book_store =  Url_prefix + "/book/store";
    static final String Url_get_book_category =  Url_prefix + "/book/category";
    static final String Url_search_book =  Url_prefix + "/book/search";
    static final String Url_search_book_category =  Url_prefix + "/book/search_category";
    static final String Url_lend_book =  Url_prefix + "/book/lend";

    static public String getIP() {
        return IP;
    }

    static public String getPort() {
        return Port;
    }

    static public String getUrl_prefix() {
        return Url_prefix;
    }

    static public String getUrl_login() {
        return Url_login;
    }

    static public String getUrl_get_info() {
        return Url_get_info;
    }

    static public String getUrl_get_books() {
        return Url_get_books;
    }

    static public String getUrl_get_book_detail() {
        return Url_get_book_detail;
    }

    static public String getUrl_get_book_store() {
        return Url_get_book_store;
    }

    static public String getUrl_get_book_category() {
        return Url_get_book_category;
    }

    static public String getUrl_search_book() {
        return Url_search_book;
    }

    static public String getUrl_search_book_category() {
        return Url_search_book_category;
    }

    static public String getUrl_lend_book() {
        return Url_lend_book;
    }
}
