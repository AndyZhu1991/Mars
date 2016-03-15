package com.koolew.android.downloadmanager;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.koolew.android.downloadmanager.disklrucache.DiskLruCache;
import com.koolew.android.utils.Utils;

import java.io.File;
import java.io.IOException;
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
    private static final int JOURNAL_COUNT_PER_FLUSH = 10;
    private static final int DISK_CACHE_VERSION = 1;
    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024;

    protected ThinDownloadManager mDownloadManager;
    protected DiskLruCache mDiskCache;
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
        try {
            mDiskCache = openDiskCache();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mDownloads = new HashMap<>();
        if (Looper.myLooper() != null) {
            mHandler = new Handler();
        }
    }

    private DiskLruCache openDiskCache() throws IOException {
        return DiskLruCache.open(new File(Utils.getCacheDir() + "lru/"),
                DISK_CACHE_VERSION, 1, DISK_CACHE_SIZE);
    }

    public File tryToGetLocalFile(String url) {
        String cacheKey = getFileKey(url);

        DiskLruCache.Snapshot snapshot;
        try {
            snapshot = getSnapshot(cacheKey);
        } catch (IOException e) {
            return null;
        }
        if (snapshot != null) {
            // File already downloaded
            return snapshot.getFile(0);
        }
        return null;
    }

    public void download(LoadListener listener, String url) throws IOException {
        download(listener, url, null, false);
    }

    public void download(LoadListener listener, String url, String filePath) throws IOException {
        download(listener, url, filePath, false);
    }

    public void download(LoadListener listener, String url, String filePath, boolean forceRedownload) throws IOException {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        String cacheKey = getFileKey(url);
        if (forceRedownload) {
            mDiskCache.remove(cacheKey);
        }

        DiskLruCache.Snapshot snapshot = getSnapshot(cacheKey);
        if (snapshot != null) {
            // File already downloaded
            downloadComplete(listener, url, snapshot.getFile(0).getAbsolutePath());
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

            DownloadEvent downloadEvent = startDownload(listener, url);
            if (downloadEvent != null) {
                mDownloads.put(downloadEvent.id, downloadEvent);
            }
        }
    }

    public void download(LoadListener listener, String url, DownloadDestination destination) {
        DownloadEvent downloadEvent = startDownload(listener, url, destination);
        mDownloads.put(downloadEvent.id, downloadEvent);
    }

    public void cleanCache() {
        DiskLruCache diskLruCache = mDiskCache;
        mDiskCache = null;

        cancelAllDownload();
        try {
            Thread.sleep(1000); // Waiting for cancel all downloads
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            diskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mDiskCache = openDiskCache();
        } catch (IOException e) {
            e.printStackTrace();
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

    private DownloadEvent startDownload(LoadListener listener, String url) throws IOException {
        DiskLruCache.Editor editor = mDiskCache.edit(getFileKey(url));
        if (editor != null) {
            return startDownload(listener, url, new DownloadDestination.
                    DiskLruCacheEditorDestination(editor));
        }
        else {
            return null;
        }
    }

    private DownloadEvent startDownload(LoadListener listener, String url, DownloadDestination destination) {
        DownloadEvent event = new DownloadEvent();
        event.listener = listener;
        event.url = url;
        event.destination = destination;

        DownloadRequest request = new DownloadRequest(Uri.parse(event.url))
                .setDestination(event.destination)
                .setDownloadListener(this);

        event.id = mDownloadManager.add(request);

        return event;
    }

    private void cancelAllDownload() {
        for (int id: mDownloads.keySet()) {
            mDownloadManager.cancel(id);
        }
        mDownloads.clear();
    }

    private void cancelDownload(DownloadEvent event) {
        mDownloadManager.cancel(event.id);
    }

    private DownloadEvent getDownloadEvent(int id) {
        return mDownloads.get(id);
    }

    private DiskLruCache.Snapshot getSnapshot(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
        if (snapshot != null) {
            incDiskCacheJournal();
        }
        return snapshot;
    }

    private int diskCacheJournal = 0;
    private void incDiskCacheJournal() throws IOException {
        diskCacheJournal++;
        if (diskCacheJournal % JOURNAL_COUNT_PER_FLUSH == 0) {
            diskCacheJournal = 0;
            mDiskCache.flush();
        }
    }

    private static String getFileKey(String url) {
        String extName = getUrlExtName(url);
        String md5String = stringToMD5(url);
        if (TextUtils.isEmpty(extName)) {
            return md5String;
        }
        else {
            return md5String + extName;
        }
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
            DownloadDestination destination = downloadEvent.destination;
            String localPath = null;
            if (destination instanceof DownloadDestination.FileDestination) {
                localPath = ((DownloadDestination.FileDestination) destination).getFilePath();
            }
            else if (destination instanceof DownloadDestination.DiskLruCacheEditorDestination) {
                try {
                    ((DownloadDestination.DiskLruCacheEditorDestination) destination)
                            .getEditor().commit();
                    DiskLruCache.Snapshot snapshot = getSnapshot(getFileKey(downloadEvent.url));
                    if (snapshot != null) {
                        localPath = snapshot.getFile(0).getAbsolutePath();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (localPath != null) {
                downloadEvent.listener.onDownloadComplete(downloadEvent.url, localPath);
            }
        }
    }

    @Override
    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
        DownloadEvent downloadEvent = getDownloadEvent(id);
        if (downloadEvent != null) {
            if (downloadEvent.destination instanceof DownloadDestination.DiskLruCacheEditorDestination) {
                try {
                    ((DownloadDestination.DiskLruCacheEditorDestination) downloadEvent.destination)
                            .getEditor().abort();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        public DownloadDestination destination;
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
