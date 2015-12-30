package com.koolew.mars.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
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

import com.koolew.mars.MarsApplication;
import com.koolew.mars.R;
import com.koolew.mars.danmaku.DanmakuShowManager;
import com.koolew.mars.danmaku.DanmakuThread;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.utils.Downloader;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    private TextureView mPlaybackTexture;
    private FrameLayout mDanmakuContainer;
    private ImageView mVideoThumb;
    private ProgressBar mProgressBar;

    protected MediaPlayer mMediaPlayer;

    protected boolean isNeedLooping;

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

    public void startPlay() {
        if (mVideoInfo != null || mVideoUrl != null) {
            mProgressBar.setVisibility(VISIBLE);
            postPlaying = true;
            try {
                Downloader.getInstance().download(this, mVideoUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onComplete(String url, String filePath) {
        mProgressBar.setVisibility(INVISIBLE);
        if (url.equals(mVideoUrl)) {
            mVideoPath = filePath;
            if (postPlaying) {
                start();
            }
        }
    }

    class InvisibleThumbThread extends Thread {
        @Override
        public void run() {
            try {
                while (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
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
                    sleep(40);
                }
            } catch (Exception e) {
                if (MarsApplication.DEBUG) {
                    throw new RuntimeException(e);
                }
                else {
                    // Do nothing
                }
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

    protected synchronized void start() {
        if (mMediaPlayer != null || TextUtils.isEmpty(mVideoPath) || isPaused) {
            return;
        }

        mMediaPlayer = generateMediaPlayer();
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(isNeedLooping);
            if (mSurface != null) {
                mMediaPlayer.setSurface(mSurface);
            }
            mMediaPlayer.start();
            if (mDanmakuManager != null) {
                mDanmakuThread = new DanmakuThread((Activity) getContext(), mDanmakuManager,
                        new DanmakuThread.PlayerWrapper() {
                            @Override
                            public long getCurrentPosition() {
                                if (mMediaPlayer == null) {
                                    return 0;
                                }
                                return mMediaPlayer.getCurrentPosition();
                            }

                            @Override
                            public boolean isPlaying() {
                                if (mMediaPlayer == null) {
                                    return false;
                                }
                                return mMediaPlayer.isPlaying();
                            }
                        });
                mDanmakuThread.start();
            }
            new InvisibleThumbThread().start();
        }
    }

    private void startAsync() {
        new Thread() {
            @Override
            public void run() {
                KoolewVideoView.this.start();
            }
        }.start();
    }

    private void pause() {
        isPaused = true;
        stop();
    }

    private void resume() {
        isPaused = false;
        startAsync();
    }

    public void stop() {
        final MediaPlayer mediaPlayer = mMediaPlayer;
        mMediaPlayer = null;
        postPlaying = false;
        if (mediaPlayer != null) {
            new Thread() {
                @Override
                public void run() {
                    doStopMediaPlayer(mediaPlayer);
                }
            }.start();
        }
        //mVideoThumb.setVisibility(VISIBLE);
        mVideoThumb.setAlpha(1.0f);
    }

    private synchronized void doStopMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            if (mDanmakuThread != null) {
                mDanmakuThread.stopDanmaku();
                mDanmakuThread = null;
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
    }

    public void setCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(mSurface);
        }
    }

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
                getVideoView(currentPlayHolder()).resume();
            }
        }

        public void onPause() {
            if (currentPlayHolder() != null) {
                getVideoView(currentPlayHolder()).pause();
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
