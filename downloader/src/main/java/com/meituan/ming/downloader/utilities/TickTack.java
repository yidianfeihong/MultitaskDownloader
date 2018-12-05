package com.meituan.ming.downloader.utilities;

import com.meituan.ming.downloader.DownloadConfig;

public class TickTack {

    private static TickTack mInstance;
    private long mLastStamp;

    private TickTack() {

    }

    public synchronized static TickTack getInstance() {
        if (mInstance == null) {
            mInstance = new TickTack();
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
