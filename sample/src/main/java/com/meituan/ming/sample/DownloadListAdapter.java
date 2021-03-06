package com.meituan.ming.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meituan.ming.downloader.DownloadManager;
import com.meituan.ming.downloader.entities.DownloadEntry;
import com.meituan.ming.downloader.utilities.DownloadInfoUtil;

import java.util.List;

/**
 * Created by shiwenming on 2018/10/23.
 */
public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.DownloadItemViewHolder> {

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
        DownloadEntry entry = mDownloadEntries.get(i);
        final DownloadEntry downloadEntry = mDownloadManager.containsDownloadEntry(entry.url) ? mDownloadManager.queryDownloadEntry(entry.url) : entry;
        holder.downloadTitle.setText(downloadEntry.name);
        holder.downloadButton.setText(downloadEntry.status.toString());
        if (downloadEntry.status == DownloadEntry.DownloadStatus.completed) {
            holder.downloadPercent.setText("下载完成");
            holder.downloadProgressBar.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.GONE);
        } else {
            holder.downloadPercent.setText(DownloadInfoUtil.getDownloadInfo(downloadEntry));
            holder.downloadProgressBar.setProgress(DownloadInfoUtil.getDownlaodPercent(downloadEntry));
            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDownloadManager.cancel(downloadEntry);
                }
            });
        }
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
                    case error:
                        mDownloadManager.reDownload(downloadEntry);
                        break;
                }
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

}

