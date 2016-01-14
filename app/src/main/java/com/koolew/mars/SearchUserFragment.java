package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 1/13/16.
 */
public class SearchUserFragment extends RecyclerListFragmentMould<FriendSimpleAdapter> {

    private static final String KEY_KEYWORD = "keyword";

    private String keyword;
    private int page;

    public SearchUserFragment() {
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyword = getArguments().getString(KEY_KEYWORD);
        ((TitleFragmentActivity) getActivity()).getTitleBar().setBackgroundColor(getThemeColor());
        ((TitleFragmentActivity) getActivity()).getTitleBar().setTitle(R.string.search_user);
    }

    @Override
    protected FriendSimpleAdapter useThisAdapter() {
        return new FriendSimpleAdapter(getContext());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_black);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        page = 0;
        return super.doRefreshRequest();
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.getSearchUserUrl(keyword, page);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        page++;
        return super.doLoadMoreRequest();
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getSearchUserUrl(keyword, page);
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        try {
            JSONArray users = result.getJSONArray("users");
            if (users.length() > 0) {
                mAdapter.setData(users);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
            JSONArray users = result.getJSONArray("users");
            if (users.length() > 0) {
                int originalLen = mAdapter.getCustomItemCount();
                mAdapter.add(users);
                mAdapter.notifyItemRangeInserted(originalLen, users.length());
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void startThisFragment(Context context, String keyword) {
        Bundle args = new Bundle();
        args.putString(KEY_KEYWORD, keyword);
        TitleFragmentActivity.launchFragment(context, SearchUserFragment.class, args);
    }
}
