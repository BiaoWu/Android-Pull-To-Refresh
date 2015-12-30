package com.biao.pulltorefresh.sample.material_refresh_view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.biao.pulltorefresh.PtrLayout;
import com.biao.pulltorefresh.sample.bean.DemoBean;
import com.biao.pulltorefresh.sample.common.BaseListFragment;
import com.biao.pulltorefresh.sample.default_refresh_view.DefaultRefreshViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by biaowu.
 */
public class MaterialRefreshViewFragment extends BaseListFragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getToolbar();
        toolbar.setTitle("MaterialRefreshView Different Mode");

        initData();
    }

    private void initData() {
        List<DemoBean> demoBeans = new ArrayList<>();
        demoBeans.add(new DemoBean("All Move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ALL_MOVE, "All Move"), true);
            }
        }));
        demoBeans.add(new DemoBean("Only Content can move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_CONTENT_MOVE, "Only Content can move"), true);
            }
        }));
        demoBeans.add(new DemoBean("Only Content cannot move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_CONTENT_NOT_MOVE, "Only Content cannot move"), true);
            }
        }));
        demoBeans.add(new DemoBean("Only header cannot move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_HEADER_NOT_MOVE, "Only header cannot move"), true);
            }
        }));
        demoBeans.add(new DemoBean("Only footer cannot move", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(MaterialHeaderFragment.newInstance(PtrLayout.MODE_ONLY_FOOTER_NOT_MOVE, "Only footer cannot move"), true);
            }
        }));
        demoBeans.add(new DemoBean("Default Refresh View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(DefaultRefreshViewFragment.newInstance(PtrLayout.MODE_ALL_MOVE, "Default Refresh View"), true);
            }
        }));
        mAdapter.addAll(demoBeans);
    }
}
