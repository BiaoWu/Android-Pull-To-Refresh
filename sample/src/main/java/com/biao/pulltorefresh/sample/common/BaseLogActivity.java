package com.biao.pulltorefresh.sample.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Set;

public abstract class BaseLogActivity extends AppCompatActivity {
    protected final String TAG;
    protected boolean showLifeInfo = false;
    protected boolean showThreadInfo = false;
    protected boolean showIntentInfo = false;


    public BaseLogActivity() {
        TAG = this.getClass().getSimpleName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (showLifeInfo)
            CommonLog.e(TAG, "onCreate");
        logIntentInfo(getIntent());
        logThreadInfo();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (showLifeInfo)
            CommonLog.e(TAG, "onNewIntent");
        logIntentInfo(intent);
        logThreadInfo();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (showLifeInfo)
            CommonLog.e(TAG, "onRestart");
        logThreadInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (showLifeInfo)
            CommonLog.e(TAG, "onStart");
        logThreadInfo();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (showLifeInfo)
            CommonLog.e(TAG, "onRestoreInstanceState");
        logThreadInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showLifeInfo)
            CommonLog.e(TAG, "onResume");
        logThreadInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (showLifeInfo)
            CommonLog.e(TAG, "onPause");
        logThreadInfo();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (showLifeInfo)
            CommonLog.e(TAG, "onSaveInstanceState");
        logThreadInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (showLifeInfo)
            CommonLog.e(TAG, "onStop");
        logThreadInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (showLifeInfo)
            CommonLog.e(TAG, "onDestroy");
        logThreadInfo();
    }

    private void logThreadInfo() {
        if (showThreadInfo) {
            Thread currentThread = Thread.currentThread();
            CommonLog.e(TAG, "thread id=" + currentThread.getId() + ",thread=" + currentThread);
        }
    }

    private void logIntentInfo(Intent intent) {
        if (showIntentInfo) {
            if (intent == null) {
                CommonLog.e(TAG, "receive intent is null!");
            } else {
                String action = intent.getAction();
                Set<String> categories = intent.getCategories();
                String dataString = intent.getDataString();
                String type = intent.getType();
                CommonLog.e(TAG, "action=" + action + ",categories=" + categories + ",data=" + dataString + ",type=" + type);
            }
        }
    }
}
