package com.koolew.mars;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.utils.VideoLoader;
import com.koolew.mars.utils.WebApiUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class TopicActivity extends ActionBarActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "koolew-TopicActivity";

    public static final String KEY_TOPIC_ID = "topic_id";

    private ListView mListView;

    private VideoCardAdapter mAdapter;
    private RequestQueue mRequestQueue;
    private VideoLoader mVideoLoader;

    private VideoView mVideoView;
    private boolean isFirstPlay = true;
    private int mCurrentVideoIndex = -1;
    private FrameLayout mCurrentVideoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnScrollListener(this);
        mVideoView = new VideoView(this);
        mVideoView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mRequestQueue = Volley.newRequestQueue(this);
        mVideoLoader = new VideoLoader(this);

        Intent intent = getIntent();
        String topicId = intent.getStringExtra(KEY_TOPIC_ID);
        getTopicVideo(topicId);
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
            if (mListView.getChildAt(newVideoIndex) != mCurrentVideoLayout) {
                Log.d(TAG, "newVideoIndex: " + newVideoIndex);
                mCurrentVideoLayout.removeView(mVideoView);
                mCurrentVideoLayout = ((VideoCardAdapter.ViewHolder)
                        mListView.getChildAt(newVideoIndex).getTag()).videoLayout;
                mCurrentVideoLayout.addView(mVideoView);
            }
            //if (mVideoView.getVisibility() == View.INVISIBLE) {
            if (!mVideoView.isPlaying()) {
                Log.d(TAG, "restart play");
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.pause();
                playVideo((String) mCurrentVideoLayout.getTag());
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (isFirstPlay && visibleItemCount > 0) {
            Log.d(TAG, "first play");
            mCurrentVideoLayout = ((VideoCardAdapter.ViewHolder) mListView.getChildAt(0).getTag()).videoLayout;
            mCurrentVideoLayout.addView(mVideoView);
            playVideo((String) mCurrentVideoLayout.getTag());
            isFirstPlay = false;
        }

        if (mVideoView != null && !mVideoView.isPlaying()) { //.getVisibility() == View.INVISIBLE) {
            return;
        }

        int newVideoIndex;
        if (visibleItemCount == 0) {
            newVideoIndex = -1;
        }
        else if (visibleItemCount == 1) {
            newVideoIndex = firstVisibleItem;
        }
        else {
            newVideoIndex = getCurrentVideoIndex();
        }

        if (newVideoIndex != -1 &&
                ((VideoCardAdapter.ViewHolder) mListView.getChildAt(newVideoIndex).getTag())
                        .videoLayout != mCurrentVideoLayout) {
            Log.d(TAG, "Stop play, newVideoIndex: " + newVideoIndex);
            mVideoView.stopPlayback();
            mVideoView.setVisibility(View.INVISIBLE);
            Log.d("stdzhu", "pause");
            long start = System.currentTimeMillis();
            //mVideoView.pause();
            mVideoView.stopPlayback();
            Log.d("stdzhu", "" + (System.currentTimeMillis() - start));
        }
    }

    private void playVideo(String url) {
        mVideoLoader.playVideo(mVideoView, url);
        //mVideoView.setVideoPath("/sdcard/1.mp4");
        //mVideoView.start();
    }

    private void resetVideoIndex(int newVideoIndex) {

        long start = System.currentTimeMillis();
        mCurrentVideoIndex = newVideoIndex;
        if (mCurrentVideoLayout != null) {
            mCurrentVideoLayout.removeView(mVideoView);
        }
        Log.d(TAG, "removeView: " + (System.currentTimeMillis() - start));
        mCurrentVideoLayout = ((VideoCardAdapter.ViewHolder)
                mListView.getChildAt(mCurrentVideoIndex).getTag()).videoLayout;
        mCurrentVideoLayout.addView(mVideoView);
        Log.d(TAG, "addView: " + (System.currentTimeMillis() - start));

        try {
            String newViderUrl = ((JSONObject) mAdapter.getItem(mCurrentVideoIndex))
                    .getString("video_url");
            Log.d(TAG, "Play: " + newViderUrl);
            mVideoView.setVideoURI(Uri.parse("/sdcard/1.mp4"));
            mVideoView.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "switch VideoView: " + (System.currentTimeMillis() - start));
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
            //Log.d(TAG, "child" + i + ". top: " + child.getTop() + ", bottom: " + child.getBottom());
            //Log.d(TAG, "videoLayout" + i + ". top: " + videoLayout.getTop() + ", bottom: " + videoLayout.getBottom());

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
}
