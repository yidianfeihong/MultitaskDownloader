package com.meituan.ming.downloader;

import android.os.Environment;

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
    private String path;
    private String url;
    private DownloadEntry.DownloadStatus mStatus;
    private Downloadlistener listener;
    private volatile boolean isCanceled;
    private volatile boolean isPaused;

    private boolean isSingleDownload;

    public DownloadThread(String url, int index, int startPos, int endPos, Downloadlistener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        if (startPos == 0 && endPos == 0) {
            isSingleDownload = true;
        }
        this.path = Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + url.substring(url.lastIndexOf("/") + 1);
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
            File file = new File(path);
            RandomAccessFile raf;
            FileOutputStream fos;
            InputStream in;
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                in = connection.getInputStream();
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPos);
                byte[] buff = new byte[2048];
                int len;
                while ((len = in.read(buff)) != -1) {
                    if (isPaused || isCanceled) {
                        break;
                    }
                    raf.write(buff, 0, len);
                    listener.onProgressChanged(index, len);
                }
                raf.close();
                in.close();
            } else if (code == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                fos = new FileOutputStream(file);
                byte[] buff = new byte[2048];
                int len;
                while ((len = in.read(buff)) != -1) {
                    if (isPaused || isCanceled) {
                        break;
                    }
                    fos.write(buff, 0, len);
                    listener.onProgressChanged(index, len);
                }
                fos.close();
                in.close();
            }
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                listener.onDownloadCanceled(index);
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
            } else
                mStatus = DownloadEntry.DownloadStatus.error;
            listener.onDownloadError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isPaused() {
        return mStatus == DownloadEntry.DownloadStatus.paused || mStatus == DownloadEntry.DownloadStatus.completed;
    }

    public boolean isCanceled() {
        return mStatus == DownloadEntry.DownloadStatus.cancelled || mStatus == DownloadEntry.DownloadStatus.completed;
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


    interface Downloadlistener {

        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(String message);

        void onDownloadPaused(int index);

        void onDownloadCanceled(int index);
    }

}
