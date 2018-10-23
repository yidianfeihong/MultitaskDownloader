package com.meituan.ming.sample;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.meituan.ming.downloader.DataWatcher;
import com.meituan.ming.downloader.DownloadEntry;
import com.meituan.ming.downloader.DownloadManager;
import com.meituan.ming.downloader.Trace;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mDownloadBtn;
    private Button mPauseBtn;
    private Button mCancelBtn;

    private DownloadEntry mDownloadEntry;
    private DownloadManager mDownloadManager;

    private DataWatcher mWatcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry entry) {
            mDownloadEntry = entry;
            if (entry.status.equals(DownloadEntry.DownloadStatus.cancelled)) {
                mDownloadEntry = null;
            }
            Trace.e(entry.toString());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDownloadManager = DownloadManager.getInstance(this);

        mDownloadBtn = findViewById(R.id.download);
        mPauseBtn = findViewById(R.id.pause);
        mCancelBtn = findViewById(R.id.cancel);

        mDownloadBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);

    }


    @Override
    protected void onPause() {
        super.onPause();
        mDownloadManager.removeObserver(mWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(mWatcher);
    }


    @Override
    public void onClick(View v) {

        if (mDownloadEntry == null) {
            mDownloadEntry = new DownloadEntry();
            mDownloadEntry.name = "test.jpg";
            mDownloadEntry.url = "http://api.stay4it.com/uploads/test.jpg";
            mDownloadEntry.id = "1";
        }

        switch (v.getId()) {

            case R.id.download:
                mDownloadManager.add(mDownloadEntry);
                break;
            case R.id.pause:
                if (mDownloadEntry.status.equals(DownloadEntry.DownloadStatus.downloading)) {
                    mDownloadManager.pause(mDownloadEntry);
                } else if (mDownloadEntry.status.equals(DownloadEntry.DownloadStatus.paused)) {
                    mDownloadManager.resume(mDownloadEntry);
                }
                break;
            case R.id.cancel:
                mDownloadManager.cancel(mDownloadEntry);
                mDownloadEntry = null;
                break;

        }

    }
}
