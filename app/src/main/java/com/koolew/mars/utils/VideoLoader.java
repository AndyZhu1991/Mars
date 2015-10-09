package com.koolew.mars.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
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
public class VideoLoader implements DownloadStatusListener {

    protected static final String TAG = "koolew-VideoLoader";

    protected static final int MAX_DOWNLOAD_COUNT = 3;

    protected Context mContext;

    protected Object mPlayer;

    protected ThinDownloadManager mDownloadManager;
    protected DownloadEvent mCurrentDownload;
    protected List<DownloadEvent> mOtherDownloads;

    protected LoadListener mLoadListener;


    public VideoLoader(Context context) {
        mContext = context;
        mDownloadManager = new ThinDownloadManager(MAX_DOWNLOAD_COUNT);
        mOtherDownloads = new LinkedList<DownloadEvent>();
    }

    public void loadVideo(Object player, String url) {

        if (TextUtils.isEmpty(url)) {
            return;
        }

        mPlayer = player;
        if (new File(url2LocalFile(url)).exists()) {
            // TODO: Play this video
            loadComplete(mPlayer, url, url2LocalFile(url));
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
                    if (mOtherDownloads.get(i).progress < minPercentage) {
                        minPercentage = mOtherDownloads.get(i).progress;
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

    public void setLoadListener(LoadListener loadListener) {
        mLoadListener = loadListener;
    }

    private DownloadEvent startDownload(String url) {
        DownloadEvent event = new DownloadEvent();
        event.url = url;
        event.localPath = url2LocalFile(url);
        event.progress = 0.0f;

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
        return Utils.getCacheDir(mContext) + stringToMD5(url) + ".mp4";
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
            loadComplete(mPlayer,mCurrentDownload.url, mCurrentDownload.localPath);
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
            event.progress = 1.0f * downloadedBytes / totalBytes;
            if (mLoadListener != null) {
                mLoadListener.onLoadProgress(event.url, event.progress);
            }
        }
    }

    protected void loadComplete(Object player, String url, String filePath) {
        if (mLoadListener != null) {
            mLoadListener.onLoadComplete(player, url, filePath);
        }
    }

    public interface LoadListener {
        void onLoadComplete(Object player, String url, String filePath);
        void onLoadProgress(String url, float progress);
    }

    protected class DownloadEvent {
        public int id;
        public String url;
        public String localPath;
        public float progress;
    }
}
