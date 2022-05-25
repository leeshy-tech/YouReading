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

package com.blikoon.qrcodescanner.slice;

import com.blikoon.qrcodescanner.ResourceTable;
import com.blikoon.qrcodescanner.decode.QrManager;
import com.blikoon.qrcodescanner.utils.QrUtils;
import com.blikoon.qrcodescanner.utils.ScreenUtils;
import com.blikoon.qrcodescanner.view.QrCodeFinderView;
import com.google.zxing.Result;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.DependentLayout;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.ToastDialog;
import ohos.agp.window.service.WindowManager;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.RawFileDescriptor;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraConfig;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.device.FrameConfig;
import ohos.media.common.BufferInfo;
import ohos.media.common.Source;
import ohos.media.image.Image;
import ohos.media.image.ImageReceiver;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageFormat;
import ohos.media.photokit.metadata.AVStorage;
import ohos.media.player.Player;
import ohos.utils.IntentConstants;
import ohos.utils.net.Uri;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

import static ohos.media.camera.device.Camera.FrameConfigType.FRAME_CONFIG_PREVIEW;
import static ohos.media.camera.params.Metadata.FlashMode.FLASH_ALWAYS_OPEN;
import static ohos.media.camera.params.Metadata.FlashMode.FLASH_CLOSE;

/**
 * QrCodeAbilitySlice
 *
 * @author ljx
 * @since 2021-04-14
 */
public class QrCodeAbilitySlice extends AbilitySlice {

    private ImageScanner scanner;
    private ImageReceiver imageReceiver;
    private CameraKit cameraKit;
    private Surface previewSurface;
    private Surface dataSurface;
    private Camera mcamera;

    private EventHandler handler;
    public static final int VIDEO_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 480;

    private QrCodeFinderView mLaserView;
    private Player mPlayer;

    private boolean isFlash = false;
    private FrameConfig.Builder frameConfigBuilder;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        WindowManager.getInstance().getTopWindow().get().setStatusBarColor(new Color(Color.getIntColor("#3F51B5")).getValue());
        super.setUIContent(ResourceTable.Layout_ability_qr_code);

        handler = new EventHandler(EventRunner.getMainEventRunner());
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);


        //初始化UI和相机，实现视频帧的获取
        SurfaceProvider surfaceProvider = (SurfaceProvider) findComponentById(ResourceTable.Id_zbar_surfaceprovider);
        surfaceProvider.getSurfaceOps().get().addCallback(new SurfaceOps.Callback() {
            @Override
            public void surfaceCreated(SurfaceOps surfaceOps) {
                previewSurface = surfaceOps.getSurface();
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceOps surfaceOps, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceOps surfaceOps) {
            }
        });
        getWindow().setTransparent(true);


        //注册编码器，实现视频帧的编码
        imageReceiver = ImageReceiver.create(VIDEO_WIDTH, VIDEO_HEIGHT, ImageFormat.YUV420_888, 10);
        imageReceiver.setImageArrivalListener(new IImageArrivalListenerImpl());

        initPlayer();
        initLayout();
    }

    private void initPlayer() {
        try {
            RawFileDescriptor filDescriptor = getResourceManager()
                    .getRawFileEntry("resources/rawfile/qrcode.mp3").openRawFileDescriptor();
            Source source = new Source(filDescriptor.getFileDescriptor(),
                    filDescriptor.getStartPosition(), filDescriptor.getFileSize());
            mPlayer = new Player(getContext());
            mPlayer.setSource(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLayout() {
        //布局容器
        DependentLayout myLayout = (DependentLayout) findComponentById(ResourceTable.Id_root_view);
        DirectionalLayout openLight = (DirectionalLayout) findComponentById(ResourceTable.Id_open_light);
        Text picture = (Text) findComponentById(ResourceTable.Id_picture);
        Text des = (Text) findComponentById(ResourceTable.Id_sdt_des);
        picture.setClickedListener(v -> picture());
        openLight.setClickedListener(v -> setFlash(des));
        ShapeElement viewShape = new ShapeElement();
        viewShape.setRgbColor(RgbColor.fromArgbInt(Color.BLACK.getValue()));
        myLayout.setBackground(viewShape);
        DependentLayout.LayoutConfig config = new DependentLayout.LayoutConfig(DependentLayout.LayoutConfig.MATCH_PARENT,
                DependentLayout.LayoutConfig.MATCH_PARENT);
        myLayout.setLayoutConfig(config);
        config.width = ScreenUtils.getDisplayWidthInPx(this);
        config.height = ScreenUtils.getDisplayWidthInPx(this);
        int px = ScreenUtils.getDisplayHeightInPX(this) - ScreenUtils.getDisplayWidthInPx(this);
        config.setMargins(0, px / 3, 0, 0);
//        surfaceProvider = new SurfaceProvider(this);
//        surfaceProvider.setLayoutConfig(config);
        getWindow().setTransparent(true);
//        // 获取SurfaceOps对象
//        SurfaceOps surfaceOps = surfaceProvider.getSurfaceOps().get();
//        // 设置屏幕一直打开
//        surfaceOps.setKeepScreenOn(true);
//        // 添加回调
//        surfaceOps.addCallback(callback);
//        myLayout.addComponent(surfaceProvider);
        mLaserView = new QrCodeFinderView(this);
        config.width = ScreenUtils.getDisplayWidthInPx(this);
        config.height = ScreenUtils.getDisplayHeightInPX(this);
        config.setMargins(0, 100, 0, 0);
        mLaserView.setLayoutConfig(config);
        myLayout.addComponent(mLaserView);
    }

    private void picture() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withAction(IntentConstants.ACTION_CHOOSE)
                .build();
        intent.setType("image/*");
        intent.setOperation(operation);
        startAbilityForResult(intent, 1);
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 1 && resultData != null) {
            String chooseImgUri = resultData.getUriString();
            DataAbilityHelper helper = DataAbilityHelper.creator(getContext());
            ImageSource imageSource = null;
            String chooseImgId;
            if (chooseImgUri.lastIndexOf("%3A") != -1) {
                chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf("%3A") + 3);
            } else {
                chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf('/') + 1);
            }
            Uri uri = Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI, chooseImgId);
            try {
                FileDescriptor fd = helper.openFile(uri, "r");
                imageSource = ImageSource.create(fd, null);

                ImageSource.DecodingOptions options = new ImageSource.DecodingOptions();
                options.allowPartialImage = true;
                options.sampleSize = 3;
                options.allowPartialImage = false;
                PixelMap pixelMap = imageSource.createPixelmap(options);
                int width = pixelMap.getImageInfo().size.width;
                int height = pixelMap.getImageInfo().size.height;
                System.out.println("----------------选择图片:" + width + "----" + height);
                byte[] yuv420sp = QrUtils.getYUV420sp(width, height, pixelMap);
                Result result = QrUtils.decodeImage(yuv420sp, width, height);
                if (null == result) {
                    QrManager.getInstance().getResultCallback().onScanSuccess("未识别到图片中的二维码，请重试。");
                } else {
                    QrManager.getInstance().getResultCallback().onScanSuccess(result.getText());
                }
                mPlayer.prepare();
                mPlayer.play();
                closeFlash();
                if (mcamera != null) {
                    mcamera.release();
                    mcamera = null;
                }
                terminate();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (imageSource != null) {
                    imageSource.release();
                }
            }
        }
    }

    /**
     * 闪光灯
     *
     * @param des text
     */
    public void setFlash(Text des) {
        try {
            isFlash = !isFlash;
            if (isFlash) {
                if (des != null) {
                    // 预览帧变焦值变更
                    frameConfigBuilder.setFlashMode(FLASH_ALWAYS_OPEN);
                    des.setText("Close Flash Light");
                }
            } else {
                frameConfigBuilder.setFlashMode(FLASH_CLOSE);
                if (des != null) {
                    des.setText("Open flash light");
                }
            }
            mcamera.triggerLoopingCapture(frameConfigBuilder.build());
        } catch (Exception e) {
            new ToastDialog(this).setText("该设备不支持闪光灯").show();
        }
    }

    public void closeFlash() {
        try {
            frameConfigBuilder.setFlashMode(FLASH_CLOSE);
            mcamera.triggerLoopingCapture(frameConfigBuilder.build());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void onStop() {
    }

    private class IImageArrivalListenerImpl implements ImageReceiver.IImageArrivalListener {
        //对监听事件的响应逻辑，实现对图像数据处理和到编码器的传输
        @Override
        public void onImageArrival(ImageReceiver imageReceiver) {

            Image mImage = imageReceiver.readNextImage();
            if (mImage != null) {
                BufferInfo bufferInfo = new BufferInfo();
                ByteBuffer mBuffer;
                byte[] YUV_DATA = new byte[VIDEO_HEIGHT * VIDEO_WIDTH * 3 / 2];
                int i;
                //采集YUV格式数据
                mBuffer = mImage.getComponent(ImageFormat.ComponentType.YUV_Y).getBuffer();
                for (i = 0; i < VIDEO_WIDTH * VIDEO_HEIGHT; i++) {
                    YUV_DATA[i] = mBuffer.get(i);
                }
                mBuffer = mImage.getComponent(ImageFormat.ComponentType.YUV_V).getBuffer();
                for (i = 0; i < VIDEO_WIDTH * VIDEO_HEIGHT / 4; i++) {
                    YUV_DATA[(VIDEO_WIDTH * VIDEO_HEIGHT) + i * 2] =
                            mBuffer.get(i * 2);
                }
                mBuffer = mImage.getComponent(ImageFormat.ComponentType.YUV_U).getBuffer();
                for (i = 0; i < VIDEO_WIDTH * VIDEO_HEIGHT / 4; i++) {
                    YUV_DATA[(VIDEO_WIDTH * VIDEO_HEIGHT) + i * 2 + 1] = mBuffer.get(i * 2);
                }
                bufferInfo.setInfo(0, VIDEO_WIDTH * VIDEO_HEIGHT * 3 / 2, mImage.getTimestamp(), 0);
                net.sourceforge.zbar.Image barcode = new net.sourceforge.zbar.Image(mImage.getImageSize().width, mImage.getImageSize().height, "Y800");
                barcode.setData(YUV_DATA);


                if (scanner.scanImage(barcode) != 0) {
                    for (Symbol sym : scanner.getResults()) {
                        handler.postSyncTask(new Runnable() {
                            @Override
                            public void run() {

                                QrManager.getInstance().getResultCallback().onScanSuccess(sym.getData());
                                mPlayer.prepare();
                                mPlayer.play();
                                closeFlash();
                                if (mcamera != null) {
                                    mcamera.release();
                                    mcamera = null;
                                }
                                terminate();
                            }
                        });
                    }
                }
                mImage.release();
                return;
            }
        }
    }

    private void openCamera() {
        // 获取 CameraKit 对象
        cameraKit = CameraKit.getInstance(this);
        if (cameraKit == null) {
            return;
        }
        try {
            // 获取当前设备的逻辑相机列表cameraIds
            String[] cameraIds = cameraKit.getCameraIds();
            // 创建相机！
            cameraKit.createCamera(cameraIds[0], new CameraStateCallbackImpl(), new EventHandler(EventRunner.create("CameraCb")));
        } catch (IllegalStateException e) {
            System.out.println("getCameraIds fail");
        }
    }


    private final class CameraStateCallbackImpl extends CameraStateCallback {
        //相机回调
        @Override
        public void onCreated(Camera camera) {
            mcamera = camera;
            //相机创建时回调
            CameraConfig.Builder cameraConfigBuilder = camera.getCameraConfigBuilder();
            if (cameraConfigBuilder == null) {
                return;
            }
            // 配置预览的 Surface
            cameraConfigBuilder.addSurface(previewSurface);
            // 配置拍照的 Surface
            dataSurface = imageReceiver.getRecevingSurface();
            cameraConfigBuilder.addSurface(dataSurface);

            frameConfigBuilder = mcamera.getFrameConfigBuilder(FRAME_CONFIG_PREVIEW);
            try {
                // 相机设备配置
                camera.configure(cameraConfigBuilder.build());
            } catch (IllegalArgumentException e) {
                System.out.println("Argument Exception");
            } catch (IllegalStateException e) {
                System.out.println("State Exception");
            }
        }

        @Override
        public void onConfigured(Camera camera) {
            // 配置预览 Surface
            frameConfigBuilder.addSurface(previewSurface);
            // 配置拍照的 Surface
            frameConfigBuilder.addSurface(dataSurface);
            try {
                // 启动循环帧捕获
                mcamera.triggerLoopingCapture(frameConfigBuilder.build());
            } catch (IllegalArgumentException e) {
                System.out.println("Argument Exception");
            } catch (IllegalStateException e) {
                System.out.println("State Exception");
            }
        }

        @Override
        public void onReleased(Camera camera) {
            // 释放相机设备
            if (mcamera != null) {
                mcamera.release();
                mcamera = null;
            }
        }
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        if (mcamera != null) {
            mcamera.stopLoopingCapture();
            mcamera.release();
            mcamera = null;
        }
        QrCodeAbilitySlice.super.terminate();
    }
}
