package com.koolew.mars.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.koolew.mars.downloadmanager.DownloadRequest;
import com.koolew.mars.downloadmanager.DownloadStatusListener;
import com.koolew.mars.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinchangzhu on 6/5/15.
 */
public abstract class VideoLoader implements DownloadStatusListener {

    private static final String TAG = "koolew-VideoLoader";

    private static final int MAX_DOWNLOAD_COUNT = 3;

    private Context mContext;

    private Object mPlayer;

    private ThinDownloadManager mDownloadManager;
    private DownloadEvent mCurrentDownload;
    private List<DownloadEvent> mOtherDownloads;


    public VideoLoader(Context context) {
        mContext = context;
        mDownloadManager = new ThinDownloadManager(MAX_DOWNLOAD_COUNT);
        mOtherDownloads = new LinkedList<DownloadEvent>();
    }

    public void loadVideo(Object player, String url) {

        mPlayer = player;
        if (new File(url2LocalFile(url)).exists()) {
            // TODO: Play this video
            loadComplete(mPlayer, url2LocalFile(url));
        }
        else {
            // TODO: Download this video
            if (mCurrentDownload != null && mCurrentDownload.url.equals(url)) {
                return;
            }

            int downloadListSize = mOtherDownloads.size();
            if (downloadListSize >= MAX_DOWNLOAD_COUNT - 1) {
                float minPercentage = 0.0f;
                int minPercentageIndex = 0;
                for (int i = 0; i < downloadListSize; i++) {
                    if (mOtherDownloads.get(i).percentage < minPercentage) {
                        minPercentage = mOtherDownloads.get(i).percentage;
                        minPercentageIndex = i;
                    }
                }
                cancelDownload(mOtherDownloads.get(minPercentageIndex));
                mOtherDownloads.remove(minPercentageIndex);
            }

            if (mCurrentDownload != null) {
                mOtherDownloads.add(mCurrentDownload);
            }

            mCurrentDownload = startDownload(url);
        }
    }

    public abstract void loadComplete(Object player, String filePath);

    private DownloadEvent startDownload(String url) {
        DownloadEvent event = new DownloadEvent();
        event.url = url;
        event.localPath = url2LocalFile(url);
        event.percentage = 0.0f;

        DownloadRequest request = new DownloadRequest(Uri.parse(event.url))
                .setDestinationURI(Uri.parse(event.localPath))
                .setDownloadListener(this);

        event.id = mDownloadManager.add(request);

        return event;
    }

    private void cancelDownload(DownloadEvent event) {
        mDownloadManager.cancel(event.id);
    }

    private String url2LocalFile(String url) {
        return mContext.getExternalCacheDir() + stringToMD5(url) + ".mp4";
    }

    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    private DownloadEvent findEventFromOtherList(int id) {
        for (DownloadEvent e: mOtherDownloads) {
            if (e.id == id) {
                return e;
            }
        }
        return null;
    }

    @Override
    public void onDownloadComplete(int id) {
        if (id == mCurrentDownload.id) {
            Log.d(TAG, mCurrentDownload.url + " completed");
            loadComplete(mPlayer, mCurrentDownload.localPath);
        }
        else {
            for (DownloadEvent event: mOtherDownloads) {
                if (event.id == id) {
                    mOtherDownloads.remove(event);
                    break;
                }
            }
            DownloadEvent event = findEventFromOtherList(id);
            if (event != null) {
                Log.d(TAG, event.url + " completed");
                mOtherDownloads.remove(event);
            }
        }
    }

    @Override
    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
        Log.d(TAG, String.format("Download failed, id: %d, %s", id, errorMessage));
    }

    @Override
    public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {

        DownloadEvent event;
        if (mCurrentDownload.id == id) {
            event = mCurrentDownload;
        }
        else {
            event = findEventFromOtherList(id);
        }

        if (event != null) {
            event.percentage = 1.0f * downloadedBytes / totalBytes;
        }
        Log.d(TAG, event.url + ": " + event.percentage);
    }

    class DownloadEvent {
        int id;
        String url;
        String localPath;
        float percentage;
    }
}
