package com.koolew.mars;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/27/15.
 */
public class WorldVideoListFragment extends CaptureInviteVideoListFragment {

    private int mCurrentPage;


    public WorldVideoListFragment() {
        super();

        mCurrentPage = 0;
    }

    // This is share in fact
    // They used same layout
    @Override
    public void onDanmakuSend(JSONObject videoItem) {
    }

    @Override
    protected VideoCardAdapter useThisAdapter() {
        return new WorldVideoCardAdapter(getActivity());
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
}
