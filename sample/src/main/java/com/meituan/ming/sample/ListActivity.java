package com.meituan.ming.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.meituan.ming.downloader.Constants;
import com.meituan.ming.downloader.DataWatcher;
import com.meituan.ming.downloader.DownloadEntry;
import com.meituan.ming.downloader.DownloadManager;
import com.meituan.ming.downloader.Trace;
import com.meituan.ming.downloader.db.DBController;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (item.getTitle().equals("pause all")) {
                item.setTitle(R.string.action_recover_all);
                mDownloadManager.pauseAll();
            } else {
                item.setTitle(R.string.action_pause_all);
                mDownloadManager.recoverAll();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setData() {
        mDownloadEntries.add(new DownloadEntry("http://img3.imgtn.bdimg.com/it/u=3226098301,2803294068&fm=26&gp=0.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://gdown.baidu.com/data/wisegame/31ce1b54da062ae0/aiqiyi_81190.apk"));
        mDownloadEntries.add(new DownloadEntry("http://gdown.baidu.com/data/wisegame/61737a31b1c20d13/tengxunshipin_17785.apk"));
        mDownloadEntries.add(new DownloadEntry("http://gdown.baidu.com/data/wisegame/4f9fe69e53e2a754/jinritoutiao_700.apk"));
        mDownloadEntries.add(new DownloadEntry("http://gdown.baidu.com/data/wisegame/844a7871663f7f79/weixin_1360.apk"));
        mDownloadEntries.add(new DownloadEntry("http://gdown.baidu.com/data/wisegame/ec1b21ea1bb45374/zhangyue_71101.apk"));
        mDownloadEntries.add(new DownloadEntry("http://gdown.baidu.com/data/wisegame/d534b0cbcbde3132/gaodeditu_6750.apk"));
        for (int i = 0; i < mDownloadEntries.size(); i++) {
            DownloadEntry entry = mDownloadEntries.get(i);
            DownloadEntry realEntry = mDownloadManager.queryDownloadEntry(entry.id);
            if (realEntry != null) {
                mDownloadEntries.set(i, realEntry);
            }
        }
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
