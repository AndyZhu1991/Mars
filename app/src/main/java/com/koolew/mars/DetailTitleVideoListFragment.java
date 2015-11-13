package com.koolew.mars;

import android.text.TextUtils;

import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/1/15.
 */
public abstract class DetailTitleVideoListFragment extends BaseVideoListFragment
        implements DetailTitleVideoCardAdapter.OnTitleVideoListener {

    @Override
    protected void onPauseLazy() {
        super.onPauseLazy();
        ((DetailTitleVideoCardAdapter) mAdapter).stopTitleVideoByOther();
    }

    @Override
    protected void setupAdapter() {
        ((DetailTitleVideoCardAdapter) mAdapter).setTitleVideoListener(this);
        super.setupAdapter();
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        setupAdapter();

        String category = null;
        try {
            category = response.getJSONObject("result").getString("category");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DetailTitleVideoCardAdapter adapter = (DetailTitleVideoCardAdapter) mAdapter;
        adapter.setCategory(category);
        if (!TextUtils.isEmpty(category)) {
            if (category.equals("video")) {
                TopicTitleDetail topicTitleDetail = getTopicDetailFromResponse(response);
                adapter.setTopicTitleDetail(topicTitleDetail);
            }
            else if (category.equals("movie")) {
                DetailTitleVideoCardAdapter.MovieInfo movieInfo = getMovieInfo(response);
                adapter.setMovieInfo(movieInfo);
            }
        }

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

    protected DetailTitleVideoCardAdapter.MovieInfo getMovieInfo(JSONObject response) {
        try {
            return new DetailTitleVideoCardAdapter.MovieInfo(response.getJSONObject("result"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onTitleVideoStart() {
        pauseScrollPlayer();
    }

    @Override
    public void onTitleVideoStop() {
        resumeScrollPlayer();
    }
}
