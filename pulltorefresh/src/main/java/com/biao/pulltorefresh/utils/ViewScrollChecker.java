package com.biao.pulltorefresh.utils;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by biaowu on 15/12/22.
 */
public class ViewScrollChecker {
    private static final String TAG = ViewScrollChecker.class.getSimpleName();
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
            L.e(TAG, "view = %s , direction is %s", view, (direction > 0 ? "up" : "down"));
        if (Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {//list view etc.
                return performAbsListView((AbsListView) view, direction);
            } else if (view instanceof RecyclerView) {
                return performRecyclerView((RecyclerView) view, direction);
            } else {
                if (DEBUG_SCROLL_CHECK)
                    L.e(TAG, "this type is not perform!!");
                return ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0;
            }
        } else {
            boolean canScrollVertically = view.canScrollVertically(direction);
            if (DEBUG_SCROLL_CHECK)
                L.e(TAG, "canViewScrollVertically = %s", canScrollVertically);
            return canScrollVertically;
        }
    }

    private static boolean performAbsListView(AbsListView view, int direction) {
        final AbsListView absListView = view;
        int childCount = absListView.getChildCount();
        if (childCount > 0) {
            switch (direction) {
                case DIRECTION_DOWN:
                    int firstItemTop = absListView.getChildAt(0).getTop();
                    int listViewTop = absListView.getTop() - absListView.getPaddingTop();
                    if (DEBUG_SCROLL_CHECK)
                        L.e(TAG, "firstItemTop=%s,listViewTop=%s", firstItemTop, listViewTop);
                    return (absListView.getFirstVisiblePosition() > 0
                            || firstItemTop < listViewTop);
                case DIRECTION_UP:
                    int lastItemBottom = absListView.getChildAt(childCount - 1).getBottom();
                    int listViewBottom = absListView.getBottom() - absListView.getPaddingBottom();
                    if (DEBUG_SCROLL_CHECK)
                        L.e(TAG, "lastItemBottom=%s,listViewBottom=%s", lastItemBottom, listViewBottom);
                    return (absListView.getLastVisiblePosition() < childCount - 1
                            || lastItemBottom > listViewBottom);
            }
        }
        if (DEBUG_SCROLL_CHECK)
            L.e(TAG, "AbsListView cannot scroll vertically or childCount is 0!!");
        return false;
    }

    private static boolean performRecyclerView(RecyclerView view, int direction) {
        RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
        int childCount = view.getAdapter().getItemCount();
        boolean canScrollVertically = layoutManager.canScrollVertically();
        if (DEBUG_SCROLL_CHECK) {
            L.e(TAG, "recyclerView canScrollVertically = %s, childCount = %s!!", canScrollVertically, childCount);
        }
        if (canScrollVertically && childCount > 0) {
            switch (direction) {
                case DIRECTION_DOWN:
                    int firstPosition = -1;
                    if (layoutManager instanceof GridLayoutManager) {
                        firstPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        firstPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        int[] firstPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                        ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(firstPositions);
                        firstPosition = findMin(firstPositions);
                    }
                    if (DEBUG_SCROLL_CHECK) {
                        L.e(TAG, "firstPosition = %s", firstPosition);
                    }
                    if (firstPosition == 0) {
                        int firstItemTop = view.findViewHolderForAdapterPosition(firstPosition).itemView.getTop();
                        int recyclerViewTop = view.getTop() - view.getPaddingTop();
                        if (DEBUG_SCROLL_CHECK)
                            L.e(TAG, "firstItemTop=%s,recyclerViewTop=%s", firstItemTop, recyclerViewTop);
                        return firstItemTop < recyclerViewTop;
                    } else {
                        return true;
                    }
                case DIRECTION_UP:
                    int lastPosition = -1;
                    if (layoutManager instanceof GridLayoutManager) {
                        lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                        ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(lastPositions);
                        lastPosition = findMax(lastPositions);
                    }
                    if (DEBUG_SCROLL_CHECK) {
                        L.e(TAG, "lastPosition = %s", lastPosition);
                    }

                    if (lastPosition == childCount - 1) {
                        int lastItemBottom = view.findViewHolderForAdapterPosition(lastPosition).itemView.getBottom();
                        int recyclerViewBottom = view.getBottom() - view.getPaddingBottom();
                        if (DEBUG_SCROLL_CHECK)
                            L.e(TAG, "lastItemBottom=%s,recyclerViewBottom=%s", lastItemBottom, recyclerViewBottom);
                        return lastItemBottom > recyclerViewBottom;
                    } else {
                        return true;
                    }
            }
        }
        return false;
    }

    private static int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private static int findMin(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value > min) {
                min = value;
            }
        }
        return min;
    }
}
