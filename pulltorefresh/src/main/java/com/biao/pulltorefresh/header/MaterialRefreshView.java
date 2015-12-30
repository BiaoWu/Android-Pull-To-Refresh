package com.biao.pulltorefresh.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.biao.pulltorefresh.PtrHandler;
import com.biao.pulltorefresh.utils.L;

public class MaterialRefreshView extends View implements PtrHandler {
    private static final String TAG = MaterialRefreshView.class.getSimpleName();
    private static final boolean DEBUG = false;
    private MaterialProgressDrawable mDrawable;
    private float mScale;

    public MaterialRefreshView(Context context) {
        super(context);
        initView();
    }

    public MaterialRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MaterialRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mDrawable = new MaterialProgressDrawable(getContext(), this);
        mDrawable.setBackgroundColor(Color.WHITE);
        mDrawable.setCallback(this);
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    public void setColorSchemeColors(int[] colors) {
        mDrawable.setColorSchemeColors(colors);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int saveCount = canvas.save();
        Rect rect = mDrawable.getBounds();
        int l = getPaddingLeft() + (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
        canvas.translate(l, getPaddingTop());
        canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void onRefreshBegin() {
        mScale = 1f;
        mDrawable.setAlpha(255);
        mDrawable.start();
    }

    @Override
    public void onRefreshEnd() {
        mDrawable.stop();
    }

    @Override
    public void onPercent(float percent) {
        if (DEBUG)
            L.e(TAG, "percent=%s", percent);
        mScale = percent;

        mDrawable.setAlpha((int) (255 * percent));
        mDrawable.showArrow(true);

        float strokeStart = ((percent) * .8f);
        mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
        mDrawable.setArrowScale(Math.min(1f, percent));

        float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
        mDrawable.setProgressRotation(rotation);
        invalidate();
    }
}
