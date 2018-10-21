package com.meituan.ming.multitaskdownloader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by shiwenming on 2018/10/21.
 */
public class DownloadService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
        int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
        doAction(action, entry);
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        if (action == Constants.KEY_DOWNLOAD_ACTION_ADD) {
            entry.status = DownloadEntry.DownloadStatus.downloading;
            DataChanger.getInstance().postStatus(entry);
        }
    }

}
