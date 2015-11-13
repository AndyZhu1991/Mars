package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.koolew.mars.R;
import com.koolew.mars.downloadmanager.DownloadRequest;
import com.koolew.mars.downloadmanager.DownloadStatusListener;
import com.koolew.mars.downloadmanager.ThinDownloadManager;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinchangzhu on 11/4/15.
 */
public class KoolewVideoView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private static final int VIDEO_WIDTH_RATIO = 4;
    private static final int VIDEO_HEIGHT_RATIO = 3;

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_POST_PLAY = 1;
    private static final int STATUS_PLAYING = 2;
    private static final int STATUS_PAUSED = 3;

    private TextureView mPlaybackTexture;
    private FrameLayout mDanmakuContainer;
    private ImageView mVideoThumb;
    private ProgressBar mProgressBar;

    private int mStatus = STATUS_IDLE;

    private MediaPlayer mMediaPlayer;

    private BaseVideoInfo mVideoInfo;
    private String mVideoUrl;

    public KoolewVideoView(Context context) {
        this(context, null);
    }

    public KoolewVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View content = LayoutInflater.from(getContext()).inflate(R.layout.koolew_video_view, null);
        mPlaybackTexture = (TextureView) content.findViewById(R.id.playback_texture);
        mPlaybackTexture.setSurfaceTextureListener(this);
        mDanmakuContainer = (FrameLayout) content.findViewById(R.id.danmaku_container);
        mVideoThumb = (ImageView) content.findViewById(R.id.video_thumb);
        mProgressBar = (ProgressBar) content.findViewById(R.id.progress);
        addView(content);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int finalWidth;
        int finalHeight;
        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
            finalWidth = widthSpecSize;
            finalHeight = heightSpecSize;
        }
        else if (widthSpecMode == MeasureSpec.EXACTLY) {
            finalWidth = widthSpecSize;
            int wantedHeight = finalWidth * VIDEO_HEIGHT_RATIO / VIDEO_WIDTH_RATIO;
            finalHeight = getBestSize(heightSpecMode, heightSpecSize, wantedHeight);
        }
        else if (heightSpecMode == MeasureSpec.EXACTLY) {
            finalHeight = heightSpecSize;
            int wantedWidth = finalHeight * VIDEO_WIDTH_RATIO / VIDEO_HEIGHT_RATIO;
            finalWidth = getBestSize(widthSpecMode, widthSpecSize, wantedWidth);
        }
        else {
            throw new RuntimeException("KoolewVideoView layout param error!");
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

    private static int getBestSize(int specMode, int specSize, int wantedSize) {
        if (specMode == MeasureSpec.EXACTLY || specSize < wantedSize) {
            return specSize;
        }
        return wantedSize;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("stdzhu", "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    public void setVideoInfo(String videoUrl, String thumb) {
        stop();
        mVideoInfo = null;
        mVideoUrl = videoUrl;
        ImageLoader.getInstance().displayImage(thumb, mVideoThumb,
                ImageLoaderHelper.topicThumbLoadOptions);
    }

    public void setVideoInfo(BaseVideoInfo videoInfo) {
        mVideoUrl = null;
        mVideoInfo = videoInfo;
        setVideoInfo(videoInfo.getVideoUrl(), videoInfo.getVideoThumb());
    }

    public void startPlay(VideoDownloader downloader) {
        if (mVideoInfo != null || mVideoUrl != null) {
            mProgressBar.setVisibility(VISIBLE);
            downloader.download(mVideoUrl, this);
            mStatus = STATUS_POST_PLAY;
        }
    }

    public void onComplete(String url, String filePath) {
        if (url.equals(mVideoUrl) && mStatus == STATUS_POST_PLAY) {
            mMediaPlayer = MediaPlayer.create(getContext(), Uri.parse("file://" + filePath));
            if (mMediaPlayer != null) {
                mMediaPlayer.setLooping(true);
                mProgressBar.setVisibility(INVISIBLE);
                if (mPlaybackTexture.getSurfaceTexture() != null) {
                    mMediaPlayer.setSurface(new Surface(mPlaybackTexture.getSurfaceTexture()));
                    mMediaPlayer.start();
                    mVideoThumb.setVisibility(INVISIBLE);
                    mStatus = STATUS_PLAYING;
                }
                else {
                    mStatus = STATUS_PAUSED;
                }
            }
        }
    }

    private void pause() {
        if (mStatus == STATUS_PLAYING && mMediaPlayer != null) {
            mMediaPlayer.pause();
            mStatus = STATUS_PAUSED;
        }
    }

    private void resume() {
        if (mStatus == STATUS_PAUSED && mMediaPlayer != null) {
            mMediaPlayer.setSurface(new Surface(mPlaybackTexture.getSurfaceTexture()));
            mMediaPlayer.start();
            mStatus = STATUS_PLAYING;
        }
    }

    public void stop() {
        mStatus = STATUS_IDLE;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mVideoThumb.setVisibility(VISIBLE);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        resume();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        pause();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    public static abstract class ScrollPlayer implements VideoDownloader {

        private RecyclerView mRecyclerView;
        private RecyclerView.ViewHolder mCurrentPlayHolder;

        public ScrollPlayer(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
            recyclerView.addOnScrollListener(mScrollListener);
        }

        private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.ViewHolder holder = getCurrentItemHolder();
                    if (holder != mCurrentPlayHolder) {
                        if (mCurrentPlayHolder != null) {
                            stopPlay(mCurrentPlayHolder);
                        }
                        mCurrentPlayHolder = holder;
                        startPlay(holder, ScrollPlayer.this);
                    }
                }
            }
        };

        private RecyclerView.ViewHolder getCurrentItemHolder() {
            RecyclerView.ViewHolder currentHolder = null;
            int maxVisibleHeight = 0;

            int count = mRecyclerView.getChildCount();
            for (int i = 0; i < count; i++) {
                RecyclerView.ViewHolder holder = mRecyclerView.
                        getChildViewHolder(mRecyclerView.getChildAt(i));
                if (holder.itemView.getVisibility() != View.VISIBLE || !isPlayable(holder)) {
                    continue;
                }

                int visibleHeight = getVideoViewVisibleHeight(holder);
                if (visibleHeight > maxVisibleHeight) {
                    maxVisibleHeight = visibleHeight;
                    currentHolder = holder;
                }
            }

            return currentHolder;
        }

        private int getVideoViewVisibleHeight(RecyclerView.ViewHolder holder) {
            Rect videoViewRect = getVideoViewRect(holder);
            int videoViewStartY = videoViewRect.top;
            int videoViewHeight = videoViewRect.height();
            int recyclerHeight = mRecyclerView.getHeight();

            if (videoViewStartY < 0) {
                return videoViewHeight + videoViewStartY;
            }
            else if (videoViewStartY + videoViewHeight > recyclerHeight) {
                return recyclerHeight - videoViewStartY;
            }
            else {
                return videoViewHeight;
            }
        }

        protected boolean isPlayable(RecyclerView.ViewHolder holder) {
            return getVideoView(holder) != null;
        }

        protected Rect getVideoViewRect(RecyclerView.ViewHolder holder) {
            KoolewVideoView videoView = getVideoView(holder);
            return new Rect(videoView.getLeft(), videoView.getTop(),
                    videoView.getRight(), videoView.getBottom());
        }

        protected void startPlay(RecyclerView.ViewHolder holder, VideoDownloader downloader) {
            getVideoView(holder).startPlay(downloader);
        }

        protected void stopPlay(RecyclerView.ViewHolder holder) {
            getVideoView(holder).stop();
        }

        protected abstract KoolewVideoView getVideoView(RecyclerView.ViewHolder holder);

        @Override
        public void download(String url, KoolewVideoView videoView) {

        }
    }

    public static class VideoDownloaderImpl implements VideoDownloader, DownloadStatusListener {

        private static final int MAX_DOWNLOAD_COUNT = 4;
        private ThinDownloadManager mDownloadManager = new ThinDownloadManager(MAX_DOWNLOAD_COUNT);
        private List<DownloadEvent> mDownloadEvents = new LinkedList<>();
        private String mCacheDir;
        private Handler mHandler;

        public VideoDownloaderImpl(Context context) {
            mCacheDir = Utils.getCacheDir(context);
            mHandler = new Handler();
        }

        @Override
        public void download(final String url, final KoolewVideoView videoView) {
            final String localPath = url2LocalPath(url);
            if (new File(localPath).exists()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        videoView.onComplete(url, localPath);
                    }
                });
            }
            else {
                if (mDownloadEvents.size() >= MAX_DOWNLOAD_COUNT) {
                    float minProgress = 1.0f;
                    DownloadEvent minProgressDownload = null;
                    for (DownloadEvent event : mDownloadEvents) {
                        if (event.progress < minProgress) {
                            minProgressDownload = event;
                        }
                    }
                    mDownloadManager.cancel(minProgressDownload.id);
                    mDownloadEvents.remove(minProgressDownload);
                }

                startDownload(url, videoView);
            }
        }

        private void startDownload(String url, KoolewVideoView videoView) {
            DownloadEvent event = new DownloadEvent();
            event.url = url;
            event.filePath = url2LocalPath(url);
            event.progress = 0.0f;
            event.videoView = videoView;
            DownloadRequest request = new DownloadRequest(Uri.parse(event.url))
                    .setDestinationURI(Uri.parse(event.filePath))
                    .setDownloadListener(this);
            event.id = mDownloadManager.add(request);
            mDownloadEvents.add(event);
        }

        private DownloadEvent findEventById(int id) {
            for (DownloadEvent event: mDownloadEvents) {
                if (event.id == id) {
                    return event;
                }
            }
            return null;
        }

        private String url2LocalPath(String url) {
            return mCacheDir + url.substring(url.lastIndexOf('/'));
        }

        @Override
        public void onDownloadComplete(int id) {
            DownloadEvent event = findEventById(id);
            if (event != null) {
                mDownloadEvents.remove(event);
                event.videoView.onComplete(event.url, event.filePath);
            }
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
            DownloadEvent event = findEventById(id);
            if (event != null) {
                mDownloadEvents.remove(event);
            }
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
            DownloadEvent event = findEventById(id);
            if (event != null) {
                event.progress = 1.0f * downloadedBytes / totalBytes;
            }
        }

        public static class DownloadEvent {
            private int id;
            private String url;
            private String filePath;
            private float progress;
            private KoolewVideoView videoView;
        }
    }

    public interface VideoDownloader {
        void download(String url, KoolewVideoView videoView);
    }
}
