package com.biao.pulltorefresh.sample.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biao.pulltorefresh.sample.R;
import com.biao.pulltorefresh.sample.bean.DemoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by biaowu on 15/12/24.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private List<DemoBean> data;

    public MainAdapter() {
        data = new ArrayList<>();
    }

    public void addAll(List<DemoBean> demoBeans) {
        data.addAll(demoBeans);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DemoBean demoBean = data.get(position);
        TextView textView = holder.mTextView;
        textView.setText(demoBean.content);
        textView.setTextSize(12);
        int color = textView.getContext().getResources().getColor(R.color.colorPrimary);
        textView.setBackgroundColor(color);
        textView.setTextColor(Color.WHITE);
        textView.setOnClickListener(demoBean.onClickListener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
