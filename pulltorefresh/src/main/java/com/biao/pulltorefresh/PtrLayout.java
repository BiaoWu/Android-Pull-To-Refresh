package com.biao.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;


/**
 * Created by biaowu.
 */
public class PtrLayout extends ViewGroup {
    private static final String TAG = PtrLayout.class.getSimpleName();
    private static final int DEFAULT_DRAG_MAX_DISTANCE = 100;//dp

    private View mHeaderView;
    private View mContentView;
    private View mFooterView;

    private int maxBottom;

    private PointF pre = new PointF();
    private PointF des = new PointF();

    private int mContentTop;
    private int mContentBottom;
    private int mHeaderOffsetTop;
    private int mFooterOffsetTop;


    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

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

        int childCount = getChildCount();
        if (childCount == 1) {
            mContentView = getChildAt(0);
        } else {
        }


        mScroller = new Scroller(context);
        mVelocityTracker = VelocityTracker.obtain();
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
            mHeaderView.bringToFront();
        }
    }

    public void setFooterView(View view) {
        if (view != null && mFooterView != view) {
            mFooterView = view;
            addView(view);
            mFooterView.bringToFront();
        }
    }

    @TargetApi(14)
    private boolean canScrollVerticallyDown() {
        return mContentView.canScrollVertically(-1);
    }

    @TargetApi(14)
    private boolean canScrollVerticallyUp() {
        return mContentView.canScrollVertically(1);
    }

    private boolean isMoveDown() {
        return mVelocityTracker.getYVelocity() > 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pre.x = event.getRawX();
                pre.y = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                des.x = event.getRawX() - pre.x;
                des.y = event.getRawY() - pre.y;


                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);

                if (isMoveDown()) {
                    intercept = !canScrollVerticallyDown();
                } else {
                    intercept = !canScrollVerticallyUp();
                }

                pre.x = event.getRawX();
                pre.y = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                pre.x = event.getRawX();
                pre.y = event.getRawY();
//                if (!mScroller.isFinished()) {
//                    mScroller.abortAnimation();
//                }
                break;
            case MotionEvent.ACTION_MOVE:
                des.x = event.getRawX() - pre.x;
                des.y = event.getRawY() - pre.y;

                if (isMoveDown()) {
                    moveDown((int) des.y);
                } else {
                    moveUp((int) des.y);
                }

                pre.x = event.getRawX();
                pre.y = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                int startY;
                if (isMoveDown()) {
                    startY = mContentTop;
                } else {
                    startY = mContentBottom;
                }
                goBack(startY);
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
    }

    private void goBack(int startY) {
        scrollY = 0;
        mScroller.startScroll(0, 0, 0, startY, 1000);
        invalidate();
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
    }

    private int scrollY;

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            if (isMoveDown()) {
                moveDown(scrollY - currY);
            } else {
                moveUp(scrollY - currY);
            }
            scrollY = currY;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mVelocityTracker.recycle();
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
