package com.koolew.mars;

import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/18/15.
 */
public class FriendFollowsFragment extends RecyclerListFragmentMould {

    private static final String TAG = FriendFollowsFragment.class.getSimpleName();

    @Override
    protected LoadMoreAdapter useThisAdapter() {
        return new FriendSimpleAdapter(getActivity()) {
            @Override
            protected void onFollowedUnfollow(int position) {
                mData.remove(position);
                notifyItemRemoved(position);
            }
        };
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().getFollows(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return null;
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            if (response.getInt("code") != 0) {
                Log.e(TAG, "Error response: " + response);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray follows = response.getJSONObject("result").getJSONArray("users");
            ((FriendSimpleAdapter) mAdapter).setData(follows);
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        return false;
    }
}
