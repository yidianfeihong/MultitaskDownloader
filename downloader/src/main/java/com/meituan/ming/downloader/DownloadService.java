package com.meituan.ming.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadService extends Service {

    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap();
    private ExecutorService mExecutors;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DataChanger.getInstance().postStatus((DownloadEntry) msg.obj);
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
        int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
        doAction(action, entry);
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {

        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                startDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
        }
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(entry.id);
        if (downloadTask != null) {
            downloadTask.cancel(entry);
        }

    }

    private void resumeDownload(DownloadEntry entry) {
        startDownload(entry);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(entry.id);
        if (downloadTask != null) {
            downloadTask.pause(entry);
        }
    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask downloadTask = new DownloadTask(entry, mHandler);
        mDownloadingTasks.put(entry.id, downloadTask);
        mExecutors.execute(downloadTask);
    }

}
