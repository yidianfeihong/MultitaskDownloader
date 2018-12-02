package com.meituan.ming.downloader;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by shiwenming on 2018/10/21.
 */
@DatabaseTable
public class DownloadEntry implements Serializable {
    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String url;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public boolean isSupportRange;
    @DatabaseField
    public DownloadStatus status = DownloadStatus.idle;
    @DatabaseField
    public int percent;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer, Integer> ranges;


    public DownloadEntry() {
    }

    public DownloadEntry(String url) {
        this.id = url;
        this.url = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }


    public enum DownloadStatus {idle, waiting, connecting, downloading, paused, resumed, cancelled, completed, error}


    public void reset() {
        this.currentLength = 0;
        this.percent = 0;
        this.ranges = null;
    }


    @Override
    public String toString() {
        return "DownloadEntry: " + name + " is " + status.name() + " with " + currentLength + "/" + totalLength;
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            return obj.hashCode() == this.hashCode();
        } else {
            return false;
        }
    }
}
