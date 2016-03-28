package com.koolew.mars.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.koolew.android.downloadmanager.Downloader;
import com.koolew.mars.MarsApplication;
import com.koolew.mars.R;
import com.koolew.mars.danmaku.DanmakuShowManager;
import com.koolew.mars.danmaku.DanmakuThread;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseVideoInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by jinchangzhu on 11/4/15.
 */
public class KoolewVideoView extends FrameLayout implements TextureView.SurfaceTextureListener,
        Downloader.LoadListener, MediaPlayer.OnCompletionListener {

    private static final int VIDEO_WIDTH_RATIO = 4;
    private static final int VIDEO_HEIGHT_RATIO = 3;

    private static final int THUMB_HIDE_DURATION = 200; // ms

    private static final int MAX_WAIT_TIME_IN_MS = 2000;
    private static Handler mediaPlayerWorkHandler;
    static {
        HandlerThread handlerThread = new HandlerThread(KoolewVideoView.class.getSimpleName());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mediaPlayerWorkHandler = new Handler(looper);
    }

    private TextureView mPlaybackTexture;
    private FrameLayout mDanmakuContainer;
    private ImageView mVideoThumb;
    private ProgressBar mProgressBar;

    protected MediaPlayer mMediaPlayer;

    protected boolean isNeedLooping;
    protected boolean isNeedSound;

    private boolean isPaused = false;
    private boolean postPlaying = false;

    private BaseVideoInfo mVideoInfo;
    private String mVideoUrl;
    private String mVideoPath;

    private DanmakuShowManager mDanmakuManager;
    private DanmakuThread mDanmakuThread;

    private Surface mSurface;

    private MediaPlayer.OnCompletionListener mCompletionListener;

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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KoolewVideoView, 0, 0);
        isNeedLooping = a.getBoolean(R.styleable.KoolewVideoView_looping, true);
        isNeedSound = a.getBoolean(R.styleable.KoolewVideoView_needSound, true);
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
            finalHeight = finalWidth * VIDEO_HEIGHT_RATIO / VIDEO_WIDTH_RATIO;
        }
        else if (heightSpecMode == MeasureSpec.EXACTLY) {
            finalHeight = heightSpecSize;
            finalWidth = finalHeight * VIDEO_WIDTH_RATIO / VIDEO_HEIGHT_RATIO;
        }
        else {
            throw new RuntimeException("KoolewVideoView layout param error!");
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVideoPath = null;
        stop();
    }

    public void setVideoInfo(String videoUrl, String thumb) {
        stop();
        mVideoInfo = null;
        mVideoUrl = videoUrl;
        ImageLoader.getInstance().displayImage(thumb, mVideoThumb,
                ImageLoaderHelper.topicThumbLoadOptions);
        mDanmakuManager = null;
        mVideoPath = null;
        mProgressBar.setVisibility(INVISIBLE);
    }

    public void setVideoInfo(BaseVideoInfo videoInfo) {
        setVideoInfo(videoInfo.getVideoUrl(), videoInfo.getVideoThumb());

        mVideoInfo = videoInfo;
        List danmakus = videoInfo.getDanmakus();
        if (danmakus == null || danmakus.size() == 0) {
            mDanmakuManager = null;
        }
        else {
            mDanmakuManager = new DanmakuShowManager(getContext(), mDanmakuContainer,
                    videoInfo.getDanmakus());
        }
    }

    protected void postOnPlayerThread(Runnable runnable) {
        mediaPlayerWorkHandler.post(runnable);
    }

    public void startPlay() {
        if (mVideoInfo != null || mVideoUrl != null) {
            mProgressBar.setVisibility(VISIBLE);
            //mediaPlayerWorkHandler.post(startPlayRunnable);
            startPlayRunnable.run();
        }
    }

    private Runnable startPlayRunnable = new Runnable() {
        @Override
        public void run() {
            postPlaying = true;
            try {
                File file = Downloader.getInstance().tryToGetLocalFile(mVideoUrl);
                if (file == null) {
                    Downloader.getInstance().download(KoolewVideoView.this, mVideoUrl);
                }
                else {
                    onComplete(mVideoUrl, file.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void onComplete(String url, String filePath) {
        mProgressBar.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(INVISIBLE);
            }
        });
        if (url.equals(mVideoUrl)) {
            mVideoPath = filePath;
            if (postPlaying) {
                start();
            }
        }
    }

    protected MediaPlayer generateMediaPlayer() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), Uri.parse("file://" + mVideoPath));
        if (mediaPlayer != null && mCompletionListener != null) {
            mediaPlayer.setOnCompletionListener(this);
        }
        return mediaPlayer;
    }

    protected void start() {
        mediaPlayerWorkHandler.post(startPlayerRunnable);
    }

    private Runnable startPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null || TextUtils.isEmpty(mVideoPath) || isPaused) {
                return;
            }

            mMediaPlayer = generateMediaPlayer();
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setLooping(isNeedLooping);
            if (!isNeedSound) {
                mMediaPlayer.setVolume(0, 0);
            }
            if (mSurface != null) {
                mMediaPlayer.setSurface(mSurface);
            }
            mMediaPlayer.start();

            try {
                //         max try times
                //                  ⬇️
                for (int i = 0; i < 50; i++) {
                    if (mMediaPlayer.getCurrentPosition() > 40) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                ObjectAnimator.ofFloat(mVideoThumb, "alpha", 1.0f, 1.0f, 0.0f)
                                        .setDuration(THUMB_HIDE_DURATION)
                                        .start();
                            }
                        });
                        break;
                    }
                    Thread.sleep(40);
                }
            } catch (Exception e) {
                if (MarsApplication.DEBUG) {
                    throw new RuntimeException(e);
                } else {
                    // Do nothing
                }
            }

            if (mDanmakuManager != null) {
                mDanmakuThread = new DanmakuThread((Activity) getContext(), mDanmakuManager,
                        new DanmakuThread.PlayerWrapper() {
                            @Override
                            public long getCurrentPosition() {
                                return KoolewVideoView.this.getCurrentPosition();
                            }

                            @Override
                            public boolean isPlaying() {
                                return KoolewVideoView.this.isPlaying();
                            }
                        });
                mDanmakuThread.start();
            }
        }
    };

    public long getCurrentPosition() {
        getCurrentPositionRunnable.currentPosition = 0;
        try {
            synchronized (getCurrentPositionRunnable) {
                mediaPlayerWorkHandler.post(getCurrentPositionRunnable);
                getCurrentPositionRunnable.wait(MAX_WAIT_TIME_IN_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getCurrentPositionRunnable.currentPosition;
    }

    private final GetCurrentPositionRunnable getCurrentPositionRunnable = new GetCurrentPositionRunnable();
    private class GetCurrentPositionRunnable implements Runnable {
        private long currentPosition;

        @Override
        public synchronized void run() {
            currentPosition = null == mMediaPlayer ? 0 : mMediaPlayer.getCurrentPosition();
            notify();
        }
    }

    public boolean isPlaying() {
        try {
            return null != mMediaPlayer && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    private void onPause() {
        isPaused = true;
        stop();
    }

    private void onResume() {
        isPaused = false;
        startPlay();
    }

    public void stop() {
        mediaPlayerWorkHandler.post(stopPlayRunnable);
    }

    private Runnable stopPlayRunnable = new Runnable() {
        @Override
        public void run() {
            postPlaying = false;
            if (mMediaPlayer != null) {
                if (mDanmakuThread != null) {
                    mDanmakuThread.stopDanmaku();
                    mDanmakuThread = null;
                }

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            //mVideoThumb.setVisibility(VISIBLE);
            post(new Runnable() {
                @Override
                public void run() {
                    mVideoThumb.setAlpha(1.0f);
                }
            });
        }
    };

    public void setCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        mediaPlayerWorkHandler.post(setSurfaceRunnable);
    }

    private Runnable setSurfaceRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mSurface != null) {
                mMediaPlayer.setSurface(mSurface);
            }
        }
    };

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        stop();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onDownloadComplete(String url, String filePath) {
        onComplete(url, filePath);
    }

    @Override
    public void onDownloadProgress(long totalBytes, long downloadedBytes, int progress) {
    }

    @Override
    public void onDownloadFailed(int errorCode, String errorMessage) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mCompletionListener != null) {
            mCompletionListener.onCompletion(mp);
        }
    }


    public static abstract class ScrollPlayer {

        private RecyclerView mRecyclerView;
        private HolderWrapper mCurrentHolderWrapper = new HolderWrapper();

        public ScrollPlayer(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
            recyclerView.addOnScrollListener(mScrollListener);
        }

        private RecyclerView.ViewHolder currentPlayHolder() {
            return mCurrentHolderWrapper.holder;
        }

        public void onResume() {
            if (currentPlayHolder() != null) {
                getVideoView(currentPlayHolder()).onResume();
            }
        }

        public void onPause() {
            if (currentPlayHolder() != null) {
                getVideoView(currentPlayHolder()).onPause();
            }
        }

        public void onRefresh() {
            mCurrentHolderWrapper.setHolder(null);
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    calcNewPlayHolder();
                }
            }, 500);
        }

        private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    calcNewPlayHolder();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (currentPlayHolder() != null) {
                    float currentVisiblePercentage = getVideoViewVisibleHeightPercentage(currentPlayHolder());
                    if (currentVisiblePercentage < 0.3) {
                        getVideoView(currentPlayHolder()).stop();
                    }
                }
            }
        };

        private void calcNewPlayHolder() {
            RecyclerView.ViewHolder holder = getCurrentItemHolder();
            if (holder == null || holder.getAdapterPosition() != mCurrentHolderWrapper.position) {
                if (currentPlayHolder() != null) {
                    stopPlay(currentPlayHolder());
                }
                mCurrentHolderWrapper.setHolder(holder);
                if (currentPlayHolder() != null) {
                    startPlay(currentPlayHolder());
                }
            }
        }

        private RecyclerView.ViewHolder getCurrentItemHolder() {
            RecyclerView.ViewHolder currentHolder = null;
            float maxVisibleHeightPercentage = 0.5f;

            int count = mRecyclerView.getChildCount();
            for (int i = 0; i < count; i++) {
                RecyclerView.ViewHolder holder = mRecyclerView.
                        getChildViewHolder(mRecyclerView.getChildAt(i));
                if (holder.itemView.getVisibility() != View.VISIBLE || !isPlayable(holder)) {
                    continue;
                }

                float visibleHeightPercentage = getVideoViewVisibleHeightPercentage(holder);
                if (visibleHeightPercentage > maxVisibleHeightPercentage) {
                    maxVisibleHeightPercentage = visibleHeightPercentage;
                    currentHolder = holder;
                }
            }

            return currentHolder;
        }

        private float getVideoViewVisibleHeightPercentage(RecyclerView.ViewHolder holder) {
            KoolewVideoView videoView = getVideoView(holder);

            int[] videoViewLocation = new int[2];
            videoView.getLocationInWindow(videoViewLocation);
            int videoViewStart = videoViewLocation[1];
            int videoViewEnd = videoViewStart + videoView.getHeight();

            int[] recyclerViewLocation = new int[2];
            mRecyclerView.getLocationInWindow(recyclerViewLocation);
            int recyclerViewStart = recyclerViewLocation[1];
            int recyclerViewEnd = recyclerViewStart + mRecyclerView.getHeight();

            int videoViewVisibleHeight = getUnionLen(
                    videoViewStart, videoViewEnd, recyclerViewStart, recyclerViewEnd);

            return 1.0f * videoViewVisibleHeight / videoView.getHeight();
        }

        private int getUnionLen(int start1, int end1, int start2, int end2) {
            int superLineStart = Math.min(start1, start2);
            int superLineEnd = Math.max(end1, end2);

            return (end1 - start1) + (end2 - start2) - (superLineEnd - superLineStart);
        }

        protected boolean isPlayable(RecyclerView.ViewHolder holder) {
            return getVideoView(holder) != null;
        }

        protected void startPlay(RecyclerView.ViewHolder holder) {
            getVideoView(holder).startPlay();
        }

        protected void stopPlay(RecyclerView.ViewHolder holder) {
            getVideoView(holder).stop();
        }

        protected abstract KoolewVideoView getVideoView(RecyclerView.ViewHolder holder);


        private static class HolderWrapper {
            private RecyclerView.ViewHolder holder;
            private int position;

            private HolderWrapper() {
                reset();
            }

            private void reset() {
                holder = null;
                position = -1;
            }

            private void setHolder(RecyclerView.ViewHolder holder) {
                if (holder == null) {
                    reset();
                    return;
                }
                this.holder = holder;
                this.position = holder.getAdapterPosition();
            }
        }
    }
}
