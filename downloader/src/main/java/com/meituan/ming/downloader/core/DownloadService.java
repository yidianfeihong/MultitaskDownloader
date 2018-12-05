package com.meituan.ming.downloader.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.meituan.ming.downloader.DownloadConfig;
import com.meituan.ming.downloader.entities.Constants;
import com.meituan.ming.downloader.notify.DataChanger;
import com.meituan.ming.downloader.entities.DownloadEntry;
import com.meituan.ming.downloader.db.DBController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadService extends Service {

    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap<>();
    private ExecutorService mExecutors;
    private LinkedBlockingQueue<DownloadEntry> mWaitingQueue = new LinkedBlockingQueue<>();

    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_CONNECTING = 5;
    public static final int NOTIFY_ERROR = 6;

    private DataChanger mDataChanger;
    private DBController mDBController;

    private NetInfoReceiver mNetInfoReceiver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOTIFY_PAUSED_OR_CANCELLED:
                case NOTIFY_COMPLETED:
                case NOTIFY_ERROR:
                    checkNext((DownloadEntry) msg.obj);
                    break;

            }
            mDataChanger.postStatus((DownloadEntry) msg.obj);
        }
    };

    private void checkNext(DownloadEntry entry) {
        mDownloadingTasks.remove(entry.id);
        DownloadEntry downloadEntry = mWaitingQueue.poll();
        if (downloadEntry != null) {
            startDownload(downloadEntry);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
        mDataChanger = DataChanger.getInstance(getApplicationContext());
        mDBController = DBController.getInstance(getApplicationContext());
        intializeDownload();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetInfoReceiver = new NetInfoReceiver();
        registerReceiver(mNetInfoReceiver, intentFilter);

    }

    private void intializeDownload() {

        ArrayList<DownloadEntry> downloadEntries = mDBController.queryAll();
        if (downloadEntries != null) {
            for (DownloadEntry entry : downloadEntries) {
                if (entry.status.equals(DownloadEntry.DownloadStatus.downloading) || entry.status.equals(DownloadEntry.DownloadStatus.waiting)) {
                    if (DownloadConfig.getConfig().isRecoverDownloadWhenStart()) {
                        if (entry.isSupportRange) {
                            entry.status = DownloadEntry.DownloadStatus.paused;
                            entry.downloadSpeed = 0;
                        } else {
                            entry.status = DownloadEntry.DownloadStatus.idle;
                            entry.reset();
                        }
                        addDownload(entry);
                    } else {
                        if (entry.isSupportRange) {
                            entry.status = DownloadEntry.DownloadStatus.paused;
                            entry.downloadSpeed = 0;
                        } else {
                            entry.status = DownloadEntry.DownloadStatus.idle;
                            entry.reset();
                        }
                        mDBController.newOrUpdate(entry);
                    }
                }
                mDataChanger.addToOperatedEntryMap(entry.id, entry);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetInfoReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            if (entry != null && mDataChanger.containsDownloadEntry(entry.id)) {
                entry = mDataChanger.queryDownloadEntryById(entry.id);
            }
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            doAction(action, entry);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {

        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(entry);
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
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;

        }
    }

    private void recoverAll() {
        ArrayList<DownloadEntry> recoverableEntries = mDataChanger.queryAllRecoverableEntries();
        if (recoverableEntries != null) {
            for (DownloadEntry downloadEntry : recoverableEntries) {
                addDownload(downloadEntry);
            }
        }
    }

    private void pauseAll() {
        for (Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
            DownloadTask downloadTask = entry.getValue();
            downloadTask.pause();
        }
        mDownloadingTasks.clear();
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry downloadEntry = mWaitingQueue.poll();
            downloadEntry.status = DownloadEntry.DownloadStatus.paused;
            mDataChanger.postStatus(downloadEntry);
        }
    }


    private void addDownload(DownloadEntry downloadEntry) {
        if (mDownloadingTasks.size() == DownloadConfig.getConfig().getMaxDownloadTasks()) {
            mWaitingQueue.offer(downloadEntry);
            downloadEntry.status = DownloadEntry.DownloadStatus.waiting;
            mDataChanger.postStatus(downloadEntry);
        } else {
            startDownload(downloadEntry);
        }
    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask downloadTask = new DownloadTask(entry, mHandler, mExecutors);
        mDownloadingTasks.put(entry.id, downloadTask);
        downloadTask.start();
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(entry.id);
        if (downloadTask != null) {
            downloadTask.cancel();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.cancelled;
            entry.reset();
            mDataChanger.postStatus(entry);
        }
    }


    private void resumeDownload(DownloadEntry entry) {
        addDownload(entry);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(entry.id);
        if (downloadTask != null) {
            downloadTask.pause();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.paused;
            mDataChanger.postStatus(entry);
        }
    }


    class NetInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) DownloadService.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo != null && networkInfo.isConnected()) {
                recoverAll();
            }else{
                pauseAll();
            }
        }
    }
}