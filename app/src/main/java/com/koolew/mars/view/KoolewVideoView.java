package com.koolew.mars.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
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

import com.koolew.mars.MarsApplication;
import com.koolew.mars.R;
import com.koolew.mars.danmaku.DanmakuShowManager;
import com.koolew.mars.danmaku.DanmakuThread;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.utils.Downloader;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 11/4/15.
 */
public class KoolewVideoView extends FrameLayout implements TextureView.SurfaceTextureListener,
        Downloader.LoadListener {

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

    private DanmakuShowManager mDanmakuManager;
    private DanmakuThread mDanmakuThread;

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
        Log.d("stdzhu", "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAsync();
    }

    public void setVideoInfo(String videoUrl, String thumb) {
        stopAsync();
        mVideoInfo = null;
        mVideoUrl = videoUrl;
        ImageLoader.getInstance().displayImage(thumb, mVideoThumb,
                ImageLoaderHelper.topicThumbLoadOptions);
        mDanmakuManager = null;
    }

    public void setVideoInfo(BaseVideoInfo videoInfo) {
        setVideoInfo(videoInfo.getVideoUrl(), videoInfo.getVideoThumb());

        mVideoInfo = videoInfo;
        mDanmakuManager = new DanmakuShowManager(getContext(), mDanmakuContainer,
                videoInfo.getDanmakus());
    }

    public void startPlay() {
        if (mVideoInfo != null || mVideoUrl != null) {
            mProgressBar.setVisibility(VISIBLE);
            Downloader.getInstance().download(this, mVideoUrl);
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
                    mDanmakuThread = new DanmakuThread((Activity) getContext(), mDanmakuManager,
                            new DanmakuThread.PlayerWrapper() {
                                @Override
                                public long getCurrentPosition() {
                                    return mMediaPlayer.getCurrentPosition();
                                }

                                @Override
                                public boolean isPlaying() {
                                    return mMediaPlayer.isPlaying();
                                }
                            });
                    mDanmakuThread.start();
                    new InvisibleThumbThread().start();
                    mStatus = STATUS_PLAYING;
                }
                else {
                    mStatus = STATUS_PAUSED;
                }
            }
        }
    }

    class InvisibleThumbThread extends Thread {
        @Override
        public void run() {
            try {
                while (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    if (mMediaPlayer.getCurrentPosition() > 80) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mVideoThumb.setVisibility(INVISIBLE);
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

    private void pause() {
        if (mStatus == STATUS_PLAYING && mMediaPlayer != null) {
            mMediaPlayer.pause();
            mStatus = STATUS_PAUSED;
        }
    }

    private void resume() {
        if (mStatus == STATUS_PAUSED && mMediaPlayer != null) {
            mMediaPlayer.start();
            mStatus = STATUS_PLAYING;
        }
    }

    public void stop() {
        doStopMediaPlayer();
        mVideoThumb.setVisibility(VISIBLE);
    }

    public void stopAsync() {
        new Thread() {
            @Override
            public void run() {
                doStopMediaPlayer();
            }
        }.start();
        mVideoThumb.setVisibility(VISIBLE);
    }

    private synchronized void doStopMediaPlayer() {
        mStatus = STATUS_IDLE;
        if (mMediaPlayer != null) {
            mDanmakuThread.stopDanmaku();
            mDanmakuThread = null;

            MediaPlayer mediaPlayer = mMediaPlayer;
            mMediaPlayer = null;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(new Surface(surface));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
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
            KoolewVideoView videoView = getVideoView(holder);

            int[] videoViewLocation = new int[2];
            videoView.getLocationInWindow(videoViewLocation);
            int videoViewStart = videoViewLocation[1];
            int videoViewEnd = videoViewStart + videoView.getHeight();

            int[] recyclerViewLocation = new int[2];
            mRecyclerView.getLocationInWindow(recyclerViewLocation);
            int recyclerViewStart = recyclerViewLocation[1];
            int recyclerViewEnd = recyclerViewStart + mRecyclerView.getHeight();

            return getUnionLen(videoViewStart, videoViewEnd, recyclerViewStart, recyclerViewEnd);
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
