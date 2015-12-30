package com.biao.pulltorefresh.sample.common;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.biao.pulltorefresh.sample.adapter.MainAdapter;
import com.biao.pulltorefresh.sample.utils.SpacesItemDecoration;

/**
 * Created by biaowu.
 */
public class BaseListFragment extends BaseRecyclerFragment {

    protected MainAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext();

        RecyclerView recyclerView = getRecyclerView();
        float density = context.getResources().getDisplayMetrics().density;
        recyclerView.addItemDecoration(new SpacesItemDecoration(recyclerView, Math.round(density * 16)));

    }

    @Override
    protected RecyclerView.LayoutManager buildLayoutManager(Context context) {
        return new GridLayoutManager(context, 2);
    }

    @Override
    protected RecyclerView.Adapter buildAdapter(Context context) {
        return mAdapter = new MainAdapter();
    }
}
