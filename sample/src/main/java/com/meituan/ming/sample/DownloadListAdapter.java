package com.meituan.ming.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meituan.ming.downloader.DownloadEntry;
import com.meituan.ming.downloader.DownloadManager;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by shiwenming on 2018/10/23.
 */
public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.DownloadItemViewHolder>{

    private List<DownloadEntry> mDownloadEntries;
    private DownloadManager mDownloadManager;

    public DownloadListAdapter(List<DownloadEntry> downloadEntries, DownloadManager downloadManager) {
        mDownloadEntries = downloadEntries;
        mDownloadManager = downloadManager;
    }

    @NonNull
    @Override
    public DownloadItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.download_item, viewGroup, false);
        DownloadItemViewHolder viewHolder = new DownloadItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadItemViewHolder holder, int i) {

        final DownloadEntry downloadEntry = mDownloadEntries.get(i);
        holder.downloadPercent.setText(getDownloadPercent(downloadEntry));
        holder.downloadProgressBar.setProgress(downloadEntry.percent);
        holder.downloadTitle.setText(downloadEntry.name);
        holder.downloadButton.setText(downloadEntry.status.toString());
        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (downloadEntry.status) {
                    case idle:
                        mDownloadManager.add(downloadEntry);
                        break;
                    case downloading:
                    case waiting:
                        mDownloadManager.pause(downloadEntry);
                        break;
                    case paused:
                        mDownloadManager.resume(downloadEntry);
                        break;
                    case cancelled:
                    case completed:
                        mDownloadManager.reDownload(downloadEntry);
                        break;
                }
            }
        });
        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManager.cancel(downloadEntry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDownloadEntries.size();
    }

    static class DownloadItemViewHolder extends RecyclerView.ViewHolder {

        TextView downloadTitle;
        ProgressBar downloadProgressBar;
        TextView downloadPercent;
        Button downloadButton;
        Button cancelButton;


        public DownloadItemViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadTitle = itemView.findViewById(R.id.download_title);
            downloadProgressBar = itemView.findViewById(R.id.download_progressbar);
            downloadPercent = itemView.findViewById(R.id.download_percent);
            downloadButton = itemView.findViewById(R.id.download_button);
            cancelButton = itemView.findViewById(R.id.download_cancel);
        }
    }


    public String getDownloadPercent(DownloadEntry entry) {
        return setSize(entry.currentLength) + "/" + setSize(entry.totalLength) + " | " + entry.percent + "%";
    }

    public String getStatus(DownloadEntry entry) {

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


    public String setSize(int size) {
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

}

