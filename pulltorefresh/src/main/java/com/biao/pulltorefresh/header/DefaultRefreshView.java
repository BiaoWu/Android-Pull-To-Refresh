package com.biao.pulltorefresh.header;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.biao.pulltorefresh.PtrHandler;
import com.biao.pulltorefresh.R;
import com.biao.pulltorefresh.utils.L;

public class DefaultRefreshView extends FrameLayout implements PtrHandler {
    private static final String TAG = DefaultRefreshView.class.getSimpleName();
    private static final boolean DEBUG = false;

    private TextView mTextView;
    private MaterialProgressDrawable mDrawable;

    private boolean isPullDown = true;

    public DefaultRefreshView(Context context) {
        super(context);
        setUpView(context);
    }

    public DefaultRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpView(context);
    }

    public DefaultRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DefaultRefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUpView(context);
    }

    private void setUpView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.header_default, this);

        mTextView = (TextView) view.findViewById(R.id.text);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);

        mDrawable = new MaterialProgressDrawable(context, imageView);
        mDrawable.setBackgroundColor(Color.WHITE);
        imageView.setImageDrawable(mDrawable);
    }

    public void setIsPullDown(boolean isPullDown) {
        this.isPullDown = isPullDown;
    }

    public void setColorSchemeColors(int[] colors) {
        mDrawable.setColorSchemeColors(colors);
    }

    @Override
    public void onRefreshBegin() {
        mTextView.setText(R.string.refresh_start);
        mDrawable.setAlpha(255);
        mDrawable.start();
    }

    @Override
    public void onRefreshEnd() {
        mTextView.setText(R.string.refresh_end);
        mDrawable.stop();
    }

    @Override
    public void onPercent(float percent) {
        if (DEBUG)
            L.e(TAG, "percent=%s", percent);

        mDrawable.setAlpha((int) (255 * percent));
        mDrawable.showArrow(true);

        float strokeStart = ((percent) * .8f);
        mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
        mDrawable.setArrowScale(Math.min(1f, percent));

        float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
        mDrawable.setProgressRotation(rotation);

        changeText(percent);
    }

    private void changeText(float percent) {
        if (percent == 1) {
            mTextView.setText(R.string.release_to_refresh);
        } else {

            mTextView.setText(isPullDown ? R.string.pull_down_to_refresh
                    : R.string.pull_up_to_refresh);
        }
    }
}
