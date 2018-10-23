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
public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.DownloadItemViewHolder> {

    private List<DownloadEntry> mDownloadEntries;
    private DownloadManager mDownloadManager;
    private DecimalFormat df;

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
        holder.downloadPercent.setText(formateDownloadPercent(downloadEntry.currentLength, downloadEntry.totalLength));
        holder.downloadProgressBar.setProgress((int) ((downloadEntry.currentLength / downloadEntry.totalLength) * 100));
        holder.downloadTitle.setText(downloadEntry.name);
        holder.downloadButton.setText(downloadEntry.status.toString());
        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadEntry.status == DownloadEntry.DownloadStatus.idle) {
                    mDownloadManager.add(downloadEntry);
                } else if (downloadEntry.status == DownloadEntry.DownloadStatus.downloading || downloadEntry.status == DownloadEntry.DownloadStatus.waiting) {
                    mDownloadManager.pause(downloadEntry);
                } else if (downloadEntry.status == DownloadEntry.DownloadStatus.paused) {
                    mDownloadManager.resume(downloadEntry);
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
        TextView downloadSpeed;
        Button downloadButton;


        public DownloadItemViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadTitle = itemView.findViewById(R.id.download_title);
            downloadProgressBar = itemView.findViewById(R.id.download_progressbar);
            downloadPercent = itemView.findViewById(R.id.download_parcent);
            downloadSpeed = itemView.findViewById(R.id.download_speed);
            downloadButton = itemView.findViewById(R.id.download_status);
        }
    }

    public String formateDownloadPercent(double currentLength, double totalLength) {
        df = new DecimalFormat("0.0");
        return df.format(currentLength / 1024) + "M" + " | " + df.format(totalLength / 1024) + "M";
    }

}

