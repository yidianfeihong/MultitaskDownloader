package com.meituan.ming.multitaskdownloader;

import java.io.Serializable;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadEntry implements Serializable{

    public String id;
    public String name;
    public String url;

    public enum DownloadStatus{waiting,downloading,pause,resume,cancel};

    public DownloadStatus status;

    int currentLength;
    int totalLength;

}
