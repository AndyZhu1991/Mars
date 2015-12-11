package com.koolew.mars;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KoolewInvolveFragment extends RecyclerListFragmentMould<TimelineAdapter> {

    private int mCurrentPage = 0;


    public KoolewInvolveFragment() {
        super();
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    protected TimelineAdapter useThisAdapter() {
        TimelineAdapter adapter = new TimelineAdapter(getActivity());
        adapter.setIsSelf();
        return adapter;
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_deep_orange);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        mCurrentPage = 0;
        return ApiWorker.getInstance().requestInvolve(mCurrentPage, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        mCurrentPage++;
        return ApiWorker.getInstance().requestInvolve(mCurrentPage, mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        return mAdapter.setItems(getInvolveTopics(response)) > 0;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        return mAdapter.addItems(getInvolveTopics(response)) > 0;
    }

    private JSONArray getInvolveTopics(JSONObject response) {
        try {
            return response.getJSONObject("result").getJSONArray("topics");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }
}
