package com.koolew.mars;

import android.os.Bundle;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/30/15.
 */
public class CheckDanmakuFragment extends BaseVideoListFragment {

    public static final String KEY_VIDEO_ID = "video id";

    private String mVideoId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoId = getActivity().getIntent().getStringExtra(KEY_VIDEO_ID);
    }

    public CheckDanmakuFragment() {
        super();

        isNeedLoadMore = false;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestSingleVideo(mVideoId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return null;
    }

    @Override
    protected JSONArray getVideosFromResponse(JSONObject response) {
        JSONArray videos = new JSONArray();
        try {
            if (response.getInt("code") == 0) {
                JSONObject video = response.getJSONObject("result").getJSONObject("video");
                videos.put(video);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videos;
    }
}
