package com.blikoon.youreading.slice;

import com.alibaba.fastjson.JSON;
import com.blikoon.qrcodescanner.decode.QrManager;
import com.blikoon.youreading.ResourceTable;
import com.blikoon.youreading.beans.*;
import com.blikoon.youreading.provider.TabPageSliderProvider;
import com.blikoon.youreading.utils.*;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.service.DisplayAttributes;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainSlice extends AbilitySlice {
    private boolean isFlag = true;
    private CommonDialog dialog;
    private String token = null;
    SimpleDateFormat datetime_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    private static long lastClickTime;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_page_main);
        Date date_now = null;
        System.out.println("--------------------------------------------MainSlice onStart");
        token =  DataBaseUtil.getValue("token",this);
        //1.初始化TabList
        System.out.println("init TabList");
        TabList tabList = (TabList) findComponentById(ResourceTable.Id_tab_list);
        String[] tabListTags = {"⾸⻚", "分类", "我的"};
        tabList.removeAllComponents();
        for (int i = 0; i < tabListTags.length; i++) {
            TabList.Tab tab = tabList.new Tab(this);
            tab.setText(tabListTags[i]);
            tabList.addTab(tab);
        }
        //2.初始化PageSlider
        List<Integer> layoutFileIds = new ArrayList<>();
        layoutFileIds.add(ResourceTable.Layout_page_index);
        layoutFileIds.add(ResourceTable.Layout_page_category);
        layoutFileIds.add(ResourceTable.Layout_page_usercenter);
        PageSlider pageSlider = (PageSlider) findComponentById(ResourceTable.Id_page_slider);
        pageSlider.setProvider(new TabPageSliderProvider(layoutFileIds, this));
        //3.TabList与PageSlider联动
        TabList.TabSelectedListener listener = new TabList.TabSelectedListener() {
            public void onSelected(TabList.Tab tab) {
                //获取点击的菜单的索引
                int index = tab.getPosition();
                System.out.println("select index:" + index);
                //设置pageSlider的索引与菜单索引一致
                pageSlider.setCurrentPage(index);
                //如果相邻两次点击的间隔超过1s才更新组件
                if (!isFastClick()){
                    if (index == 0){
                        initIndex(pageSlider);
                    }
                    else if(index == 1){
                        initCategory(pageSlider);
                    }
                    else if(index == 2){
                        initUserCenter(pageSlider);
                    }
                }
            }
            public void onUnselected(TabList.Tab tab) {
            }
            public void onReselected(TabList.Tab tab) {
            }
        };
        tabList.addTabSelectedListener(listener);
        pageSlider.addPageChangedListener(new PageSlider.PageChangedListener() {
            public void onPageSliding(int i, float v, int i1) {
            }
            public void onPageSlideStateChanged(int i) {
            }
            public void onPageChosen(int i) {
                //参数i就表单当前pageSlider的索引
                if (tabList.getSelectedTabIndex() != i) {
                    tabList.selectTabAt(i);
                }
            }
        });
        //4.tabList默认选中第⼀个菜单，加载PageSlider的第⼀个⻚⾯（默认）
        tabList.selectTabAt(0);
    }
    //首页初始化
    public void initIndex(PageSlider pageSlider){
        System.out.println("initIndex");
        //绑定搜索框逻辑
        TextField search_textfield = (TextField) findComponentById(ResourceTable.Id_search_input);
        Button search_btn = (Button) findComponentById(ResourceTable.Id_search_btn);
        search_btn.setClickedListener(component -> {
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
                Intent intent = new Intent();
                intent.setParam("keyword",search_textfield.getText());
                present(new SearchSlice(),intent);
            });
        });
        //绑定扫码逻辑
        Image image_scan = (Image) findComponentById(ResourceTable.Id_image_scan);
        image_scan.setClickedListener(v -> {
            QrManager.getInstance().startScan(this, new QrManager.OnScanResultCallback() {
                @Override
                public void onScanSuccess(String result) {
                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            if (isFlag){
                                lend_book(getContext(),result);
                            }
                            isFlag = false;
                        }
                    });
                }
            });
        });
        //更新推荐书籍
        TableLayout bookListTable = (TableLayout) findComponentById(ResourceTable.Id_book_list_table);
        try{
            //开新线程
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).syncDispatch(()->{
                bookListTable.removeAllComponents();
                //发GET请求
                String url = Data.getUrl_get_books();
                String return_json_Body = HttpRequestUtil.sendGetRequest(this,url);
                System.out.println("return_json_Body:" + return_json_Body);
                //解析json
                Books_info books_info = JSON.parseObject(return_json_Body, Books_info.class);
                System.out.println(books_info.getMsg());
                List<Book_info> book_info_list = books_info.getBook_info_list();
                System.out.println(book_info_list);
                for (Book_info book_info:book_info_list){
                    DependentLayout template = (DependentLayout)
                            LayoutScatter.getInstance(this).parse(ResourceTable.Layout_template_book_list_item1,null,false);
                    Image image = (Image) template.findComponentById(ResourceTable.Id_image_book);
                    Text author_text = (Text) template.findComponentById(ResourceTable.Id_book_author);
                    Text title_text = (Text) template.findComponentById(ResourceTable.Id_book_title);
                    LoadImageUtil.loadImg(this,book_info.getCover_img(),image);
                    author_text.setText(book_info.getAuthor());
                    title_text.setText(book_info.getName());
                    //回到主线程，给item绑定跳转到详情页的逻辑
                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            bookListTable.addComponent(template);
                            System.out.println("insert item");
                            template.setClickedListener(component -> {
                                Intent intent = new Intent();
                                intent.setParam("ISBN",book_info.getISBN());
                                present(new BookDetailSlice(),intent);
                            });
                        }
                    });
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //分类页初始化
    public void initCategory(PageSlider pageSlider){
        System.out.println("initCategory");
        //绑定搜索框逻辑
        TextField search_textfield = (TextField) findComponentById(ResourceTable.Id_search_input);
        Button search_btn = (Button) findComponentById(ResourceTable.Id_search_btn);
        search_btn.setClickedListener(component -> {
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
                Intent intent = new Intent();
                intent.setParam("keyword",search_textfield.getText());
                present(new SearchSlice(),intent);
            });
        });
        //更新一级分类和二级分类
        TableLayout Category1Table = (TableLayout) findComponentById(ResourceTable.Id_category1_table);
        Category1Table.removeAllComponents();
        TableLayout Category2Table = (TableLayout) findComponentById(ResourceTable.Id_category2_table);
        //新线程
        TaskDispatcher globalTaskDispatcher = this.getGlobalTaskDispatcher(TaskPriority.DEFAULT);
        globalTaskDispatcher.asyncDispatch(()->{
            //发送GET请求
            String url = Data.getUrl_get_book_category();
            String return_json_Body = HttpRequestUtil.sendGetRequest(this,url);
            System.out.println("return_json_Body:" + return_json_Body);
            //解析结果
            Categorys categorys = JSON.parseObject(return_json_Body, Categorys.class);
            List<Category> category_list = categorys.getBook_category();
            String msg = categorys.getMsg();
            System.out.println("msg:" + msg);
            System.out.println("category_list.size():" + category_list.size());
            System.out.println("category1:" + category_list.get(0).getCategory1());
            System.out.println("category2:" + category_list.get(0).getCategory2());
            DirectionalLayout[] fcts = new DirectionalLayout[category_list.size()];
            getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    //加载一级分类
                    for(int i=0;i<category_list.size();i++){
                        Category category = category_list.get(i);
                        DirectionalLayout category1_template = (DirectionalLayout) LayoutScatter.getInstance(getContext()).
                                parse(ResourceTable.Layout_template_category1_item,null,false);
                        String category1 = category.getCategory1();
                        Text text_category1 = (Text) category1_template.findComponentById(ResourceTable.Id_category1_text);
                        text_category1.setText(category1);
                        Category1Table.addComponent(category1_template);
                        System.out.println("insert category1");
                        fcts[i] = category1_template;
                        //默认选中第一个一级分类
                        if (i==0){
                            text_category1.setTextColor(new Color(Color.rgb(255,0x38,0x3b)));
                            List<String> category2_list = category_list.get(0).getCategory2();
                            loadCategory2(category2_list,Category2Table);
                        }
                    }
                    for(int i=0;i<fcts.length;i++){
                        DirectionalLayout fct = fcts[i];
                        List<String> category2_list = category_list.get(i).getCategory2();
                        fct.setClickedListener(component -> {
                            for (int j=0;j<fcts.length;j++){
                                Text text = (Text)  fcts[j].findComponentById(ResourceTable.Id_category1_text);
                                text.setTextColor(Color.BLACK);
                            }
                            Text textChecked = (Text) component.findComponentById(ResourceTable.Id_category1_text);
                            textChecked.setTextColor(new Color(Color.rgb(255,0x38,0x3b)));
                            loadCategory2(category2_list,Category2Table);
                        });
                    }
                }
            });
        });
    }
    //个人页初始化
    public void initUserCenter(PageSlider pageSlider){
        System.out.println("initUserCenter");
        if(token != null){
            System.out.println(token);
            Image image = (Image) findComponentById(ResourceTable.Id_user_photo);
            TableLayout record_table = (TableLayout) findComponentById(ResourceTable.Id_record_table);
            record_table.removeAllComponents();
            //建新线程
            TaskDispatcher globalTaskDispatcher = this.getGlobalTaskDispatcher(TaskPriority.DEFAULT);
            //异步
            globalTaskDispatcher.asyncDispatch(()->{
                //发送请求
                String url = Data.getUrl_get_info();
                String return_json_Body = HttpRequestUtil.sendGetRequestWithToken(this,url,token);
                UserInfo userInfo = JSON.parseObject(return_json_Body, UserInfo.class);

                System.out.println("msg:" + userInfo.getMsg());
                Image user_image = (Image) findComponentById(ResourceTable.Id_user_photo);
                Text user_id = (Text) findComponentById(ResourceTable.Id_user_id);
                Text user_name = (Text) findComponentById(ResourceTable.Id_user_name);
                Button btn_exit_login = (Button) findComponentById(ResourceTable.Id_btn_exit_login);
                btn_exit_login.setClickedListener(component -> {
                    DataBaseUtil.delValue("token",this);
                    present(new LoginSlice(),new Intent());
                });
                //回到主线程
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        LoadImageUtil.loadImg(getContext(),userInfo.getImg_url(),user_image);
                        user_id.setText(userInfo.getId());
                        user_name.setText(userInfo.getUser_name());
                    }
                });
                // 更新借阅记录
                List<LendRecord> lend_record_list = userInfo.getLend_record();
                for (LendRecord lend_record:lend_record_list){
                    DependentLayout template = (DependentLayout) LayoutScatter.getInstance(this).
                            parse(ResourceTable.Layout_template_book_list_item2,null,false);
                    Image image_book = (Image) template.findComponentById(ResourceTable.Id_image_book);
                    Text book_title = (Text) template.findComponentById(ResourceTable.Id_book_title);
                    Text lend_time_text = (Text) template.findComponentById(ResourceTable.Id_book_lend_time);
                    Text ddl_text = (Text) template.findComponentById(ResourceTable.Id_book_ddl);
                    Button btn_return = (Button) template.findComponentById(ResourceTable.Id_btn_return);
                    Button btn_renew = (Button) template.findComponentById(ResourceTable.Id_btn_renew);
                    LoadImageUtil.loadImg(this,lend_record.getCover_img(),image_book);
                    book_title.setText(lend_record.getName());
                    try {
                        Date date_time_lend_time = datetime_format.parse(lend_record.getRecord_time());
                        lend_time_text.setText(date_format.format(date_time_lend_time) + "借阅");
                        if(Objects.equals(lend_record.getOperation(), "还")){
                            ddl_text.setText("已归还");
                        }
                        else{
                            Date date_now = new Date(System.currentTimeMillis());
                            Date date_estimated_return_time = date_format.parse(lend_record.getEstimated_return_time());
                            boolean overdue = date_estimated_return_time.before(date_now);
                            if (overdue){
                                ddl_text.setText("已逾期");
                            }
                            else{
                                String return_days = (date_estimated_return_time.getTime() - date_now.getTime())
                                        / (24 * 60 * 60 * 1000) + "天";
                                ddl_text.setText(return_days + "后到期");
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //回到主线程
                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("insert item");
                            template.setClickedListener(component -> {
                                Intent intent = new Intent();
                                intent.setParam("ISBN",lend_record.getISBN());
                                present(new BookDetailSlice(),intent);
                            });
                            if(Objects.equals(lend_record.getOperation(), "还")){
                                //如果是还书记录，就不加载还书和续借的按钮
                                template.removeComponentById(ResourceTable.Id_btn_renew);
                                template.removeComponentById(ResourceTable.Id_btn_return);
                            }
                            else{
                                //给还书和续借按钮绑定逻辑
                                btn_return.setClickedListener(component -> {
                                    return_or_renew_book(getContext(),lend_record.getISBN(),
                                            lend_record.getNumber(),"还");
                                });
                                btn_renew.setClickedListener(component -> {
                                    return_or_renew_book(getContext(),lend_record.getISBN(),
                                            lend_record.getNumber(),"续");
                                });
                            }
                            record_table.addComponent(template);
                        }
                    });
                }
            });
        }
        else{
            present(new LoginSlice(), new Intent());
        }
    }
    //加载二级分类
    public void loadCategory2(List<String> secondCategoryList,TableLayout category_table){
        getUITaskDispatcher().asyncDispatch(()->{
            category_table.removeAllComponents();
            for (String category2:secondCategoryList){
                DirectionalLayout category2_template = (DirectionalLayout) LayoutScatter.getInstance(this).
                        parse(ResourceTable.Layout_template_category2_item,null,false);
                Text text = (Text) category2_template.findComponentById(ResourceTable.Id_category2_text);
                text.setText(category2);
                category_table.addComponent(category2_template);
                category2_template.setClickedListener(component -> {
                    Intent intent = new Intent();
                    intent.setParam("category",category2);
                    present(new SearchSlice(),intent);
                });
                System.out.println("insert category2:" + category2);
            }
        });
    }
    //借书函数
    private void lend_book(Context context, String text) {
        if(token != null){
            DirectionalLayout directionalLayout = (DirectionalLayout) LayoutScatter.getInstance(context)
                    .parse(ResourceTable.Layout_dialog_picker, null, false);
            Text result = (Text) directionalLayout.findComponentById(ResourceTable.Id_tv_result);
            Text confirm = (Text) directionalLayout.findComponentById(ResourceTable.Id_tv_confirm);
            dialog = new CommonDialog(context);
            dialog.setContentCustomComponent(directionalLayout);
            dialog.setAutoClosable(true);
            dialog.setAutoClosable(true);
            dialog.setSize(vp2px(this,340),DirectionalLayout.LayoutConfig.MATCH_CONTENT);
            dialog.setAlignment(LayoutAlignment.CENTER);
            dialog.setCornerRadius(vp2px(this,15));
            dialog.show();
            // 发送借书请求
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
                String text_json = "{\"ISBN\":\"978-7-03-025415-3\",\"number\":\"0\",\"operation\":\"借\"}";
                String url = Data.getUrl_lend_book();
                String msg = HttpRequestUtil.sendPostRequestWithToken(this,url,token,text);

                getUITaskDispatcher().asyncDispatch(()->{result.setText(msg);});
            });
            confirm.setClickedListener(v -> {
                dialog.destroy();
                isFlag = true;
            });
            dialog.setDestroyedListener(new CommonDialog.DestroyedListener() {
                @Override
                public void onDestroy() {
                    isFlag = true;
                }
            });
        }
        else{
            present(new LoginSlice(),new Intent());
        }
    }
    //续借或归还函数
    private void return_or_renew_book(Context context,String ISBN,String number,String operation){
        if(token != null){
            DirectionalLayout directionalLayout = (DirectionalLayout) LayoutScatter.getInstance(context)
                    .parse(ResourceTable.Layout_dialog_picker, null, false);
            Text result = (Text) directionalLayout.findComponentById(ResourceTable.Id_tv_result);
            Text confirm = (Text) directionalLayout.findComponentById(ResourceTable.Id_tv_confirm);
            dialog = new CommonDialog(context);
            dialog.setContentCustomComponent(directionalLayout);
            dialog.setAutoClosable(true);
            dialog.setAutoClosable(true);
            dialog.setSize(vp2px(this,340),DirectionalLayout.LayoutConfig.MATCH_CONTENT);
            dialog.setAlignment(LayoutAlignment.CENTER);
            dialog.setCornerRadius(vp2px(this,15));
            dialog.show();
            // 发送借书请求
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()->{
                String text_json = "{\"ISBN\":\"" + ISBN + "\",\"number\":\"" +
                        number + "\",\"operation\":\"" +operation + "\"}";
                String url = Data.getUrl_lend_book();
                String msg = HttpRequestUtil.sendPostRequestWithToken(this,url,token,text_json);
                getUITaskDispatcher().asyncDispatch(()->{result.setText(msg);});
            });
            confirm.setClickedListener(v -> {
                dialog.destroy();
            });
        }
    }
    //判断点击间隔是否大于1s
    public static boolean isFastClick(){
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= 1000 ) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }
    /**
     * vp转像素
     *
     * @param context
     * @param vp
     * @return int
     */
    private int vp2px(Context context, float vp) {
        DisplayAttributes attributes = DisplayManager.getInstance().getDefaultDisplay(context).get().getAttributes();
        return (int) (attributes.densityPixels * vp);
    }
    @Override
    public void onActive() {
        super.onActive();
        System.out.println("------------------------------------MainSlice onActive");
        TabList tabList = (TabList) findComponentById(ResourceTable.Id_tab_list);
        tabList.selectTabAt(0);
    }
    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
        System.out.println("------------------------------------MainSlice onForeground");
    }
}