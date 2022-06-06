package com.yan.zrefreshview;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.Component;

/**
 * 工具类
 */
public class AnimationUtil {
    private static AnimationUtil mInstrument = null;
    private AnimatorValue mCurrentAnimation = null;

    /**
     * 返回实例
     *
     * @return Instrument实例
     */
    public static AnimationUtil getInstance() {
        if (mInstrument == null) {
            mInstrument = new AnimationUtil();
        }
        return mInstrument;
    }

    /**
     * 滑动
     *
     * @param component 组件
     * @param delta     偏移量
     */
    public void slidingByDelta(Component component, float delta) {
        if (component == null) {
            return;
        }
        clearAnimation();
        component.setTranslationY(delta);
    }

    /**
     * 滑动
     *
     * @param component 组件
     * @param positionY 坐标Y
     */
    public void slidingToY(Component component, float positionY) {
        if (component == null) {
            return;
        }
        clearAnimation();
        component.setContentPositionY(positionY);
    }


    /**
     * 回弹动画
     *
     * @param component    组件
     * @param translationY 偏移量
     * @param duration     动画时间
     */
    public void reset(Component component, float translationY, long duration) {
        reset(component, translationY, null, duration);
    }

    /**
     * @param component
     * @param translationY
     * @param listener
     * @param duration
     */
    public void reset(Component component, float translationY, Animator.StateChangedListener listener, long duration) {
        if (component == null) {
            return;
        }
        clearAnimation();
        mCurrentAnimation = new AnimatorValue();
        mCurrentAnimation.setDuration(duration);
        mCurrentAnimation.setCurveType(Animator.CurveType.ACCELERATE_DECELERATE); // 匀速
        mCurrentAnimation.setStateChangedListener(listener);
        mCurrentAnimation.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float value) {
                component.setTranslationY(translationY - translationY * value);
            }
        });
        mCurrentAnimation.start();
    }

    /**
     * 滑动动画
     *
     * @param component    组件
     * @param translationY 偏移量
     * @param duration     动画时间
     */
    public void smoothTo(Component component, float translationY, long duration) {
        if (component == null) {
            return;
        }
        clearAnimation();
        mCurrentAnimation = new AnimatorValue();
        mCurrentAnimation.setDuration(duration);
        mCurrentAnimation.setCurveType(Animator.CurveType.LINEAR); // 匀速
        mCurrentAnimation.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float value) {
                component.setTranslationY(translationY * value);
            }
        });
        mCurrentAnimation.start();
    }

    /**
     * 停止并释放动画
     */
    public void clearAnimation() {
        if (mCurrentAnimation != null) {
            mCurrentAnimation.stop();
            mCurrentAnimation.release();
            mCurrentAnimation = null;
        }
    }

}
