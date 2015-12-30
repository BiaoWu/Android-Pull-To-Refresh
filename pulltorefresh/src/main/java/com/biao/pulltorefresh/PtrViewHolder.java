package com.biao.pulltorefresh;

import android.view.View;

import com.biao.pulltorefresh.utils.L;
import com.biao.pulltorefresh.utils.ViewScrollChecker;

/**
 * Created by biaowu.
 */
class PtrViewHolder {
    public static final int FLAG_PIN = 0;
    public static final int FLAG_MOVE = 1;

    View view;
    int flag;
    int offsetY;
    int releaseDist;
    PtrHandler ptrHandler;
    OnRefreshListener mOnRefreshListener;

    PtrViewHolder() {
        flag = FLAG_MOVE;
    }

    boolean isNotEmpty() {
        return view != null;
    }

    View getView() {
        return view;
    }

    void setView(View view) {
        this.view = view;
    }

    int getHeight() {
        return isNotEmpty() ? view.getHeight() : 0;
    }

    void setFlag(int flag) {
        if (checkFlag(flag)) {
            this.flag = flag;
        } else {
            L.e("flag value is bad!");
        }
    }

    void offsetTopAndBottom(int dist) {
        if (canScroll()) {
            view.offsetTopAndBottom(dist);
            offsetY += dist;
        }
    }

    boolean canScroll() {
        return isNotEmpty() && flag == FLAG_MOVE;
    }

    boolean isNotOriginalLocation() {
        return offsetY != 0;
    }

    boolean isOutOriginalLocation(byte direction, float dist) {
        if (isNotEmpty()) {
            switch (direction) {
                case ViewScrollChecker.DIRECTION_DOWN:
                    return offsetY + dist < 0;
                case ViewScrollChecker.DIRECTION_UP:
                    return offsetY + dist > 0;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    boolean canScrollAndIsNotOriginalLocation() {
        return canScroll() && isNotOriginalLocation();
    }

    private static boolean checkFlag(int flag) {
        return flag == FLAG_PIN || flag == FLAG_MOVE;
    }

    void setPtrHandler(PtrHandler ptrHandler) {
        this.ptrHandler = ptrHandler;
    }

    void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }
}
