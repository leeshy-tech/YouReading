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

import ohos.aafwk.ability.delegation.AbilityDelegatorRegistry;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * 测试
 *
 * @author ljx
 * @since 2021-05-08
 */
public class ExampleOhosTest {
    @Test
    public void testBundleName() {
        final String actualBundleName = AbilityDelegatorRegistry.getArguments().getTestBundleName();
        assertEquals("com.blikoon.qrcodescannerlibrary", actualBundleName);
    }

    @Test
    public void getDisplayWidthInPx() {
        try {
            Class  mainAbilitySlice = Class.forName("com.blikoon.qrcodescanner.utils.ScreenUtils");
            Method log = mainAbilitySlice.getMethod("getDisplayWidthInPx");
            Object obj = mainAbilitySlice.getConstructor().newInstance();
            log.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDisplayHeightInPX() {
        try {
            Class  mainAbilitySlice = Class.forName("com.blikoon.qrcodescanner.utils.ScreenUtils");
            Method log = mainAbilitySlice.getMethod("getDisplayHeightInPX");
            Object obj = mainAbilitySlice.getConstructor().newInstance();
            log.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void vp2px() {
        try {
            Class  mainAbilitySlice = Class.forName("com.blikoon.qrcodescanner.utils.ScreenUtils");
            Method log = mainAbilitySlice.getMethod("vp2px");
            Object obj = mainAbilitySlice.getConstructor().newInstance();
            log.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}