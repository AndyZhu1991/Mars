package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/24/15.
 */
public class TopicListVideoFragment extends BaseListFragment
        implements VideoCardAdapter.OnDanmakuSendListener {

    public static final String KEY_TOPIC_ID = "topic_id";

    private String mTopicId;

    private VideoCardAdapter mAdapter;
    private ScrollPlayer mScrollPlayer;


    public TopicListVideoFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTopicId = getActivity().getIntent().getStringExtra(KEY_TOPIC_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = new VideoCardAdapter(getActivity());
        mAdapter.setOnDanmakuSendListener(this);
        mScrollPlayer = new VideoCardAdapter.TopicScrollPlayer(mAdapter, mListView);
        mListFooter.setup(mListView, mScrollPlayer);
        mListView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScrollPlayer.onActivityResume();
    }

    @Override
    public void onPause() {
        mScrollPlayer.onActivityPause();
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

    @Override
    protected void handleRefresh(JSONObject response) {
        mAdapter.setData(response);
        mAdapter.notifyDataSetChanged();
        mScrollPlayer.onListRefresh();
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        int loadedCount = mAdapter.addData(response);
        mAdapter.notifyDataSetChanged();
        return loadedCount > 0;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestTopicVideo(mTopicId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestTopicVideo(
                mTopicId, mAdapter.getOldestVideoTime(), mLoadMoreListener, null);
    }

    @Override
    public void onDanmakuSend(JSONObject videoItem) {
        Intent intent = new Intent(getActivity(), SendDanmakuActivity.class);
        intent.putExtra(SendDanmakuActivity.KEY_VIDEO_JSON, videoItem.toString());
        startActivity(intent);
    }

    public String getTopicId() {
        return mTopicId;
    }
}
