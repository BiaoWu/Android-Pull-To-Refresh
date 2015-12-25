package com.biao.pulltorefresh.utils;

import android.util.Log;

/**
 * LogUtil
 * Created by biaowu.
 */
public class L {
    private static final String TAG = L.class.getSimpleName();

    public static void e(String message) {
        e(TAG, message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void e(String tag, String message, Object... args) {
        Log.e(tag, format(message, args));
    }

    public static String format(String message, Object... args) {
        if (args != null && args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }
}
