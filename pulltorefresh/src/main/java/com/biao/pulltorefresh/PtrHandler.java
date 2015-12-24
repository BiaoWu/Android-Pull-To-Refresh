package com.biao.pulltorefresh;

/**
 * Ptr Handler
 * Created by biaowu.
 */
public interface PtrHandler {

    /** when refresh begin */
    void onRefreshBegin();

    /** when refresh end */
    void onRefreshEnd();

    /** when refresh pulling */
    void onPercent(float percent);
}
