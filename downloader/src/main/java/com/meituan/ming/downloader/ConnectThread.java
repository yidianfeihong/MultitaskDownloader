package com.meituan.ming.downloader;

import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectThread implements Runnable {

    private String mUrl;
    private ConnectListener mListener;
    public volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        mUrl = url;
        mListener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(mUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(Constants.CONNECTTIMEOUT);
            connection.setReadTimeout(Constants.READTIMEOUT);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            int length = connection.getContentLength();
            boolean isSupportRange = false;
            if (code == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                mListener.onConnected(isSupportRange, length);
            } else {
                mListener.onConnectError("server error:" + code);
            }
            isRunning = false;
        } catch (Exception e) {
            isRunning = false;
            mListener.onConnectError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }


    }

    public void cancel() {
        Thread.currentThread().interrupt();

    }

    interface ConnectListener {

        void onConnected(boolean isSupportRange, int totalLenth);

        void onConnectError(String message);
    }

}
