package com.koolew.mars;

import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.UrlHelper;

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
    protected int getNoDataViewResId() {
        return R.layout.no_fans_layout;
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
        return getResources().getColor(R.color.koolew_deep_blue);
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.FRIEND_FANS_URL;
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getFriendFansUrl(mAdapter.getLastUpdateTime());
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        JSONArray users = FriendCurrentFragment.queryUsers(result);
        if (users != null && users.length() > 0) {
            mAdapter.setData(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        JSONArray users = FriendCurrentFragment.queryUsers(result);
        if (users != null && users.length() > 0) {
            mAdapter.add(users);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }
}
