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
    private static final boolean DEBUG_TOUCH = true;

    private static final int DEFAULT_RELEASE_DISTANCE = 70;//dp
    private static final float DRAG_RATE = .5f;

    private static final int HEADER_MOVE = 0x1 << 2;
    private static final int CONTENT_MOVE = 0x1 << 1;
    private static final int FOOTER_MOVE = 0x1;

    public static final int MODE_ALL_MOVE = HEADER_MOVE | CONTENT_MOVE | FOOTER_MOVE;
    public static final int MODE_ONLY_CONTENT_NOT_MOVE = HEADER_MOVE | FOOTER_MOVE;
    public static final int MODE_ONLY_FOOTER_NOT_MOVE = HEADER_MOVE | CONTENT_MOVE;
    public static final int MODE_ONLY_HEADER_NOT_MOVE = CONTENT_MOVE | FOOTER_MOVE;
    public static final int MODE_ONLY_CONTENT_MOVE = CONTENT_MOVE;

    //config
    private int mTouchSlop;
    private int mDefaultReleaseDist;
    private int mMode;

    //touch event
    private float mInitialDownY;
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

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mDefaultReleaseDist = Math.round(
                DEFAULT_RELEASE_DISTANCE * context.getResources().getDisplayMetrics().density);

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

        if (mHeaderView.isNotEmpty()) {
            measureChildWithMargins(mHeaderView.view, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContentView.isNotEmpty()) {
            measureChildWithMargins(mContentView.view, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mFooterView.isNotEmpty()) {
            measureChildWithMargins(mFooterView.view, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mHeaderView.isNotEmpty()) {
            layoutChild(mHeaderView.view,
                    0,
                    mHeaderView.canScroll()
                            ? (-mHeaderView.view.getMeasuredHeight() + mHeaderView.offsetY)
                            : 0,
                    0, 0);
            if (!mContentView.canScroll()) {
                bringChildToFront(mHeaderView.view);
            }
        }

        if (mContentView.isNotEmpty()) {
            layoutChild(mContentView.view, 0, 0, 0, 0);
        }

        if (mFooterView.isNotEmpty()) {
            int footerTop = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            layoutChild(mFooterView.view,
                    0,
                    mFooterView.canScroll()
                            ? footerTop + mFooterView.offsetY
                            : footerTop + mFooterView.offsetY - mFooterView.view.getMeasuredHeight(),
                    0, 0);
            if (!mContentView.canScroll()) {
                bringChildToFront(mFooterView.view);
            }
        }
    }

    private void layoutChild(View child, int leftUsed, int topUsed, int rightUsed, int bottomUsed) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int left = paddingLeft + lp.leftMargin + leftUsed;
        final int top = paddingTop + lp.topMargin + topUsed;
        final int right = left + child.getMeasuredWidth() + rightUsed;
        final int bottom = top + child.getMeasuredHeight() + bottomUsed;
        child.layout(left, top, right, bottom);
    }

    /** set mode ,  PtrLayout.MODE_ALL_MOVE */
    public void setMode(int mode) {
        switch (mode) {
            default:
                L.e(TAG, "mode value is Bad!");
            case MODE_ALL_MOVE:
                mMode = MODE_ALL_MOVE;
                break;
            case MODE_ONLY_CONTENT_MOVE:
            case MODE_ONLY_CONTENT_NOT_MOVE:
            case MODE_ONLY_FOOTER_NOT_MOVE:
            case MODE_ONLY_HEADER_NOT_MOVE:
                mMode = mode;
                break;
        }
        mHeaderView.setFlag((mMode & HEADER_MOVE) >> 2);
        mContentView.setFlag((mMode & CONTENT_MOVE) >> 1);
        mFooterView.setFlag(mMode & FOOTER_MOVE);
    }

    /** set the header animation view */
    public void setHeaderView(View view) {
        setView(mHeaderView, view);
    }

    /** set the footer animation view */
    public void setFooterView(View view) {
        setView(mFooterView, view);
    }

    private void setView(PtrViewHolder ptrViewHolder, View view) {
        if (ptrViewHolder == null) {
            L.e("ptrViewHolder can not be null!");
            return;
        }
        if (view == null) {
            L.e("view is null!!!");
            return;
        }
        View originalView = ptrViewHolder.getView();
        if (originalView != null) {
            removeView(originalView);
            if (originalView instanceof PtrHandler) {
                ((PtrHandler) originalView).onRefreshEnd();
            }
        }
        if (view instanceof PtrHandler) {
            ptrViewHolder.setPtrHandler((PtrHandler) view);
        }
        ptrViewHolder.setView(view);
        if (mMode == MODE_ONLY_CONTENT_NOT_MOVE) {
            addView(view);
        } else {
            addView(view, 0);
        }
    }

    private int getReleaseDist(int dist) {
        return Math.max(mDefaultReleaseDist, dist);
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
        if (!isEnabled() || (!mContentView.canScroll() && mIsRefreshing)) {
            if (DEBUG_INTERCEPT)
                L.e(TAG, "fast return !!");
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mInitialDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mContentView.canScrollAndIsNotOriginalLocation()) {
                    mIsBeingDragged = true;
                } else {
                    mDistanceY = event.getRawY() - mInitialDownY;
                    if (Math.abs(mDistanceY) > mTouchSlop) {
                        mLastDownY = mInitialDownY + mTouchSlop;
                        mInterceptDirection = mDistanceY > 0
                                ? ViewScrollChecker.DIRECTION_DOWN
                                : ViewScrollChecker.DIRECTION_UP;
                        mIsBeingDragged = !ViewScrollChecker.canViewScrollVertically(
                                mContentView.view, mInterceptDirection);
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
        PtrViewHolder ptrViewHolder = null;

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

                ptrViewHolder = getInterceptPtrView();
                //skip the out of boundary
                if ((ptrViewHolder.canScroll()
                        && ptrViewHolder.isOutOriginalLocation(mInterceptDirection, mDistanceY))
                        || (ptrViewHolder != mContentView && mContentView.canScroll()
                        && mContentView.isOutOriginalLocation(mInterceptDirection, mDistanceY))) {
                    if (DEBUG_TOUCH)
                        L.e(TAG, "mDistanceY=" + mDistanceY
                                + ",mInterceptDirection=" + mInterceptDirection);
                } else {
                    performMove(ptrViewHolder, (int) mDistanceY);
                }

                mLastDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                // fast return
                if (!hasRefreshView(mInterceptDirection)) {
                    if (DEBUG_TOUCH)
                        L.e(TAG, "refresh view is none!");
                    mPtrScroller.smoothScroll(mContentView.offsetY);
                    return true;
                }

                ptrViewHolder = getInterceptPtrView();
                final int releaseDist = getReleaseDist(ptrViewHolder.getHeight());
                if (!mIsRefreshing) {
                    int scrollY = getInterceptOffsetY();
                    if (DEBUG_TOUCH)
                        L.e(TAG, "mIsRefreshing=" + false + ",scrollY=" + scrollY);

                    if (Math.abs(scrollY) < releaseDist) {
                        mPtrScroller.smoothScroll(scrollY);
                    } else {
                        performRefresh();
                    }
                } else {
                    int scrollY = getRefreshOffsetY();
                    if (DEBUG_TOUCH)
                        L.e(TAG, "mIsRefreshing=" + true + ",scrollY=" + scrollY);
                    if (mRefreshDirection == mInterceptDirection
                            && Math.abs(scrollY) > releaseDist) {
                        scrollY = scrollY > 0
                                ? scrollY - releaseDist
                                : scrollY + releaseDist;
                    }
                    mPtrScroller.smoothScroll(scrollY);
                }
        }
        return true;
    }

    private boolean hasRefreshView(byte interceptDirection) {
        switch (interceptDirection) {
            case ViewScrollChecker.DIRECTION_DOWN:
                return mHeaderView.isNotEmpty();
            case ViewScrollChecker.DIRECTION_UP:
                return mFooterView.isNotEmpty();
            default:
                return false;
        }
    }

    private PtrViewHolder getInterceptPtrView() {
        return getPtrView(mInterceptDirection, true);
    }

    private PtrViewHolder getRefreshPtrView() {
        return getPtrView(mRefreshDirection, true);
    }

    private PtrViewHolder getInterceptPtrView(boolean ignoreScroll) {
        return getPtrView(mInterceptDirection, ignoreScroll);
    }

    private PtrViewHolder getRefreshPtrView(boolean ignoreScroll) {
        return getPtrView(mRefreshDirection, ignoreScroll);
    }

    private PtrViewHolder getPtrView(int direction, boolean ignoreScroll) {
        switch (direction) {
            case ViewScrollChecker.DIRECTION_DOWN:
                return ignoreScroll || mHeaderView.canScroll() ? mHeaderView : mContentView;
            case ViewScrollChecker.DIRECTION_UP:
                return ignoreScroll || mFooterView.canScroll() ? mFooterView : mContentView;
            default:
                return mContentView;
        }
    }

    private int getInterceptOffsetY() {
        return getOffsetY(mInterceptDirection);
    }

    private int getRefreshOffsetY() {
        return getOffsetY(mRefreshDirection);
    }

    private int getOffsetY(int direction) {
        return getPtrView(direction, false).offsetY;
    }

    private void performMove(PtrViewHolder ptrViewHolder, int distanceY) {
        if (ptrViewHolder == null) {
            return;
        }

        ptrViewHolder.offsetTopAndBottom(distanceY);
        if (ptrViewHolder != mContentView) {
            mContentView.offsetTopAndBottom(distanceY);
        } else {
            ptrViewHolder = getInterceptPtrView();
        }

        invalidate();

        if (!mIsRefreshing && ptrViewHolder.ptrHandler != null) {
            int releaseDist = getReleaseDist(ptrViewHolder.getHeight());
            float percent = Math.abs(getInterceptOffsetY() * 1f / releaseDist);
            ptrViewHolder.ptrHandler.onPercent(Math.min(1f, percent));
        }
    }

    private void performRefresh() {
        mRefreshDirection = mInterceptDirection;
        PtrViewHolder ptrViewHolder = getRefreshPtrView();
        if (ptrViewHolder == null) {
            L.e(TAG, "performRefresh direction = " + mRefreshDirection + " is bug!  let's fix it!");
            return;
        }

        mIsRefreshing = true;
        int offsetY = getRefreshOffsetY();

        if (ptrViewHolder.ptrHandler != null) {
            ptrViewHolder.ptrHandler.onRefreshBegin();
        }

        if (ptrViewHolder.mOnRefreshListener != null) {
            ptrViewHolder.mOnRefreshListener.onRefresh();
        }
        int releaseDist = getReleaseDist(ptrViewHolder.getHeight());
        int endY = offsetY > 0 ? offsetY - releaseDist
                : releaseDist - Math.abs(offsetY);
        mPtrScroller.smoothScroll(endY);
    }

    /** when call onRefresh() ! you will need to call this method to complete this refresh */
    public void onRefreshComplete() {
        L.e(TAG, "onRefreshComplete !!!");
        PtrViewHolder ptrViewHolder = getRefreshPtrView();
        if (ptrViewHolder == null) {
            L.e(TAG, "onRefreshComplete direction = " + mRefreshDirection + " is bug! let's fix it!");
            return;
        }

        if (ptrViewHolder.ptrHandler != null) {
            ptrViewHolder.ptrHandler.onRefreshEnd();
        }
        int endY = getRefreshOffsetY();
        if (!mIsBeingDragged) {
            mPtrScroller.smoothScroll(endY);
        }

        mIsRefreshing = false;
        mRefreshDirection = 0;
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
                performMove(getInterceptPtrView(false), mLastScrollY - currY);
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
