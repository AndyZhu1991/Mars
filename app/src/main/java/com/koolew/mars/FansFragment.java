package com.koolew.mars;

import android.os.Bundle;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/25/15.
 */
public class FansFragment extends TitleFragmentActivity.BaseTitleFragment<FriendSimpleAdapter> {

    public static final String KEY_UID = "uid";

    private String mUid;

    public FansFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = getActivity().getIntent().getStringExtra(KEY_UID);

        mActivity.getTitleBar().setBackgroundColor(getThemeColor());
    }

    @Override
    protected FriendSimpleAdapter useThisAdapter() {
        return new FriendSimpleAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().getFans(mUid, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getFans(mUid, mAdapter.getLastUpdateTime(),
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
