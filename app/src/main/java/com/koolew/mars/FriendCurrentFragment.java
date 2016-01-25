package com.koolew.mars;

import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FriendCurrentFragment extends RecyclerListFragmentMould<FriendCurrentAdapter> {

    private static final String TAG = "koolew-FriendCurrentF";

    public FriendCurrentFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    protected int getNoDataViewResId() {
        return R.layout.no_friends_layout;
    }

    @Override
    protected FriendCurrentAdapter useThisAdapter() {
        return new FriendCurrentAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_deep_blue);
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.CURRENT_FRIEND_URL;
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getCurrentFriendUrl(mAdapter.getLastUpdateTime());
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        JSONArray users = queryUsers(result);
        if (users != null && users.length() > 0) {
            mAdapter.setData(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        JSONArray users = queryUsers(result);
        if (users != null && users.length() > 0) {
            mAdapter.add(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    public static JSONArray queryUsers(JSONObject result) {
        try {
            return result.getJSONArray("users");
        } catch (JSONException e) {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
