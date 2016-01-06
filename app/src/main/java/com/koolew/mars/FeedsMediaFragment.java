package com.koolew.mars;

import android.content.Context;

import com.koolew.mars.topicmedia.MediaItem;
import com.koolew.mars.topicmedia.VideoItem;
import com.koolew.mars.webapi.UrlHelper;

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
    protected boolean handleRefreshResult(JSONObject result) {
        boolean ret = super.handleRefreshResult(result);
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
    protected String getRefreshRequestUrl() {
        return UrlHelper.getTopicVideoFriendUrl(mTopicId);
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getTopicVideoFriendUrl(mTopicId, mAdapter.getLastUpdateTime());
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
