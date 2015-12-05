package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Rect;
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

import com.koolew.mars.R;
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
                        startPlay(holder);
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

        protected void startPlay(RecyclerView.ViewHolder holder) {
            getVideoView(holder).startPlay();
        }

        protected void stopPlay(RecyclerView.ViewHolder holder) {
            getVideoView(holder).stop();
        }

        protected abstract KoolewVideoView getVideoView(RecyclerView.ViewHolder holder);
    }
}
