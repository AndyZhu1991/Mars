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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FriendCurrentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendCurrentFragment newInstance() {
        FriendCurrentFragment fragment = new FriendCurrentFragment();
        return fragment;
    }

    public FriendCurrentFragment() {
        // Required empty public constructor
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
        return ApiWorker.getInstance().requestCurrentFriend(mRefreshListener, null);
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
            JSONArray users = response.getJSONObject("result").getJSONArray("users");
            mAdapter.setData(users);
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
