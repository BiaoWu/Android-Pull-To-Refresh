package com.biao.pulltorefresh.utils;

import android.os.Build;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by biaowu on 15/12/22.
 */
public class ViewScrollChecker {
    public static final byte DIRECTION_DOWN = -1;
    public static final byte DIRECTION_UP = 1;
    private static final boolean DEBUG_SCROLL_CHECK = false;

    public static boolean isDirectionDown(byte direction) {
        return direction == DIRECTION_DOWN;
    }

    public static boolean isDirectionUp(byte direction) {
        return direction == DIRECTION_UP;
    }

    public static boolean canViewScrollVerticallyDown(View view) {
        return canViewScrollVertically(view, DIRECTION_DOWN);
    }

    public static boolean canViewScrollVerticallyUp(View view) {
        return canViewScrollVertically(view, DIRECTION_UP);
    }

    public static boolean canViewScrollVertically(View view, int direction) {
        if (DEBUG_SCROLL_CHECK)
            L.e("direction is " + (direction > 0 ? "up" : "down"));
        if (Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {//list view etc.
                final AbsListView absListView = (AbsListView) view;
                int childCount = absListView.getChildCount();
                if (childCount > 0) {
                    switch (direction) {
                        case DIRECTION_DOWN:
                            return (absListView.getFirstVisiblePosition() > 0
                                    || absListView.getChildAt(0)
                                    .getTop() < absListView.getPaddingTop());
                        case DIRECTION_UP:
                            return (absListView.getLastVisiblePosition() < childCount
                                    || absListView.getChildAt(childCount - 1)
                                    .getBottom() > absListView.getBottom());
                        default:
                            return false;
                    }
                } else {
                    return false;
                }
            } else {//TODO RecyclerView
                return view.getScrollY() > 0;
            }
        } else {
            boolean canScrollVertically = view.canScrollVertically(direction);
            if (DEBUG_SCROLL_CHECK)
                L.e("canViewScrollVertically = " + canScrollVertically);
            return canScrollVertically;
        }
    }
}
