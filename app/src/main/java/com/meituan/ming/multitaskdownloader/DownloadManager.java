package com.meituan.ming.multitaskdownloader;

import android.content.Context;
import android.content.Intent;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadManager {

    private static DownloadManager mInstance;
    private final Context mContext;


    private DownloadManager(Context context) {
        this.mContext = context;
    }

    public synchronized DownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    public void add(Context context, DownloadEntry entry) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }


    public void pause() {


    }

    public void resume() {


    }

    public void cancel() {


    }

    public void addObserver(DataWatcher watcher) {

        DataChanger.getInstance().addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        DataChanger.getInstance().deleteObserver(watcher);
    }

}
