package com.biao.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.biao.pulltorefresh.utils.L;
import com.biao.pulltorefresh.utils.ViewScrollChecker;


/**
 * Created by biaowu.
 */
public class PtrLayout extends ViewGroup {
    private static final String TAG = PtrLayout.class.getSimpleName();
    private static final boolean DEBUG_INTERCEPT = false;
    private static final boolean DEBUG_TOUCH = false;

    private static final int DEFAULT_PULL_MAX_DISTANCE = 80;//dp
    private static final float DRAG_RATE = .5f;

    private static final int FLAG_NONE = 0;
    private static final int FLAG_IS_INTERCEPT = 1;
    private static final int FLAG_IS_REFRESH_DOWN = 1 << 1;
    private static final int FLAG_IS_REFRESH_UP = 1 << 2;

    //config
    private int mTouchSlop;
    private int mTotalDragDistance;
    private boolean mOpenDownRefresh;
    private boolean mOpenUpRefresh;

    //touch event
    private float mLastDownY;
    private float mDistanceY;
    private byte mInterceptDirection;
    private boolean mIsBeingDragged;
    private boolean mNoBodyNeedEvent;

    //scroller
    private PtrScroller mPtrScroller;

    //
    private PtrViewHolder mHeaderView;
    private PtrViewHolder mFooterView;
    private PtrViewHolder mContentView;

    private PtrHandler mHeaderPtrHandler;
    private PtrHandler mFooterPtrHandler;
    //state
    private boolean mIsRefreshing;
    private int mFlag;


    private OnRefreshListener mOnRefreshListener;

    public interface OnRefreshListener {
        void onPullDownRefresh();

        void onPullUpRefresh();
    }

    public PtrLayout(Context context) {
        super(context);
    }

    public PtrLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PtrLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PtrLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Context context = getContext();
        mHeaderView = new PtrViewHolder();
        mFooterView = new PtrViewHolder();
        mContentView = new PtrViewHolder();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = Math.round(
                DEFAULT_PULL_MAX_DISTANCE * context.getResources().getDisplayMetrics().density);

        int childCount = getChildCount();
        if (childCount == 1) {
            mContentView.view = getChildAt(0);
        } else {//TODO 考虑考虑
            throw new IllegalStateException("PtrLayout can only have one child now !!");
        }
        mPtrScroller = new PtrScroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView.isNotEmpty()) {
            measureChildWithMargins(mHeaderView.view, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContentView.isNotEmpty()) {
            final MarginLayoutParams lp = (MarginLayoutParams) mContentView.view.getLayoutParams();

            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);

            mContentView.view.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        if (mFooterView.isNotEmpty()) {
            measureChildWithMargins(mFooterView.view, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView.isNotEmpty()) {
            int headerHeight = mHeaderView.view.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.view.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = -headerHeight + mHeaderView.offsetY;
            final int right = left + mHeaderView.view.getMeasuredWidth();
            final int bottom = top + headerHeight;
            mHeaderView.view.layout(left, top, right, bottom);
        }

        if (mContentView.isNotEmpty()) {
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.view.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mContentView.view.getMeasuredWidth();
            final int bottom = top + mContentView.view.getMeasuredHeight();
            mContentView.view.layout(left, top, right, bottom);
        }

        if (mFooterView.isNotEmpty()) {
            int headerHeight = mFooterView.view.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.view.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = getMeasuredHeight() + mFooterView.offsetY;
            final int right = left + mFooterView.view.getMeasuredWidth();
            final int bottom = top + headerHeight;
            mFooterView.view.layout(left, top, right, bottom);
        }
    }

    public void setHeaderView(View view) {
        if (view != null && !mHeaderView.isNotEmpty()) {
            mOpenDownRefresh = true;
            mHeaderView.view = view;
            addView(view);
            if (view instanceof PtrHandler) {
                mHeaderPtrHandler = (PtrHandler) view;
            }
        }
    }

    public void setFooterView(View view) {
        if (view != null && !mFooterView.isNotEmpty()) {
            mOpenUpRefresh = true;
            mFooterView.view = view;
            addView(view);
            if (view instanceof PtrHandler) {
                mFooterPtrHandler = (PtrHandler) view;
            }
        }
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            if (DEBUG_INTERCEPT)
                L.e(TAG, "not enabled!");
            return false;
        }

        if (isNeedIntercept()) {
            if (DEBUG_INTERCEPT)
                L.e(TAG, "Animating!");
            return mIsBeingDragged = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mNoBodyNeedEvent = true;
                mIsBeingDragged = false;
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mNoBodyNeedEvent = false;
                //TODO 优化
                mDistanceY = event.getRawY() - mLastDownY;
                if (Math.abs(mDistanceY) < mTouchSlop) {
                    if (DEBUG_INTERCEPT)
                        L.e(TAG, "Distance so small!");
                    return mIsBeingDragged = false;
                }

                mInterceptDirection = mDistanceY > 0 ? ViewScrollChecker.DIRECTION_DOWN : ViewScrollChecker.DIRECTION_UP;
                mIsBeingDragged = !ViewScrollChecker.canViewScrollVertically(mContentView.view, mInterceptDirection);
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        if (DEBUG_INTERCEPT)
            L.e(TAG, "IsBeingDragged = " + mIsBeingDragged);
        return mIsBeingDragged;
    }

    private boolean isNeedIntercept() {
        return mPtrScroller.isAnimating() || mIsRefreshing;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        //do nothing for onTouchEvent
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            if (DEBUG_TOUCH)
                L.e(TAG, "is not enable!");
            return false;
        }

        if (mPtrScroller.isAnimating()) {
            return true;
        }

        if (mNoBodyNeedEvent) {
//            onTouchEventNoIntercept(event);
//            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownY = event.getRawY();
                mPtrScroller.abortScroll();
                break;
            case MotionEvent.ACTION_MOVE:
                mDistanceY = event.getRawY() - mLastDownY;
                mDistanceY *= DRAG_RATE;

                if (DEBUG_TOUCH)
                    L.e(TAG, "mDistanceY=" + mDistanceY
                            + ",mNoBodyNeedEvent=" + mNoBodyNeedEvent
                            + ",mInterceptDirection=" + mInterceptDirection);

                switch (mInterceptDirection) {
                    case ViewScrollChecker.DIRECTION_DOWN:
                        performDown((int) mDistanceY);
                        break;
                    case ViewScrollChecker.DIRECTION_UP:
                        performUp((int) mDistanceY);
                        break;
                }
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                int scrollY = 0;
                if (DEBUG_TOUCH)
                    L.e(TAG, "mInterceptDirection = " + mInterceptDirection);
                switch (mInterceptDirection) {
                    case ViewScrollChecker.DIRECTION_DOWN:
                        if (!mOpenDownRefresh) {
                            mPtrScroller.smoothScroll(mContentView.offsetY);
                            if (DEBUG_TOUCH)
                                L.e(TAG, "Down Refresh not open");
                            return true;
                        }
                        scrollY = mHeaderView.offsetY;
                        break;
                    case ViewScrollChecker.DIRECTION_UP:
                        if (!mOpenUpRefresh) {
                            mPtrScroller.smoothScroll(mContentView.offsetY);
                            if (DEBUG_TOUCH)
                                L.e(TAG, "Up Refresh not open");
                            return true;
                        }
                        scrollY = mFooterView.offsetY;
                        break;
                }

                if (!mIsRefreshing) {
                    if (Math.abs(scrollY) < mTotalDragDistance) {
                        mPtrScroller.smoothScroll(scrollY);
                    } else {
                        performRefresh(scrollY);
                    }
                } else {
                    if (Math.abs(scrollY) > mTotalDragDistance) {
                        mPtrScroller.smoothScroll(scrollY > 0 ? scrollY - mTotalDragDistance
                                : scrollY + mTotalDragDistance);
                    } else {
                        mPtrScroller.smoothScroll(scrollY);
                    }
                }
                break;
        }
        return true;
    }

    //TODO
    private void onTouchEventNoIntercept(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //fix no body need event
                if (mNoBodyNeedEvent && !mIsRefreshing) {
                    switch (mInterceptDirection) {
                        case ViewScrollChecker.DIRECTION_DOWN:
                            if (mHeaderView.offsetY == 0 && mContentView.offsetY == 0) {
                                mInterceptDirection = 0;
                            }
                            break;
                        case ViewScrollChecker.DIRECTION_UP:
                            if (mFooterView.offsetY == 0 && mContentView.offsetY == 0) {
                                mInterceptDirection = 0;
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //fix no body need event
                if (mNoBodyNeedEvent && mInterceptDirection == 0 && mDistanceY != 0) {
                    mInterceptDirection = mDistanceY > 0 ? ViewScrollChecker.DIRECTION_DOWN
                            : ViewScrollChecker.DIRECTION_UP;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
    }

    private void performMove(int distanceY) {
        mContentView.offsetTopAndBottom(distanceY);

        invalidate();
    }

    private void performDown(int distanceY) {
        if (mHeaderView.isNotEmpty() && mHeaderView.offsetY + distanceY < 0) {
            return;
        }

        mContentView.offsetTopAndBottom(distanceY);
        mHeaderView.offsetTopAndBottom(distanceY);

        invalidate();

        if (!mIsRefreshing && mHeaderPtrHandler != null) {
            float percent = mHeaderView.offsetY * 1f / mTotalDragDistance;
            mHeaderPtrHandler.onPercent(percent);
        }
    }

    private void performUp(int distanceY) {
        if (mFooterView.isNotEmpty() && mFooterView.offsetY + distanceY > 0) {
            return;
        }

        mContentView.offsetTopAndBottom(distanceY);
        mFooterView.offsetTopAndBottom(distanceY);

        invalidate();

        if (!mIsRefreshing && mFooterPtrHandler != null) {
            float percent = Math.abs(mFooterView.offsetY * 1f) / mTotalDragDistance;
            mFooterPtrHandler.onPercent(percent);
        }
    }


    private void performRefresh(int startY) {
        mIsRefreshing = true;
        switch (mInterceptDirection) {
            case ViewScrollChecker.DIRECTION_DOWN:
                if (mHeaderPtrHandler != null) {
                    mHeaderPtrHandler.onRefreshBegin();
                }
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onPullDownRefresh();
                }
                break;
            case ViewScrollChecker.DIRECTION_UP:
                if (mFooterPtrHandler != null) {
                    mFooterPtrHandler.onRefreshBegin();
                }
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onPullUpRefresh();
                }
                break;
        }

        int endY = startY > 0 ? startY - mTotalDragDistance : mTotalDragDistance - Math.abs(startY);
        mPtrScroller.smoothScroll(endY);
    }


    public void onDownRefreshComplete() {
        performRefreshComplete(mHeaderView.offsetY);
    }

    public void onUpRefreshComplete() {
        performRefreshComplete(mFooterView.offsetY);
    }

    private void performRefreshComplete(int startY) {
        mIsRefreshing = false;
        switch (mInterceptDirection) {
            case ViewScrollChecker.DIRECTION_DOWN:
                if (mHeaderPtrHandler != null) {
                    mHeaderPtrHandler.onRefreshEnd();
                }
                break;
            case ViewScrollChecker.DIRECTION_UP:
                if (mFooterPtrHandler != null) {
                    mFooterPtrHandler.onRefreshEnd();
                }
                break;
        }

        if (!mIsBeingDragged) {
            mPtrScroller.smoothScroll(startY);
        }
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    private class PtrScroller implements Runnable {
        private static final int DEFAULT_RESET_TIME = 500;
        //scroller
        private int mLastScrollY;
        private Scroller mScroller;
        private boolean mIsAnimating;

        PtrScroller(Context context) {
            mScroller = new Scroller(context);
        }

        boolean isAnimating() {
            return mIsAnimating;
        }

        void smoothScroll(int startY, int duration) {
            mIsAnimating = true;
            mLastScrollY = 0;
            mScroller.startScroll(0, 0, 0, startY, duration);
            post(this);
        }

        void smoothScroll(int startY) {
            smoothScroll(startY, DEFAULT_RESET_TIME);
        }


        void finish() {
            removeCallbacks(this);
            mIsAnimating = false;
        }

        void abortScroll() {
            removeCallbacks(this);
//            mScroller.forceFinished(true);
            mScroller.abortAnimation();
            mIsAnimating = false;
        }

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                int currY = mScroller.getCurrY();
                switch (mInterceptDirection) {
                    case ViewScrollChecker.DIRECTION_DOWN:
                        performDown(mLastScrollY - currY);
                        break;
                    case ViewScrollChecker.DIRECTION_UP:
                        performUp(mLastScrollY - currY);
                        break;
                    default:
                        performMove(mLastScrollY - currY);
                        break;
                }
                mLastScrollY = currY;
                post(this);
            } else {
                finish();
            }
        }
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
