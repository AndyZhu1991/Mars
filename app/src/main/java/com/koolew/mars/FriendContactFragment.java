package com.koolew.mars;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.koolew.mars.utils.ContactUtil;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class FriendContactFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "koolew-FriendContactF";

    private ListView mListView;
    private FriendSimpleAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    private List<ContactUtil.SimpleContactInfo> mContacts;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FriendContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendContactFragment newInstance() {
        FriendContactFragment fragment = new FriendContactFragment();
        return fragment;
    }

    public FriendContactFragment() {
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
        if (mContacts == null || mContacts.size() == 0) {
            new GetContactsTask().execute();
        }
        else {
            requestContactFriend();
        }
    }

    private void requestContactFriend() {
        ApiWorker.getInstance().requestContactFriend(mContacts, new RefreshListener(), null);
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

            if (getActivity() == null) {
                return;
            }
            mAdapter = new FriendContactAdapter(getActivity(), mContacts);
            try {
                JSONArray relations = jsonObject.getJSONObject("result").getJSONArray("relations");
                mAdapter.add(relations);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListView.setAdapter(mAdapter);

            mRefreshLayout.setRefreshing(false);
        }
    }

    class GetContactsTask extends AsyncTask<Void, Void, List<ContactUtil.SimpleContactInfo>> {

        @Override
        protected List<ContactUtil.SimpleContactInfo> doInBackground(Void... params) {
            return ContactUtil.getPhoneContacts(getActivity());
        }

        @Override
        protected void onPostExecute(List<ContactUtil.SimpleContactInfo> simpleContactInfos) {
            mContacts = simpleContactInfos;
            if (mContacts == null || mContacts.size() == 0) {
                mRefreshLayout.setRefreshing(false);
            }
            else {
                requestContactFriend();
            }
        }
    }
}
