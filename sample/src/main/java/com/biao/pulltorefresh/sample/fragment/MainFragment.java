package com.biao.pulltorefresh.sample.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.biao.pulltorefresh.OnRefreshListener;
import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.header.MaterialHeader;
import com.biao.pulltorefresh.sample.adapter.MainAdapter;
import com.biao.pulltorefresh.sample.bean.DemoBean;
import com.biao.pulltorefresh.sample.common.BaseRecyclerFragment;
import com.biao.pulltorefresh.sample.common.CommonLog;
import com.biao.pulltorefresh.sample.utils.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by biaowu.
 */
public class MainFragment extends BaseRecyclerFragment {

    private MainAdapter mMainAdapter;

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

        RecyclerView recyclerView = getRecyclerView();
        recyclerView.addItemDecoration(new SpacesItemDecoration(recyclerView, 32));

        initData();
        SwipeRefreshLayout swipeRefreshLayout;
    }

    private void initData() {
        List<DemoBean> demoBeans = new ArrayList<>();
        demoBeans.add(new DemoBean("None Refresh View", "None Refresh View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new BaseRecyclerFragment(), true);
            }
        }));
        demoBeans.add(new DemoBean("MODE_ALL_MOVE", "MODE_ALL_MOVE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ALL_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("MODE_ONLY_CONTENT_MOVE", "MODE_ONLY_CONTENT_MOVE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_CONTENT_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("MODE_ONLY_CONTENT_NOT_MOVE", "MODE_ONLY_CONTENT_NOT_MOVE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_CONTENT_NOT_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("MODE_ONLY_HEADER_NOT_MOVE", "MODE_ONLY_HEADER_NOT_MOVE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_HEADER_NOT_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("MODE_ONLY_FOOTER_NOT_MOVE", "MODE_ONLY_FOOTER_NOT_MOVE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_FOOTER_NOT_MOVE), true);
            }
        }));
        mMainAdapter.addAll(demoBeans);
    }

    @Override
    protected RecyclerView.LayoutManager buildLayoutManager(Context context) {
        return new GridLayoutManager(context, 3);
    }

    @Override
    protected RecyclerView.Adapter buildAdapter(Context context) {
        return mMainAdapter = new MainAdapter();
    }
}
