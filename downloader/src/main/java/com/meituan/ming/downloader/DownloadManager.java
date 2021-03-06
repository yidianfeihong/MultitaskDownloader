package com.meituan.ming.downloader;

import android.content.Context;
import android.content.Intent;

import com.meituan.ming.downloader.core.DownloadService;
import com.meituan.ming.downloader.entities.Constants;
import com.meituan.ming.downloader.entities.DownloadEntry;
import com.meituan.ming.downloader.notify.DownloadChanger;
import com.meituan.ming.downloader.notify.DownloadWatcher;

import java.io.File;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadManager {

    private static DownloadManager mInstance;
    private final Context mContext;
    private long mLastOperatedTime = 0;

    private DownloadManager(Context context) {
        this.mContext = context;
        mContext.startService(new Intent(context, DownloadService.class));
    }

    public synchronized static DownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    public void add(DownloadEntry entry) {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        mContext.startService(intent);
    }


    public void pause(DownloadEntry entry) {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        mContext.startService(intent);

    }

    public void resume(DownloadEntry entry) {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME);
        mContext.startService(intent);

    }


    public void pauseAll() {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        mContext.startService(intent);

    }


    public void recoverAll() {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        mContext.startService(intent);

    }

    public void cancel(DownloadEntry entry) {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        mContext.startService(intent);

    }

    public void addObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance(mContext).addObserver(watcher);
    }

    public void removeObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance(mContext).deleteObserver(watcher);
    }


    public boolean checkIfExecutable() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - mLastOperatedTime > DownloadConfig.getConfig().getMinOperateInterval()) {
            mLastOperatedTime = currentTimeMillis;
            return true;
        }
        return false;
    }

    public DownloadEntry queryDownloadEntry(String id) {
        return DownloadChanger.getInstance(mContext).queryDownloadEntryById(id);
    }

    public void reDownload(DownloadEntry downloadEntry) {
        downloadEntry.reset();
        add(downloadEntry);
    }


    public boolean containsDownloadEntry(String id) {
        return DownloadChanger.getInstance(mContext).containsDownloadEntry(id);
    }

    public void deleteDownloadEntry(boolean forceDelete, String id) {
        DownloadChanger.getInstance(mContext).deleteDownloadEntry(id);
        if (forceDelete) {
            File file = DownloadConfig.getConfig().getDownloadFile(id);
            if (file.exists())
                file.delete();
        }
    }

}
