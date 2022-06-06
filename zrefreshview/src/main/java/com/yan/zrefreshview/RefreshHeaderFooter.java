package com.yan.zrefreshview;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.TextTool;
import ohos.app.Context;


/**
 * @author  yandeqing
 * 备注:头部底部布局
 * @date 2021/8/13
 *
 */
public class RefreshHeaderFooter extends StackLayout {

    private Context mContext;
    private Component headerView;
    private Text tipTextView;

    //箭头ImageView
    private Image arrowImageView;

    //等待ImageView（有动画）
    private Image waitImageView;

    //刷新成功之后显示的ImageView
    private Image successImageView;

    //刷新失败之后显示的ImageView
    private Image failureImageView;

    private String completeHintText;

    /**
     *刷新或者加载完成的时候显示的文本
     * @param completeHintText
     */
    public void setCompleteHintText(String completeHintText) {
        this.completeHintText = completeHintText;
    }

    /**
     * 根据当前状态设置HeaderView的子控件
     */
    public void changeWidgetState(RefreshState mState) {
        switch (mState) {
            case PULL_TO_REFRESH:
                tipTextView.setText("下拉刷新");
                arrowImageView.setRotation(0);
                arrowImageView.setVisibility(Component.VISIBLE);
                waitImageView.setVisibility(Component.INVISIBLE);
                successImageView.setVisibility(Component.INVISIBLE);
                break;
            case RELEASE_TO_REFRESH:
                tipTextView.setText("释放立即刷新");
                arrowImageView.setVisibility(Component.VISIBLE);
                waitImageView.setVisibility(Component.INVISIBLE);
                successImageView.setVisibility(Component.INVISIBLE);
                arrowImageView.setRotation(180);
                break;
            case REFRESHING_STATE:
                arrowImageView.setVisibility(Component.INVISIBLE);
                waitImageView.setVisibility(Component.VISIBLE);
                successImageView.setVisibility(Component.INVISIBLE);
                startWaitAnimation();
                tipTextView.setText("正在刷新...");
                break;
            case FINISHED_TO_REFRESH:
                arrowImageView.setVisibility(Component.INVISIBLE);
                waitImageView.setVisibility(Component.INVISIBLE);
                successImageView.setVisibility(Component.VISIBLE);
                if (!TextTool.isNullOrEmpty(completeHintText)) {
                    tipTextView.setText(completeHintText);
                }else {
                    tipTextView.setText("刷新完成!");
                }
                break;
        }
    }

    private void startWaitAnimation() {
        AnimatorValue animator = new AnimatorValue();
        animator.setCurveType(Animator.CurveType.LINEAR); // 匀速
        animator.setDuration(500);
        animator.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float v) {
                waitImageView.setRotation(v * 360);
            }
        });
        animator.setLoopedCount(100);
        animator.start();
    }

    public RefreshHeaderFooter(Context context) {
        this(context, null);
    }

    public RefreshHeaderFooter(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public RefreshHeaderFooter(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        mContext = context;
        initView(attrSet);
    }


    private void initView(AttrSet attrSet) {
        headerView = LayoutScatter.getInstance(mContext).parse(ResourceTable.Layout_refresh_header, null, true);
        tipTextView = (Text) headerView.findComponentById(ResourceTable.Id_pull_to_refresh_text);
        arrowImageView = (Image) headerView.findComponentById(ResourceTable.Id_refresh_arrow_image);
        waitImageView = (Image) headerView.findComponentById(ResourceTable.Id_wait_circuit_image);
        successImageView = (Image) headerView.findComponentById(ResourceTable.Id_refresh_success_image);
        failureImageView = (Image) headerView.findComponentById(ResourceTable.Id_refresh_failure_image);
        ComponentContainer.LayoutConfig config = getLayoutConfig();
        config.width = ComponentContainer.LayoutConfig.MATCH_PARENT;
        config.height =AttrHelper.vp2px(50, mContext);
        addComponent(headerView, config);
        setLayoutConfig(config);
    }


    /**
     * 加载时的文字
     *
     * @param loadingStr 文字内容
     */
    public void setHeaderLodingStr(String loadingStr) {
        tipTextView.setText(loadingStr);
    }

    /**
     *
     * @param color
     */
    public void setTextColor(int color) {
        tipTextView.setTextColor(new Color(color));
    }




}
