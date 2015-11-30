package com.koolew.mars.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import com.koolew.mars.danmaku.DanmakuItemInfo;
import com.koolew.mars.danmaku.DanmakuShowManager;
import com.koolew.mars.danmaku.DanmakuThread;
import com.koolew.mars.utils.VideoLoader;
import com.koolew.mars.video.VideoRepeater;
import com.koolew.mars.video.VideoUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by jinchangzhu on 7/23/15.
 */
public abstract class ScrollPlayer implements AbsListView.OnScrollListener,
        VideoLoader.LoadListener, TextureView.SurfaceTextureListener {

    private static final int STATE_NORMAL = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_DESTROYED = 3;

    private int mState = STATE_PAUSED;

    private ListView mListView;
    private Context mContext;

    private VideoLoader mVideoLoader;

    private TextureView mPlaySurface;
    private IjkRecyclerPlayer mRecyclerPlayer;

    private DanmakuThread mDanmakuThread;

    private View mCurrentItem;

    private boolean isFirstPlay;

    private boolean isNeedDanmaku;
    private boolean isNeedSound;


    public ScrollPlayer(ListView listView) {
        mListView = listView;
        listView.setOnScrollListener(this);
        mContext = listView.getContext();
        mVideoLoader = new RepeatVideoLoader(mContext);
        mVideoLoader.setLoadListener(this);

        mPlaySurface = new TextureView(mContext);
        mPlaySurface.setSurfaceTextureListener(this);
        mPlaySurface.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRecyclerPlayer = new IjkRecyclerPlayer();

        isFirstPlay = true;

        isNeedDanmaku = true;
        isNeedSound = true;
    }

    /**
     *  Invoke it when the ListView refresh.
     */
    public void onListRefresh() {
        stopCurrentPlay();
        isFirstPlay = true;
    }

    /**
     *  Invoke it in Activity's onPause() to pause the player.
     */
    public void onActivityPause() {
        mState = STATE_PAUSED;
        if (mCurrentItem != null) {
            mRecyclerPlayer.pause();
        }
    }

    /**
     *  Invoke it in Activity's onResume() to resume the player.
     */
    public void onActivityResume() {
        mState = STATE_NORMAL;
        if (mCurrentItem != null) {
            mRecyclerPlayer.resume();
        }
    }

    /**
     *  Invoke it in Activity's onDestroy() to release resources.
     */
    public void onActivityDestroy() {
        mState = STATE_DESTROYED;
        mCurrentItem = null;
        mRecyclerPlayer.destory();
    }

    public void setNeedDanmaku(boolean isNeedDanmaku) {
        this.isNeedDanmaku = isNeedDanmaku;
    }

    public void setNeedSound(boolean isNeedSound) {
        this.isNeedSound = isNeedSound;
    }

    private View getCurrentItemView() {

        View currentItemView = null;
        int maxVisibleHeight = 0;

        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mListView.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || !isItemView(child)) {
                continue;
            }

            int visibleHeight = getSurfaceVisibleHeight(child);
            int surfaceHeight = getSurfaceHeight(child);
            if (visibleHeight >= surfaceHeight / 2 && visibleHeight > maxVisibleHeight) {
                maxVisibleHeight = visibleHeight;
                currentItemView = child;
            }
        }

        return currentItemView;
    }

    private int getSurfaceStartY(View itemView) {
        return itemView.getTop() + getSurfaceContainer(itemView).getTop();
    }

    private int getSurfaceVisibleHeight(View itemView) {
        int surfaceStartY = getSurfaceStartY(itemView);
        int surfaceHeight = getSurfaceHeight(itemView);
        int listHeight = mListView.getHeight();

        if (surfaceStartY < 0) {
            return surfaceHeight + surfaceStartY;
        }
        else if (surfaceStartY + surfaceHeight > listHeight) {
            return listHeight - surfaceStartY;
        }
        else {
            return surfaceHeight;
        }
    }

    private int getSurfaceHeight(View itemView) {
        return getSurfaceContainer(itemView).getHeight();
    }

    private void stopCurrentPlay() {
        if (mCurrentItem != null) {
            if (mDanmakuThread != null) {
                mDanmakuThread.stopDanmaku();
            }
            getThumbImage(mCurrentItem).setVisibility(View.VISIBLE);
            getSurfaceContainer(mCurrentItem).removeView(mPlaySurface);

            mCurrentItem = null;

            mRecyclerPlayer.stop();
        }
    }

    private void setupCurrentPlay() {
        if (mCurrentItem != null) {
            onStartPlay();
            getProgressView(mCurrentItem).setVisibility(View.VISIBLE);
            mVideoLoader.loadVideo(null, getVideoUrl(mCurrentItem));
        }
    }

    protected void onStartPlay() {
    }

    @Override
    public void onLoadComplete(Object player, String url, String filePath) {
        if (mCurrentItem != null && getVideoUrl(mCurrentItem).equals(url)) {
            if (TextUtils.isEmpty(filePath)) {
                getProgressView(mCurrentItem).setVisibility(View.INVISIBLE);
                return;
            }

            getSurfaceContainer(mCurrentItem).addView(mPlaySurface, 0);
            mRecyclerPlayer.play(filePath);

            if (isNeedDanmaku) {
                DanmakuShowManager danmakuManager = new DanmakuShowManager(mContext,
                        getDanmakuContainer(mCurrentItem), getDanmakuList(mCurrentItem));
                int duration = 0;
                if (VideoRepeater.isRepeatedVideo(filePath)) {
                    duration = VideoRepeater.getVideoRealDuration(filePath);
                }
                mDanmakuThread = new DanmakuThread((Activity) mContext, danmakuManager,
                        new DanmakuThread.PlayerWrapper() {
                            @Override
                            public long getCurrentPosition() {
                                return mRecyclerPlayer.getCurrentPosition();
                            }

                            @Override
                            public boolean isPlaying() {
                                return mRecyclerPlayer.isPlaying();
                            }
                        },
                        duration == 0 ? DanmakuThread.IGNORE_REAL_VIDEO_LEN : duration);
                mDanmakuThread.start();
            }

            getProgressView(mCurrentItem).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoadProgress(String url, float progress) {

    }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mCurrentItem == null) {
            if (visibleItemCount > 0 && isFirstPlay) {
                // ListView init
                isFirstPlay = false;
                onScrollStateChanged(listView, SCROLL_STATE_IDLE);
            }
            return;
        }

        if (getSurfaceVisibleHeight(mCurrentItem) <= 0) {
            stopCurrentPlay();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            View newItem = getCurrentItemView();
            if (newItem != mCurrentItem) {
                if (mCurrentItem != null) {
                    stopCurrentPlay();
                }
                mCurrentItem = newItem;
                setupCurrentPlay();
            }
        }
    }

    public void refreshPlayingItem() {
        if (mCurrentItem != null) {
            stopCurrentPlay();
        }
        mCurrentItem = getCurrentItemView();
        setupCurrentPlay();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mRecyclerPlayer.setSurface(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public abstract boolean isItemView(View childView);

    public abstract ViewGroup getSurfaceContainer(View itemView);

    public abstract ViewGroup getDanmakuContainer(View itemView);

    public abstract ArrayList<DanmakuItemInfo> getDanmakuList(View itemView);

    public abstract ImageView getThumbImage(View itemView);

    public abstract View getProgressView(View itemView);

    public abstract String getVideoUrl(View itemView);


    class IjkRecyclerPlayer implements IMediaPlayer.OnCompletionListener {

        private IjkMediaPlayer mCurrentPlayer;

        private Stack<IjkMediaPlayer> mPlayerPool;

        public IjkRecyclerPlayer() {
            mPlayerPool = new Stack<>();
        }

        public void pause() {
            if (mCurrentPlayer != null) {
                mCurrentPlayer.pause();
            }
        }

        public void resume() {
            if (mCurrentPlayer != null) {
                mCurrentPlayer.start();
            }
        }

        public boolean isPlaying() {
            if (mCurrentPlayer != null) {
                return mCurrentPlayer.isPlaying();
            }
            return false;
        }

        public long getCurrentPosition() {
            if (mCurrentPlayer != null) {
                return mCurrentPlayer.getCurrentPosition();
            }
            return 0;
        }

        public void destory() {
            new Thread() {
                @Override
                public void run() {
                    if (mCurrentPlayer != null) {
                        synchronized (mCurrentPlayer) {
                            if (mCurrentPlayer.isPlaying()) {
                                mCurrentPlayer.stop();
                            }
                            mCurrentPlayer.release();
                        }
                    }
                    synchronized (mPlayerPool) {
                        while (mPlayerPool.size() > 0) {
                            IjkMediaPlayer player = mPlayerPool.pop();
                            player.release();
                        }
                    }
                }
            }.start();
        }

        public void play(String filePath) {
            if (mCurrentPlayer != null) {
                throw new IllegalStateException("Should recyclePlayer before preparePlayer");
            }

            mCurrentPlayer = generatePlayer();

            doPlay(mCurrentPlayer, filePath);
        }

        private void doPlay(final IjkMediaPlayer player, final String filePath) {
            new Thread() {
                @Override
                public void run() {
                    synchronized (player) {
                        try {
                            player.setDataSource(filePath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        prepareSync(player);
                        startPlayWithThumb(player);
                    }
                }
            }.start();
        }

        private void startPlayWithThumb(final IjkMediaPlayer player) {
            player.start();

            new Thread() {
                @Override
                public void run() {
                    while (player != null && player.isPlaying()) {
                        if (player.getCurrentPosition() > 40) { // One more frame.
                            mListView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCurrentItem != null) {
                                        getThumbImage(mCurrentItem).setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                            break; // break while
                        }

                        try {
                            Thread.sleep(40); // 40ms == 1frame, 25fps
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mState != STATE_NORMAL) {
                        try {
                            player.pause();
                        }
                        catch (IllegalStateException ise) {
                        }
                    }
                }
            }.start();
        }

        public void stop() {
            if (mCurrentPlayer != null) {
                IjkMediaPlayer player = mCurrentPlayer;
                mCurrentPlayer = null;
                recyclePlayer(player);
            }
        }

        public void setSurface(Surface surface) {
            if (mCurrentPlayer != null) {
                mCurrentPlayer.setSurface(surface);
            }
        }

        private void recyclePlayer(final IjkMediaPlayer player) {
            new Thread() {
                @Override
                public void run() {
                    synchronized (player) {
                        if (player.isPlaying()) {
                            player.stop();
                        }
                        player.reset();
                        synchronized (mPlayerPool) {
                            mPlayerPool.push(player);
                        }
                    }
                }
            }.start();
        }

        private IjkMediaPlayer generatePlayer() {
            IjkMediaPlayer player;
            if (mPlayerPool.size() == 0) {
                player = new IjkMediaPlayer();
            }
            else {
                player = mPlayerPool.pop();
            }
            player.setOnCompletionListener(this);
            if (!isNeedSound) {
                player.setVolume(0.0f, 0.0f);
            }
            return player;
        }

        private void prepareSync(IjkMediaPlayer player) {
            final Object prepareLock = new Object();
            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    synchronized (prepareLock) {
                        prepareLock.notifyAll();
                    }
                }
            });
            player.prepareAsync();

            try {
                synchronized (prepareLock) {
                    prepareLock.wait(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            iMediaPlayer.start();
        }
    }

    class RepeatVideoLoader extends VideoLoader {

        public RepeatVideoLoader(Context context) {
            super(context);
        }

        @Override
        protected void loadComplete(Object player, String url, String filePath) {
            if (VideoUtil.isMp4FileBroken(url, filePath)) {
                new File(filePath).delete();
                super.loadComplete(player, url, null);
                return;
            }

            if (VideoRepeater.isNeedRepeat(filePath)) {
                new VideoRepeatTask(player, url, filePath).execute();
            }
            else {
                super.loadComplete(player, url, filePath);
            }
        }

        class VideoRepeatTask extends AsyncTask<Void, Void, String> {
            private Object player;
            private String url;
            private String filePath;

            public VideoRepeatTask(Object player, String url, String filePath) {
                this.player = player;
                this.url = url;
                this.filePath = filePath;
            }

            @Override
            protected String doInBackground(Void... params) {
                VideoRepeater.repeatVideo(filePath);
                return VideoRepeater.getFinalRepeatedVideoPath(filePath);
            }

            @Override
            protected void onPostExecute(String finalVideoPath) {
                RepeatVideoLoader.super.loadComplete(player, url, finalVideoPath);
            }
        }
    }
}
