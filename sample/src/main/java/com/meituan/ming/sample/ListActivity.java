package com.meituan.ming.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.meituan.ming.downloader.DataWatcher;
import com.meituan.ming.downloader.DownloadEntry;
import com.meituan.ming.downloader.DownloadManager;
import com.meituan.ming.downloader.Trace;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<DownloadEntry> mDownloadEntries;
    private DownloadManager mDownloadManager;
    private DownloadListAdapter mAdapter;

    private DataWatcher mWatcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry entry) {
            int index = mDownloadEntries.indexOf(entry);
            if (index != -1) {
                mDownloadEntries.remove(entry);
                mDownloadEntries.add(index, entry);
                mAdapter.notifyDataSetChanged();
            }
            Trace.e(entry.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDownloadEntries = new ArrayList<>();
        mDownloadManager = DownloadManager.getInstance(this);
        mRecyclerView = findViewById(R.id.download_list);
        setData();
    }

    private void setData() {
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test0.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test1.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test2.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test3.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test4.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test5.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test0.jpg"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new DownloadListAdapter(mDownloadEntries, mDownloadManager);
        mRecyclerView.setAdapter(mAdapter);
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

}
