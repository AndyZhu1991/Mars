package com.koolew.mars.topicmedia;

import android.os.Bundle;

import com.koolew.mars.MarsApplication;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 12/8/15.
 */
public abstract class BaseTopicMediaFragment<CMA extends UniversalMediaAdapter>
        extends RecyclerListFragmentMould<CMA> {

    protected ScrollPlayer mScrollPlayer;


    @Override
    protected void onPageEnd() {
        super.onPageEnd();

        mScrollPlayer.onPause();
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();

        mScrollPlayer.onResume();
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);

        mScrollPlayer = new ScrollPlayer(mRecyclerView);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONObject result = getResultFromResponse(response);
        boolean ret = mAdapter.handleRefreshResult(result);
        mScrollPlayer.onRefresh();
        return ret;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONObject result = getResultFromResponse(response);
        return mAdapter.handleLoadMoreResult(result);
    }

    public static JSONObject getResultFromResponse(JSONObject response) {
        JSONObject result = null;
        try {
            int code = response.getInt("code");
            if (code != 0) {
                if (MarsApplication.DEBUG) {
                    throw new RuntimeException("Api response error code: " + code);
                }
            }
            else {
                result = JsonUtil.getJSONObjectIfHas(response, "result", null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (result == null) {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("It's no result in response?!");
            }
            else {
                // No result in response -_-!
            }
        }

        return result;
    }
}
