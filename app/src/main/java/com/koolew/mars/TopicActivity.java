package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.danmaku.DanmakuItemInfo;
import com.koolew.mars.danmaku.DanmakuShowManager;
import com.koolew.mars.utils.VideoLoader;
import com.koolew.mars.utils.WebApiUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class TopicActivity extends ActionBarActivity implements AbsListView.OnScrollListener,
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener {

    private static final String TAG = "koolew-TopicActivity";

    public static final String KEY_TOPIC_ID = "topic_id";

    private ListView mListView;

    private VideoCardAdapter mAdapter;
    private RequestQueue mRequestQueue;
    private VideoLoader mVideoLoader;

    private SurfaceView mPlaySurface;
    private IjkMediaPlayer mIjkPlayer;
    private String mCurrentVideoPath;
    private String mCurrentVideoUrl;
    private boolean isFirstPlay = true;
    private FrameLayout mCurrentVideoLayout;

    private DanmakuShowManager mDanmakuManager;
    private DanmakuThread mDanmakuThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnScrollListener(this);
        mPlaySurface = new SurfaceView(this);
        mPlaySurface.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mPlaySurface.getHolder().addCallback(mPlaybackSurfaceCallback);

        mRequestQueue = Volley.newRequestQueue(this);
        mVideoLoader = new TopicVideoLoader(this);

        mIjkPlayer = new IjkMediaPlayer();
        mIjkPlayer.setOnPreparedListener(TopicActivity.this);
        mIjkPlayer.setOnCompletionListener(TopicActivity.this);

        Intent intent = getIntent();
        String topicId = intent.getStringExtra(KEY_TOPIC_ID);
        getTopicVideo(topicId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIjkPlayer.start();
    }

    @Override
    protected void onPause() {
        mIjkPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIjkPlayer != null) {
            mIjkPlayer.release();
        }
    }

    private void getTopicVideo(String topicId) {
        String url = WebApiUtil.getTopicVideoFriendUrl(topicId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response: " + response);
                        mAdapter = new VideoCardAdapter(TopicActivity.this);
                        mAdapter.setData(response);
                        mListView.setAdapter(mAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return WebApiUtil.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonObjectRequest);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {
            int newVideoIndex = getCurrentVideoIndex();
            FrameLayout newVideoLayout = ((VideoCardAdapter.ViewHolder)
                    mListView.getChildAt(newVideoIndex).getTag()).videoLayout;
            if (mCurrentVideoLayout.getTag().toString().equals(mCurrentVideoUrl) // Url 还是原来的
                    && mCurrentVideoLayout == newVideoLayout) { // VideoLayout 也是原来的
                if (!mIjkPlayer.isPlaying()) {
                    try {
                        mIjkPlayer.start();
                    }
                    catch (IllegalStateException ise) {
                        Log.e(TAG, "Catch a fatal exception:\n" + ise);
                        return;
                    }
                    mCurrentVideoLayout.findViewById(R.id.video_thumb).setVisibility(View.INVISIBLE);
                }
            }
            else {
                stopDanmaku();
                mCurrentVideoLayout.findViewById(R.id.video_thumb).setVisibility(View.VISIBLE);
                mCurrentVideoLayout.removeView(mPlaySurface);
                mCurrentVideoLayout = ((VideoCardAdapter.ViewHolder)
                        mListView.getChildAt(newVideoIndex).getTag()).videoLayout;
                playVideo((String) mCurrentVideoLayout.getTag());
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        // First play.
        if (isFirstPlay && visibleItemCount > 0) {
            Log.d(TAG, "first play");
            mCurrentVideoLayout = ((VideoCardAdapter.ViewHolder) mListView.getChildAt(0).getTag()).videoLayout;
            //mCurrentVideoLayout.addView(mPlaySurface, 0);
            //mCurrentVideoLayout.findViewById(R.id.video_thumb).setVisibility(View.INVISIBLE);
            playVideo((String) mCurrentVideoLayout.getTag());
            isFirstPlay = false;
        }

        if (mAdapter == null) return;

        int currentPosition = mAdapter.getPositionByVideoLayout(mCurrentVideoLayout);
        Log.d(TAG, "firstVisible: " + firstVisibleItem + ", lastVisible: " + mListView.getLastVisiblePosition() + ", current: " + currentPosition);
        if ((currentPosition < firstVisibleItem || currentPosition > mListView.getLastVisiblePosition())
                && mIjkPlayer != null && mIjkPlayer.isPlaying()) {
            mIjkPlayer.pause();
            mCurrentVideoLayout.findViewById(R.id.video_thumb).setVisibility(View.VISIBLE);
        }
    }

    private void playVideo(String url) {
        mCurrentVideoUrl = url;
        mVideoLoader.loadVideo(mIjkPlayer, url);
    }

    private int getCurrentVideoIndex() {

        int videoIndex = -1;
        int maxVisiblePixel = 0;

        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mListView.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }

            VideoCardAdapter.ViewHolder holder = (VideoCardAdapter.ViewHolder) child.getTag();
            FrameLayout videoLayout = holder.videoLayout;

            int listViewHeight = mListView.getHeight();
            int videoLayoutTopInList = child.getTop() + videoLayout.getTop();
            int videoLayoutBottomInList = child.getTop() + videoLayout.getBottom();
            int videoLayoutHeight = videoLayout.getHeight();
            int visiblePixel = 0;
            if (videoLayoutTopInList < 0) {
                visiblePixel = videoLayoutHeight + videoLayoutTopInList;
            }
            else if (videoLayoutBottomInList > listViewHeight) {
                visiblePixel = videoLayoutHeight - (videoLayoutBottomInList - listViewHeight);
            }
            else {
                visiblePixel = videoLayoutHeight;
            }

            if (visiblePixel > maxVisiblePixel) {
                maxVisiblePixel = visiblePixel;
                videoIndex = i;
            }
        }

        return videoIndex;
    }

    class TopicVideoLoader extends VideoLoader {
        public TopicVideoLoader(Context context) {
            super(context);
        }

        @Override
        public void loadComplete(Object player, final String filePath) {
            mCurrentVideoPath = filePath;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    // reset() 函数执行要 1000+ 毫秒
                    mIjkPlayer.reset();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    // 如果 file != mCurrentVideoPath 表明执行了多次 removeView()、
                    // 多次 loadComplete(), 下面的代码也会执行多次
                    if (filePath == mCurrentVideoPath) {
                        try {
                            mIjkPlayer.setDataSource(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mIjkPlayer.prepareAsync();
                        mCurrentVideoLayout.addView(mPlaySurface, 0);
                    }
                }
            }.execute();
        }
    }

    private SurfaceHolder.Callback mPlaybackSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIjkPlayer.setDisplay(mPlaySurface.getHolder());
        }

        @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        @Override public void surfaceDestroyed(SurfaceHolder holder) {}
    };

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        iMediaPlayer.start();
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mIjkPlayer.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCurrentVideoLayout.findViewById(R.id.video_thumb).setVisibility(View.INVISIBLE);
            }
        }, 120);

        startDanmaku();
    }

    private void startDanmaku() {
        try {
            int currentPosition = mAdapter.getPositionByVideoLayout(mCurrentVideoLayout);
            JSONObject currentItemData = mAdapter.getItemData(currentPosition);
            JSONArray danmakuArray = currentItemData.getJSONArray("comment");
            mDanmakuManager = new DanmakuShowManager(this, mCurrentVideoLayout,
                    DanmakuItemInfo.fromJSONArray(danmakuArray));
            mDanmakuThread = new DanmakuThread();
            mDanmakuThread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void stopDanmaku() {
        if (mDanmakuThread != null) {
            mDanmakuThread.stopDanmaku();
            mDanmakuThread = null;
        }
    }

    class DanmakuThread extends Thread {
        private long lastPosition;
        private boolean runDanmaku;

        @Override
        public void run() {
            lastPosition = 0;
            runDanmaku = true;
            while (runDanmaku) {
                if (mIjkPlayer != null && mIjkPlayer.isPlaying()) {
                    long currentPosition = mIjkPlayer.getCurrentPosition();
                    if (currentPosition < lastPosition) {
                        danmakuManagerClear();
                    }
                    danmakuManagerUpdate((int) currentPosition);
                    lastPosition = currentPosition;
                }

                try {
                    Thread.sleep(40); // 40ms is 1 frame
                } catch (InterruptedException e) {
                }
            }
            danmakuManagerClear();
        }

        void stopDanmaku() {
            runDanmaku = false;
        }

        private void danmakuManagerClear() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDanmakuManager.clear();
                }
            });
        }

        private void danmakuManagerUpdate(final int millis) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDanmakuManager.update(millis);
                }
            });
        }
    }
}
