package com.biao.pulltorefresh.sample.common;

import android.util.Log;

/**
 * CommonLog
 * <p/>
 * Created by biaowu.
 */
public class CommonLog {
    private static final String TAG = CommonLog.class.getSimpleName();

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void e(String message) {
        e(TAG, message);
    }
}
