package com.biao.pulltorefresh.sample.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.biao.pulltorefresh.OnRefreshListener;
import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.header.DefaultRefreshView;
import com.biao.pulltorefresh.sample.common.BaseRecyclerFragment;
import com.biao.pulltorefresh.sample.common.CommonLog;

/**
 * Created by biaowu.
 */
public class DefaultRefreshViewFragment extends BaseRecyclerFragment {
    private static final String MODE = "mode";
    private int mMode;

    public static DefaultRefreshViewFragment newInstance(int mode) {
        DefaultRefreshViewFragment fragment = new DefaultRefreshViewFragment();
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    public static DefaultRefreshViewFragment newInstance(int mode, String title) {
        DefaultRefreshViewFragment fragment = new DefaultRefreshViewFragment();
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMode = arguments.getInt(MODE, PtrLayout.MODE_ALL_MOVE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};

        final PtrLayout ptrLayout = getPtrLayout();
        Context context = ptrLayout.getContext();
        ptrLayout.setMode(mMode);

        final DefaultRefreshView headerView = new DefaultRefreshView(context);
        headerView.setColorSchemeColors(colors);
        headerView.setIsPullDown(true);
        setHeaderView(headerView);

        final DefaultRefreshView footerView = new DefaultRefreshView(context);
        footerView.setColorSchemeColors(colors);
        footerView.setIsPullDown(false);
        setFooterView(footerView);

        ptrLayout.setOnPullDownRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                headerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommonLog.e("OnPullDownRefresh");
                        ptrLayout.onRefreshComplete();
                    }
                }, 3000);
            }
        });


        ptrLayout.setOnPullUpRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                headerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommonLog.e("OnPullUpRefresh");
                        ptrLayout.onRefreshComplete();
                    }
                }, 3000);
            }
        });
    }
}
