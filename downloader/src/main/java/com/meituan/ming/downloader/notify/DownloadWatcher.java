package com.meituan.ming.downloader.notify;

import com.meituan.ming.downloader.entities.DownloadEntry;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by shiwenming on 2018/10/21.
 */
public abstract class DownloadWatcher implements Observer {


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry) arg);
        }
    }

    public abstract void notifyUpdate(DownloadEntry entry);
}
