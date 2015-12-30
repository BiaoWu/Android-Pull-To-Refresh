package com.biao.pulltorefresh.sample;

import android.os.Bundle;

import com.biao.pulltorefresh.sample.common.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        replaceFragment(new MainFragment());
    }
}
