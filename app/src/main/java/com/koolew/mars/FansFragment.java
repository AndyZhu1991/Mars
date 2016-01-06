package com.koolew.mars;

import android.os.Bundle;

import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/25/15.
 */
public class FansFragment extends RecyclerListFragmentMould<FriendSimpleAdapter> {

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

        ((TitleFragmentActivity) getActivity()).getTitleBar().setBackgroundColor(getThemeColor());
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
    protected String getRefreshRequestUrl() {
        return UrlHelper.getFriendFansUrl(mUid);
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getFriendFansUrl(mUid, mAdapter.getLastUpdateTime());
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        try {
            JSONArray users = result.getJSONArray("users");
            if (users != null && users.length() > 0) {
                mAdapter.setData(users);
                mAdapter.notifyDataSetChanged();
                return true;
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
            JSONArray users = result.getJSONArray("users");
            if (users != null && users.length() > 0) {
                mAdapter.add(users);
                mAdapter.notifyDataSetChanged();
                return true;
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
