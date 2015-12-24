package com.biao.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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

    //config
    private int mTouchSlop;
    private int mTotalDragDistance;

    //touch event
    private float mLastDownY;
    private float mDistanceY;
    private byte mInterceptDirection;
    private boolean mIsBeingDragged;

    //scroller
    private PtrScroller mPtrScroller;

    private PtrViewHolder mHeaderView;
    private PtrViewHolder mFooterView;
    private PtrViewHolder mContentView;

    //state
    private boolean mIsRefreshing;
    private byte mRefreshDirection;

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
        mContentView = new PtrViewHolder();
        mFooterView = new PtrViewHolder();
//        mHeaderView = new PtrViewHolder(PtrViewHolder.FLAG_MOVE);
//        mContentView = new PtrViewHolder(PtrViewHolder.FLAG_MOVE);
//        mFooterView = new PtrViewHolder(PtrViewHolder.FLAG_MOVE);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = Math.round(
                DEFAULT_PULL_MAX_DISTANCE * context.getResources().getDisplayMetrics().density);

        int childCount = getChildCount();
        if (childCount == 1) {
            mContentView.view = getChildAt(0);
        } else {//TODO think about any other ???
            throw new IllegalStateException("PtrLayout can only have one child now !!");
        }
        mPtrScroller = new PtrScroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mHeaderView.isNotEmpty()) {
            measureChildWithMargins(mHeaderView.view, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContentView.isNotEmpty()) {
            final MarginLayoutParams lp = (MarginLayoutParams) mContentView.view.getLayoutParams();
            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                    widthSize);
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin, heightSize);
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

    /** set the header animation view */
    public void setHeaderView(View view) {
        setHeaderView(view, PtrViewHolder.FLAG_MOVE);
    }

    public void setHeaderView(int flag) {
        setHeaderView(null, flag);
    }

    /** if the header view is null this method will not effect */
    public void setHeaderView(View view, int flag) {
        setView(mHeaderView, view, flag);
    }

    /** set the footer animation view */
    public void setFooterView(View view) {
        setFooterView(view, PtrViewHolder.FLAG_MOVE);
    }

    public void setFooterView(int flag) {
        setFooterView(null, flag);
    }

    /** if the footer view is null this method will not effect */
    public void setFooterView(View view, int flag) {
        setView(mFooterView, view, flag);
    }

    private void setView(PtrViewHolder ptrViewHolder, View view, int flag) {
        if (ptrViewHolder == null) {
            L.e("ptrViewHolder can not be null!");
            return;
        }
        View originalView = ptrViewHolder.getView();
        if (view == null) {
            if (originalView != null) {
                ptrViewHolder.setFlag(flag);
            } else {
                L.e("view is null and original view is null!!!");
            }
        } else {
            if (originalView != null) {
                removeView(originalView);
                if (originalView instanceof PtrHandler) {
                    ((PtrHandler) originalView).onRefreshEnd();
                }
            }
            addView(view);
            if (view instanceof PtrHandler) {
                ptrViewHolder.setPtrHandler((PtrHandler) view);
            }
            ptrViewHolder.setView(view);
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

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mHeaderView.canScrollAndIsNotOriginalLocation()
                        || mFooterView.canScrollAndIsNotOriginalLocation()
                        || mContentView.canScrollAndIsNotOriginalLocation()) {
                    if (DEBUG_INTERCEPT)
                        L.e(TAG, "ex onInterceptTouchEvent!");
                    mIsBeingDragged = true;
                }

                if (!mIsBeingDragged) {
                    mDistanceY = event.getRawY() - mLastDownY;
                    if (Math.abs(mDistanceY) > mTouchSlop) {
                        mInterceptDirection = mDistanceY > 0
                                ? ViewScrollChecker.DIRECTION_DOWN
                                : ViewScrollChecker.DIRECTION_UP;
                        mIsBeingDragged = !ViewScrollChecker.canViewScrollVertically(
                                mContentView.view, mInterceptDirection);
                        mLastDownY = event.getRawY();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                break;
        }
        if (DEBUG_INTERCEPT)
            L.e(TAG, "IsBeingDragged = " + mIsBeingDragged);
        return mIsBeingDragged;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        //do nothing ! only onTouchEvent!
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            if (DEBUG_TOUCH)
                L.e(TAG, "is not enable!");
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownY = event.getRawY();
                mPtrScroller.abortScroll();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                mDistanceY = event.getRawY() - mLastDownY;
                mDistanceY *= DRAG_RATE;

                switch (mInterceptDirection) {
                    case ViewScrollChecker.DIRECTION_DOWN:
                        if ((mHeaderView.canScroll()
                                && mHeaderView.offsetY + mDistanceY < 0)
                                || (mContentView.canScroll()
                                && mContentView.offsetY + mDistanceY < 0)) {
                            if (DEBUG_TOUCH)
                                L.e(TAG, "mDistanceY=" + mDistanceY
                                        + ",mInterceptDirection=" + mInterceptDirection);
                        } else {
                            performDown((int) mDistanceY);
                        }
                        break;
                    case ViewScrollChecker.DIRECTION_UP:
                        if ((mFooterView.isNotEmpty()
                                && mFooterView.offsetY + mDistanceY > 0)
                                || (mContentView.canScroll()
                                && mContentView.offsetY + mDistanceY > 0)) {
                            if (DEBUG_TOUCH)
                                L.e(TAG, "mDistanceY=" + mDistanceY
                                        + ",mInterceptDirection=" + mInterceptDirection);
                        } else {
                            performUp((int) mDistanceY);
                        }

                        break;
                }
                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                int scrollY = 0;
                if (DEBUG_TOUCH)
                    L.e(TAG, "mInterceptDirection = " + mInterceptDirection);
                switch (mInterceptDirection) {
                    case ViewScrollChecker.DIRECTION_DOWN:
                        if (!mHeaderView.isNotEmpty()) {
                            mPtrScroller.smoothScroll(mContentView.offsetY);
                            if (DEBUG_TOUCH)
                                L.e(TAG, "Down Refresh not open");
                            return true;
                        }
                        scrollY = mHeaderView.offsetY;
                        break;
                    case ViewScrollChecker.DIRECTION_UP:
                        if (!mFooterView.isNotEmpty()) {
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
                        performRefresh();
                    }
                } else {
                    if (mRefreshDirection == mInterceptDirection
                            && Math.abs(scrollY) > mTotalDragDistance) {
                        scrollY = scrollY > 0
                                ? scrollY - mTotalDragDistance
                                : scrollY + mTotalDragDistance;
                    }
                    mPtrScroller.smoothScroll(scrollY);
                }
        }
        return true;
    }


    private void performMove(PtrViewHolder ptrViewHolder, int distanceY) {
        if (ptrViewHolder == null) {
            return;
        }

        mContentView.offsetTopAndBottom(distanceY);
        ptrViewHolder.view.offsetTopAndBottom(distanceY);

        invalidate();

        if (!mIsRefreshing && ptrViewHolder.ptrHandler != null) {
            float percent = mHeaderView.offsetY * 1f / mTotalDragDistance;
            ptrViewHolder.ptrHandler.onPercent(Math.min(1f, percent));
        }
    }

    private void performDown(int distanceY) {
        mContentView.offsetTopAndBottom(distanceY);
        mHeaderView.offsetTopAndBottom(distanceY);

        invalidate();

        if (!mIsRefreshing && mHeaderView.ptrHandler != null) {
            float percent = mHeaderView.offsetY * 1f / mTotalDragDistance;
            mHeaderView.ptrHandler.onPercent(Math.min(1f, percent));
        }
    }

    private void performUp(int distanceY) {
        mContentView.offsetTopAndBottom(distanceY);
        mFooterView.offsetTopAndBottom(distanceY);

        invalidate();

        if (!mIsRefreshing && mFooterView.ptrHandler != null) {
            float percent = Math.abs(mFooterView.offsetY * 1f) / mTotalDragDistance;
            mFooterView.ptrHandler.onPercent(percent);
        }
    }

    private void performRefresh() {
        mRefreshDirection = mInterceptDirection;
        PtrViewHolder ptrViewHolder = null;
        switch (mRefreshDirection) {
            case ViewScrollChecker.DIRECTION_DOWN:
                ptrViewHolder = mHeaderView;
                break;
            case ViewScrollChecker.DIRECTION_UP:
                ptrViewHolder = mFooterView;
                break;
            default:
                L.e(TAG, "performRefresh direction = " + mRefreshDirection + " is bug!  let's fix it!");
                break;
        }
        performRefresh(ptrViewHolder);
    }

    private void performRefresh(PtrViewHolder ptrViewHolder) {
        if (ptrViewHolder == null) {
            return;
        }

        mIsRefreshing = true;
        int offsetY = ptrViewHolder.offsetY;

        if (ptrViewHolder.ptrHandler != null) {
            ptrViewHolder.ptrHandler.onRefreshBegin();
        }

        if (ptrViewHolder.mOnRefreshListener != null) {
            ptrViewHolder.mOnRefreshListener.onRefresh();
        }

        int endY = offsetY > 0 ? offsetY - mTotalDragDistance
                : mTotalDragDistance - Math.abs(offsetY);
        mPtrScroller.smoothScroll(endY);
    }

    /** when call onRefresh() ! you will need to call this method to complete this refresh */
    public void onRefreshComplete() {
        PtrViewHolder ptrViewHolder = null;
        switch (mRefreshDirection) {
            case ViewScrollChecker.DIRECTION_DOWN:
                ptrViewHolder = mHeaderView;
                break;
            case ViewScrollChecker.DIRECTION_UP:
                ptrViewHolder = mFooterView;
                break;
            default:
                L.e(TAG, "onRefreshComplete direction = " + mRefreshDirection + " is bug! let's fix it!");
                break;
        }
        performRefreshComplete(ptrViewHolder);
    }

    private void performRefreshComplete(PtrViewHolder ptrViewHolder) {
        if (ptrViewHolder == null) {
            return;
        }

        mIsRefreshing = false;
        mRefreshDirection = 0;

        int endY = ptrViewHolder.offsetY;
        if (ptrViewHolder.ptrHandler != null) {
            ptrViewHolder.ptrHandler.onRefreshEnd();
        }

        if (!mIsBeingDragged) {
            mPtrScroller.smoothScroll(endY);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPtrScroller.abortScroll();
    }

    /** set pull down refresh listener */
    public void setOnPullDownRefreshListener(OnRefreshListener onRefreshListener) {
        mHeaderView.setOnRefreshListener(onRefreshListener);
    }

    /** set pull up refresh listener */
    public void setOnPullUpRefreshListener(OnRefreshListener onRefreshListener) {
        mFooterView.setOnRefreshListener(onRefreshListener);
    }

    private class PtrScroller implements Runnable {
        private static final int DEFAULT_RESET_TIME = 300;
        //scroller
        private int mLastScrollY;
        private Scroller mScroller;
        private boolean mIsAnimating;

        PtrScroller(Context context) {
            mScroller = new Scroller(context, new DecelerateInterpolator());
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
            mIsAnimating = false;
        }

        void abortScroll() {
            mScroller.abortAnimation();
            removeCallbacks(this);
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
