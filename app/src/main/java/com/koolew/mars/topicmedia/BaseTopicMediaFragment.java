package com.koolew.mars.topicmedia;

import android.os.Bundle;

import com.koolew.mars.mould.RecyclerListFragmentMould;

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
    protected boolean handleRefreshResult(JSONObject result) {
        boolean ret = mAdapter.handleRefreshResult(result);
        mScrollPlayer.onRefresh();
        return ret;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        return mAdapter.handleLoadMoreResult(result);
    }
}
