package com.meituan.ming.downloader.util;

import com.meituan.ming.downloader.DownloadEntry;

import java.text.DecimalFormat;

/**
 * Created by shiwenming on 2018/12/5.
 */
public class DownloadUtils {

    public static int getDownlaodPercent(DownloadEntry entry) {
        int percent;
        if (entry.totalLength > 0) {
            percent = (int) (entry.currentLength * 100l / entry.totalLength);
        } else {
            percent = 0;
        }
        return percent;
    }

    public static String getDownloadInfo(DownloadEntry entry) {
        return getFormatSize(entry.currentLength) + "/" + getFormatSize(entry.totalLength) + " | " + getSpeedInfo(entry.downloadSpeed);
    }

    public static String getSpeedInfo(int speed) {
        return getFormatSize(speed) + "/s";
    }


    public static String getFormatSize(int size) {
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.0");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B" +
                    "";
        }
        return resultSize;
    }


    public static String getStatus(DownloadEntry entry) {

        switch (entry.status) {
            case idle:
                return "下载";
            case downloading:
                return "暂停";
            case paused:
                return "继续";
            case completed:
                return "完成";
            default:
                return "下载";
        }
    }
}
