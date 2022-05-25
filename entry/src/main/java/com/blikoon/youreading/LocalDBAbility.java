package com.blikoon.youreading;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.data.DatabaseHelper;
import ohos.data.dataability.DataAbilityUtils;
import ohos.data.rdb.*;
import ohos.data.resultset.ResultSet;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.net.Uri;

public class LocalDBAbility extends Ability {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100, "Demo");

    //RdbStore 对象就表示与数据库的连接，通过此对象可以完成对数据表中数据的CRUD操作
    private RdbStore rdbStore;

    //StoreConfig对象关联数据⽂件配置(数据库)
    private StoreConfig config = StoreConfig.newDefaultConfig("Users.db");

    //RdbOpenCallback 使⽤rdbStore对象回调此RdbOpenCallback对象的onCreate创建数据表
    private RdbOpenCallback callback = new RdbOpenCallback() {
        @Override
        public void onCreate(RdbStore rdbStore) {
            //使⽤rdbStore对象执⾏SQL创建数据表
            rdbStore.executeSql("create table if not exists user_info(" +
                    "id integer primary key autoincrement," +
                    "key text not null unique," +
                    "value text not null)");
        }
        @Override
        public void onUpgrade(RdbStore rdbStore, int i, int i1) {
        }
    };
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        HiLog.info(LABEL_LOG, "LocalDBAbility onStart");

        //初始化与数据库的连接
        DatabaseHelper helper = new DatabaseHelper(this);
        rdbStore = helper.getRdbStore(config,1,callback);
    }

    public ResultSet query(Uri uri, String[] columns, DataAbilityPredicates predicates) {
        RdbPredicates rdbPredicates = DataAbilityUtils.createRdbPredicates(predicates, "user_info");
        ResultSet resultSet = rdbStore.query(rdbPredicates, columns);
        return resultSet;
    }

    public int insert(Uri uri, ValuesBucket value) {
        int i = -1;
        String path = uri.getLastPath();
        if("user_info".equalsIgnoreCase(path)){
            i = (int)rdbStore.insert("user_info",value);
        }
        return i;
    }

    public int delete(Uri uri, DataAbilityPredicates predicates) {
        RdbPredicates rdbPredicates = DataAbilityUtils.createRdbPredicates(predicates, "user_info");
        int i = rdbStore.delete(rdbPredicates);
        return i;
    }

    public int update(Uri uri, ValuesBucket value, DataAbilityPredicates predicates) {
        RdbPredicates rdbPredicates = DataAbilityUtils.createRdbPredicates(predicates, "user_info");
        int i = rdbStore.update(value, rdbPredicates);
        return i;
    }
}