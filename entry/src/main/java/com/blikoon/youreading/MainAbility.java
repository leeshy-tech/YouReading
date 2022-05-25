/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blikoon.youreading;

import com.blikoon.youreading.slice.LoginSlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;


public class MainAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(LoginSlice.class.getName());

        //判断是否有相机权限
        if (verifySelfPermission("ohos.permission.CAMERA") != IBundleManager.PERMISSION_GRANTED) {
            // 应用未被授予权限，判断是否可以申请弹框授权(首次申请或者用户未选择禁止且不再提示)
            if (canRequestPermission("ohos.permission.CAMERA")) {
                //申请相机权限弹框
                requestPermissionsFromUser(new String[] {"ohos.permission.CAMERA"} , 1);
            } else {
                // 显示应用需要权限的理由，提示用户进入设置授权
            }
        } else {
            // 权限已被授予
        }
    }
}