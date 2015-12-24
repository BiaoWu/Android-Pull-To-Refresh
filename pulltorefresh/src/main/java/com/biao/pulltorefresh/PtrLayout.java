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

    //    private int mContentScrollY;
    private int mHeaderOffsetTop;
    private int mFooterOffsetTop;


    private Scroller mScroller;

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
                    getPaddingTop() + getPaddingBottom() + lp.topMargin, heightSize);

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
            final int top = -headerHeight + mHeaderOffsetTop;
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
            final int top = cotentViewHeight + mFooterOffsetTop;
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + headerHeight;
            mFooterView.layout(left, top, right, bottom);
        }
    }

    public void setHeaderView(View view) {
        if (view != null && mHeaderView != view) {
            mHeaderView = view;
            addView(view);
            if (view instanceof PtrHandler) {
                mHeaderPtrHandler = (PtrHandler) view;
            }
            reset();
        }
    }

    private void reset() {
//        mContentScrollY = 0;
        mHeaderOffsetTop = 0;
        mFooterOffsetTop = 0;
    }

    public void setFooterView(View view) {
        if (view != null && mFooterView != view) {
            mFooterView = view;
            addView(view);
            if (view instanceof PtrHandler) {
                mFooterPtrHandler = (PtrHandler) view;
            }
            reset();
        }
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    private boolean canScrollVerticallyDown() {
        return canScrollVertically(DIRECTION_DOWN);
    }

    private boolean canScrollVerticallyUp() {
        return canScrollVertically(DIRECTION_UP);
    }

    public boolean canScrollVertically(int direction) {
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
            return mContentView.canScrollVertically(direction);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        if (mAnimating) {
            return mIsBeingDragged = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mLastDownY = mInitialDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mHeaderOffsetTop == 0 && mFooterOffsetTop == 0) {
                    mDistanceY = event.getRawY() - mLastDownY;
                    if (Math.abs(mDistanceY) < mTouchSlop) {
                        return mIsBeingDragged = false;
                    }

                    //only mIsIntercept=true this direction can use
                    mInterceptDirection = mDistanceY > 0 ? DIRECTION_DOWN : DIRECTION_UP;
                    switch (mInterceptDirection) {
                        case DIRECTION_DOWN:
                            mIsBeingDragged = !canScrollVerticallyDown();
                            break;
                        case DIRECTION_UP:
                            mIsBeingDragged = !canScrollVerticallyUp();
                            break;
                    }

                    mLastDownY = event.getRawY();
                } else {
                    mIsBeingDragged = true;
                    mInterceptDirection = mHeaderOffsetTop > 0 ? DIRECTION_DOWN : DIRECTION_UP;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mIsBeingDragged || mNestedScrollInProgress) {
            return false;
        }

        if (mAnimating) {
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
                        moveDown((int) mDistanceY);
                        break;
                    case DIRECTION_UP:
                        moveUp((int) mDistanceY);
                        break;
                }
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                int scrollY = 0;
                switch (mInterceptDirection) {
                    case DIRECTION_DOWN:
                        scrollY = mHeaderOffsetTop;
                        break;
                    case DIRECTION_UP:
                        scrollY = mFooterOffsetTop;
                        break;
                }
                if (!mRefreshing) {
                    if (Math.abs(scrollY) < mTotalDragDistance) {
                        smootchScroll(scrollY);
                    } else {
                        startRefresh(scrollY);
                    }
                } else {
                    if (Math.abs(scrollY) > mTotalDragDistance) {
                        smootchScroll(scrollY > 0 ? scrollY - mTotalDragDistance
                                : scrollY + mTotalDragDistance);
                    } else {
                        smootchScroll(scrollY);
                    }
                }
                break;
        }
        return true;
    }

    private void moveDown(int y) {
        if (mHeaderOffsetTop + y < 0) {
            return;
        }

        mHeaderOffsetTop += y;

//        dispatch2Content(y);
        dispatch2Header(y);

        invalidate();

        if (!mRefreshing && mHeaderPtrHandler != null) {
            float percent = mHeaderOffsetTop * 1f / mTotalDragDistance;
            mHeaderPtrHandler.onPercent(percent);
        }
    }

    private void moveUp(int y) {
        if (mFooterOffsetTop + y > 0) {
            return;
        }

        mFooterOffsetTop += y;

        dispatch2Content(y);
        dispatch2Footer(y);

        invalidate();

        if (!mRefreshing && mFooterPtrHandler != null) {
            float percent = Math.abs(mFooterOffsetTop * 1f) / mTotalDragDistance;
            mFooterPtrHandler.onPercent(percent);
        }
    }

    private void dispatch2Header(int y) {
        if (mHeaderView != null) {
            mHeaderView.offsetTopAndBottom(y);
        }
    }

    private void dispatch2Content(int y) {
        if (mContentView != null) {
            mContentView.offsetTopAndBottom(y);
        }
    }

    private void dispatch2Footer(int y) {
        if (mFooterView != null) {
            mFooterView.offsetTopAndBottom(y);
        }
    }

    private void startRefresh(int startY) {
        mRefreshing = true;
        int endY = startY > 0 ? startY - mTotalDragDistance : mTotalDragDistance - Math.abs(startY);
        smootchScroll(endY, DEFAULT_RESET_TIME);

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
        performRefreshComplete(mHeaderOffsetTop);
    }

    public void onUpRefreshComplete() {
        performRefreshComplete(mFooterOffsetTop);
    }

    private void performRefreshComplete(int startY) {
        mRefreshing = false;
        mAnimating = true;
        smootchScroll(startY, DEFAULT_RESET_TIME);
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


    private int scrollY;

    private void smootchScroll(int startY, int duration) {
        scrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, duration);
        invalidate();
    }

    private void smootchScroll(int startY) {
        scrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, DEFAULT_RESET_TIME);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            switch (mInterceptDirection) {
                case DIRECTION_DOWN:
                    moveDown(scrollY - currY);
                    break;
                case DIRECTION_UP:
                    moveUp(scrollY - currY);
                    break;
            }
            scrollY = currY;
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
