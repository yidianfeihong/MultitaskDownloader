package com.meituan.ming.downloader;

import android.os.Environment;

import com.meituan.ming.downloader.utilities.DownloadInfoUtil;

import java.io.File;

/**
 * Created by shiwenming on 2018/12/5.
 */
public class DownloadConfig {

    private static DownloadConfig mConfig;
    private int maxDownloadTasks = 3;
    private int maxDownloadThreads = 3;
    private File downloadDir = null;

    private boolean recoverDownloadWhenStart = true;
    private int maxRetrycount = 3;
    private int minOperateInterval = 500;
    private double minNotifyInterval = 1000;

    public double getMinNotifyInterval() {
        return minNotifyInterval;
    }

    public void setMinNotifyInterval(double minNotifyInterval) {
        this.minNotifyInterval = minNotifyInterval;
    }


    private DownloadConfig() {
        downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static DownloadConfig getConfig() {
        if (mConfig == null) {
            mConfig = new DownloadConfig();
        }
        return mConfig;
    }

    public int getMaxDownloadTasks() {
        return maxDownloadTasks;
    }

    public void setMaxDownloadTasks(int maxDownloadTasks) {
        this.maxDownloadTasks = maxDownloadTasks;
    }

    public int getMaxDownloadThreads() {
        return maxDownloadThreads;
    }

    public void setMaxDownloadThreads(int maxDownloadThreads) {
        this.maxDownloadThreads = maxDownloadThreads;
    }

    public File getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    public int getMinOperateInterval() {
        return minOperateInterval;
    }

    public void setMinOperateInterval(int minOperateInterval) {
        this.minOperateInterval = minOperateInterval;
    }

    public boolean isRecoverDownloadWhenStart() {
        return recoverDownloadWhenStart;
    }

    public void setRecoverDownloadWhenStart(boolean recoverDownloadWhenStart) {
        this.recoverDownloadWhenStart = recoverDownloadWhenStart;
    }

    public int getMaxRetrycount() {
        return maxRetrycount;
    }

    public void setMaxRetrycount(int maxRetrycount) {
        this.maxRetrycount = maxRetrycount;
    }


    public File getDownloadFile(String url) {
        return new File(downloadDir, DownloadInfoUtil.getMd5FileName(url));
    }

}
