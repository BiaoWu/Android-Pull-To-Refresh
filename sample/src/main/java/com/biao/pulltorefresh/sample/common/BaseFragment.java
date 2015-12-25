package com.biao.pulltorefresh.sample.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.biao.pulltorefresh.sample.R;

/**
 * Created by biaowu.
 */
public abstract class BaseFragment extends Fragment {
    protected static final String TITLE = "title";
    private Toolbar mToolbar;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_base, container, false);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.fl_container);
        viewGroup.addView(onCreateContentView(inflater, viewGroup, savedInstanceState));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String title = arguments.getString(TITLE);
            if (!TextUtils.isEmpty(title)) {
                mToolbar.setTitle(title);
            }
        }

    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    protected abstract View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, false);
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).replaceFragment(fragment, addToBackStack);
        }
    }
}