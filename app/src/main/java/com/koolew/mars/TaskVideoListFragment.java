package com.koolew.mars;

import android.os.Bundle;

import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class TaskVideoListFragment extends FeedsVideoListFragment {

    public static final String KEY_INVITER = "inviter";

    private String mInviter;

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_green);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInviter = getActivity().getIntent().getStringExtra(KEY_INVITER);
    }

    @Override
    protected TopicTitleDetail getTopicDetailFromResponse(JSONObject response) {
        TopicTitleDetail topicTitleDetail = super.getTopicDetailFromResponse(response);

        topicTitleDetail.setType(DetailTitleVideoCardAdapter.TYPE_TASK);

        topicTitleDetail.setInviter(mInviter);

        return topicTitleDetail;
    }

    @Override
    protected DetailTitleVideoCardAdapter.MovieDetailInfo getMovieInfo(JSONObject response) {
        DetailTitleVideoCardAdapter.MovieDetailInfo movieDetailInfo = super.getMovieInfo(response);
        movieDetailInfo.setType(DetailTitleVideoCardAdapter.TYPE_TASK);
        return movieDetailInfo;
    }
}
