package com.blikoon.youreading.slice;

import com.alibaba.fastjson.JSON;
import com.blikoon.youreading.ResourceTable;
import com.blikoon.youreading.beans.Book_info;
import com.blikoon.youreading.beans.Book_store;
import com.blikoon.youreading.beans.Books_info;
import com.blikoon.youreading.beans.Books_store;
import com.blikoon.youreading.utils.Data;
import com.blikoon.youreading.utils.HttpRequestUtil;
import com.blikoon.youreading.utils.LoadImageUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.app.dispatcher.task.TaskPriority;

import java.util.List;

public class BookDetailSlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_page_book_detail);

        String ISBN = (String) intent.getParams().getParam("ISBN");
        System.out.println(ISBN);
        TableLayout StoreTable = (TableLayout) findComponentById(ResourceTable.Id_store_table);
        StoreTable.removeAllComponents();
        //线程1，更新上半部分
        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
            String url = Data.getUrl_get_book_detail();
            String ISBN_json = "{\"ISBN\":\"" + ISBN + "\"}";
            String return_json_Body = HttpRequestUtil.sendPostRequest(this,url,ISBN_json);
            System.out.println("return_json_Body:" + return_json_Body);
            Books_info books_info = JSON.parseObject(return_json_Body, Books_info.class);
            System.out.println("msg:" + books_info.getMsg());
            List<Book_info> book_info_list = books_info.getBook_info_list();
            Book_info book_info = book_info_list.get(0);
            Image image = (Image) findComponentById(ResourceTable.Id_book);
            Text text_author = (Text) findComponentById(ResourceTable.Id_author);
            Text text_title = (Text) findComponentById(ResourceTable.Id_title);
            Text text_ISBN = (Text) findComponentById(ResourceTable.Id_ISBN);
            Text text_press = (Text) findComponentById(ResourceTable.Id_press);
            LoadImageUtil.loadImg(this,book_info.getCover_img(),image);
            //回到主线程
            getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    text_author.setText(book_info.getAuthor());
                    text_ISBN.setText(book_info.getISBN());
                    text_title.setText(book_info.getName());
                    text_press.setText(book_info.getPress());
                }
            });

        });
        //线程2，更新馆藏信息
        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
            String url = Data.getUrl_get_book_store();
            String ISBN_json = "{\"ISBN\":\"" + ISBN + "\"}";
            String return_json_Body = HttpRequestUtil.sendPostRequest(this,url,ISBN_json);
            Books_store books_store = JSON.parseObject(return_json_Body,Books_store.class);
            List<Book_store> book_store_list = books_store.getBooks_store();
            for (Book_store book_store:book_store_list){
                DirectionalLayout template = (DirectionalLayout)
                        LayoutScatter.getInstance(this).parse(ResourceTable.Layout_template_table_item,null,false);
                Text text_shelf = (Text) template.findComponentById(ResourceTable.Id_text_shelf);
                Text text_state = (Text) template.findComponentById(ResourceTable.Id_text_state);
                Text text_department = (Text) template.findComponentById(ResourceTable.Id_text_department);
                text_shelf.setText(book_store.getShelf());
                text_state.setText(book_store.getState());
                text_department.setText(book_store.getLib());
                //回到主线程
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        StoreTable.addComponent(template);
                    }
                });
            }
        });
    }
}
