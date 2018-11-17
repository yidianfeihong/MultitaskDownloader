package com.meituan.ming.downloader;

import java.io.Serializable;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadEntry implements Serializable {

    public String id;
    public String name;
    public String url;

    public double currentLength;
    public double totalLength;

    public DownloadStatus status = DownloadStatus.idle;

    public enum DownloadStatus {idle, waiting, downloading, paused, resumed, cancelled, completed}

    public DownloadEntry() {
    }

    public DownloadEntry(String url) {
        this.id = url;
        this.url = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    public String toString() {
        return "DownloadEntry: " + url + " is " + status.name() + " with " + currentLength + "/" + totalLength;
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
