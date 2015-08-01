package com.koolew.mars;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/24/15.
 */
public class BaseVideoListFragment extends BaseListFragment
        implements VideoCardAdapter.OnDanmakuSendListener, VideoCardAdapter.OnKooClickListener {

    public static final String KEY_TOPIC_ID = "topic_id";
    public static final String KEY_TOPIC_TITLE = "topic_title";

    protected static final int REQUEST_CAPTURE = 1;

    protected String mTopicId;
    protected String mTopicTitle;

    protected VideoCardAdapter mAdapter;
    private ScrollPlayer mScrollPlayer;

    private SoundPool mSoundPool;
    private int mKooSound;


    public BaseVideoListFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        mTopicId = intent.getStringExtra(KEY_TOPIC_ID);
        mTopicTitle = intent.getStringExtra(KEY_TOPIC_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = useThisAdapter();
        mAdapter.setTopicTitle(mTopicTitle);
        mAdapter.setOnDanmakuSendListener(this);
        mAdapter.setOnKooClickListener(this);
        mScrollPlayer = new VideoCardAdapter.TopicScrollPlayer(mAdapter, mListView);
        if (isNeedLoadMore) {
            mListFooter.setup(mListView, mScrollPlayer);
        }

        return root;
    }

    // Override it if need different adapter.
    protected VideoCardAdapter useThisAdapter() {
        return new VideoCardAdapter(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mScrollPlayer.onActivityResume();
        mSoundPool = new SoundPool(5, AudioManager.STREAM_RING, 0);
        mKooSound = mSoundPool.load(getActivity(), R.raw.koo, 1);
    }

    @Override
    public void onPause() {
        mScrollPlayer.onActivityPause();
        mSoundPool.release();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScrollPlayer.onActivityDestroy();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_orange);
    }

    protected JSONArray getVideosFromResponse(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                return response.getJSONObject("result").getJSONArray("videos");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getTopicTitleFromResponse(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                return response.getJSONObject("result").getString("content");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupAdapter() {
        if (mListView.getAdapter() == null) {
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        setupAdapter();

        JSONArray videos = getVideosFromResponse(response);
        String topicTitle = getTopicTitleFromResponse(response);
        if (!TextUtils.isEmpty(topicTitle)) {
            mTopicTitle = topicTitle;
            mAdapter.setTopicTitle(mTopicTitle);
        }

        if (videos == null || videos.length() == 0) {
            return false;
        }

        mAdapter.setData(videos);
        mAdapter.notifyDataSetChanged();
        mScrollPlayer.onListRefresh();

        return true;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONArray videos = getVideosFromResponse(response);
        if (videos == null || videos.length() == 0) {
            return false;
        }

        mAdapter.addData(videos);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestFeedsTopicVideo(mTopicId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestFeedsTopicVideo(
                mTopicId, mAdapter.getOldestVideoTime(), mLoadMoreListener, null);
    }

    @Override
    public void onDanmakuSend(JSONObject videoItem) {
        Intent intent = new Intent(getActivity(), SendDanmakuActivity.class);
        intent.putExtra(SendDanmakuActivity.KEY_VIDEO_JSON, videoItem.toString());
        startActivity(intent);
    }

    @Override
    public void onKooClick(String videoId) {
        mSoundPool.play(mKooSound, 1, 1, 0, 0, 1);
    }

    public String getTopicId() {
        return mTopicId;
    }

    public String getTopicTitle() {
        return mTopicTitle;
    }

    protected void capture() {
        Intent intent = new Intent(getActivity(), VideoShootActivity.class);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, getTopicId());
        startActivity(intent);
    }

    protected void invite() {
        Intent intent = new Intent(getActivity(), InviteActivity.class);
        intent.putExtra(InviteActivity.KEY_TOPIC_ID, getTopicId());
        intent.putExtra(InviteActivity.KEY_TITLE, getTopicTitle());
        startActivity(intent);
    }
}
