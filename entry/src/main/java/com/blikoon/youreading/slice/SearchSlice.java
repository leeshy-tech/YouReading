package com.blikoon.youreading.slice;

import com.alibaba.fastjson.JSON;
import com.blikoon.youreading.ResourceTable;
import com.blikoon.youreading.beans.Book_info;
import com.blikoon.youreading.beans.Books_info;
import com.blikoon.youreading.utils.Data;
import com.blikoon.youreading.utils.HttpRequestUtil;
import com.blikoon.youreading.utils.LoadImageUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.app.dispatcher.task.TaskPriority;

import java.util.List;

public class SearchSlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_page_search_list);
        //绑定搜索逻辑
        TextField search_textfield = (TextField) findComponentById(ResourceTable.Id_search_input);
        search_textfield.setText("");
        Button search_btn = (Button) findComponentById(ResourceTable.Id_search_btn);
        search_btn.setClickedListener(component -> {
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
                Intent intent1 = new Intent();
                intent1.setParam("keyword",search_textfield.getText());
                present(new SearchSlice(),intent1);
            });
        });
        //加载搜索结果
        String keyword = (String) intent.getParams().getParam("keyword");
        String category = (String) intent.getParams().getParam("category");
        TableLayout book_table = (TableLayout) findComponentById(ResourceTable.Id_books_table);
        book_table.removeAllComponents();
        String url = "";
        String request_json = "";
        if (keyword != null){
            url = Data.getUrl_search_book();
            request_json = "{\"keyword\":\"" + keyword + "\"}";
        }
        if (category != null){
            url = Data.getUrl_search_book_category();
            request_json = "{\"category\":\"" + category + "\"}";
        }
        String finalUrl = url;
        String finalRequest_json = request_json;
        System.out.println(finalRequest_json);
        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
            String return_json_Body = HttpRequestUtil.sendPostRequest(this, finalUrl, finalRequest_json);
            System.out.println("return_json_Body:" + return_json_Body);
            Books_info books_info = JSON.parseObject(return_json_Body, Books_info.class);
            System.out.println("msg:" + books_info.getMsg());
            List<Book_info> book_info_list = books_info.getBook_info_list();
            System.out.println("book_info_list.size()" + book_info_list.size());
            for (Book_info book_info:book_info_list){
                DependentLayout template = (DependentLayout)
                        LayoutScatter.getInstance(this).parse(ResourceTable.Layout_template_book_list_item3,null,false);
                Image image = (Image) template.findComponentById(ResourceTable.Id_image_book);
                Text author_text = (Text) template.findComponentById(ResourceTable.Id_book_author);
                Text title_text = (Text) template.findComponentById(ResourceTable.Id_book_title);
                Text collection_text = (Text) template.findComponentById(ResourceTable.Id_book_collection);
                Text can_borrow_text = (Text) template.findComponentById(ResourceTable.Id_book_can_borrow);
                LoadImageUtil.loadImg(this,book_info.getCover_img(),image);
                author_text.setText(book_info.getAuthor());
                title_text.setText(book_info.getName());
                collection_text.setText("馆藏" + String.valueOf(book_info.getCollection()));
                can_borrow_text.setText("可借" + String.valueOf(book_info.getCan_borrow()));
                //回到主线程
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        //添加item
                        book_table.addComponent(template);
                        System.out.println("insert term");
                        template.setClickedListener(component -> {
                            Intent intent = new Intent();
                            intent.setParam("ISBN",book_info.getISBN());
                            present(new BookDetailSlice(),intent);
                        });
                    }
                });
            }
        });
    }
}
