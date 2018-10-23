package com.meituan.ming.downloader;

import java.util.Observable;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DataChanger extends Observable {

    public static DataChanger mInstance = null;

    public static DataChanger getInstance() {
        if (mInstance == null) {
            mInstance = new DataChanger();
        }
        return mInstance;
    }

    public void postStatus(DownloadEntry entry) {
        setChanged();
        notifyObservers(entry );
    }

}
