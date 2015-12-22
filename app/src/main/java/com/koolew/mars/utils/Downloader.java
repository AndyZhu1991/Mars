package com.koolew.mars.utils;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.koolew.mars.downloadmanager.DownloadRequest;
import com.koolew.mars.downloadmanager.DownloadStatusListener;
import com.koolew.mars.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinchangzhu on 11/13/15.
 */
public class Downloader implements DownloadStatusListener {

    protected static final int MAX_DOWNLOAD_COUNT = 5;

    protected ThinDownloadManager mDownloadManager;
    protected Map<Integer, DownloadEvent> mDownloads;
    protected Handler mHandler;

    public static Downloader sDownloader;


    public static void init() {
        sDownloader = new Downloader();
    }

    public static Downloader getInstance() {
        return sDownloader;
    }

    private Downloader() {
        mDownloadManager = new ThinDownloadManager(MAX_DOWNLOAD_COUNT);
        mDownloads = new HashMap<>();
        if (Looper.myLooper() != null) {
            mHandler = new Handler();
        }
    }

    public void download(LoadListener listener, String url) {
        download(listener, url, null, false);
    }

    public void download(LoadListener listener, String url, String filePath) {
        download(listener, url, filePath, false);
    }

    public void download(LoadListener listener, String url, String filePath, boolean forceRedownload) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!forceRedownload && new File(url2LocalFile(url, filePath)).exists()) {
            // File already downloaded
            downloadComplete(listener, url, url2LocalFile(url, filePath));
        }
        else {
            // If this url is downloading, update the listener only
            for (int key: mDownloads.keySet()) {
                DownloadEvent downloadEvent = mDownloads.get(key);
                if (downloadEvent.url.equals(url)) {
                    downloadEvent.listener = listener;
                    return;
                }
            }

            if (mDownloads.size() >= MAX_DOWNLOAD_COUNT) {
                long minDownloadedBytes = Long.MAX_VALUE;
                int minDownloadKey = 0;
                for (int key: mDownloads.keySet()) {
                    DownloadEvent downloadEvent = mDownloads.get(key);
                    if (downloadEvent.downloadsBytes < minDownloadedBytes) {
                        minDownloadKey = key;
                    }
                }
                cancelDownload(mDownloads.get(minDownloadKey));
                mDownloads.remove(minDownloadKey);
            }

            DownloadEvent downloadEvent = startDownload(listener, url, filePath);
            mDownloads.put(downloadEvent.id, downloadEvent);
        }
    }

    protected void downloadComplete(final LoadListener listener, final String url,
                                    final String filePath) {
        Runnable completeCallback = new Runnable() {
            @Override
            public void run() {
                listener.onDownloadComplete(url, filePath);
            }
        };
        if (mHandler != null) {
            mHandler.post(completeCallback);
        }
        else {
            new Thread(completeCallback).start();
        }
    }

    private DownloadEvent startDownload(LoadListener listener, String url, String filePath) {
        DownloadEvent event = new DownloadEvent();
        event.listener = listener;
        event.url = url;
        event.localPath = url2LocalFile(url, filePath);

        DownloadRequest request = new DownloadRequest(Uri.parse(event.url))
                .setDestinationURI(Uri.parse(event.localPath))
                .setDownloadListener(this);

        event.id = mDownloadManager.add(request);

        return event;
    }

    private void cancelDownload(DownloadEvent event) {
        mDownloadManager.cancel(event.id);
    }

    private DownloadEvent getDownloadEvent(int id) {
        return mDownloads.get(id);
    }

    private static String url2LocalFile(String url, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        return Utils.getCacheDir() + stringToMD5(url) + getUrlExtName(url);
    }

    private static String getUrlExtName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        Uri uri = Uri.parse(url);
        String lastPathSeg = uri.getLastPathSegment();
        if (TextUtils.isEmpty(lastPathSeg)) {
            return "";
        }

        int lastDotIndex = lastPathSeg.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return "";
        }

        return lastPathSeg.substring(lastDotIndex);
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

    @Override
    public void onDownloadComplete(int id) {
        DownloadEvent downloadEvent = getDownloadEvent(id);
        if (downloadEvent != null) {
            downloadEvent.listener.onDownloadComplete(downloadEvent.url, downloadEvent.localPath);
        }
    }

    @Override
    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
        DownloadEvent downloadEvent = getDownloadEvent(id);
        if (downloadEvent != null) {
            downloadEvent.listener.onDownloadFailed(errorCode, errorMessage);
        }
    }

    @Override
    public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
        DownloadEvent downloadEvent = getDownloadEvent(id);
        if (downloadEvent != null) {
            downloadEvent.totalBytes = totalBytes;
            downloadEvent.downloadsBytes = downloadedBytes;
            downloadEvent.listener.onDownloadProgress(totalBytes, downloadedBytes, progress);
        }
    }

    public interface LoadListener {
        void onDownloadComplete(String url, String filePath);
        void onDownloadProgress(long totalBytes, long downloadedBytes, int progress);
        void onDownloadFailed(int errorCode, String errorMessage);
    }

    protected class DownloadEvent {
        public int id;
        public String url;
        public String localPath;
        public long totalBytes;
        public long downloadsBytes;
        public LoadListener listener;
    }

    public static class DownloadFuture implements LoadListener {
        private String filePath = null;

        public synchronized String download() {
            try {
                wait(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return filePath;
        }

        @Override
        public synchronized void onDownloadComplete(String url, String filePath) {
            this.filePath = filePath;
            notifyAll();
        }

        @Override
        public void onDownloadProgress(long totalBytes, long downloadedBytes, int progress) {
        }

        @Override
        public synchronized void onDownloadFailed(int errorCode, String errorMessage) {
            notifyAll();
        }
    }
}
