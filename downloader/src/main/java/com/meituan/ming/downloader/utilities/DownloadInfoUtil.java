package com.meituan.ming.downloader.utilities;

import com.meituan.ming.downloader.entities.DownloadEntry;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

/**
 * Created by shiwenming on 2018/12/5.
 */
public class DownloadInfoUtil {


    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 10 + 26; // 10 digits + 26 letters

    public static String getMd5FileName(String url) {
        byte[] md5 = getMD5(url.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(RADIX) + url.substring(url.lastIndexOf("/") + 1);
    }

    private static byte[] getMD5(byte[] data) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LogUtil.e(e.getMessage());
        }
        return hash;
    }


    public static int getDownloadPercent(DownloadEntry entry) {
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
