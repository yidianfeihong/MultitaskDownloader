package com.meituan.ming.downloader.core;

import com.meituan.ming.downloader.OkHttpManager;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ConnectThread implements Runnable {

    private String url;
    private ConnectListener listener;
    public volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }


    @Override
    public void run() {
        isRunning = true;

        OkHttpManager.getInstance().initRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isRunning = false;
                listener.onConnectError(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int length = (int) response.body().contentLength();
                    int code = response.code();
                    boolean isSupportRange = false;
                    if (code == HttpURLConnection.HTTP_OK) {
                        String ranges = response.header("Accept-Ranges");
                        if ("bytes".equals(ranges)) {
                            isSupportRange = true;
                        }
                        listener.onConnected(isSupportRange, length);
                    } else {
                        listener.onConnectError("server error:" + code);
                    }
                    isRunning = false;
                } catch (Exception e) {
                    isRunning = false;
                    listener.onConnectError(e.getMessage());
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            }
        });

    }

    public void cancel() {
    }

    interface ConnectListener {

        void onConnected(boolean isSupportRange, int totalLenth);

        void onConnectError(String message);
    }

}
