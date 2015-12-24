package com.biao.pulltorefresh.sample.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.sample.R;
import com.biao.pulltorefresh.sample.adapter.RecyclerAdapter;

/**
 * Created by biaowu on 15/12/24.
 */
public class BaseRecyclerFragment extends Fragment {

    private PtrLayout mPtrLayout;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.ptr_recycler_view, container, false);
        mPtrLayout = (PtrLayout) view;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(buildLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(buildAdapter(view.getContext()));
        return view;
    }

    protected RecyclerView.Adapter buildAdapter(Context context) {
        return new RecyclerAdapter();
    }

    protected RecyclerView.LayoutManager buildLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }

    protected void setHeaderView(View view) {
        mPtrLayout.setHeaderView(view);
    }

    protected void setFooterView(View view) {
        mPtrLayout.setFooterView(view);
    }
}