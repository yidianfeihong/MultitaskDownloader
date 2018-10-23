package com.meituan.ming.downloader;

import android.os.Handler;
import android.os.Message;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadTask implements Runnable {
    private Handler mHandler;
    private DownloadEntry mDownloadEntry;
    private boolean isPaused;
    private boolean isCanceled;
    private Message mMsg;

    public DownloadTask(DownloadEntry entry, Handler handler) {
        this.mDownloadEntry = entry;
        this.mHandler = handler;
    }

    public void start() {
        mDownloadEntry.status = DownloadEntry.DownloadStatus.downloading;
        mDownloadEntry.totalLength = 100 * 1024;
        update(mDownloadEntry);
        for (int i = 0; i < mDownloadEntry.totalLength; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isPaused || isCanceled) {
                mDownloadEntry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancel;
                update(mDownloadEntry);
                return;
            }
            i += 1024;
            mDownloadEntry.currentLength += 1024;
            update(mDownloadEntry);
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.completed;
        update(mDownloadEntry);

    }

    private void update(DownloadEntry entry) {
        mMsg = mHandler.obtainMessage();
        mMsg.obj = entry;
        mHandler.sendMessage(mMsg);
    }

    public void pause(DownloadEntry entry) {
        isPaused = true;
        Trace.e("download paused");
    }

    public void cancel(DownloadEntry entry) {
        isCanceled = true;
    }

    @Override
    public void run() {
        start();
    }
}
