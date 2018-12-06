package com.meituan.ming.downloader.core;

import com.meituan.ming.downloader.OkHttpManager;
import com.meituan.ming.downloader.entities.Constants;
import com.meituan.ming.downloader.entities.DownloadEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int code = response.code();
                    RandomAccessFile raf;
                    FileOutputStream fos;
                    InputStream in;
                    if (code == HttpURLConnection.HTTP_PARTIAL) {
                        in = response.body().byteStream();
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
                        in = response.body().byteStream();
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
                    if (response != null) {
                        response.close();
                    }
                }
            }
        };
        if (isSingleDownload) {
            OkHttpManager.getInstance().initRequest(url, callback);
        } else {
            OkHttpManager.getInstance().initRequest(url, startPos, endPos, callback);
        }
    }


    public boolean isRunning() {
        return mStatus == DownloadEntry.DownloadStatus.downloading;
    }

    public void pause() {
        isPaused = true;
    }

    public void cancel() {
        isCanceled = true;
    }

    public void cancelByError() {
        isError = true;
    }


    interface DownloadListener {

        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String message);

        void onDownloadPaused(int index);

        void onDownloadCanceled(int index);
    }

}
