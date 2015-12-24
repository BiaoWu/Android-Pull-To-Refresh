package com.biao.pulltorefresh.utils;

import android.util.Log;

/**
 * LogUtil
 * <p/>
 * Created by biaowu.
 */
public class L {
    private static final String TAG = L.class.getSimpleName();

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void e(String message) {
        e(TAG, message);
    }
}
