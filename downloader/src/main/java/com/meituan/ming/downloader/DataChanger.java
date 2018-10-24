package com.meituan.ming.downloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DataChanger extends Observable {

    public static DataChanger mInstance = null;
    private LinkedHashMap<String, DownloadEntry> mOperatedEntries;

    private DataChanger() {
        mOperatedEntries = new LinkedHashMap<>();
    }

    public static DataChanger getInstance() {
        if (mInstance == null) {
            mInstance = new DataChanger();
        }
        return mInstance;
    }

    public void postStatus(DownloadEntry entry) {
        mOperatedEntries.put(entry.id, entry);
        setChanged();
        notifyObservers(entry);
    }

    public ArrayList queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> recoverableEntries = null;
        for (Map.Entry<String, DownloadEntry> entry : mOperatedEntries.entrySet()) {
            if (entry.getValue().status == DownloadEntry.DownloadStatus.paused) {
                if (recoverableEntries == null) {
                    recoverableEntries = new ArrayList<>();
                }
                recoverableEntries.add(entry.getValue());
            }
        }
        return recoverableEntries;
    }
}
