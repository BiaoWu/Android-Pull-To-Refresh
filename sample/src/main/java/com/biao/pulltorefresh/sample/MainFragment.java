package com.biao.pulltorefresh.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.sample.auto_refresh.HeaderAutoRefreshFragment;
import com.biao.pulltorefresh.sample.bean.DemoBean;
import com.biao.pulltorefresh.sample.common.BaseListFragment;
import com.biao.pulltorefresh.sample.common.BaseRecyclerFragment;
import com.biao.pulltorefresh.sample.default_refresh_view.DefaultRefreshViewFragment;
import com.biao.pulltorefresh.sample.material_refresh_view.MaterialRefreshViewActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by biaowu.
 */
public class MainFragment extends BaseListFragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context context = getContext();
        Toolbar toolbar = getToolbar();
        toolbar.setTitle(context.getString(R.string.app_name));

        initData();
    }

    private void initData() {
        List<DemoBean> demoBeans = new ArrayList<>();
        demoBeans.add(new DemoBean("None Refresh View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(BaseRecyclerFragment.newInstance("None Refresh View"), true);
            }
        }));
        demoBeans.add(new DemoBean("Material Refresh View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), MaterialRefreshViewActivity.class));
            }
        }));
        demoBeans.add(new DemoBean("Default Refresh View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(DefaultRefreshViewFragment.newInstance(PtrLayout.MODE_ALL_MOVE, "Default Refresh View"), true);
            }
        }));
        demoBeans.add(new DemoBean("Header Auto Refresh", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(HeaderAutoRefreshFragment.newInstance(PtrLayout.MODE_ALL_MOVE, "Header Auto Refresh"), true);
            }
        }));
        mAdapter.addAll(demoBeans);
    }
}
