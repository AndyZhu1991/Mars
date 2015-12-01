package com.koolew.mars;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/27/15.
 */
public class WorldVideoListFragment extends DetailTitleVideoListFragment {

    private int mCurrentPage;


    public WorldVideoListFragment() {
        super();

        mCurrentPage = 0;
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected VideoCardAdapter useThisAdapter() {
        return new WorldVideoCardAdapter(getActivity(), mTopicId);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        mCurrentPage++;
        return ApiWorker.getInstance().requestWorldTopicVideo(
                mTopicId, mCurrentPage, mLoadMoreListener, null);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        mCurrentPage = 0;
        return ApiWorker.getInstance().requestWorldTopicVideo(
                mTopicId, mCurrentPage, mRefreshListener, null);
    }

    @Override
    protected TopicTitleDetail getTopicDetailFromResponse(JSONObject response) {
        TopicTitleDetail topicTitleDetail = super.getTopicDetailFromResponse(response);

        topicTitleDetail.setType(DetailTitleVideoCardAdapter.TYPE_WORLD);

        return topicTitleDetail;
    }

    @Override
    protected DetailTitleVideoCardAdapter.MovieDetailInfo getMovieInfo(JSONObject response) {
        DetailTitleVideoCardAdapter.MovieDetailInfo movieDetailInfo = super.getMovieInfo(response);
        movieDetailInfo.setType(DetailTitleVideoCardAdapter.TYPE_WORLD);
        return movieDetailInfo;
    }
}
