package com.meituan.ming.downloader.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * Created by shiwenming on 2018/12/6.
 */
public class NetworkUtil {

    /**
     * 没有网络
     */
    public static final String NETWORKTYPE_UNAVAILABLE = "unavailable";
    /**
     * 移动网络
     */
    public static final String NETWORKTYPE_MOBILE = "mobile";
    /**
     * 无线网络
     */
    public static final String NETWORKTYPE_WIFI = "wifi";

    public static String getNetWorkState(Context context) {
        //得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        //如果网络连接，判断该网络类型
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORKTYPE_WIFI;//wifi
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORKTYPE_MOBILE;//mobile
            }
        }
        return NETWORKTYPE_UNAVAILABLE;
    }

// TODO: 2018/12/6 网络变化显示对话框

    /**
     * 弹出选择对话框
     */
    private void showNetDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alertDialog = builder.setTitle("当前wifi已断开，是否继续下载?").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: 2018/12/6
            }
        }).setPositiveButton("继续下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: 2018/12/6

            }
        }).setCancelable(false).create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }


    /**
     * 弹出选择对话框
     */
    private void showSettingDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alertDialog = builder.setTitle("网络异常").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);

            }
        }).setCancelable(false).create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

}
