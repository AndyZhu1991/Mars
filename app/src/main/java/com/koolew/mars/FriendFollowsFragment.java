package com.koolew.mars;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/18/15.
 */
public class FriendFollowsFragment extends RecyclerListFragmentMould<FriendSimpleAdapter> {

    private static final String TAG = FriendFollowsFragment.class.getSimpleName();

    public FriendFollowsFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    protected FriendSimpleAdapter useThisAdapter() {
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
        return ApiWorker.getInstance().getFollows(mAdapter.getLastUpdateTime(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONArray users = FriendCurrentFragment.queryUsers(response);
        if (users != null && users.length() > 0) {
            mAdapter.setData(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONArray users = FriendCurrentFragment.queryUsers(response);
        if (users != null && users.length() > 0) {
            mAdapter.add(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }
}
