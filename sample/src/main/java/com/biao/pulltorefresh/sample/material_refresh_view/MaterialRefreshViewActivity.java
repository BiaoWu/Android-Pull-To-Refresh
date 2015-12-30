package com.biao.pulltorefresh.sample.material_refresh_view;

import android.os.Bundle;

import com.biao.pulltorefresh.sample.common.BaseActivity;

/**
 * Created by biaowu.
 */
public class MaterialRefreshViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        replaceFragment(new MaterialRefreshViewFragment());
    }
}
