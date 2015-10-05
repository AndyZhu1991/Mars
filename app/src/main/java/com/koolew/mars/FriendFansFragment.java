package com.koolew.mars;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.koolew.mars.infos.TypedUserInfo.TYPE_FAN;

/**
 * Created by jinchangzhu on 9/18/15.
 */
public class FriendFansFragment extends RecyclerListFragmentMould<FriendSimpleAdapter> {

    private static final String TAG = FriendFollowsFragment.class.getSimpleName();

    public FriendFansFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    protected FriendSimpleAdapter useThisAdapter() {
        return new FriendSimpleAdapter(getActivity()) {
            @Override
            protected void onFriendUnfollow(int position) {
                FriendInfo info = mData.get(position);
                info.setType(TYPE_FAN);
                notifyItemChanged(position);
            }
        };
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().getFans(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getFans(mAdapter.getLastUpdateTime(),
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
