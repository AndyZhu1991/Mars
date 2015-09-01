package com.koolew.mars;

import com.koolew.mars.DetailTitleVideoCardAdapter.KooCountUserInfo;
import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/1/15.
 */
public abstract class DetailTitleVideoListFragment extends BaseVideoListFragment {

    @Override
    protected boolean handleRefresh(JSONObject response) {
        setupAdapter();
        TopicTitleDetail topicTitleDetail = getTopicDetailFromResponse(response);
        ((DetailTitleVideoCardAdapter) mAdapter).setTopicTitleDetail(topicTitleDetail);
        return super.handleRefresh(response);
    }

    protected TopicTitleDetail getTopicDetailFromResponse(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                TopicTitleDetail topicTitleDetail = new TopicTitleDetail();

                JSONObject result = response.getJSONObject("result");
                topicTitleDetail.setTitle(result.getString("content"));
                topicTitleDetail.setDescription(result.getString("desc"));
                topicTitleDetail.setVideoCount(result.getInt("video_cnt"));

                JSONArray kooRanks = result.getJSONArray("koo_ranks");
                int length = kooRanks.length();
                KooCountUserInfo[] kooCountUserInfos = new KooCountUserInfo[length];
                for (int i = 0; i < length; i++) {
                    kooCountUserInfos[i] = new KooCountUserInfo(kooRanks.getJSONObject(i));
                }
                topicTitleDetail.setKooRankUsers(kooCountUserInfos);

                return topicTitleDetail;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
