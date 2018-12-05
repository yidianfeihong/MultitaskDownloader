package com.meituan.ming.downloader;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {
    private Handler mHandler;
    private DownloadEntry mDownloadEntry;
    private volatile boolean isPaused;
    private volatile boolean isCanceled;
    private ExecutorService mExecutors;

    private ConnectThread mConnectThread;
    private DownloadThread[] mDownloadThreads;

    private long mLastTime;
    private long mDownloadLength;

    private DownloadEntry.DownloadStatus[] mDownloadStatuses;


    public DownloadTask(DownloadEntry entry, Handler handler, ExecutorService executors) {
        this.mDownloadEntry = entry;
        this.mHandler = handler;
        mExecutors = executors;
        mDownloadLength = mDownloadEntry.currentLength;
    }

    public void start() {
        if (mDownloadEntry.totalLength > 0) {
            Trace.e("do not check");
            startDownload();
        } else {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.connecting;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_CONNECTING);
            mConnectThread = new ConnectThread(mDownloadEntry.url, this);
            mExecutors.execute(mConnectThread);
        }

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
        if (mConnectThread != null && mConnectThread.isRunning) {
            mConnectThread.cancel();
        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                DownloadThread thread = mDownloadThreads[i];
                if (thread != null && thread.isRunning()) {
                    if (mDownloadEntry.isSupportRange) {
                        thread.pause();
                    } else {
                        thread.cancel();
                    }
                }
            }
        }
    }

    public void cancel() {
        isCanceled = true;
        Trace.e("download cancelled");
        if (mConnectThread != null && mConnectThread.isRunning) {
            mConnectThread.cancel();
        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                DownloadThread thread = mDownloadThreads[i];
                if (thread != null && thread.isRunning()) {
                    thread.cancel();
                }
            }
        }
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        mDownloadEntry.isSupportRange = isSupportRange;
        mDownloadEntry.totalLength = totalLength;
        startDownload();
    }


    private void startDownload() {
        if (mDownloadEntry.isSupportRange && mDownloadEntry.totalLength > 0) {
            startMultiThreadDownload();
        } else {
            startSingleThreadDownload();
        }
    }

    private void startSingleThreadDownload() {
        mDownloadEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);

        mDownloadThreads = new DownloadThread[1];
        mDownloadStatuses = new DownloadEntry.DownloadStatus[1];
        mDownloadThreads[0] = new DownloadThread(mDownloadEntry.url, 0, 0, 0, this);
        mDownloadStatuses[0] = DownloadEntry.DownloadStatus.downloading;
        mExecutors.execute(mDownloadThreads[0]);
    }

    private void startMultiThreadDownload() {
        mDownloadEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);
        int block = mDownloadEntry.totalLength / Constants.MAX_DOWNLOAD_THREADS;
        int startPos;
        int endPos;
        if (mDownloadEntry.ranges == null) {
            mDownloadEntry.ranges = new HashMap<>();
            for (int i = 0; i < Constants.MAX_DOWNLOAD_THREADS; i++) {
                mDownloadEntry.ranges.put(i, 0);
            }
        }
        mDownloadStatuses = new DownloadEntry.DownloadStatus[Constants.MAX_DOWNLOAD_THREADS];
        mDownloadThreads = new DownloadThread[Constants.MAX_DOWNLOAD_THREADS];
        for (int i = 0; i < Constants.MAX_DOWNLOAD_THREADS; i++) {
            startPos = block * i + mDownloadEntry.ranges.get(i);
            if (i == Constants.MAX_DOWNLOAD_THREADS - 1) {
                endPos = mDownloadEntry.totalLength;
            } else {
                endPos = block * (i + 1) - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(mDownloadEntry.url, i, startPos, endPos, this);
                mDownloadStatuses[i] = DownloadEntry.DownloadStatus.downloading;
                mExecutors.execute(mDownloadThreads[i]);
            } else {
                mDownloadStatuses[i] = DownloadEntry.DownloadStatus.completed;
            }
        }
    }

    @Override
    public void onConnectError(String message) {
        if (isPaused || isCanceled) {
            mDownloadEntry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancelled;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
        } else {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.error;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_ERROR);
        }
    }

    @Override
    public synchronized void onProgressChanged(int index, int progress) {
        if (mDownloadEntry.isSupportRange) {
            int range = mDownloadEntry.ranges.get(index) + progress;
            mDownloadEntry.ranges.put(index, range);
        }
        int currDownloadLength = mDownloadEntry.currentLength;
        mDownloadEntry.currentLength += progress;
        long currTime = System.currentTimeMillis();
        if (currTime - mLastTime >= Constants.NOTIFY_PERIOD) {
            mLastTime = currTime;
            mDownloadEntry.downloadSpeed = (int) ((currDownloadLength - mDownloadLength) * 1000 / Constants.NOTIFY_PERIOD);
            mDownloadLength = currDownloadLength;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_UPDATING);
        }

//        if (mDownloadEntry.totalLength > 0) {
//            int downloadSpeed = (int) (mDownloadEntry.currentLength * 100l / mDownloadEntry.totalLength);
//            if (downloadSpeed > mDownloadEntry.downloadSpeed) {
//                mDownloadEntry.downloadSpeed = downloadSpeed;
//                notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_UPDATING);
//            }
//
//        } else {
//            int temp = mDownloadEntry.currentLength / 1024;
//            if (temp > mLastTime) {
//                mLastTime = temp;
//                notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_UPDATING);
//            }
//        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {

        mDownloadStatuses[index] = DownloadEntry.DownloadStatus.completed;
        for (int i = 0; i < mDownloadStatuses.length; i++) {
            if (mDownloadStatuses[i] != DownloadEntry.DownloadStatus.completed) {
                return;
            }
        }

        if (mDownloadEntry.totalLength > 0 && mDownloadEntry.currentLength != mDownloadEntry.totalLength) {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.error;
            mDownloadEntry.reset();
            String path = Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + mDownloadEntry.name;
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            mDownloadEntry.downloadSpeed = 0;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_ERROR);
        } else {
            mDownloadEntry.downloadSpeed = 0;
            mDownloadEntry.status = DownloadEntry.DownloadStatus.completed;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_COMPLETED);
        }
    }

    @Override
    public synchronized void onDownloadError(int index, String message) {

        mDownloadStatuses[index] = DownloadEntry.DownloadStatus.error;
        for (int i = 0; i < mDownloadStatuses.length; i++) {
            if (mDownloadStatuses[i] != DownloadEntry.DownloadStatus.completed && mDownloadStatuses[i] != DownloadEntry.DownloadStatus.error) {
                mDownloadThreads[i].cancelByError();
                return;
            }
        }

        mDownloadEntry.status = DownloadEntry.DownloadStatus.error;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_ERROR);
    }

    @Override
    public synchronized void onDownloadPaused(int index) {

        mDownloadStatuses[index] = DownloadEntry.DownloadStatus.paused;
        for (int i = 0; i < mDownloadStatuses.length; i++) {
            if (mDownloadStatuses[i] != DownloadEntry.DownloadStatus.completed && mDownloadStatuses[i] != DownloadEntry.DownloadStatus.paused) {
                return;
            }
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.paused;
        mDownloadEntry.downloadSpeed = 0;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadCanceled(int index) {

        mDownloadStatuses[index] = DownloadEntry.DownloadStatus.cancelled;
        for (int i = 0; i < mDownloadStatuses.length; i++) {
            if (mDownloadStatuses[i] != DownloadEntry.DownloadStatus.completed && mDownloadStatuses[i] != DownloadEntry.DownloadStatus.cancelled) {
                return;
            }
        }

        mDownloadEntry.status = DownloadEntry.DownloadStatus.cancelled;
        mDownloadEntry.reset();
        String path = Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + mDownloadEntry.name;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        mDownloadEntry.downloadSpeed = 0;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }
}
