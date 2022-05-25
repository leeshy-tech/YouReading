package com.blikoon.youreading.slice;

import com.alibaba.fastjson.JSON;
import com.blikoon.youreading.ResourceTable;
import com.blikoon.youreading.beans.Account;
import com.blikoon.youreading.beans.LoginMsg;
import com.blikoon.youreading.utils.Data;
import com.blikoon.youreading.utils.DataBaseUtil;
import com.blikoon.youreading.utils.HttpRequestUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.TextField;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;

public class LoginSlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_page_login);

        String token_s =  DataBaseUtil.getValue("token",this);
        if (token_s != null){
            present(new MainSlice(), new Intent());
        }
        else{
            Button btn_login = (Button) findComponentById(ResourceTable.Id_login_btn);
            TextField tf_userid = (TextField) findComponentById(ResourceTable.Id_login_id_textfield);
            TextField tf_userPwd = (TextField) findComponentById(ResourceTable.Id_login_pwd_textfield);

            String url = Data.getUrl_login();
            btn_login.setClickedListener(component -> {
                //开新线程
                TaskDispatcher globalTaskDispatcher = this.getGlobalTaskDispatcher(TaskPriority.DEFAULT);
                //异步
                globalTaskDispatcher.asyncDispatch(() -> {
                    String user_id = tf_userid.getText();
                    String user_pwd = tf_userPwd.getText();
                    //发送http请求，并获得数据
                    Account account = new Account(user_id, user_pwd);
                    String account_json = JSON.toJSONString(account);
                    String login_msg = HttpRequestUtil.sendPostRequest(this, url, account_json);
                    LoginMsg login_msg_obj = JSON.parseObject(login_msg, LoginMsg.class);
                    String token = login_msg_obj.getToken();
                    System.out.println("token:" + token);
                    String msg = login_msg_obj.getMsg();
                    System.out.println("msg:" + msg);
                    if (token != null) {
                        //将token存入本地数据库，并跳到个人页
                        DataBaseUtil.setValue("token",token,this);
                        present(new MainSlice(), new Intent());
                    } else {
                        //返回主线程进行UI重绘，原因是show方法不能在子线程中运行
                        getUITaskDispatcher().asyncDispatch(new Runnable() {
                            @Override
                            public void run() {
                                new ToastDialog(getContext()).setText(msg).show();
                            }
                        });
                    }
                });
            });
        }

    }

    @Override
    public void onActive() {
        super.onActive();
        //程序重新返回前台调用
        //若已经登陆，则导航到个人页
        String token_s =  DataBaseUtil.getValue("token",this);
        if (token_s != null){
            present(new MainSlice(), new Intent());
        }
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);

    }
}
