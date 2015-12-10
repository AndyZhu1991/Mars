package com.koolew.mars;

import android.content.Context;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.topicmedia.MediaItem;
import com.koolew.mars.topicmedia.VideoItem;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 12/4/15.
 */
public class FeedsMediaFragment extends CommonMediaFragment<FeedsMediaFragment.FeedsMediaAdapter> {

    private String mTargetVideoId;

    public FeedsMediaFragment(String topicId, String targetVideoId) {
        super(topicId);

        mTargetVideoId = targetVideoId;
    }

    public FeedsMediaFragment(String topicId) {
        super(topicId);
    }

    @Override
    protected FeedsMediaAdapter useThisAdapter() {
        return new FeedsMediaAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_orange);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        boolean ret = super.handleRefresh(response);
        if (mTargetVideoId != null) {
            int targetPosition = mAdapter.findTargetVideoPosition(mTargetVideoId);
            if (targetPosition >= 0) {
                mRecyclerView.scrollToPosition(targetPosition);
            }
            mTargetVideoId = null;
        }
        return ret;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestFeedsTopicVideo(mTopicId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestFeedsTopicVideo(
                mTopicId, mAdapter.getLastUpdateTime(), mLoadMoreListener, null);
    }


    public static class FeedsMediaAdapter extends CommonMediaFragment.CommonMediaAdapter {

        public FeedsMediaAdapter(Context context) {
            super(context);
        }

        private int findTargetVideoPosition(String targetVideoId) {
            for (int i = 0; i < mData.size(); i++) {
                MediaItem mediaItem = mData.get(i);
                if (mediaItem instanceof VideoItem) {
                    if (((VideoItem) mediaItem).getVideoInfo().getVideoId().equals(targetVideoId)) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }
}
