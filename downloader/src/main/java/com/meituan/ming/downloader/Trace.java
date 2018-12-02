package com.meituan.ming.downloader;

import android.util.Log;

/**
 * Created by shiwenming on 2018/10/22.
 */
public class Trace {

    public static String TAG = "trace";
    public static final boolean DEBUG = true;


    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }


}
