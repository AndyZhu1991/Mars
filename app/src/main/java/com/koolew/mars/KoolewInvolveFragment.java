package com.koolew.mars;

import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KoolewInvolveFragment extends RecyclerListFragmentMould<TimelineAdapter> {

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
    protected String getRefreshRequestUrl() {
        return UrlHelper.getInvolveUrl();
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getInvolveUrl(mAdapter.getLastUpdateTime());
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        return mAdapter.setItems(getInvolveTopics(result)) > 0;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        return mAdapter.addItems(getInvolveTopics(result)) > 0;
    }

    @Override
    protected int getNoDataViewResId() {
        return R.layout.involve_no_data;
    }

    private JSONArray getInvolveTopics(JSONObject result) {
        try {
            return result.getJSONArray("topics");
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return new JSONArray();
    }
}
