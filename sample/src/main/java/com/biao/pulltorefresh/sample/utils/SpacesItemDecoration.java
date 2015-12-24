package com.biao.pulltorefresh.sample.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int space; //px

    /**
     * 自行处理不和谐
     * @param space px
     */
    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    /**
     * only setPadding
     * @param recyclerView   RecyclerView
     * @param space px
     */
    public SpacesItemDecoration(RecyclerView recyclerView, int space) {
        this(space);
        int halfSpace = space / 2;
        recyclerView.setPadding(halfSpace, halfSpace, halfSpace, halfSpace);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int halfSpace = this.space / 2;
        outRect.left = halfSpace;
        outRect.right = halfSpace;
        outRect.top = halfSpace;
        outRect.bottom = halfSpace;
    }
}