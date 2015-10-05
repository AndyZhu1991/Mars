package com.koolew.mars;

import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.ApiWorker;

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
    protected FriendCurrentAdapter useThisAdapter() {
        return new FriendCurrentAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().getFriends(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getFriends(mAdapter.getLastUpdateTime(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONArray users = queryUsers(response);
        if (users != null && users.length() > 0) {
            mAdapter.setData(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONArray users = queryUsers(response);
        if (users != null && users.length() > 0) {
            mAdapter.add(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    public static JSONArray queryUsers(JSONObject response) {
        try {
            if (response.getInt("code") != 0) {
                Log.e(TAG, "Error response: " + response);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            return response.getJSONObject("result").getJSONArray("users");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
