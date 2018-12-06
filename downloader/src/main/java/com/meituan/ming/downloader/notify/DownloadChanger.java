package com.meituan.ming.downloader.notify;

import android.content.Context;

import com.meituan.ming.downloader.entities.DownloadEntry;
import com.meituan.ming.downloader.db.DBController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadChanger extends Observable {

    public static DownloadChanger mInstance;
    private Context mContext;
    private LinkedHashMap<String, DownloadEntry> mOperatedEntries;

    private DownloadChanger(Context context) {
        mContext = context;
        mOperatedEntries = new LinkedHashMap<>();
    }

    public static DownloadChanger getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadChanger(context);
        }
        return mInstance;
    }

    public void postStatus(DownloadEntry entry) {
        mOperatedEntries.put(entry.id, entry);
        DBController.getInstance(mContext).newOrUpdate(entry);
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


    public DownloadEntry queryDownloadEntryById(String id) {
        return mOperatedEntries.get(id);
    }

    public void addToOperatedEntryMap(String key, DownloadEntry value) {
        mOperatedEntries.put(key, value);
    }

    public boolean containsDownloadEntry(String id) {
        return mOperatedEntries.containsKey(id);
    }

    public void deleteDownloadEntry(String id) {
        mOperatedEntries.remove(id);
        DBController.getInstance(mContext).deleteById(id);
    }


}
