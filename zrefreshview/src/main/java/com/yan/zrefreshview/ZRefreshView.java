package com.yan.zrefreshview;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.app.Context;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yandeqing
 * 备注:
 * @date 2021/7/20
 */
public class ZRefreshView extends DirectionalLayout implements Component.LayoutRefreshedListener {

    /**
     * 下拉拖动的黏性比率
     */
    private static final float STICK_RATIO = .418f;

    /**
     * 最小滑动距离检测前提
     */
    private static final float BLOCK_DISTANCE = 30;

    private static final int RESET_DURATION = 200;
    /**
     * 刷新完成头部停留时长
     */
    private int showDuration = 1000;

    private RefreshListener refreshListener;

    private LoadMoreListener loadMoreListener;


    private static final String AutoLoadMore = "AutoLoadMore";

    private static final String EnableLoadMore = "EnableLoadMore";

    private static final String EnableRefresh = "EnableRefresh";

    private static final String RefreshBgColor = "RefreshBgColor";

    private static final String RefreshTextColor = "RefreshTextColor";

    /**
     * 是否自动加载更多
     */
    boolean isAutoLoadMore = false;

    boolean isEnableLoadMore = false;

    boolean isEnableRefresh = true;


    private int bgColor;

    private int refreshTextColor;

    public void setOnRefreshListener(RefreshListener listener) {
        refreshListener = listener;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void setEnableRefresh(boolean enableRefresh) {
        isEnableRefresh = enableRefresh;
    }

    public boolean isEnableRefresh() {
        return isEnableRefresh;
    }

    public void setAutoLoadMore(boolean autoLoadMore) {
        isAutoLoadMore = autoLoadMore;
    }


    public void setEnableLoadMore(boolean enableLoadMore) {
        isEnableLoadMore = enableLoadMore;
    }

    /**
     * 设置刷新完成头部停留时长 ms
     *
     * @param showDuration
     */
    public void setShowDuration(int showDuration) {
        this.showDuration = showDuration;
    }

    /**
     * 刷新完成时候显示的文案
     *
     * @param hintText
     */
    public void setHeaderTx(String hintText) {
        header.setCompleteHintText(hintText);
    }

    /**
     * 下拉头的View
     */
    private RefreshHeaderFooter header;
    /**
     * 上拉底部的View
     */
    private RefreshHeaderFooter footer;


    // 正面Component
    private Component mTargetComponent;

    /**
     * 下拉上拉控件高度
     */
    private int hideHeaderHeight = AttrHelper.vp2px(50, mContext);

    /**
     * 当前状态
     */
    private RefreshState currentStatus = RefreshState.IDLE_STATE;

    /**
     * 手指按下时屏幕纵坐标
     */
    private float preDownY;


    public ZRefreshView(Context context) {
        this(context, null);
    }

    public ZRefreshView(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public ZRefreshView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        setOrientation(Component.VERTICAL);
        if (getChildCount() > 1) {
            throw new RuntimeException("ZRefreshView can only contain one View getChildCount=" + getChildCount());
        }
        initView(context, attrSet);
    }


    private void initView(Context context, AttrSet attrs) {
        header = new RefreshHeaderFooter(context, attrs);
        addComponent(header, 0);
        setLayoutRefreshedListener(this);

        boolean isEnableRefreshPresent = attrs.getAttr(EnableRefresh).isPresent();
        if (isEnableRefreshPresent) {
            this.isEnableRefresh = attrs.getAttr(EnableRefresh).get().getBoolValue();
        }

        boolean isEnableAutoLoadMorePresent = attrs.getAttr(EnableLoadMore).isPresent();
        if (isEnableAutoLoadMorePresent) {
            this.isEnableLoadMore = attrs.getAttr(EnableLoadMore).get().getBoolValue();
        }

        boolean isPresent = attrs.getAttr(AutoLoadMore).isPresent();
        if (isPresent) {
            this.isAutoLoadMore = attrs.getAttr(AutoLoadMore).get().getBoolValue();
        }

        boolean refreshBg = attrs.getAttr(RefreshBgColor).isPresent();
        if (refreshBg) {
            this.bgColor = attrs.getAttr(RefreshBgColor).get().getColorValue().getValue();
        } else {
            this.bgColor = Color.getIntColor("#22000000");
        }
        boolean refreshTextBg = attrs.getAttr(RefreshTextColor).isPresent();
        if (refreshTextBg) {
            this.refreshTextColor = attrs.getAttr(RefreshTextColor).get().getColorValue().getValue();
        } else {
            this.refreshTextColor = Color.getIntColor("#000000");
        }
    }


    @Override
    public void onRefreshed(Component component) {
        if (getChildCount() == 0) {
            return;
        }
        if (mTargetComponent == null) {
            ensureTarget();
        }
        if (mTargetComponent == null) {
            return;
        }
        hideHeaderHeight = -header.getHeight();
        header.setMarginTop(hideHeaderHeight);
        footer = new RefreshHeaderFooter(getContext());
        addComponent(footer);
        footer.setHeaderLodingStr("正在加载...");
        footer.setMarginTop(hideHeaderHeight);
        footer.setVisibility(Component.HIDE);

        if (bgColor != 0) {
            ShapeElement element = new ShapeElement();
            element.setRgbColor(RgbColor.fromArgbInt(bgColor));
            header.setBackground(element);
            footer.setBackground(element);
        }

        if (refreshTextColor != 0) {
            header.setTextColor(refreshTextColor);
            footer.setTextColor(refreshTextColor);
        }

        setDraggedListener(DRAG_VERTICAL, draggedListener);
        setLayoutRefreshedListener(null);
        if (getTargetComponent() instanceof ListContainer) {
            ListContainer targetComponent = (ListContainer) getTargetComponent();
            targetComponent.setScrollListener(() -> {
                if (isScrollBottom() && isAutoLoadMore && isEnableLoadMore) {
                    LogUtil.info(TAG, "onScrollFinished().滑动到底部");
                    if (!isReFreshingOrLoading()) {
                        if (loadMoreListener != null) {
                            loadMoreListener.onLoadMore();
                            getAnimationUtil().slidingByDelta(mTargetComponent, -footer.getHeight());
                            currentStatus = RefreshState.REFRESHING_STATE;
                            footer.changeWidgetState(currentStatus);
                            footer.setHeaderLodingStr("正在加载...");
                            footer.setVisibility(Component.VISIBLE);
                        }
                    }
                }

            });
        }

    }

    /**
     * 检测容器是否滑动到底部
     *
     * @return boolean
     */
    boolean isScrollBottom() {
        if (getTargetComponent() instanceof ListContainer) {
            ListContainer targetComponent = (ListContainer) getTargetComponent();
            int lastVisibleItemPosition = targetComponent.getItemPosByVisibleIndex(targetComponent.getVisibleIndexCount() - 1);
            return lastVisibleItemPosition == targetComponent.getChildCount() - 1;
        } else {
            return false;
        }
    }

    private void ensureTarget() {
        if (mTargetComponent == null) {
            mTargetComponent = getComponentAt(getChildCount() - 1);
        }
    }

    /**
     * 获取正面Component
     *
     * @return 正面组件
     */
    public Component getTargetComponent() {
        return this.mTargetComponent;
    }


    private AnimationUtil getAnimationUtil() {
        return AnimationUtil.getInstance();
    }


    private static final String TAG = ZRefreshView.class.getSimpleName();


    private boolean isReFreshingOrLoading() {
        return currentStatus == RefreshState.REFRESHING_STATE ||
                currentStatus == RefreshState.FINISHED_TO_REFRESH;
    }

    private DraggedListener draggedListener = new DraggedListener() {


        @Override
        public void onDragDown(Component component, DragInfo dragInfo) {

        }

        @Override
        public void onDragStart(Component component, DragInfo dragInfo) {
            if (!isEnableRefresh()) {
                return;
            }
            if (isReFreshingOrLoading()) {
                return;
            }
            preDownY = dragInfo.startPoint.getPointYToInt();
        }

        @Override
        public void onDragUpdate(Component component, DragInfo dragInfo) {

            if (!isEnableRefresh()) {
                return;
            }

            if (isReFreshingOrLoading()) {
                return;
            }

            int updateY = dragInfo.updatePoint.getPointYToInt();
            int scrollValue = getTargetComponent().getScrollValue(Component.AXIS_Y);
            int estimatedHeight = getTargetComponent().getEstimatedHeight();
            LogUtil.info(TAG, "onDragUpdate().estimatedHeight=" + estimatedHeight);
            LogUtil.info(TAG, "onDragUpdate().scrollValue=" + scrollValue);
            LogUtil.info(TAG, "onDragUpdate().updateY=" + updateY + ",preDownY=" + preDownY);
            if (scrollValue > 0 && updateY > preDownY) {
                return;
            }

            float currY = dragInfo.updatePoint.getPointYToInt();
            float distance = currY - preDownY;
            if (Math.abs(distance) > BLOCK_DISTANCE) {
                if (distance > 0) {
                    float offsetY = distance * STICK_RATIO;
                    if (Math.abs(offsetY) > Math.abs(hideHeaderHeight) * 2) {
                        return;
                    }
                    if (offsetY > Math.abs(hideHeaderHeight)) {
                        // 头部全部被下拉出来的时候状态转换为释放刷新
                        currentStatus = RefreshState.RELEASE_TO_REFRESH;
                    } else {
                        currentStatus = RefreshState.PULL_TO_REFRESH;
                    }
                    // 通过偏移下拉头的topMargin值，来实现下拉效果
                    header.setMarginTop((int) (offsetY + hideHeaderHeight));
                    header.changeWidgetState(currentStatus);
                } else {
                    if (isEnableLoadMore && isScrollBottom()) {
                        if (Math.abs(distance * STICK_RATIO) > Math.abs(hideHeaderHeight)) {
                            return;
                        }
                        float tempOffset = 1 - Math.abs(mTargetComponent.getTranslationY()
                                / mTargetComponent.getHeight());
                        distance = distance * STICK_RATIO * tempOffset;
                        getAnimationUtil().slidingByDelta(mTargetComponent, distance);
                    }
                }
            }
        }

        @Override
        public void onDragEnd(Component component, DragInfo dragInfo) {
            if (!isEnableRefresh()) {
                return;
            }
            if (isReFreshingOrLoading()) {
                return;
            }

            float currY = dragInfo.updatePoint.getPointYToInt();
            float distance = currY - preDownY;
            if (distance > 0) {
                if (currentStatus == RefreshState.PULL_TO_REFRESH) {
                    resetHeader();
                } else if (currentStatus == RefreshState.RELEASE_TO_REFRESH) {
                    startRefresh();
                }
            } else {
                if (isEnableLoadMore && isScrollBottom()) {
                    if (loadMoreListener != null) {
                        loadMoreListener.onLoadMore();
                        currentStatus = RefreshState.REFRESHING_STATE;
                        footer.changeWidgetState(currentStatus);
                        footer.setHeaderLodingStr("正在加载...");
                        footer.setVisibility(Component.VISIBLE);
                    }
                }
            }
        }

        @Override
        public void onDragCancel(Component component, DragInfo dragInfo) {
        }
    };


    // 回滚到头部刷新控件的高度，并触发后台刷新任务
    private void startRefresh() {
        AnimatorValue rbToHeaderAnimator = new AnimatorValue();
        rbToHeaderAnimator.setDuration(RESET_DURATION);
        rbToHeaderAnimator.setCurveType(Animator.CurveType.ACCELERATE_DECELERATE);
        int topMargin = header.getMarginTop();
        rbToHeaderAnimator.setValueUpdateListener((animatorValue, v) -> {
            int marginValue = (int) (topMargin * (1 - v));
            header.setMarginTop(marginValue);
        });
        rbToHeaderAnimator.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                currentStatus = RefreshState.REFRESHING_STATE;
                header.changeWidgetState(currentStatus);
                if (refreshListener != null) {
                    refreshListener.onPullRefreshing();
                }
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        rbToHeaderAnimator.start();
    }


    // 回滚下拉刷新头部控件
    private void resetHeader() {
        int tempHeaderTopMargin = header.getMarginTop();
        AnimatorValue rbAnimator = new AnimatorValue();
        rbAnimator.setDuration(RESET_DURATION);
        rbAnimator.setCurveType(Animator.CurveType.DECELERATE);
        rbAnimator.setValueUpdateListener((animatorValue, v) -> {
            int marginValue = (int) ((header.getHeight() + tempHeaderTopMargin) * v);
            header.setMarginTop(-marginValue + tempHeaderTopMargin);
        });
        rbAnimator.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                if (currentStatus == RefreshState.PULL_TO_REFRESH || currentStatus == RefreshState.FINISHED_TO_REFRESH) {
                    currentStatus = RefreshState.IDLE_STATE;
                    return;
                }
                currentStatus = RefreshState.IDLE_STATE;
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        rbAnimator.start();
    }


    private boolean isEmptyByText(String text) {
        return text == null || text.equals("");
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        currentStatus = RefreshState.FINISHED_TO_REFRESH;
        header.changeWidgetState(currentStatus);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                resetHeader();
            }
        }, 1000);
    }


    public void finishLoadMore() {
        currentStatus = RefreshState.FINISHED_TO_REFRESH;
        footer.changeWidgetState(currentStatus);
        float translationY = mTargetComponent.getTranslationY();
        getAnimationUtil().reset(mTargetComponent, translationY, new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {

            }

            @Override
            public void onStop(Animator animator) {

            }

            @Override
            public void onCancel(Animator animator) {

            }

            @Override
            public void onEnd(Animator animator) {
                footer.setVisibility(Component.HIDE);
                if (currentStatus == RefreshState.PULL_TO_REFRESH || currentStatus == RefreshState.FINISHED_TO_REFRESH) {
                    currentStatus = RefreshState.IDLE_STATE;
                    return;
                }
                currentStatus = RefreshState.IDLE_STATE;
            }

            @Override
            public void onPause(Animator animator) {

            }

            @Override
            public void onResume(Animator animator) {

            }
        }, RESET_DURATION);

    }


    /**
     * 设置加载开始文字
     *
     * @param loadingText 加载文字
     */
    public void setLoadingText(String loadingText) {
        if (!isEmptyByText(loadingText)) {
            header.setHeaderLodingStr(loadingText);
        }
    }


    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     */
    public interface RefreshListener {
        /**
         * 刷新时回调方法
         */
        void onPullRefreshing();

    }

    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     */
    public interface LoadMoreListener {
        /**
         * 上拉时回调方法
         */
        void onLoadMore();
    }

}
