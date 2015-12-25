package com.biao.pulltorefresh.sample.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.sample.adapter.MainAdapter;
import com.biao.pulltorefresh.sample.bean.DemoBean;
import com.biao.pulltorefresh.sample.common.BaseRecyclerFragment;
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
        RecyclerView recyclerView = getRecyclerView();
        float density = recyclerView.getContext().getResources().getDisplayMetrics().density;
        recyclerView.addItemDecoration(new SpacesItemDecoration(recyclerView, Math.round(density * 16)));

        initData();
    }

    private void initData() {
        List<DemoBean> demoBeans = new ArrayList<>();
        demoBeans.add(new DemoBean("None Refresh View", "None Refresh View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new BaseRecyclerFragment(), true);
            }
        }));
        demoBeans.add(new DemoBean("All Move", "All Move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ALL_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("Only Content can move", "Only Content can move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_CONTENT_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("Only Content cannot move", "Only Content cannot move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_CONTENT_NOT_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("Only header cannot move", "Only header cannot move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_HEADER_NOT_MOVE), true);
            }
        }));
        demoBeans.add(new DemoBean("Only footer cannot move", "Only footer cannot move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_FOOTER_NOT_MOVE), true);
            }
        }));
        mMainAdapter.addAll(demoBeans);
    }

    @Override
    protected RecyclerView.LayoutManager buildLayoutManager(Context context) {
        return new GridLayoutManager(context, 2);
    }

    @Override
    protected RecyclerView.Adapter buildAdapter(Context context) {
        return mMainAdapter = new MainAdapter();
    }
}
