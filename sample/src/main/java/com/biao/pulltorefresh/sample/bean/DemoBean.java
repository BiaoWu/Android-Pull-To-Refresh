package com.biao.pulltorefresh.sample.bean;

import android.view.View;

/**
 * Created by biaowu on 15/12/24.
 */
public class DemoBean {
    public String title;
    public String content;
    public View.OnClickListener onClickListener;

    public DemoBean(String title, String content, View.OnClickListener onClickListener) {
        this.title = title;
        this.content = content;
        this.onClickListener = onClickListener;
    }
}
