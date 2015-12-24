package com.biao.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
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

    private static final float DRAG_RATE = .5f;

    private float mRatio = 1.3f;

    private View mHeaderView;
    private View mContentView;
    private View mFooterView;

    private PtrHandler mHeaderPtrHandler;
    private PtrHandler mFooterPtrHandler;

    private int mTouchSlop;
    private int mPullMaxDistance;//pull 的最大距离，影响弹簧效果
    private boolean mIsIntercept;

    private float mDownY;
    private float mLastY;//最后一次移动的点Y
    private float mDistanceY;//没次移动的距离Y
    private float mPullPercent;

    private boolean mIsRefreshing;
    private boolean mInterceptDirection;

    private int maxBottom;

    private int mContentTop;
    private int mContentBottom;
    private int mHeaderOffsetTop;
    private int mFooterOffsetTop;


    private Scroller mScroller;

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
        mPullMaxDistance = Math.round(
                DEFAULT_PULL_MAX_DISTANCE * context.getResources().getDisplayMetrics().density * mRatio);

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
            maxBottom = bottom;
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
        mContentTop = 0;
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
        return mIsRefreshing;
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
                            return (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                                    .getTop() < absListView.getPaddingTop());
                        case DIRECTION_UP:
                            return (absListView.getLastVisiblePosition() < childCount || absListView.getChildAt(childCount - 1)
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
            return ViewCompat.canScrollVertically(mContentView, direction);
        }
    }

    private boolean skipIntercept() {
        return isRefreshing();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (skipIntercept()) {
            return mIsIntercept = false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsIntercept = false;
                mLastY = mDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mDistanceY = event.getRawY() - mLastY;
                if (Math.abs(mDistanceY) < mTouchSlop) {
                    return mIsIntercept = false;
                }

                //only mIsIntercept=true this direction can use
                mInterceptDirection = mDistanceY > 0;
                if (mInterceptDirection) {
                    mIsIntercept = !canScrollVerticallyDown();
                } else {
                    mIsIntercept = !canScrollVerticallyUp();
                }

                mLastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return mIsIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsIntercept) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //TODO 增加弹簧效果
                mDistanceY = event.getRawY() - mLastY;

                mDistanceY *= DRAG_RATE;

                if (mInterceptDirection) {
                    moveDown((int) mDistanceY);
                } else {
                    moveUp((int) mDistanceY);
                }
                mLastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                int startY;
                if (mInterceptDirection) {
                    startY = mContentTop;
                } else {
                    startY = mContentBottom;
                }
//                L.e("action up startY=" + startY);
                scroll2RefreshPosition(startY);
                break;
        }
        return true;
    }

    private void moveUp(int y) {
        int contentBottom = mContentBottom;
        if (contentBottom + y > maxBottom) {
            return;
        }

        if (mFooterView != null) {
            mFooterView.offsetTopAndBottom(y);
            mFooterOffsetTop += y;
        }

        if (mContentView != null) {
            mContentView.offsetTopAndBottom(y);
            mContentBottom += y;
        }

        invalidate();

        if (mFooterPtrHandler != null) {
            float percent = Math.abs(mFooterOffsetTop * 1f) / mPullMaxDistance;
//                L.e("percent = " + percent);
            mFooterPtrHandler.onPercent(percent);
        }
    }

    private void moveDown(int y) {
        int contentTop = mContentTop;
        if (contentTop + y < 0) {
            return;
        }

        if (mHeaderView != null) {
            mHeaderView.offsetTopAndBottom(y);
            mHeaderOffsetTop += y;
        }

        if (mContentView != null) {
            mContentView.offsetTopAndBottom(y);
            mContentTop += y;
        }

        invalidate();

        if (mHeaderPtrHandler != null) {
            float percent = mHeaderOffsetTop * 1f / mPullMaxDistance;
//                L.e("percent = " + percent);
            mHeaderPtrHandler.onPercent(percent);
        }
    }


    private void scroll2RefreshPosition(int startY) {
        if (Math.abs(startY) < mPullMaxDistance) {
            goBack(startY);
        } else {
            final int timeForRelease = 250;
            int endY = startY > 0 ? startY - mPullMaxDistance : mPullMaxDistance - Math.abs(startY);
            smootchScroll(endY, timeForRelease);

            final int release = startY > 0 ? mPullMaxDistance : -mPullMaxDistance;

            if (mInterceptDirection) {
                if (mHeaderPtrHandler != null) {
                    mHeaderPtrHandler.onRefreshBegin();
                }
            } else {
                if (mFooterPtrHandler != null) {
                    mFooterPtrHandler.onRefreshBegin();
                }
            }

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    goBack(release);
                    if (mInterceptDirection) {
                        if (mHeaderPtrHandler != null) {
                            mHeaderPtrHandler.onRefreshEnd();
                        }
                    } else {
                        if (mFooterPtrHandler != null) {
                            mFooterPtrHandler.onRefreshEnd();
                        }
                    }
                }
            }, timeForRelease + 1000);
        }
    }

    private void smootchScroll(int startY, int duration) {
        scrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, duration);
        invalidate();
    }


    private int scrollY;

    private void goBack(int startY) {
        L.e("goBack from" + startY);
        scrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            if (mInterceptDirection) {
                moveDown(scrollY - currY);
            } else {
                moveUp(scrollY - currY);
            }
            scrollY = currY;
        }
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
//        mCircleView.bringToFront();
//        mCircleView.offsetTopAndBottom(offset);
//        mCurrentTargetOffsetTop = mCircleView.getTop();
//        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
//            invalidate();
//        }
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
