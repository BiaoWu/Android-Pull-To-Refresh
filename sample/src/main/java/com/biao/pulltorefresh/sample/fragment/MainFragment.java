package com.biao.pulltorefresh.sample.fragment;

import android.graphics.Color;
import android.os.Bundle;

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

        ptrLayout.setOnRefreshListener(new PtrLayout.OnRefreshListener() {
            @Override
            public void onPullDownRefresh() {
                header.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommonLog.e("");
                        ptrLayout.onDownRefreshComplete();
                    }
                }, 3000);
            }

            @Override
            public void onPullUpRefresh() {
                header.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommonLog.e("");
                        ptrLayout.onUpRefreshComplete();
                    }
                }, 3000);
            }
        });

    }
}
