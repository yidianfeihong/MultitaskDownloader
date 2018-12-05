package com.meituan.ming.downloader.core;

import android.os.Environment;

import com.meituan.ming.downloader.entities.Constants;
import com.meituan.ming.downloader.entities.DownloadEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadThread implements Runnable {

    private int index;
    private int startPos;
    private int endPos;
    private String url;
    private DownloadEntry.DownloadStatus mStatus;
    private DownloadListener listener;
    private volatile boolean isCanceled;
    private volatile boolean isPaused;
    private volatile boolean isError;

    private final File destFile;
    private boolean isSingleDownload;

    public DownloadThread(String url, File destFile, int index, int startPos, int endPos, DownloadListener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        if (startPos == 0 && endPos == 0) {
            isSingleDownload = true;
        } else {
            isSingleDownload = false;
        }
        this.destFile = destFile;
        this.listener = listener;

    }

    @Override
    public void run() {
        mStatus = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            connection.setConnectTimeout(Constants.CONNECTTIMEOUT);
            connection.setReadTimeout(Constants.READTIMEOUT);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            RandomAccessFile raf;
            FileOutputStream fos;
            InputStream in;
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                in = connection.getInputStream();
                raf = new RandomAccessFile(destFile, "rw");
                raf.seek(startPos);
                byte[] buff = new byte[2048];
                int len;
                while ((len = in.read(buff)) != -1) {
                    if (isPaused || isCanceled || isError) {
                        break;
                    }
                    raf.write(buff, 0, len);
                    listener.onProgressChanged(index, len);
                }
                raf.close();
                in.close();
            } else if (code == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                fos = new FileOutputStream(destFile);
                byte[] buff = new byte[2048];
                int len;
                while ((len = in.read(buff)) != -1) {
                    if (isPaused || isCanceled || isError) {
                        break;
                    }
                    fos.write(buff, 0, len);
                    listener.onProgressChanged(index, len);
                }
                fos.close();
                in.close();
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index, "server error:" + code);
                return;
            }
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                listener.onDownloadCanceled(index);
            } else if (isError) {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index, "cancel manually by error");
            } else {
                mStatus = DownloadEntry.DownloadStatus.completed;
                listener.onDownloadCompleted(index);
            }
        } catch (Exception e) {
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                listener.onDownloadCanceled(index);
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index, e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return mStatus == DownloadEntry.DownloadStatus.downloading;
    }

    public void pause() {
        isPaused = true;
        Thread.currentThread().interrupt();
    }

    public void cancel() {
        isCanceled = true;
        Thread.currentThread().interrupt();
    }

    public void cancelByError() {
        isError = true;
        Thread.currentThread().interrupt();
    }


    interface DownloadListener {

        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String message);

        void onDownloadPaused(int index);

        void onDownloadCanceled(int index);
    }

}