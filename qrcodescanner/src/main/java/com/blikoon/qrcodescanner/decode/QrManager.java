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

package com.blikoon.qrcodescanner.decode;

import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;

/** QrManager
 *
 * @author ljx
 * @since 2021-04-14
 */
public class QrManager {
    private static QrManager instance;
    /**
     * resultCallback
     */
    private OnScanResultCallback resultCallback;

    /** getInstance
     *
     * @return QrManager
     */
    public static synchronized QrManager getInstance() {
        if (instance == null) {
            instance = new QrManager();
        }
        return instance;
    }

    public OnScanResultCallback getResultCallback() {
        return resultCallback;
    }

    /** startScan
     *
     * @param context
     * @param resultCall
     */
    public void startScan(final Context context, OnScanResultCallback resultCall) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName("com.blikoon.qrcodescannerlibrary")
                .withAbilityName("com.blikoon.qrcodescanner.QrCodeAbility")
                .build();
        intent.setOperation(operation);
        context.startAbility(intent, 0);
        resultCallback = resultCall;
    }

    /** OnScanResultCallback
     *
     * @author ljx
     * @since 2021-04-14
     */
    public interface OnScanResultCallback {
        /** onScanSuccess
         *
         * @param result
         */
        void onScanSuccess(String result);
    }

    public void setResultCallback(OnScanResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }
}
