package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FriendCurrentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {

    private static final String TAG = "koolew-FriendCurrentF";

    private ListView mListView;
    private FriendSimpleAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_friend_contact, container, false);

        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.koolew_light_blue);
        mRefreshLayout.setOnRefreshListener(this);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        if (mAdapter == null) {
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(true);
                    doRefresh();
                }
            });
        }
        else {
            mListView.setAdapter(mAdapter);
        }

        return root;
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        requestContactFriend();
    }

    private void requestContactFriend() {
        ApiWorker.getInstance().requestCurrentFriend(new RefreshListener(), null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
        String uid = ((FriendSimpleAdapter.FriendInfo) mAdapter.getItem(position)).getUid();
        intent.putExtra(FriendInfoActivity.KEY_UID, uid);
        startActivity(intent);
    }

    class RefreshListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                if (jsonObject.getInt("code") != 0) {
                    Log.e(TAG, "Error response: " + jsonObject);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mAdapter = new FriendCurrentAdapter(getActivity());
            try {
                JSONArray users = jsonObject.getJSONObject("result").getJSONArray("users");
                mAdapter.add(users);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListView.setAdapter(mAdapter);

            mRefreshLayout.setRefreshing(false);
        }
    }

}
