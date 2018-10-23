package com.meituan.ming.downloader;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by shiwenming on 2018/10/21.
 */
public abstract class DataWatcher implements Observer {


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry) arg);
        }
    }

    public abstract void notifyUpdate(DownloadEntry entry);
}
