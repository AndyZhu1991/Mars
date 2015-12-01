package com.koolew.mars;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/27/15.
 */
public class FeedsVideoListFragment extends DetailTitleVideoListFragment {

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
    protected TopicTitleDetail getTopicDetailFromResponse(JSONObject response) {
        TopicTitleDetail topicTitleDetail = super.getTopicDetailFromResponse(response);

        topicTitleDetail.setType(DetailTitleVideoCardAdapter.TYPE_FEEDS);

        return topicTitleDetail;
    }

    @Override
    protected DetailTitleVideoCardAdapter.MovieDetailInfo getMovieInfo(JSONObject response) {
        DetailTitleVideoCardAdapter.MovieDetailInfo movieDetailInfo = super.getMovieInfo(response);
        movieDetailInfo.setType(DetailTitleVideoCardAdapter.TYPE_FEEDS);
        return movieDetailInfo;
    }
}
