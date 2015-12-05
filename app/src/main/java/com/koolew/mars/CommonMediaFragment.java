package com.koolew.mars;

import android.app.Activity;
import android.content.Context;

import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.topicmedia.MediaItem;
import com.koolew.mars.topicmedia.UniversalMediaAdapter;
import com.koolew.mars.topicmedia.VideoItem;
import com.koolew.mars.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 12/4/15.
 */
public abstract class CommonMediaFragment<CMA extends CommonMediaFragment.CommonMediaAdapter>
        extends RecyclerListFragmentMould<CMA> {

    protected String mTopicId;
    protected OnTopicInfoUpdateListener onTopicInfoUpdateListener;

    public CommonMediaFragment(String topicId) {
        mTopicId = topicId;
        isLazyLoad = true;
        isNeedLoadMore = true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnTopicInfoUpdateListener)) {
            throw new RuntimeException("This activity must implements OnTopicInfoUpdateListener");
        }
        onTopicInfoUpdateListener = (OnTopicInfoUpdateListener) activity;
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONObject result = JsonUtil.getJSONObjectIfHas(response, "result", null);

        if (result == null) {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("It's no result in response?!");
            }
            else {
                // No result in response -_-!
                return false;
            }
        }
        else {
            boolean ret = mAdapter.handleRefreshResult(result);
            onTopicInfoUpdateListener.onCategoryDetermined(mAdapter.getTopicInfo());
            return ret;
        }
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONObject result = JsonUtil.getJSONObjectIfHas(response, "result", null);

        if (result == null) {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("It's no result in response?!");
            }
            else {
                // No result in response -_-!
                return false;
            }
        }
        else {
            return mAdapter.handleLoadMoreResult(result);
        }
    }

    public static class CommonMediaAdapter extends UniversalMediaAdapter {
        public CommonMediaAdapter(Context context) {
            super(context);
        }

        @Override
        protected JSONObject findTopicJson(JSONObject result) {
            return result;
        }

        @Override
        protected JSONArray getRefreshArray(JSONObject result) {
            try {
                return result.getJSONArray("videos");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONArray();
        }

        @Override
        protected MediaItem fromEveryRefreshObject(JSONObject itemObject) {
            return new VideoItem(new BaseVideoInfo(itemObject));
        }
    }

    public interface OnTopicInfoUpdateListener {
        void onCategoryDetermined(BaseTopicInfo topicInfo);
    }
}
