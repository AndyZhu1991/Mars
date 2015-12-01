package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class UserVideoListFragment extends DetailTitleVideoListFragment {

    public static final String KEY_UID = "uid";

    protected String mUid;
    protected int mUserType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUid = getActivity().getIntent().getStringExtra(KEY_UID);
    }

    @Override
    protected VideoCardAdapter useThisAdapter() {
        return new UserTopicAdapter(getActivity());
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestUserTopic(mUid, mTopicId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestUserTopic(mUid, mTopicId,
                mAdapter.getOldestVideoTime(), mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            mUserType = response.getJSONObject("result").getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.handleRefresh(response);
    }

    @Override
    protected TopicTitleDetail getTopicDetailFromResponse(JSONObject response) {
        TopicTitleDetail topicTitleDetail = new TopicTitleDetail();
        topicTitleDetail.setType(DetailTitleVideoCardAdapter.TYPE_USER);
        try {
            JSONObject result = response.getJSONObject("result");
            topicTitleDetail.setKooRankUsers(null);
            topicTitleDetail.setTitle(result.getString("content"));
            topicTitleDetail.setDescription(result.getString("desc"));
            topicTitleDetail.setVideoCount(result.getInt("video_cnt"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return topicTitleDetail;
    }

    @Override
    protected DetailTitleVideoCardAdapter.MovieDetailInfo getMovieInfo(JSONObject response) {
        DetailTitleVideoCardAdapter.MovieDetailInfo movieDetailInfo = super.getMovieInfo(response);
        movieDetailInfo.setType(DetailTitleVideoCardAdapter.TYPE_USER);
        return movieDetailInfo;
    }

    class UserTopicAdapter extends DetailTitleVideoCardAdapter {

        public UserTopicAdapter(Context context) {
            super(context, UserVideoListFragment.this.mTopicId);
        }
    }
}
