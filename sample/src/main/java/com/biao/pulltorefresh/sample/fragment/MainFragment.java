package com.biao.pulltorefresh.sample.fragment;

import android.graphics.Color;
import android.os.Bundle;

import com.biao.pulltorefresh.OnRefreshListener;
import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.header.MaterialHeader;
import com.biao.pulltorefresh.sample.common.BaseRecyclerFragment;
import com.biao.pulltorefresh.sample.common.CommonLog;

/**
 * Created by biaowu.
 */
public class MainFragment extends BaseRecyclerFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};

        final MaterialHeader header = new MaterialHeader(getContext());
        header.setColorSchemeColors(colors);
        setHeaderView(header);


        final MaterialHeader footer = new MaterialHeader(getContext());
        footer.setColorSchemeColors(colors);
        setFooterView(footer);

        final PtrLayout ptrLayout = getPtrLayout();

        ptrLayout.setOnPullDownRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                header.postDelayed(new Runnable() {
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
                header.postDelayed(new Runnable() {
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
