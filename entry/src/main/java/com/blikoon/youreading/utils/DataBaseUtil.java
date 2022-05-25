package com.blikoon.youreading.utils;

import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.app.Context;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.utils.net.Uri;

//数据库查询、插入 帮助类
public class DataBaseUtil {
    private static Uri uri = Uri.parse("dataability:///com.blikoon.qrcodescannerlibrary.LocalDBAbility/user_info");
    public static String getValue(String key, Context context){
        String value = null;
        DataAbilityHelper dataAbilityHelper = DataAbilityHelper.creator(context);
        String[] colums = {"value"};
        DataAbilityPredicates predicates = new DataAbilityPredicates();
        predicates.equalTo("key",key);
        try {
            ResultSet rs = dataAbilityHelper.query(uri, colums, predicates);
            if(rs.getRowCount() >0){
                rs.goToFirstRow();
                value = rs.getString(0);
            }
        } catch (DataAbilityRemoteException e) {
            e.printStackTrace();
        }
        return value;
    }
    public static int setValue(String key,String value,Context context) {
        int i = 0;
        ValuesBucket valuesBucket = new ValuesBucket();
        valuesBucket.putString("key",key);
        valuesBucket.putString("value",value);
        DataAbilityHelper dataAbilityHelper = DataAbilityHelper.creator(context);
        try {
            i = dataAbilityHelper.insert(uri, valuesBucket);
        } catch (DataAbilityRemoteException e) {
            e.printStackTrace();
        }
        return i;
    }
    public static int delValue(String key,Context context){
        int i = 0;
        ValuesBucket valuesBucket = new ValuesBucket();
        DataAbilityHelper dataAbilityHelper = DataAbilityHelper.creator(context);
        DataAbilityPredicates predicates = new DataAbilityPredicates();
        predicates.equalTo("key",key);
        try {
            i = dataAbilityHelper.delete(uri, predicates);
        } catch (DataAbilityRemoteException e) {
            e.printStackTrace();
        }
        return i;
    }
}