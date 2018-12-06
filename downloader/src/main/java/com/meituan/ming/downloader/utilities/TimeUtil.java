package com.meituan.ming.downloader.utilities;

import com.meituan.ming.downloader.DownloadConfig;

public class TimeUtil {

    private static TimeUtil mInstance;
    private long mLastStamp;

    private TimeUtil() {

    }

    public synchronized static TimeUtil getInstance() {
        if (mInstance == null) {
            mInstance = new TimeUtil();
        }
        return mInstance;
    }

    public synchronized boolean needToNotify() {
        long stamp = System.currentTimeMillis();
        if (stamp - mLastStamp > DownloadConfig.getConfig().getMinNotifyInterval()) {
            mLastStamp = stamp;
            return true;
        }
        return false;
    }
}
