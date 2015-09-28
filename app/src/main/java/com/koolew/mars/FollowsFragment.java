package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/28/15.
 */
public class FollowsFragment extends TitleFragmentActivity.BaseTitleFragment<FriendSimpleAdapter> {

    public static final String KEY_UID = "uid";

    private String mUid;

    public FollowsFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = getActivity().getIntent().getStringExtra(KEY_UID);
    }

    @Override
    public int getTitleBarColor(Context context) {
        if (context == null) {
            context = getActivity();
        }
        return context.getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    protected FriendSimpleAdapter useThisAdapter() {
        return new FriendSimpleAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getTitleBarColor(null);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().getFollows(mUid, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getFollows(mUid, mAdapter.getLastUpdateTime(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                JSONArray users = response.getJSONObject("result").getJSONArray("users");
                if (users != null && users.length() > 0) {
                    mAdapter.setData(users);
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                JSONArray users = response.getJSONObject("result").getJSONArray("users");
                if (users != null && users.length() > 0) {
                    mAdapter.add(users);
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
