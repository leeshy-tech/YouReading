package com.blikoon.qrcodescanner.view;

import com.blikoon.qrcodescanner.ResourceTable;
import com.blikoon.qrcodescanner.utils.ScreenUtils;
import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.DependentLayout;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.StackLayout;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.agp.utils.Rect;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayAttributes;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;

import java.math.BigDecimal;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial transparency outside
 * it, as well as the laser scanner animation and result points.
 */
public final class QrCodeFinderView extends DependentLayout implements Component.DrawTask {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private Paint mPaint;
    private int mScannerAlpha;
    private Color mMaskColor;
    private Color mFrameColor;
    private Color mLaserColor;
    private Color mTextColor;
    private Rect mFrameRect;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;
    private AnimatorValue mValueAnimator;

    public QrCodeFinderView(Context context) {
        this(context, null);
    }

    public QrCodeFinderView(Context context, AttrSet attrs) {
        this(context, attrs, "");
    }

    public QrCodeFinderView(Context context, AttrSet attrs, String defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();


        mMaskColor = Color.TRANSPARENT;
        mFrameColor = Color.TRANSPARENT;
        mLaserColor = Color.GREEN;
        mTextColor = Color.WHITE;

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;
        mScannerAlpha = 0;
        init(context);
    }

    private void init(Context context) {
        Component component = LayoutScatter.getInstance(context).parse(
                ResourceTable.Layout_qr_code_scanner, this, true);

        //DependentLayout relativeLayout = (DependentLayout) component.findComponentById(ResourceTable.Id_root);
        StackLayout frameLayout = (StackLayout) component.findComponentById(ResourceTable.Id_qr_code_fl_scanner);
        mFrameRect = new Rect();

        LayoutConfig layoutParams = (LayoutConfig) frameLayout.getLayoutConfig();

        mFrameRect.left = (ScreenUtils.getDisplayWidthInPx(context) - layoutParams.width) / 2;
        mFrameRect.top = layoutParams.getMarginTop();
        mFrameRect.right = mFrameRect.left + layoutParams.width;
        mFrameRect.bottom = mFrameRect.top + layoutParams.height;


        addDrawTask(this::onDraw);
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        Rect frame = mFrameRect;
        if (frame == null) {
            return;
        }
//        int width = canvas.getWidth();
//        int height = canvas.getHeight();
        int width = vp2px(getContext(), 250);
        int height = vp2px(getContext(), 250);

        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        drawFocusRect(canvas, frame);
        drawAngle(canvas, frame);
        drawText(canvas, frame);
        drawLaser(canvas, frame);
        initScanValueAnim();
        //postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }

    private void drawFocusRect(Canvas canvas, Rect rect) {
        mPaint.setColor(mFrameColor);
        //Up
        canvas.drawRect(rect.left + mAngleLength, rect.top, rect.right - mAngleLength, rect.top + mFocusThick, mPaint);
        //Left
        canvas.drawRect(rect.left, rect.top + mAngleLength, rect.left + mFocusThick, rect.bottom - mAngleLength,
                mPaint);
        //Right
        canvas.drawRect(rect.right - mFocusThick, rect.top + mAngleLength, rect.right, rect.bottom - mAngleLength,
                mPaint);
        //Down
        canvas.drawRect(rect.left + mAngleLength, rect.bottom - mFocusThick, rect.right - mAngleLength, rect.bottom,
                mPaint);
    }

    /**
     * Draw four purple angles
     *
     * @param canvas
     * @param rect
     */
    private void drawAngle(Canvas canvas, Rect rect) {
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(OPAQUE);
        mPaint.setStyle(Paint.Style.FILL_STYLE);
        mPaint.setStrokeWidth(mAngleThick);
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        // Top left angle
        canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, mPaint);
        canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, mPaint);
        // Top right angle
        canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, mPaint);
        canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, mPaint);
        // bottom left angle
        canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, mPaint);
        canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, mPaint);
        // bottom right angle
        canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, mPaint);
        canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, mPaint);
    }

    private void drawText(Canvas canvas, Rect rect) {
        int margin = 40;
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(vp2px(getContext(), 13));
        String text = "Position QR Code";
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        BigDecimal bignum1 = new BigDecimal(fontMetrics.bottom);
        BigDecimal bignum2 = new BigDecimal(fontMetrics.top);
        float fontTotalHeight = bignum1.subtract(bignum2).floatValue();

        BigDecimal bignum3 = new BigDecimal(fontTotalHeight);
        BigDecimal bignum4 = new BigDecimal(fontMetrics.bottom);
        BigDecimal bignum5 = new BigDecimal(2);
        float offY = bignum3.divide(bignum5).subtract(bignum4).floatValue();
        BigDecimal bignum6 = new BigDecimal(rect.bottom);
        BigDecimal bignum7 = new BigDecimal(margin);
        BigDecimal bignum8 = new BigDecimal(offY);
        float newY = bignum6.add(bignum7).add(bignum8).floatValue();

        DisplayAttributes attributes = DisplayManager.getInstance().getDefaultDisplay(getContext()).get().getAttributes();
        float screenScale = attributes.densityPixels;

        int left = (getDisplayWidthInPx(getContext()) - (mPaint.getTextSize()) * text.length()) / 2;

        BigDecimal bignum9 = new BigDecimal(left);
        BigDecimal bignum10 = new BigDecimal(55);
        BigDecimal bignum11 = new BigDecimal(screenScale);
        float correctedLeft = bignum10.multiply(bignum11).add(bignum9).floatValue();
        canvas.drawText(mPaint, text, correctedLeft, newY);
    }

    /**
     * vp转像素
     *
     * @param context 上下文
     * @param vp      vp值
     * @return int
     */
    public static int vp2px(Context context, float vp) {
        DisplayAttributes attributes = DisplayManager.getInstance().getDefaultDisplay(context).get().getAttributes();
        return (int) (attributes.densityPixels * vp);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕宽度
     */
    private int getDisplayWidthInPx(Context context) {
        Display display = DisplayManager.getInstance().getDefaultDisplay(context).get();
        Point point = new Point();
        display.getSize(point);
        return (int) point.getPointX();
    }

    private int i = 0;
    private int height;

    private void drawLaser(Canvas canvas, Rect rect) {
        height = rect.getHeight();
        i = i + 5;
        if (i >= height - 100) {
            i = 0;
        }
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
        mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = rect.top + 50 + i;
        canvas.drawRect(rect.left + 2, middle - 1, rect.right - 1, middle + 2, mPaint);
    }

    private void initScanValueAnim() {
        mValueAnimator = new AnimatorValue();
        mValueAnimator.setDuration(1000);
        mValueAnimator.setLoopedCount(AnimatorValue.INFINITE);
        mValueAnimator.setCurveType(Animator.CurveType.DECELERATE);
        mValueAnimator.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {

            @Override
            public void onUpdate(AnimatorValue animatorValue, float v) {
                if (height < 1){
                    height = 700;
                }
                height = (int) (height *  v);
                invalidate();
            }
        });
        mValueAnimator.start();
    }
}