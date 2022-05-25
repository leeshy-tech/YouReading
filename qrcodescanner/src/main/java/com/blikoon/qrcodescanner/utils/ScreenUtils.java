package com.blikoon.qrcodescanner.utils;

import ohos.agp.utils.Point;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;

public class ScreenUtils {

    private ScreenUtils(){}

    /**
     * 获取屏幕宽度
     *
     * @param context       上下文
     * @return 屏幕宽度
     */
    public static int getDisplayWidthInPx(Context context) {
        Display display = DisplayManager.getInstance().getDefaultDisplay(context).get();
        Point point = new Point();
        display.getSize(point);
        return (int) point.getPointX();
    }

    /**
     * 获取屏幕高度
     *
     * @param context       上下文
     * @return 屏幕宽度
     */
    public static int getDisplayHeightInPX(Context context){
        Display display = DisplayManager.getInstance().getDefaultDisplay(context).get();
        Point point = new Point();
        display.getSize(point);
        return (int) point.getPointY();
    }
}