package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;

import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.player.ScrollPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by jinchangzhu on 7/24/15.
 */
public abstract class BaseVideoListFragment extends BaseLazyListFragment
        implements VideoCardAdapter.OnDanmakuSendListener, VideoCardAdapter.OnKooClickListener,
        VideoCardAdapter.OnMoreMenuClickListener, ShareVideoWindow.OnVideoOperatedListener {

    public static final String KEY_TOPIC_ID = "topic_id";
    public static final String KEY_TOPIC_TITLE = "topic_title";

    protected static final int REQUEST_CAPTURE = 1;

    protected String mTopicId;
    protected String mTopicTitle;

    protected VideoCardAdapter mAdapter;
    private ScrollPlayer mScrollPlayer;

    private SoundPool mSoundPool;
    private int mKooSound;

    protected TopicInfoInterface mTopicInfoInterface;


    public BaseVideoListFragment() {
        super();
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);

        mListView.setPadding(0, 0, 0, 0);
        mAdapter = useThisAdapter();
        mAdapter.setOnDanmakuSendListener(this);
        mAdapter.setOnKooClickListener(this);
        mAdapter.setOnMoreMenuClickListener(this);
        mScrollPlayer = mAdapter.new TopicScrollPlayer(mListView);
        if (isNeedLoadMore) {
            mListFooter.setup(mListView, mScrollPlayer);
        }
    }

    // Override it if need different adapter.
    protected VideoCardAdapter useThisAdapter() {
        return new DetailTitleVideoCardAdapter(getActivity(), mTopicId);
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();
        mScrollPlayer.onActivityResume();
        mSoundPool = new SoundPool(5, AudioManager.STREAM_RING, 0);
        mKooSound = mSoundPool.load(getActivity(), R.raw.koo, 1);
    }

    @Override
    protected void onPageEnd() {
        super.onPageEnd();
        if (mScrollPlayer != null) {
            mScrollPlayer.onActivityPause();
        }
        if (mSoundPool != null) {
            mSoundPool.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScrollPlayer != null) {
            mScrollPlayer.onActivityDestroy();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mTopicInfoInterface = (TopicInfoInterface) activity;
            mTopicId = mTopicInfoInterface.getTopicId();
        } catch (ClassCastException cce) {
            throw new ClassCastException(activity.toString() + " must implements TopicInfoInterface");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTopicInfoInterface = null;
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

    protected void setupAdapter() {
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
    public void onDanmakuSend(BaseVideoInfo videoInfo) {
        Intent intent = new Intent(getActivity(), SendDanmakuActivity.class);
        intent.putExtra(SendDanmakuActivity.KEY_VIDEO_INFO, videoInfo);
        startActivity(intent);
    }

    @Override
    public void onKooClick(String videoId) {
        mSoundPool.play(mKooSound, 1, 1, 0, 0, 1);
    }

    @Override
    public void onMoreMenuClick(BaseVideoInfo videoInfo) {
        ShareVideoWindow shareVideoWindow =
                new ShareVideoWindow(getActivity(), videoInfo, mTopicTitle);
        shareVideoWindow.setOnVideoOperatedListener(this);
        shareVideoWindow.showAtLocation(getView(), Gravity.TOP, 0, 0);
    }

    public String getTopicId() {
        return mTopicId;
    }

    public String getTopicTitle() {
        return mTopicTitle;
    }

    @Override
    public void onVideoDeleted(String videoId) {
        removeVideo(videoId);
    }

    @Override
    public void onVideoAgainst(String videoId) {
        removeVideo(videoId);
    }

    private void removeVideo(String videoId) {
        mAdapter.removeVideo(videoId);
        mAdapter.notifyDataSetChanged();
        mScrollPlayer.refreshPlayingItem();
    }

    public interface TopicInfoInterface {
        String getTopicId();
    }
}
