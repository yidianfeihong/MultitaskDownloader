package com.meituan.ming.downloader;

import android.os.Handler;
import android.os.Message;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadTask implements Runnable {
    private Handler mHandler;
    private DownloadEntry mDownloadEntry;
    private volatile boolean isPaused;
    private volatile boolean isCanceled;


    public DownloadTask(DownloadEntry entry, Handler handler) {
        this.mDownloadEntry = entry;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        mDownloadEntry.status = DownloadEntry.DownloadStatus.downloading;
        mDownloadEntry.totalLength = 100 * 1024;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);
        for (int i = 0; i < mDownloadEntry.totalLength; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isPaused || isCanceled) {
                mDownloadEntry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancelled;
                notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
                return;
            }
            i += 1024;
            mDownloadEntry.currentLength += 1024;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_UPDATING);
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.completed;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_COMPLETED);

    }

    private void notifyUpdate(DownloadEntry entry, int what) {
        Message msg = mHandler.obtainMessage();
        msg.obj = entry;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    public void pause() {
        isPaused = true;
        Trace.e("download paused");
    }

    public void cancel(DownloadEntry entry) {
        isCanceled = true;
        Trace.e("download cancelled");
    }

}
