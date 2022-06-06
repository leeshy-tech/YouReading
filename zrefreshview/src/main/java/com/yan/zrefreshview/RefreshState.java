package com.yan.zrefreshview;
/**
 * 刷新状态枚举：刷新中、初始状态、下拉刷新（已拉动）、释放刷新（已拉动）
 */
public enum RefreshState {
    REFRESHING_STATE,
    IDLE_STATE,
    PULL_TO_REFRESH,
    RELEASE_TO_REFRESH,
    FINISHED_TO_REFRESH,
}