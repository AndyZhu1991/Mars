package com.koolew.mars;

import android.content.Context;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.webapi.ApiWorker;

/**
 * Created by jinchangzhu on 12/4/15.
 */
public class FeedsMediaFragment extends CommonMediaFragment<FeedsMediaFragment.FeedsMediaAdapter> {

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
    }
}
