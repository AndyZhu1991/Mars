package com.koolew.mars;

import android.app.Activity;
import android.text.TextUtils;

import com.koolew.mars.DetailTitleVideoCardAdapter.TopicTitleDetail;
import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/1/15.
 */
public abstract class DetailTitleVideoListFragment extends BaseVideoListFragment
        implements DetailTitleVideoCardAdapter.OnTitleVideoListener {

    protected OnTopicCategoryListener onTopicCategoryListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnTopicCategoryListener)) {
            throw new RuntimeException("This activity must implements OnTopicInfoUpdateListener");
        }
        onTopicCategoryListener = (OnTopicCategoryListener) activity;
    }

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
                onTopicCategoryListener.onCategoryDetermined(category, topicTitleDetail.getTitle());
            }
            else if (category.equals("movie")) {
                DetailTitleVideoCardAdapter.MovieDetailInfo movieDetailInfo = getMovieInfo(response);
                adapter.setMovieDetail(movieDetailInfo);
                onTopicCategoryListener.onCategoryDetermined(category, movieDetailInfo);
            }
        }

        return super.handleRefresh(response);
    }

    protected TopicTitleDetail getTopicDetailFromResponse(JSONObject response) {
        TopicTitleDetail topicTitleDetail = new TopicTitleDetail();
        try {
            if (response.getInt("code") == 0) {
                JSONObject result = response.getJSONObject("result");
                topicTitleDetail.setTitle(JsonUtil.getStringIfHas(result, "content"));
                topicTitleDetail.setDescription(JsonUtil.getStringIfHas(result, "desc"));
                topicTitleDetail.setVideoCount(JsonUtil.getIntIfHas(result, "video_cnt"));

                JSONArray kooRanks = JsonUtil.getJSONArrayIfHas(result, "koo_ranks", new JSONArray());
                int length = kooRanks.length();
                KooCountUserInfo[] kooCountUserInfos = new KooCountUserInfo[length];
                for (int i = 0; i < length; i++) {
                    kooCountUserInfos[i] = new KooCountUserInfo(kooRanks.getJSONObject(i));
                }
                topicTitleDetail.setKooRankUsers(kooCountUserInfos);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return topicTitleDetail;
    }

    protected DetailTitleVideoCardAdapter.MovieDetailInfo getMovieInfo(JSONObject response) {
        try {
            return new DetailTitleVideoCardAdapter.MovieDetailInfo(response.getJSONObject("result"));
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

    public interface OnTopicCategoryListener {
        void onCategoryDetermined(String category, Object extra);
    }
}
