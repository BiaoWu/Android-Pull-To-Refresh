package com.biao.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Scroller;

import com.biao.pulltorefresh.utils.L;


/**
 * Created by biaowu.
 */
public class PtrLayout extends ViewGroup {
    private static final String TAG = PtrLayout.class.getSimpleName();
    private static final byte DIRECTION_DOWN = -1;
    private static final byte DIRECTION_UP = 1;
    private static final int DEFAULT_PULL_MAX_DISTANCE = 50;//dp
    private static final int DEFAULT_RESET_TIME = 500;
    private static final float DRAG_RATE = .5f;
    private static final int INVALID_POINTER = -1;

    private static final boolean DEBUG_INTERCEPT = false;
    private static final boolean DEBUG_TOUCH = true;

    private float mRatio = 1.3f;

    private View mHeaderView;
    private View mContentView;
    private View mFooterView;

    private PtrHandler mHeaderPtrHandler;
    private PtrHandler mFooterPtrHandler;

    private int mTouchSlop;
    private int mTotalDragDistance;//pull 的最大距离，影响弹簧效果

    private float mLastDownY;//最后一次移动的点Y
    private float mDistanceY;//没次移动的距离Y
    private float mPullPercent;


    private byte mInterceptDirection;

    private int mContentOffsetY;
    private int mHeaderOffsetY;
    private int mFooterOffsetY;


    private int mLastScrollY;
    private Scroller mScroller;

    private boolean mOpenDownRefresh;
    private boolean mOpenUpRefresh;
    private boolean mAnimating;

    //swipe
    private boolean mNestedScrollInProgress;
    private boolean mRefreshing;
    private boolean mIsBeingDragged;
    private float mInitialDownY;
    private float mInitialMotionY;

    protected int mOriginalOffsetTop;
    private int mCurrentTargetOffsetTop;
    private float mSpinnerFinalOffset;


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

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = Math.round(
                DEFAULT_PULL_MAX_DISTANCE * context.getResources().getDisplayMetrics().density * mRatio);

        //TODO
        int childCount = getChildCount();
        if (childCount == 1) {
            mContentView = getChildAt(0);
        } else {
        }
        mScroller = new Scroller(context);

//        super.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return onTouchEvent(event);
//            }
//        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContentView != null) {
            final MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();

            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);

            mContentView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        if (mFooterView != null) {
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            int headerHeight = mHeaderView.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
//            final int top = mCurrentTargetOffsetTop - headerHeight;
//            final int top = 0;
            final int top = -headerHeight + mHeaderOffsetY;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + headerHeight;
            mHeaderView.layout(left, top, right, bottom);
        }

        int cotentViewHeight = 0;
        if (mContentView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mContentView.getMeasuredWidth();

            cotentViewHeight = mContentView.getMeasuredHeight();
            final int bottom = top + cotentViewHeight;
            mContentView.layout(left, top, right, bottom);
        }

        if (mFooterView != null) {
            int headerHeight = mFooterView.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = cotentViewHeight + mFooterOffsetY;
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + headerHeight;
            mFooterView.layout(left, top, right, bottom);
        }
    }

    public void setHeaderView(View view) {
        if (view != null && mHeaderView != view) {
            mOpenDownRefresh = true;
            mHeaderView = view;
            addView(view);
            if (view instanceof PtrHandler) {
                mHeaderPtrHandler = (PtrHandler) view;
            }
        }
    }

    public void setFooterView(View view) {
        if (view != null && mFooterView != view) {
            mOpenUpRefresh = true;
            mFooterView = view;
            addView(view);
            if (view instanceof PtrHandler) {
                mFooterPtrHandler = (PtrHandler) view;
            }
        }
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    private boolean canContentScrollVerticallyDown() {
        return canContentScrollVertically(DIRECTION_DOWN);
    }

    private boolean canContentScrollVerticallyUp() {
        return canContentScrollVertically(DIRECTION_UP);
    }

    private boolean canContentScrollVertically(int direction) {
        if (DEBUG_INTERCEPT)
            L.e("direction is " + (direction > 0 ? "up" : "down"));
        if (Build.VERSION.SDK_INT < 14) {
            if (mContentView instanceof AbsListView) {//list view etc.
                final AbsListView absListView = (AbsListView) mContentView;
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
                return mContentView.getScrollY() > 0;
            }
        } else {
            boolean canScrollVertically = mContentView.canScrollVertically(direction);
            if (DEBUG_INTERCEPT)
                L.e("canViewScrollVertically = " + canScrollVertically);
            return canScrollVertically;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || mNestedScrollInProgress) {
            if (DEBUG_INTERCEPT)
                L.e("not enabled!");
            return false;
        }

        if (mAnimating) {
            if (DEBUG_INTERCEPT)
                L.e("Animating!");
            return mIsBeingDragged = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mInterceptDirection = 0;
                mLastDownY = mInitialDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //TODO 优化
//                if (mHeaderOffsetY == 0 && mFooterOffsetY == 0) {
                mDistanceY = event.getRawY() - mLastDownY;
                if (Math.abs(mDistanceY) < mTouchSlop) {
                    if (DEBUG_INTERCEPT)
                        L.e("Distance so small!");
                    return mIsBeingDragged = false;
                }

                if (DEBUG_INTERCEPT)
                    L.e("mDistanceY = " + mDistanceY);

                //only mIsIntercept=true this direction can use
                mInterceptDirection = mDistanceY > 0 ? DIRECTION_DOWN : DIRECTION_UP;
                switch (mInterceptDirection) {
                    case DIRECTION_DOWN:
                        mIsBeingDragged = !canContentScrollVerticallyDown();
                        break;
                    case DIRECTION_UP:
                        mIsBeingDragged = !canContentScrollVerticallyUp();
                        break;
                }
                mLastDownY = event.getRawY();
//                } else {
//                    mIsBeingDragged = true;
//                    mInterceptDirection = mHeaderOffsetY > 0 ? DIRECTION_DOWN : DIRECTION_UP;
//                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        if (DEBUG_INTERCEPT)
            L.e("IsBeingDragged = " + mIsBeingDragged);
        return mIsBeingDragged;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
//        super.setOnTouchListener(l);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || mNestedScrollInProgress) {
            if (DEBUG_TOUCH)
                L.e("is not enable!");
            return false;
        }

        if (mAnimating) {
            if (DEBUG_TOUCH)
                L.e("Animating");
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownY = mInitialDownY = event.getRawY();
            case MotionEvent.ACTION_MOVE:
                mDistanceY = event.getRawY() - mLastDownY;
                mDistanceY *= DRAG_RATE;
                switch (mInterceptDirection) {
                    case DIRECTION_DOWN:
                        performDown((int) mDistanceY);
                        break;
                    case DIRECTION_UP:
                        performUp((int) mDistanceY);
                        break;
                    default:
                        performMove((int) mDistanceY);
                        break;
                }
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                int scrollY;
                if (DEBUG_TOUCH)
                    L.e("mInterceptDirection = " + mInterceptDirection);
                switch (mInterceptDirection) {
                    case DIRECTION_DOWN:
                        if (!mOpenDownRefresh) {
                            smoothScroll(mContentOffsetY);
                            if (DEBUG_TOUCH)
                                L.e("Down Refresh not open");
                            return true;
                        }
                        scrollY = mHeaderOffsetY;
                        break;
                    case DIRECTION_UP:
                        if (!mOpenUpRefresh) {
                            smoothScroll(mContentOffsetY);
                            if (DEBUG_TOUCH)
                                L.e("Up Refresh not open");
                            return true;
                        }
                        scrollY = mFooterOffsetY;
                        break;
                    default:
                        scrollY = mContentOffsetY;
                        smoothScroll(scrollY);
                        return true;
                }

                if (!mRefreshing) {
                    if (Math.abs(scrollY) < mTotalDragDistance) {
                        smoothScroll(scrollY);
                    } else {
                        performRefresh(scrollY);
                    }
                } else {
                    if (Math.abs(scrollY) > mTotalDragDistance) {
                        smoothScroll(scrollY > 0 ? scrollY - mTotalDragDistance
                                : scrollY + mTotalDragDistance);
                    } else {
                        smoothScroll(scrollY);
                    }
                }
                break;
        }
        return true;
    }

    private void performMove(int distanceY) {
        offsetContent(distanceY);

        invalidate();
    }

    private void performDown(int distanceY) {
        if (mHeaderView != null && mHeaderOffsetY + distanceY < 0) {
            return;
        }

        offsetContent(distanceY);
        offsetHeader(distanceY);

        invalidate();

        if (!mRefreshing && mHeaderPtrHandler != null) {
            float percent = mHeaderOffsetY * 1f / mTotalDragDistance;
            mHeaderPtrHandler.onPercent(percent);
        }
    }

    private void performUp(int distanceY) {
        if (mFooterView != null && mFooterOffsetY + distanceY > 0) {
            return;
        }

        offsetContent(distanceY);
        offsetFooter(distanceY);

        invalidate();

        if (!mRefreshing && mFooterPtrHandler != null) {
            float percent = Math.abs(mFooterOffsetY * 1f) / mTotalDragDistance;
            mFooterPtrHandler.onPercent(percent);
        }
    }

    private void offsetHeader(int distanceY) {
        if (mHeaderView != null) {
            mHeaderView.offsetTopAndBottom(distanceY);
            mHeaderOffsetY += distanceY;
        }
    }

    private void offsetContent(int distanceY) {
        if (mContentView != null) {
            mContentView.offsetTopAndBottom(distanceY);
            mContentOffsetY += distanceY;
        }
    }

    private void offsetFooter(int distanceY) {
        if (mFooterView != null) {
            mFooterView.offsetTopAndBottom(distanceY);
            mFooterOffsetY += distanceY;
        }
    }

    private void performRefresh(int startY) {
        mRefreshing = true;
        int endY = startY > 0 ? startY - mTotalDragDistance : mTotalDragDistance - Math.abs(startY);
        smoothScroll(endY, DEFAULT_RESET_TIME);

        switch (mInterceptDirection) {
            case DIRECTION_DOWN:
                if (mHeaderPtrHandler != null) {
                    mHeaderPtrHandler.onRefreshBegin();
                }
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onPullDownRefresh();
                }
                break;
            case DIRECTION_UP:
                if (mFooterPtrHandler != null) {
                    mFooterPtrHandler.onRefreshBegin();
                }
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onPullUpRefresh();
                }
                break;
        }
    }


    public void onDownRefreshComplete() {
        performRefreshComplete(mHeaderOffsetY);
    }

    public void onUpRefreshComplete() {
        performRefreshComplete(mFooterOffsetY);
    }

    private void performRefreshComplete(int startY) {
        mRefreshing = false;
        mAnimating = true;
        smoothScroll(startY, DEFAULT_RESET_TIME);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mAnimating = false;
            }
        }, DEFAULT_RESET_TIME);

        switch (mInterceptDirection) {
            case DIRECTION_DOWN:
                if (mHeaderPtrHandler != null) {
                    mHeaderPtrHandler.onRefreshEnd();
                }
                break;
            case DIRECTION_UP:
                if (mFooterPtrHandler != null) {
                    mFooterPtrHandler.onRefreshEnd();
                }
                break;
        }
    }

    private void smoothScroll(int startY, int duration) {
        mLastScrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, duration);
        invalidate();
    }

    private void smoothScroll(int startY) {
        mLastScrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, DEFAULT_RESET_TIME);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            switch (mInterceptDirection) {
                case DIRECTION_DOWN:
                    performDown(mLastScrollY - currY);
                    break;
                case DIRECTION_UP:
                    performUp(mLastScrollY - currY);
                    break;
                default:
                    performMove(mLastScrollY - currY);
                    break;
            }
            mLastScrollY = currY;
        }
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
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
